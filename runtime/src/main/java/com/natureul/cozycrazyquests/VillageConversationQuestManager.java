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
 * Authored villager quest lifecycle.
 *
 * Bountiful remains a public-notice/civic-reputation integration, but it no longer owns settlement
 * identity. Authored quests are keyed to VillageContext so a new, boardless, destroyed-board, or
 * not-yet-recorded village can still participate in Conversations and chapter progression.
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
    private static final int VILLAGE_RETURN_RADIUS = 160;

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

        VillageContext village = VillageContext.resolve(level, villager.blockPosition());
        if (village == null) {
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (!active.isEmpty()) {
            if (sameIssuingVillage(active, level, village)) {
                boolean surveyed = active.getBoolean("surveyed");
                ConversationBridge.setDialogue(villager, surveyed ? TURNIN_DIALOGUE : ACTIVE_DIALOGUE);
            } else {
                ConversationBridge.clearOwnDialogue(villager);
            }
            return;
        }

        if (isCompleted(root, definition, level, village)) {
            root.remove(PENDING);
            saveRoot(player, root);
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        ZoneBridge.Cell villageCell = village.cell();
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

        String targetName = NamedPlaceBridge.structureName(level, target.id(), target.pos());
        writePending(root, level, villager, village, villageCell, definition, target, targetName, player);
        saveRoot(player, root);
        ConversationBridge.setDialogue(villager, OFFER_DIALOGUE);

        player.displayClientMessage(
                Component.literal(village.name() + " survey lead  •  " + targetName + "  •  about "
                                + target.distanceBlocks() + " blocks " + direction(village.anchor(), target.pos()))
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
        if (!sameDimension(level, pending)) return;

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
        if (!sameDimension(level, active)) return;

        BlockPos villageAnchor = readPos(active, "village");
        long dx = (long) player.blockPosition().getX() - villageAnchor.getX();
        long dz = (long) player.blockPosition().getZ() - villageAnchor.getZ();
        if (dx * dx + dz * dz > (long) VILLAGE_RETURN_RADIUS * VILLAGE_RETURN_RADIUS) {
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

        String villageKey = active.getString("village_key");
        if (villageKey.isBlank()) {
            VillageContext recovered = VillageContext.resolve(level, villageAnchor);
            villageKey = recovered != null ? recovered.key() : legacyVillageKey(level, active, villageAnchor);
        }

        VillageProgressState.recordAccomplishment(
                player,
                villageKey,
                VillageProgressState.AccomplishmentCategory.EXPLORATION,
                definition.id()
        );
        VillageProgressState.Snapshot progress = VillageProgressState.snapshot(player, villageKey);

        boolean boardSynced = false;
        if (active.getBoolean("has_board") || active.contains("boardX")) {
            BlockPos boardPos = readPos(active, "board");
            boardSynced = BountifulBridge.awardBoardCompletion(level, boardPos, player);
        }

        markCompleted(root, definition, level, villageKey, active);
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
                                + " Payment: " + definition.emeraldReward() + " emeralds and a spyglass. "
                                + "Standing: " + display(progress.trust()) + ".")
                        .withStyle(ChatFormatting.GREEN)
        );
        if ((active.getBoolean("has_board") || active.contains("boardX")) && !boardSynced) {
            CozyCrazyQuests.LOGGER.debug("Authored survey completed without Bountiful board synchronization for {}", villageKey);
        }
    }

    private static NearbyStructureResolver.ResolvedStructure resolveTarget(
            ServerLevel level,
            VillageContext village,
            ZoneBridge.Cell villageCell,
            VillageQuestCatalog.Definition definition
    ) {
        String cacheKey = level.getSeed() + ":" + village.key() + ":" + definition.id();
        CachedTarget cached = TARGET_CACHE.get(cacheKey);
        if (cached != null && level.getGameTime() - cached.checkedAt() <= TARGET_CACHE_LIFETIME) {
            return cached.target();
        }

        NearbyStructureResolver.ResolvedStructure found = NearbyStructureResolver.findNearest(
                level,
                village.anchor(),
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
            VillageContext village,
            ZoneBridge.Cell villageCell,
            VillageQuestCatalog.Definition definition,
            NearbyStructureResolver.ResolvedStructure target,
            String targetName,
            ServerPlayer player
    ) {
        CompoundTag pending = new CompoundTag();
        pending.putString("quest_id", definition.id());
        pending.putString("title", definition.title());
        pending.putString("giver_uuid", villager.getUUID().toString());
        pending.putString("giver_profession", "cartographer");
        pending.putString("village_dimension", level.dimension().location().toString());
        // Keep the legacy dimension key so an older 0.3.x active contract can still be consumed.
        pending.putString("board_dimension", level.dimension().location().toString());
        pending.putString("village_key", village.key());
        putPos(pending, "village", village.anchor());
        pending.putString("village_name", village.name());
        pending.putString("village_macro", villageCell.macro());
        pending.putString("village_tier", villageCell.tier());
        pending.putBoolean("has_board", village.hasBoard());
        if (village.hasBoard()) putPos(pending, "board", village.boardPos());
        pending.putString("target_dimension", level.dimension().location().toString());
        pending.putString("target_structure", target.id().toString());
        putPos(pending, "target", target.pos());
        pending.putInt("target_radius", definition.targetRadiusBlocks());
        pending.putString("target_name", targetName);
        pending.putInt("target_distance", target.distanceBlocks());
        pending.putString("target_direction", direction(village.anchor(), target.pos()));
        pending.putLong("created_game_time", level.getGameTime());
        pending.putInt("trust_when_offered", village.legacyBoardTrust(level));
        pending.putString("semantic_trust_when_offered", VillageProgressState.snapshot(player, village.key()).trust().name());
        root.put(PENDING, pending);
    }

    private static boolean sameIssuingVillage(CompoundTag active, ServerLevel level, VillageContext village) {
        if (!sameDimension(level, active)) return false;
        String key = active.getString("village_key");
        if (!key.isBlank()) return key.equals(village.key());
        if (active.contains("boardX") && village.hasBoard()) {
            return village.boardPos().equals(readPos(active, "board"));
        }
        String oldName = active.getString("village_name");
        return !oldName.isBlank() && oldName.equals(village.name());
    }

    private static boolean isCompleted(
            CompoundTag root,
            VillageQuestCatalog.Definition definition,
            ServerLevel level,
            VillageContext village
    ) {
        CompoundTag completed = root.getCompound(COMPLETED);
        if (completed.getBoolean(completionKey(definition.id(), village.key()))) return true;
        return village.hasBoard() && completed.getBoolean(legacyCompletionKey(definition.id(), level, village.boardPos()));
    }

    private static void markCompleted(
            CompoundTag root,
            VillageQuestCatalog.Definition definition,
            ServerLevel level,
            String villageKey,
            CompoundTag active
    ) {
        CompoundTag completed = root.getCompound(COMPLETED);
        completed.putBoolean(completionKey(definition.id(), villageKey), true);
        if (active.contains("boardX")) {
            completed.putBoolean(legacyCompletionKey(definition.id(), level, readPos(active, "board")), true);
        }
        root.put(COMPLETED, completed);
    }

    private static String completionKey(String questId, String villageKey) {
        return questId + "@village@" + villageKey;
    }

    private static String legacyCompletionKey(String questId, ServerLevel level, BlockPos boardPos) {
        return questId + "@" + level.dimension().location() + "@" + boardPos.getX() + "," + boardPos.getY() + "," + boardPos.getZ();
    }

    private static String legacyVillageKey(ServerLevel level, CompoundTag active, BlockPos anchor) {
        String name = active.getString("village_name");
        if (!name.isBlank() && !"the village".equalsIgnoreCase(name)) {
            return level.dimension().location() + "|legacy-name|" + name.toLowerCase().replace(' ', '_');
        }
        return level.dimension().location() + "|legacy|" + Math.floorDiv(anchor.getX(), 128) + "," + Math.floorDiv(anchor.getZ(), 128);
    }

    private static boolean sameDimension(ServerLevel level, CompoundTag tag) {
        String expected = tag.getString("village_dimension");
        if (expected.isBlank()) expected = tag.getString("board_dimension");
        return level.dimension().location().toString().equals(expected);
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

    private static String display(VillageProgressState.Trust trust) {
        String lower = trust.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
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
