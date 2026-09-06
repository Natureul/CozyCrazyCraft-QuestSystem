package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Location-bound authored contracts.
 *
 * Unlike synthetic "walk 150 blocks east" work, every quest in this manager is withheld until a
 * real generated structure instance can be resolved in the correct CozyCrazyZones cell. The target
 * position, persistent place name, giver, village, objective, and return instruction are frozen into
 * the accepted contract so the paper remains an honest record of what the villager actually asked.
 */
public final class VillageStructureConversationQuestManager {
    private static final String ROOT = "CozyCrazyStructureQuests";
    private static final String CORE_ROOT = "CozyCrazyVillagerQuests";
    private static final String PENDING = "pending";
    private static final String ACTIVE = "active";
    private static final String COMPLETED = "completed";

    private static final long PENDING_LIFETIME = 2400L;
    private static final long TARGET_CACHE_LIFETIME = 6000L;
    private static final int VILLAGE_RETURN_RADIUS = 176;

    private static final Map<String, CachedTarget> TARGET_CACHE = new HashMap<>();

    private VillageStructureConversationQuestManager() {}

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (!ConversationBridge.available()) return;

        VillagerNameService.ensureNamed(level, villager);
        VillageContext village = VillageContext.resolve(level, villager.blockPosition());
        if (village == null) return;

        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (!active.isEmpty()) {
            VillageStructureQuestCatalog.Definition definition =
                    VillageStructureQuestCatalog.byId(active.getString("quest_id"));
            if (definition != null
                    && definition.accepts(villager.getVillagerData().getProfession())
                    && sameIssuingVillage(active, level, village)) {
                ConversationBridge.setDialogue(
                        villager,
                        active.getBoolean("objective_complete")
                                ? definition.turninDialogue()
                                : definition.activeDialogue()
                );
            } else {
                // A structure contract is already in progress. Do not allow the earlier Zone-1
                // prototype to sneak a second authored quest onto an unrelated villager click.
                ConversationBridge.clearOwnDialogue(villager);
            }
            return;
        }

        if (coreQuestActive(player)) return;

        // The primary social-progression manager gets first refusal. Its arrival/branching work is
        // the intended introduction; structure contracts become additional authored work once that
        // profession has no higher-priority core conversation to offer.
        if (ConversationBridge.hasOwnDialogue(villager)) return;

        Offer offer = selectOffer(level, player, villager, village, root);
        if (offer == null) return;

        writePending(root, level, player, villager, village, offer.definition(), offer.target());
        saveRoot(player, root);
        ConversationBridge.setDialogue(villager, offer.definition().offerDialogue());

        PreparedTarget target = offer.target();
        player.displayClientMessage(
                Component.literal(offer.definition().title() + "  •  " + target.displayName() + "  •  about "
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
        if (active.isEmpty() || active.getBoolean("objective_complete")) return;
        if (!sameDimension(level, active)) return;

        VillageStructureQuestCatalog.Definition definition =
                VillageStructureQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null) return;

        if (definition.objectiveType() == VillageStructureQuestCatalog.ObjectiveType.STRUCTURE_SURVEY) {
            BlockPos target = readPos(active, "target");
            if (!insideRadius(player.blockPosition(), target, active.getInt("target_radius"))) return;
            completeObjective(player, root, active, definition,
                    "Survey complete: " + active.getString("target_name") + ".");
            return;
        }

        if (definition.objectiveType() == VillageStructureQuestCatalog.ObjectiveType.STRUCTURE_RECOVERY) {
            if (!hasItem(player, definition.recoveryItemId())) return;
            completeObjective(player, root, active, definition,
                    "Recovered " + recoveryName(definition) + ".");
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) return;

        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (active.isEmpty() || active.getBoolean("objective_complete") || !sameDimension(level, active)) return;

        VillageStructureQuestCatalog.Definition definition =
                VillageStructureQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null
                || definition.objectiveType() != VillageStructureQuestCatalog.ObjectiveType.STRUCTURE_HOSTILE_CLEAR) return;

        BlockPos target = readPos(active, "target");
        if (!insideRadius(event.getEntity().blockPosition(), target, active.getInt("target_radius"))) return;

        int required = Math.max(1, active.getInt("required_kills"));
        int count = Math.min(required, active.getInt("kill_count") + 1);
        active.putInt("kill_count", count);
        if (count >= required) active.putBoolean("objective_complete", true);
        root.put(ACTIVE, active);
        saveRoot(player, root);

        if (count >= required) {
            confirmTargetKnowledge(player, level, active);
            player.sendSystemMessage(
                    Component.literal(definition.title() + ": the site is clear. " + returnInstruction(active))
                            .withStyle(ChatFormatting.AQUA)
            );
        } else {
            player.displayClientMessage(
                    Component.literal(definition.title() + "  •  " + count + "/" + required + " hostiles cleared")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
        }
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        if (oldData.contains(ROOT)) {
            event.getEntity().getPersistentData().put(ROOT, oldData.getCompound(ROOT).copy());
        }
    }

    static boolean consumeConversationAction(ServerPlayer player, String action) {
        if ("structure_accept".equals(action)) {
            acceptPending(player);
            return true;
        }
        if ("structure_turnin".equals(action)) {
            turnInActive(player);
            return true;
        }
        return false;
    }

    private static Offer selectOffer(
            ServerLevel level,
            ServerPlayer player,
            Villager villager,
            VillageContext village,
            CompoundTag root
    ) {
        for (VillageStructureQuestCatalog.Definition definition
                : VillageStructureQuestCatalog.forProfession(villager.getVillagerData().getProfession())) {
            if (!definition.issuingTier().equals(village.cell().tier())) continue;
            if (!"*".equals(definition.issuingMacro()) && !definition.issuingMacro().equals(village.cell().macro())) continue;
            if (isCompleted(root, definition, village.key())) continue;

            PreparedTarget target = prepareTarget(level, player, village, definition);
            if (target != null) return new Offer(definition, target);
        }
        return null;
    }

    private static PreparedTarget prepareTarget(
            ServerLevel level,
            ServerPlayer player,
            VillageContext village,
            VillageStructureQuestCatalog.Definition definition
    ) {
        NearbyStructureResolver.ResolvedStructure resolved = resolveTarget(level, village, definition);
        if (resolved == null) return null;

        String targetName = NamedPlaceBridge.structureName(level, resolved.id(), resolved.pos());
        String targetKey = targetSubjectKey(level, resolved.id(), resolved.pos());
        VillageLocalFactSavedData.Fact fact = VillageLocalFactSavedData.get(level).rememberStructureSurvey(
                village,
                resolved.id(),
                resolved.pos(),
                targetName,
                level.getGameTime()
        );

        PlayerKnowledgeState.Provenance provenance = definition.giverProfessions().contains(net.minecraft.world.entity.npc.VillagerProfession.CARTOGRAPHER)
                ? PlayerKnowledgeState.Provenance.MAP_RECORD
                : PlayerKnowledgeState.Provenance.VILLAGE_REPORT;
        PlayerKnowledgeState.advance(player, targetKey, PlayerKnowledgeState.Knowledge.KNOWN, provenance);

        return new PreparedTarget(
                resolved.pos(),
                resolved.distanceBlocks(),
                targetName,
                resolved.id(),
                targetKey,
                fact.factId()
        );
    }

    private static NearbyStructureResolver.ResolvedStructure resolveTarget(
            ServerLevel level,
            VillageContext village,
            VillageStructureQuestCatalog.Definition definition
    ) {
        String cacheKey = level.getSeed() + ":" + village.key() + ":structure:" + definition.id();
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
        if (found != null && !legalTarget(level, village.cell(), found, definition)) found = null;
        TARGET_CACHE.put(cacheKey, new CachedTarget(found, level.getGameTime()));
        return found;
    }

    private static boolean legalTarget(
            ServerLevel level,
            ZoneBridge.Cell villageCell,
            NearbyStructureResolver.ResolvedStructure target,
            VillageStructureQuestCatalog.Definition definition
    ) {
        ZoneBridge.Cell targetCell = ZoneBridge.cellAt(level, target.pos());
        if (!villageCell.known() || !targetCell.known()) return false;

        int issuingRank = ZoneBridge.tierRank(villageCell.tier());
        int targetRank = ZoneBridge.tierRank(targetCell.tier());
        if (issuingRank < 0 || targetRank < 0) return false;
        if (targetRank < issuingRank + definition.targetMinTierOffset()) return false;
        if (targetRank > issuingRank + definition.targetMaxTierOffset()) return false;

        return !definition.sameMacroRegion()
                || "SHARED_CORE".equals(villageCell.band())
                || villageCell.macro().equals(targetCell.macro());
    }

    private static void writePending(
            CompoundTag root,
            ServerLevel level,
            ServerPlayer player,
            Villager villager,
            VillageContext village,
            VillageStructureQuestCatalog.Definition definition,
            PreparedTarget target
    ) {
        CompoundTag pending = new CompoundTag();
        pending.putString("quest_id", definition.id());
        pending.putString("title", definition.title());
        pending.putString("giver_uuid", villager.getUUID().toString());
        pending.putString("giver_name", villager.getDisplayName().getString());
        pending.putString("village_dimension", level.dimension().location().toString());
        pending.putString("village_key", village.key());
        putPos(pending, "village", village.anchor());
        pending.putString("village_name", village.name());
        pending.putBoolean("has_board", village.hasBoard());
        if (village.hasBoard()) putPos(pending, "board", village.boardPos());

        pending.putString("target_dimension", level.dimension().location().toString());
        pending.putString("target_structure", target.structureId().toString());
        pending.putString("target_key", target.targetKey());
        pending.putString("fact_id", target.factId());
        putPos(pending, "target", target.pos());
        pending.putString("target_name", target.displayName());
        pending.putInt("target_distance", target.distanceBlocks());
        pending.putString("target_direction", direction(village.anchor(), target.pos()));
        pending.putInt("target_radius", definition.targetRadiusBlocks());
        pending.putInt("required_kills", definition.requiredKills());
        pending.putInt("kill_count", 0);
        pending.putBoolean("objective_complete", false);
        pending.putLong("created_game_time", level.getGameTime());
        root.put(PENDING, pending);
    }

    private static void acceptPending(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (coreQuestActive(player)) return;

        CompoundTag root = root(player);
        if (!root.getCompound(ACTIVE).isEmpty()) return;
        CompoundTag pending = root.getCompound(PENDING);
        if (pending.isEmpty() || !sameDimension(level, pending)) return;

        VillageStructureQuestCatalog.Definition definition =
                VillageStructureQuestCatalog.byId(pending.getString("quest_id"));
        if (definition == null) return;

        long age = level.getGameTime() - pending.getLong("created_game_time");
        if (age < 0 || age > PENDING_LIFETIME) {
            root.remove(PENDING);
            saveRoot(player, root);
            player.sendSystemMessage(Component.literal("That local lead has gone stale. Ask around again.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag active = pending.copy();
        active.remove("created_game_time");
        active.putBoolean("objective_complete", false);
        root.put(ACTIVE, active);
        root.remove(PENDING);
        saveRoot(player, root);

        ItemStack contract = new ItemStack(ModItems.VILLAGE_CONTRACT.get());
        contract.setHoverName(Component.literal(definition.title()).withStyle(ChatFormatting.GOLD));
        CompoundTag tag = contract.getOrCreateTag();
        tag.putString(VillageContractItem.QUEST_ID, definition.id());
        tag.putString(VillageContractItem.TARGET_NAME, active.getString("target_name"));
        tag.putInt(VillageContractItem.TARGET_DISTANCE, active.getInt("target_distance"));
        tag.putString(VillageContractItem.TARGET_DIRECTION, active.getString("target_direction"));
        tag.putString(VillageContractItem.ISSUING_VILLAGE, active.getString("village_name"));
        tag.putString(VillageContractItem.ISSUING_VILLAGER, active.getString("giver_name"));
        tag.putString(VillageContractItem.OBJECTIVE_TEXT, objectiveText(definition, active));
        tag.putString(VillageContractItem.RETURN_TEXT, returnInstruction(active).replace(".", ""));
        giveOrDrop(player, contract);

        if (definition.revealAtlasOnAccept()) {
            NamedPlaceBridge.revealStructureToAtlas(
                    player,
                    ResourceLocation.tryParse(active.getString("target_structure")),
                    readPos(active, "target"),
                    active.getString("target_name")
            );
        }

        player.sendSystemMessage(
                Component.literal("Accepted: " + definition.title() + ". " + objectiveText(definition, active)
                                + " — about " + active.getInt("target_distance") + " blocks "
                                + active.getString("target_direction") + ".")
                        .withStyle(ChatFormatting.GOLD)
        );
    }

    private static void turnInActive(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (active.isEmpty() || !active.getBoolean("objective_complete") || !sameDimension(level, active)) return;

        VillageStructureQuestCatalog.Definition definition =
                VillageStructureQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null) return;

        BlockPos villageAnchor = readPos(active, "village");
        if (!insideRadius(player.blockPosition(), villageAnchor, VILLAGE_RETURN_RADIUS)) {
            player.sendSystemMessage(Component.literal("Return to " + active.getString("village_name") + " before turning this in.")
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        if (definition.objectiveType() == VillageStructureQuestCatalog.ObjectiveType.STRUCTURE_RECOVERY
                && !hasItem(player, definition.recoveryItemId())) {
            active.putBoolean("objective_complete", false);
            root.put(ACTIVE, active);
            saveRoot(player, root);
            player.sendSystemMessage(Component.literal("You still need to bring back " + recoveryName(definition) + ".")
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        if (definition.objectiveType() == VillageStructureQuestCatalog.ObjectiveType.STRUCTURE_RECOVERY) {
            consumeItem(player, definition.recoveryItemId());
        }

        giveOrDrop(player, new ItemStack(Items.EMERALD, definition.emeraldReward()));
        for (VillageStructureQuestCatalog.RewardStack reward : definition.rewardItems()) giveRewardStack(player, reward);
        player.giveExperiencePoints(definition.experienceReward());

        String villageKey = active.getString("village_key");
        VillageProgressState.recordAccomplishment(
                player,
                villageKey,
                definition.accomplishmentCategory(),
                definition.id()
        );
        VillageProgressState.Snapshot progress = VillageProgressState.snapshot(player, villageKey);

        String factId = active.getString("fact_id");
        if (!factId.isBlank()) VillageLocalFactSavedData.get(level).resolve(villageKey, factId, level.getGameTime());
        confirmTargetKnowledge(player, level, active);

        if (active.getBoolean("has_board")) {
            BountifulBridge.awardBoardCompletion(level, readPos(active, "board"), player);
        }

        CompoundTag completed = root.getCompound(COMPLETED);
        completed.putBoolean(completionKey(definition.id(), villageKey), true);
        root.put(COMPLETED, completed);
        root.remove(ACTIVE);
        root.remove(PENDING);
        saveRoot(player, root);
        removeContract(player, definition.id());

        player.sendSystemMessage(
                Component.literal("Completed: " + definition.title() + ". " + active.getString("village_name")
                                + " remembers it. Standing: " + display(progress.trust()) + ".")
                        .withStyle(ChatFormatting.GREEN)
        );
    }

    private static void completeObjective(
            ServerPlayer player,
            CompoundTag root,
            CompoundTag active,
            VillageStructureQuestCatalog.Definition definition,
            String message
    ) {
        active.putBoolean("objective_complete", true);
        root.put(ACTIVE, active);
        saveRoot(player, root);
        confirmTargetKnowledge(player, player.serverLevel(), active);
        player.sendSystemMessage(
                Component.literal(message + " " + returnInstruction(active)).withStyle(ChatFormatting.AQUA)
        );
    }

    private static void confirmTargetKnowledge(ServerPlayer player, ServerLevel level, CompoundTag active) {
        String key = active.getString("target_key");
        if (key.isBlank()) {
            ResourceLocation structure = ResourceLocation.tryParse(active.getString("target_structure"));
            if (structure == null) return;
            key = targetSubjectKey(level, structure, readPos(active, "target"));
        }
        PlayerKnowledgeState.advance(
                player,
                key,
                PlayerKnowledgeState.Knowledge.CONFIRMED,
                PlayerKnowledgeState.Provenance.QUEST_PROOF
        );
    }

    private static boolean coreQuestActive(ServerPlayer player) {
        CompoundTag core = player.getPersistentData().getCompound(CORE_ROOT);
        return !core.getCompound(ACTIVE).isEmpty();
    }

    private static boolean sameIssuingVillage(CompoundTag active, ServerLevel level, VillageContext village) {
        return sameDimension(level, active) && active.getString("village_key").equals(village.key());
    }

    private static boolean sameDimension(ServerLevel level, CompoundTag tag) {
        return level.dimension().location().toString().equals(tag.getString("village_dimension"));
    }

    private static boolean isCompleted(
            CompoundTag root,
            VillageStructureQuestCatalog.Definition definition,
            String villageKey
    ) {
        return root.getCompound(COMPLETED).getBoolean(completionKey(definition.id(), villageKey));
    }

    private static String completionKey(String questId, String villageKey) {
        return questId + "@village@" + villageKey;
    }

    private static String objectiveText(VillageStructureQuestCatalog.Definition definition, CompoundTag active) {
        String target = active.getString("target_name");
        return switch (definition.objectiveType()) {
            case STRUCTURE_SURVEY -> "Survey " + target;
            case STRUCTURE_HOSTILE_CLEAR -> "Clear " + Math.max(1, definition.requiredKills()) + " hostiles around " + target;
            case STRUCTURE_RECOVERY -> "Recover " + recoveryName(definition) + " from " + target;
        };
    }

    private static String returnInstruction(CompoundTag active) {
        String giver = active.getString("giver_name");
        String village = active.getString("village_name");
        if (!giver.isBlank() && !village.isBlank()) return "Return to " + giver + " in " + village + ".";
        if (!village.isBlank()) return "Return to the issuing villager in " + village + ".";
        return "Return to the villager who gave you the job.";
    }

    private static String recoveryName(VillageStructureQuestCatalog.Definition definition) {
        String name = definition.recoveryItemName();
        return name == null || name.isBlank() ? "the requested item" : name;
    }

    private static boolean hasItem(ServerPlayer player, ResourceLocation itemId) {
        if (itemId == null) return false;
        Item wanted = ForgeRegistries.ITEMS.getValue(itemId);
        if (wanted == null || wanted == Items.AIR) return false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(wanted)) return true;
        }
        return false;
    }

    private static boolean consumeItem(ServerPlayer player, ResourceLocation itemId) {
        if (itemId == null) return false;
        Item wanted = ForgeRegistries.ITEMS.getValue(itemId);
        if (wanted == null || wanted == Items.AIR) return false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(wanted)) continue;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    private static void giveRewardStack(ServerPlayer player, VillageStructureQuestCatalog.RewardStack reward) {
        Item item = ForgeRegistries.ITEMS.getValue(reward.itemId());
        if (item == null || item == Items.AIR || reward.count() <= 0) return;
        ItemStack stack = new ItemStack(item, reward.count());
        if (reward.customName() != null && !reward.customName().isBlank()) {
            stack.setHoverName(Component.literal(reward.customName()).withStyle(ChatFormatting.GOLD));
        }
        giveOrDrop(player, stack);
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

    private static boolean insideRadius(BlockPos pos, BlockPos target, int radius) {
        long dx = (long) pos.getX() - target.getX();
        long dz = (long) pos.getZ() - target.getZ();
        return dx * dx + dz * dz <= (long) radius * radius;
    }

    private static String targetSubjectKey(ServerLevel level, ResourceLocation structureId, BlockPos target) {
        return level.dimension().location() + "|structure|" + structureId + "|"
                + Math.floorDiv(target.getX(), 16) + "," + Math.floorDiv(target.getZ(), 16);
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

    private record CachedTarget(NearbyStructureResolver.ResolvedStructure target, long checkedAt) {}

    private record PreparedTarget(
            BlockPos pos,
            int distanceBlocks,
            String displayName,
            ResourceLocation structureId,
            String targetKey,
            String factId
    ) {}

    private record Offer(VillageStructureQuestCatalog.Definition definition, PreparedTarget target) {}
}
