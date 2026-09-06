package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * First authored NPC quest lifecycle for CozyCrazyCraft.
 *
 * Public Bountiful notices remain lightweight repeatable civic work. This manager owns deliberate
 * villager-issued contracts whose availability depends on the issuing village, profession, Trust,
 * and actual nearby world content. 0.3.0 intentionally implements one complete Cartographer loop
 * before the larger Master Bible is imported.
 */
public final class VillageConversationQuestManager {
    private static final ResourceLocation OFFER_DIALOGUE = id("cartographer_first_real_map");
    private static final ResourceLocation ACTIVE_DIALOGUE = id("cartographer_quest_active");
    private static final ResourceLocation TURNIN_DIALOGUE = id("cartographer_quest_turnin");

    private static final String ROOT = "CozyCrazyVillagerQuests";
    private static final String PENDING = "pending";
    private static final String ACTIVE = "active";
    private static final String COMPLETED = "completed";

    private static final long PENDING_LIFETIME = 2400L;
    private static final long TARGET_CACHE_LIFETIME = 6000L;
    private static final int VILLAGE_RECORD_RADIUS = 160;
    private static final int VILLAGE_NAME_SEARCH_RADIUS = 256;

    private static final Map<String, CachedTarget> TARGET_CACHE = new HashMap<>();

    private VillageConversationQuestManager() {}

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (!ConversationBridge.available()) return;

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.FIRST_REAL_MAP;
        if (villager.getVillagerData().getProfession() != definition.giverProfession()) {
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        VillageBoardSavedData.VillageRecord village = VillageBoardSavedData.get(level)
                .findNearby(villager.blockPosition(), VILLAGE_RECORD_RADIUS)
                .orElse(null);
        if (village == null) {
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (!active.isEmpty()) {
            if (sameIssuingVillage(active, level, village.board())) {
                boolean surveyed = active.getBoolean("surveyed");
                ConversationBridge.setDialogue(villager, surveyed ? TURNIN_DIALOGUE : ACTIVE_DIALOGUE);
            } else {
                ConversationBridge.clearOwnDialogue(villager);
            }
            return;
        }

        if (isCompleted(root, definition, level, village.board())) {
            root.remove(PENDING);
            saveRoot(player, root);
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        ZoneBridge.Cell villageCell = ZoneBridge.cellAt(level, village.center());
        if (!definition.issuingTier().equals(villageCell.tier())) {
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        NearbyStructureResolver.ResolvedStructure target = resolveTarget(level, village, villageCell, definition);
        if (target == null) {
            root.remove(PENDING);
            saveRoot(player, root);
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        // CozyCrazyZones already gives both settlements and structures persistent world-global
        // names. Reuse those names everywhere in the quest instead of calling Pumpkin Hollow
        // "the issuing village" or The Amber Watch "a watch tower" after the player knows better.
        String villageName = NamedPlaceBridge.nearestVillageName(level, village.center(), VILLAGE_NAME_SEARCH_RADIUS);
        String targetName = NamedPlaceBridge.structureName(level, target.id(), target.pos());

        writePending(root, level, villager, village, villageCell, definition, target, villageName, targetName);
        saveRoot(player, root);
        ConversationBridge.setDialogue(villager, OFFER_DIALOGUE);

        player.displayClientMessage(
                Component.literal(villageName + " survey lead  •  " + targetName + "  •  about "
                                + target.distanceBlocks() + " blocks " + direction(village.center(), target.pos()))
                        .withStyle(ChatFormatting.GOLD),
                true
        );
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.tickCount % 20 != 0) return;

        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (active.isEmpty() || active.getBoolean("surveyed")) return;
        if (!level.dimension().location().toString().equals(active.getString("target_dimension"))) return;

        BlockPos target = readPos(active, "target");
        int radius = active.getInt("target_radius");
        long dx = (long) player.blockPosition().getX() - target.getX();
        long dz = (long) player.blockPosition().getZ() - target.getZ();
        if (dx * dx + dz * dz > (long) radius * radius) return;

        active.putBoolean("surveyed", true);
        root.put(ACTIVE, active);
        saveRoot(player, root);
        String villageName = active.getString("village_name");
        if (villageName.isBlank()) villageName = "the issuing village";
        player.sendSystemMessage(
                Component.literal("Survey complete: " + active.getString("target_name")
                                + ". Return to a cartographer in " + villageName + ".")
                        .withStyle(ChatFormatting.AQUA)
        );
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        if (!oldData.contains(ROOT)) return;
        event.getEntity().getPersistentData().put(ROOT, oldData.getCompound(ROOT).copy());
    }

    static void consumeConversationAction(ServerPlayer player, String action) {
        if ("accept".equals(action)) {
            acceptPending(player);
        } else if ("turnin".equals(action)) {
            turnInActive(player);
        }
    }

    private static void acceptPending(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        CompoundTag root = root(player);
        if (!root.getCompound(ACTIVE).isEmpty()) return;

        CompoundTag pending = root.getCompound(PENDING);
        if (pending.isEmpty()) return;
        if (!VillageQuestCatalog.FIRST_REAL_MAP.id().equals(pending.getString("quest_id"))) return;
        if (!level.dimension().location().toString().equals(pending.getString("board_dimension"))) return;

        long age = level.getGameTime() - pending.getLong("created_game_time");
        if (age < 0 || age > PENDING_LIFETIME) {
            root.remove(PENDING);
            saveRoot(player, root);
            player.sendSystemMessage(Component.literal("That local lead has gone stale. Speak to the cartographer again.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag active = pending.copy();
        active.remove("created_game_time");
        active.putBoolean("surveyed", false);
        root.put(ACTIVE, active);
        root.remove(PENDING);
        saveRoot(player, root);

        ItemStack contract = new ItemStack(ModItems.VILLAGE_CONTRACT.get());
        contract.setHoverName(Component.literal(active.getString("title")).withStyle(ChatFormatting.GOLD));
        contract.getOrCreateTag().putString(VillageContractItem.QUEST_ID, active.getString("quest_id"));
        contract.getOrCreateTag().putString(VillageContractItem.TARGET_NAME, active.getString("target_name"));
        contract.getOrCreateTag().putInt(VillageContractItem.TARGET_DISTANCE, active.getInt("target_distance"));
        contract.getOrCreateTag().putString(VillageContractItem.TARGET_DIRECTION, active.getString("target_direction"));
        contract.getOrCreateTag().putString(VillageContractItem.ISSUING_VILLAGE, active.getString("village_name"));
        if (!player.addItem(contract)) player.drop(contract, false);

        ResourceLocation targetId = ResourceLocation.tryParse(active.getString("target_structure"));
        boolean atlasMarked = targetId != null && NamedPlaceBridge.revealStructureToAtlas(
                player,
                targetId,
                readPos(active, "target"),
                active.getString("target_name")
        );

        String villageName = active.getString("village_name");
        String prefix = villageName.isBlank() || "the village".equals(villageName)
                ? "Accepted: " + active.getString("title")
                : villageName + " — " + active.getString("title");
        player.sendSystemMessage(
                Component.literal(prefix + ": survey " + active.getString("target_name") + ", about "
                                + active.getInt("target_distance") + " blocks "
                                + active.getString("target_direction") + ".")
                        .withStyle(ChatFormatting.GOLD)
        );
        if (atlasMarked) {
            player.sendSystemMessage(
                    Component.literal("The cartographer marks " + active.getString("target_name") + " on your Atlas.")
                            .withStyle(ChatFormatting.AQUA)
            );
        }
    }

    private static void turnInActive(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (active.isEmpty() || !active.getBoolean("surveyed")) return;
        if (!level.dimension().location().toString().equals(active.getString("board_dimension"))) return;

        BlockPos boardPos = readPos(active, "board");
        long dx = (long) player.blockPosition().getX() - boardPos.getX();
        long dz = (long) player.blockPosition().getZ() - boardPos.getZ();
        if (dx * dx + dz * dz > (long) VILLAGE_RECORD_RADIUS * VILLAGE_RECORD_RADIUS) {
            String villageName = active.getString("village_name");
            if (villageName.isBlank()) villageName = "the village that issued this survey";
            player.sendSystemMessage(Component.literal("Return to " + villageName + " before turning this survey in.")
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.FIRST_REAL_MAP;
        giveOrDrop(player, new ItemStack(Items.EMERALD, definition.emeraldReward()));
        giveOrDrop(player, new ItemStack(Items.SPYGLASS));
        player.giveExperiencePoints(definition.experienceReward());

        boolean trustAwarded = BountifulBridge.awardBoardCompletion(level, boardPos, player);
        markCompleted(root, definition, level, boardPos);
        root.remove(ACTIVE);
        root.remove(PENDING);
        saveRoot(player, root);
        removeContract(player, definition.id());

        String villageName = active.getString("village_name");
        String records = villageName.isBlank() || "the village".equals(villageName)
                ? "The village records your survey."
                : villageName + " records your survey.";
        player.sendSystemMessage(
                Component.literal("Completed: " + definition.title() + ". " + records
                                + " Payment: 5 emeralds, a spyglass, and Village Trust.")
                        .withStyle(ChatFormatting.GREEN)
        );
        if (!trustAwarded) {
            player.sendSystemMessage(Component.literal("The survey completed, but Village Trust could not be synchronized with the bounty board; check the log.")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static NearbyStructureResolver.ResolvedStructure resolveTarget(
            ServerLevel level,
            VillageBoardSavedData.VillageRecord village,
            ZoneBridge.Cell villageCell,
            VillageQuestCatalog.Definition definition
    ) {
        String cacheKey = level.getSeed() + ":" + level.dimension().location() + ":" + village.board().asLong();
        CachedTarget cached = TARGET_CACHE.get(cacheKey);
        if (cached != null && level.getGameTime() - cached.checkedAt() <= TARGET_CACHE_LIFETIME) {
            return cached.target();
        }

        NearbyStructureResolver.ResolvedStructure found = NearbyStructureResolver.findNearest(
                level,
                village.center(),
                definition.structureCandidates(),
                definition.searchRadiusBlocks()
        );
        if (found != null && !legalTarget(level, villageCell, found, definition)) found = null;

        TARGET_CACHE.put(cacheKey, new CachedTarget(found, level.getGameTime()));
        return found;
    }

    private static boolean legalTarget(
            ServerLevel level,
            ZoneBridge.Cell villageCell,
            NearbyStructureResolver.ResolvedStructure target,
            VillageQuestCatalog.Definition definition
    ) {
        ZoneBridge.Cell targetCell = ZoneBridge.cellAt(level, target.pos());
        if (!villageCell.known() || !targetCell.known()) return false;

        int issuingRank = ZoneBridge.tierRank(villageCell.tier());
        int targetRank = ZoneBridge.tierRank(targetCell.tier());
        if (issuingRank < 0 || targetRank < 0) return false;
        if (targetRank < issuingRank + definition.targetMinTierOffset()) return false;
        if (targetRank > issuingRank + definition.targetMaxTierOffset()) return false;

        if (definition.sameMacroRegion() && !"SHARED_CORE".equals(villageCell.band())) {
            return villageCell.macro().equals(targetCell.macro());
        }
        return true;
    }

    private static void writePending(
            CompoundTag root,
            ServerLevel level,
            Villager villager,
            VillageBoardSavedData.VillageRecord village,
            ZoneBridge.Cell villageCell,
            VillageQuestCatalog.Definition definition,
            NearbyStructureResolver.ResolvedStructure target,
            String villageName,
            String targetName
    ) {
        CompoundTag pending = new CompoundTag();
        pending.putString("quest_id", definition.id());
        pending.putString("title", definition.title());
        pending.putString("giver_uuid", villager.getUUID().toString());
        pending.putString("giver_profession", "cartographer");
        pending.putString("board_dimension", level.dimension().location().toString());
        putPos(pending, "board", village.board());
        putPos(pending, "village", village.center());
        pending.putString("village_name", villageName);
        pending.putString("village_macro", villageCell.macro());
        pending.putString("village_tier", villageCell.tier());
        pending.putString("target_dimension", level.dimension().location().toString());
        pending.putString("target_structure", target.id().toString());
        putPos(pending, "target", target.pos());
        pending.putInt("target_radius", definition.targetRadiusBlocks());
        pending.putString("target_name", targetName);
        pending.putInt("target_distance", target.distanceBlocks());
        pending.putString("target_direction", direction(village.center(), target.pos()));
        pending.putLong("created_game_time", level.getGameTime());
        pending.putInt("trust_when_offered", BountifulBridge.boardCompletedCount(level, village.board()));
        root.put(PENDING, pending);
    }

    private static boolean sameIssuingVillage(CompoundTag active, ServerLevel level, BlockPos boardPos) {
        return level.dimension().location().toString().equals(active.getString("board_dimension"))
                && boardPos.equals(readPos(active, "board"));
    }

    private static boolean isCompleted(
            CompoundTag root,
            VillageQuestCatalog.Definition definition,
            ServerLevel level,
            BlockPos boardPos
    ) {
        return root.getCompound(COMPLETED).getBoolean(completionKey(definition.id(), level, boardPos));
    }

    private static void markCompleted(
            CompoundTag root,
            VillageQuestCatalog.Definition definition,
            ServerLevel level,
            BlockPos boardPos
    ) {
        CompoundTag completed = root.getCompound(COMPLETED);
        completed.putBoolean(completionKey(definition.id(), level, boardPos), true);
        root.put(COMPLETED, completed);
    }

    private static String completionKey(String questId, ServerLevel level, BlockPos boardPos) {
        return questId + "@" + level.dimension().location() + "@" + boardPos.getX() + "," + boardPos.getY() + "," + boardPos.getZ();
    }

    private static CompoundTag root(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }

    private static void saveRoot(ServerPlayer player, CompoundTag root) {
        player.getPersistentData().put(ROOT, root);
    }

    private static void putPos(CompoundTag tag, String prefix, BlockPos pos) {
        tag.putInt(prefix + "X", pos.getX());
        tag.putInt(prefix + "Y", pos.getY());
        tag.putInt(prefix + "Z", pos.getZ());
    }

    private static BlockPos readPos(CompoundTag tag, String prefix) {
        return new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"));
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) player.drop(stack, false);
    }

    private static void removeContract(ServerPlayer player, String questId) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.VILLAGE_CONTRACT.get()) || !stack.hasTag()) continue;
            if (!questId.equals(stack.getTag().getString(VillageContractItem.QUEST_ID))) continue;
            stack.shrink(1);
            return;
        }
    }

    private static String direction(BlockPos from, BlockPos to) {
        long dx = (long) to.getX() - from.getX();
        long dz = (long) to.getZ() - from.getZ();
        double angle = Math.atan2(dx, -dz);
        int octant = Math.floorMod((int) Math.round(angle / (Math.PI / 4.0)), 8);
        return switch (octant) {
            case 0 -> "north";
            case 1 -> "northeast";
            case 2 -> "east";
            case 3 -> "southeast";
            case 4 -> "south";
            case 5 -> "southwest";
            case 6 -> "west";
            default -> "northwest";
        };
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }

    private record CachedTarget(NearbyStructureResolver.ResolvedStructure target, long checkedAt) {}
}
