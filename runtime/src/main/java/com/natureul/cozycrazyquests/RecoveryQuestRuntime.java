package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Physical recovery flow and turn-in guard for recovery-specialized structure contracts. */
final class RecoveryQuestRuntime {
    private static final String SEEDED = "recovery_evidence_seeded";
    private static final String SOURCE_X = "recovery_source_x";
    private static final String SOURCE_Y = "recovery_source_y";
    private static final String SOURCE_Z = "recovery_source_z";
    static final String GENERATION = "recovery_evidence_generation";

    private RecoveryQuestRuntime() {}

    /**
     * A recovery object is bound into the first real loot/storage cache the player opens while inside a
     * real piece of the exact assigned structure instance. The evidence is never awarded merely for
     * crossing a bounding box and is never silently handed to the player because a container is full.
     * If the cache has no free slot, the player must make room and open it again. That keeps one visible,
     * physical truth for the quest object: it came out of the assigned structure's cache.
     */
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        BlockPos clicked = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(clicked);
        if (!(blockEntity instanceof RandomizableContainerBlockEntity container)) return;

        CompoundTag root = VillageQuestState.root(player);
        boolean changed = false;

        for (CompoundTag active : VillageQuestState.allActives(root)) {
            if (active.getBoolean("objective_complete") || active.getBoolean("surveyed")) continue;
            VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
            if (definition == null || !definition.isRecovery()) continue;
            if (!level.dimension().location().toString().equals(active.getString("target_dimension"))) continue;
            if (RecoveredEvidence.has(player, active)) continue;
            if (active.getBoolean(SEEDED)) continue;

            ResourceLocation structureId = ResourceLocation.tryParse(active.getString("target_structure"));
            if (structureId == null) continue;
            int expectedChunkX = active.contains("target_start_chunk_x")
                    ? active.getInt("target_start_chunk_x")
                    : Integer.MIN_VALUE;
            int expectedChunkZ = active.contains("target_start_chunk_z")
                    ? active.getInt("target_start_chunk_z")
                    : Integer.MIN_VALUE;
            BlockPos locate = readPos(active, "target");

            if (!NamedPlaceBridge.insideExactStructure(
                    level, clicked, structureId, expectedChunkX, expectedChunkZ, locate)) continue;

            int emptySlot = firstEmptySlot(container);
            if (emptySlot < 0) {
                player.displayClientMessage(
                        Component.literal("Cache full — free one slot.").withStyle(ChatFormatting.GRAY),
                        true
                );
                return;
            }

            ItemStack evidence = RecoveredEvidence.create(definition, active);
            container.setItem(emptySlot, evidence);
            container.setChanged();

            active.putBoolean(SEEDED, true);
            active.putBoolean("visited_target", true);
            active.putInt(SOURCE_X, clicked.getX());
            active.putInt(SOURCE_Y, clicked.getY());
            active.putInt(SOURCE_Z, clicked.getZ());
            VillageQuestState.putActive(root, active.getString("village_key"), active);
            changed = true;

            CozyCrazyQuests.LOGGER.info(
                    "Bound recovery evidence generation {} for '{}' to physical cache {} in exact assigned structure near {}",
                    active.getInt(GENERATION), active.getString("quest_id"), clicked, locate
            );
            // One click should never seed evidence for two simultaneous contracts that happen to overlap.
            break;
        }

        if (changed) VillageQuestState.save(player, root);
    }

    /** Complete a recovery objective only after the current-generation quest object reaches inventory. */
    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 10 != 0) return;

        CompoundTag root = VillageQuestState.root(player);
        boolean changed = false;
        for (CompoundTag active : VillageQuestState.allActives(root)) {
            if (active.getBoolean("objective_complete") || active.getBoolean("surveyed")) continue;
            VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
            if (definition == null || !definition.isRecovery()) continue;
            if (!RecoveredEvidence.has(player, active)) continue;

            active.putBoolean("recovery_collected", true);
            active.putBoolean("objective_complete", true);
            active.putBoolean("visited_target", true);
            VillageQuestState.putActive(root, active.getString("village_key"), active);
            changed = true;

            String targetKey = active.getString("target_key");
            if (!targetKey.isBlank()) {
                PlayerKnowledgeState.advance(
                        player,
                        targetKey,
                        PlayerKnowledgeState.Knowledge.CONFIRMED,
                        PlayerKnowledgeState.Provenance.LOCAL_OBSERVATION
                );
            }

            String object = definition.recoveryObjectName().isBlank()
                    ? "Evidence"
                    : definition.recoveryObjectName();
            player.displayClientMessage(
                    Component.literal("Recovered: " + object + ".").withStyle(ChatFormatting.AQUA),
                    true
            );
        }
        if (changed) VillageQuestState.save(player, root);
    }

    /** True when an evidence copy was seeded but the player no longer possesses the valid generation. */
    static boolean needsReplacementSupport(ServerPlayer player, CompoundTag active) {
        if (active == null || active.isEmpty() || !active.getBoolean(SEEDED)) return false;
        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        return definition != null && definition.isRecovery() && !RecoveredEvidence.has(player, active);
    }

    /**
     * Player-authorized recovery reset. Incrementing the generation invalidates any old copy that later
     * reappears from a moved/destroyed cache, so replacement never creates two simultaneously valid truths.
     */
    static boolean resetLostEvidence(ServerPlayer player) {
        CompoundTag root = VillageQuestState.root(player);
        String villageKey = VillageQuestState.conversationVillage(root);
        if (villageKey.isBlank()) {
            VillageContext village = VillageContext.resolve(player.serverLevel(), player.blockPosition());
            if (village != null) villageKey = village.key();
        }

        CompoundTag active = VillageQuestState.activeForVillage(root, villageKey);
        if (active.isEmpty()) return false;
        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null || !definition.isRecovery()) return false;

        if (RecoveredEvidence.has(player, active)) {
            player.displayClientMessage(Component.literal("Evidence already in inventory.")
                    .withStyle(ChatFormatting.GRAY), true);
            return true;
        }
        if (!active.getBoolean(SEEDED)) {
            player.displayClientMessage(Component.literal("No evidence copy needs resetting.")
                    .withStyle(ChatFormatting.GRAY), true);
            return true;
        }

        invalidateCurrentGeneration(active);
        VillageQuestState.putActive(root, villageKey, active);
        VillageQuestState.save(player, root);
        player.displayClientMessage(Component.literal("Recovery evidence reset.")
                .withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    /**
     * Runs immediately before the normal authored turn-in action. Ordinary quests pass through.
     * Recovery jobs consume their quest-bound evidence; if the player lost it, the current generation
     * is voided and the contract becomes active again for a clean physical replacement.
     */
    static boolean beforeTurnIn(ServerPlayer player) {
        CompoundTag root = VillageQuestState.root(player);
        String villageKey = VillageQuestState.conversationVillage(root);
        if (villageKey.isBlank()) {
            VillageContext village = VillageContext.resolve(player.serverLevel(), player.blockPosition());
            if (village != null) villageKey = village.key();
        }

        CompoundTag active = VillageQuestState.activeForVillage(root, villageKey);
        if (active.isEmpty()) return true;
        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null || !definition.isRecovery()) return true;

        if (RecoveredEvidence.consume(player, active)) return true;

        invalidateCurrentGeneration(active);
        VillageQuestState.putActive(root, villageKey, active);
        VillageQuestState.save(player, root);

        String object = definition.recoveryObjectName().isBlank()
                ? "the recovered evidence"
                : definition.recoveryObjectName();
        player.sendSystemMessage(
                Component.literal("You no longer have " + object + ". The old copy was voided; recover a replacement from "
                                + active.getString("target_name") + ".")
                        .withStyle(ChatFormatting.GOLD)
        );
        return false;
    }

    private static void invalidateCurrentGeneration(CompoundTag active) {
        int current = Math.max(0, active.getInt(GENERATION));
        active.putInt(GENERATION, current == Integer.MAX_VALUE ? 1 : current + 1);
        active.putBoolean("objective_complete", false);
        active.putBoolean("recovery_collected", false);
        active.remove(SEEDED);
        active.remove(SOURCE_X);
        active.remove(SOURCE_Y);
        active.remove(SOURCE_Z);
    }

    private static int firstEmptySlot(RandomizableContainerBlockEntity container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (container.getItem(slot).isEmpty()) return slot;
        }
        return -1;
    }

    private static BlockPos readPos(CompoundTag tag, String prefix) {
        return new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"));
    }
}
