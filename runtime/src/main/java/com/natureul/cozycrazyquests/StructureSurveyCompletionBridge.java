package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;

/**
 * Reliable completion path for authored structure visits.
 *
 * Locate coordinates are navigation hints, never completion proof. New contracts require the player to
 * occupy a real piece of the exact assigned generated structure for a short dwell. Legacy contracts that
 * predate exact-instance binding may recover from an existing CozyCrazyZones discovery record, but current
 * exact-instance contracts never complete from that compatibility path.
 */
final class StructureSurveyCompletionBridge {
    private static final String ZONES_DISCOVERED = "cozycrazyzones:discovered_structures";
    private static final int LEGACY_DISCOVERY_MATCH_BLOCKS = 160;
    private static final int QUALIFYING_DWELL_TICKS = 20;
    private static final String PRESENCE_TICKS = "structure_presence_ticks";

    private StructureSurveyCompletionBridge() {}

    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.tickCount % 10 != 0) return;

        CompoundTag root = VillageQuestState.root(player);
        boolean changed = false;

        for (CompoundTag active : VillageQuestState.allActives(root)) {
            if (objectiveComplete(active)) continue;
            VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
            if (definition == null
                    || definition.objectiveType() != VillageQuestCatalog.ObjectiveType.STRUCTURE_SURVEY
                    || definition.isRecovery()) continue;
            if (!level.dimension().location().toString().equals(active.getString("target_dimension"))) continue;

            ResourceLocation structureId = ResourceLocation.tryParse(active.getString("target_structure"));
            if (structureId == null) continue;
            BlockPos locate = readPos(active, "target");

            boolean hasExactStart = active.contains("target_start_chunk_x") && active.contains("target_start_chunk_z");
            int expectedChunkX = hasExactStart ? active.getInt("target_start_chunk_x") : Integer.MIN_VALUE;
            int expectedChunkZ = hasExactStart ? active.getInt("target_start_chunk_z") : Integer.MIN_VALUE;

            boolean physicallyInside = NamedPlaceBridge.insideExactStructure(
                    level,
                    player.blockPosition(),
                    structureId,
                    expectedChunkX,
                    expectedChunkZ,
                    locate
            );

            // Only old contracts without an exact generated-start identity may use the old discovery
            // record as a compatibility escape hatch. Current contracts must physically enter a piece.
            boolean legacyPreviouslyDiscovered = !hasExactStart
                    && !physicallyInside
                    && alreadyDiscoveredMatchingTarget(player, structureId, locate, expectedChunkX, expectedChunkZ);

            if (!physicallyInside && !legacyPreviouslyDiscovered) {
                if (active.getInt(PRESENCE_TICKS) != 0) {
                    active.putInt(PRESENCE_TICKS, 0);
                    VillageQuestState.putActive(root, active.getString("village_key"), active);
                    changed = true;
                }
                continue;
            }

            if (physicallyInside) {
                int dwell = Math.min(QUALIFYING_DWELL_TICKS, active.getInt(PRESENCE_TICKS) + 10);
                active.putInt(PRESENCE_TICKS, dwell);
                VillageQuestState.putActive(root, active.getString("village_key"), active);
                changed = true;
                if (dwell < QUALIFYING_DWELL_TICKS) continue;
            }

            active.putBoolean("objective_complete", true);
            active.putBoolean("surveyed", true);
            active.remove(PRESENCE_TICKS);
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

            player.displayClientMessage(
                    Component.literal("Survey complete: " + active.getString("target_name") + ".")
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            CozyCrazyQuests.LOGGER.info(
                    "Completed structure survey '{}' by {} at/near {}",
                    active.getString("quest_id"),
                    physicallyInside ? "exact structure-piece dwell" : "legacy CozyCrazyZones discovery recovery",
                    locate
            );
        }

        if (changed) VillageQuestState.save(player, root);
    }

    private static boolean alreadyDiscoveredMatchingTarget(
            ServerPlayer player,
            ResourceLocation structureId,
            BlockPos locate,
            int expectedChunkX,
            int expectedChunkZ
    ) {
        CompoundTag discovered = player.getPersistentData().getCompound(ZONES_DISCOVERED);
        if (discovered.isEmpty()) return false;

        String prefix = "structure@" + structureId + "@";
        long maxSq = (long) LEGACY_DISCOVERY_MATCH_BLOCKS * LEGACY_DISCOVERY_MATCH_BLOCKS;
        for (String key : discovered.getAllKeys()) {
            if (!discovered.getBoolean(key) || !key.startsWith(prefix)) continue;
            String raw = key.substring(prefix.length());
            int comma = raw.indexOf(',');
            if (comma <= 0 || comma >= raw.length() - 1) continue;

            try {
                int chunkX = Integer.parseInt(raw.substring(0, comma));
                int chunkZ = Integer.parseInt(raw.substring(comma + 1));
                if (expectedChunkX != Integer.MIN_VALUE && expectedChunkZ != Integer.MIN_VALUE) {
                    if (chunkX == expectedChunkX && chunkZ == expectedChunkZ) return true;
                    continue;
                }

                long x = chunkX * 16L + 8L;
                long z = chunkZ * 16L + 8L;
                long dx = x - locate.getX();
                long dz = z - locate.getZ();
                if (dx * dx + dz * dz <= maxSq) return true;
            } catch (NumberFormatException ignored) {
                // Ignore malformed/legacy keys and keep looking for the matching generated instance.
            }
        }
        return false;
    }

    private static boolean objectiveComplete(CompoundTag active) {
        return active.getBoolean("objective_complete") || active.getBoolean("surveyed");
    }

    private static BlockPos readPos(CompoundTag tag, String prefix) {
        return new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"));
    }
}
