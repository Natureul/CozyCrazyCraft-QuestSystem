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
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authored villager quest lifecycle and executable Zone 1 social lattice.
 *
 * Bountiful remains the public repeatable/civic layer. These contracts are village-owned,
 * semantically categorized, recoverable through compatible professions, and grounded in real local
 * geography. Structure-bound work resolves an actual generated structure before it is offered and
 * freezes that exact place into quest state.
 */
public final class VillageConversationQuestManager {
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

        VillageContext village = VillageContext.resolve(level, villager.blockPosition());
        if (village == null) {
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (!active.isEmpty()) {
            VillageQuestCatalog.Definition activeDefinition = VillageQuestCatalog.byId(active.getString("quest_id"));
            if (activeDefinition != null
                    && activeDefinition.accepts(villager.getVillagerData().getProfession())
                    && sameIssuingVillage(active, level, village)) {
                ConversationBridge.setDialogue(
                        villager,
                        objectiveComplete(active) ? activeDefinition.turninDialogue() : activeDefinition.activeDialogue()
                );
            } else {
                ConversationBridge.clearOwnDialogue(villager);
            }
            return;
        }

        VillageProgressState.Snapshot progress = VillageProgressState.snapshot(player, village.key());
        Offer offer = selectOffer(level, player, villager, village, progress, root);
        if (offer == null) {
            root.remove(PENDING);
            saveRoot(player, root);
            ConversationBridge.clearOwnDialogue(villager);
            return;
        }

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
        if (active.isEmpty() || objectiveComplete(active)) return;

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null || definition.objectiveType() != VillageQuestCatalog.ObjectiveType.STRUCTURE_SURVEY) return;
        if (!level.dimension().location().toString().equals(active.getString("target_dimension"))) return;

        BlockPos target = readPos(active, "target");
        int radius = active.getInt("target_radius");
        long dx = (long) player.blockPosition().getX() - target.getX();
        long dz = (long) player.blockPosition().getZ() - target.getZ();
        if (dx * dx + dz * dz > (long) radius * radius) return;

        active.putBoolean("objective_complete", true);
        active.putBoolean("surveyed", true);
        root.put(ACTIVE, active);
        saveRoot(player, root);

        String targetKey = active.getString("target_key");
        if (targetKey.isBlank()) targetKey = legacyTargetSubjectKey(level, active, target);
        PlayerKnowledgeState.advance(
                player,
                targetKey,
                PlayerKnowledgeState.Knowledge.CONFIRMED,
                PlayerKnowledgeState.Provenance.QUEST_PROOF
        );

        player.sendSystemMessage(
                Component.literal("Survey complete: " + active.getString("target_name") + ". "
                                + returnInstruction(definition, active.getString("village_name")))
                        .withStyle(ChatFormatting.AQUA)
        );
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (event.getEntity().getType().getCategory() != MobCategory.MONSTER) return;

        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (active.isEmpty() || objectiveComplete(active)) return;

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null) return;
        if (definition.objectiveType() != VillageQuestCatalog.ObjectiveType.LOCAL_HOSTILE_CLEAR
                && definition.objectiveType() != VillageQuestCatalog.ObjectiveType.STRUCTURE_HOSTILE_CLEAR) return;
        if (!level.dimension().location().toString().equals(active.getString("target_dimension"))) return;

        BlockPos target = readPos(active, "target");
        int radius = active.getInt("target_radius");
        long dx = (long) event.getEntity().blockPosition().getX() - target.getX();
        long dz = (long) event.getEntity().blockPosition().getZ() - target.getZ();
        if (dx * dx + dz * dz > (long) radius * radius) return;

        int required = Math.max(1, active.getInt("required_kills"));
        int count = Math.min(required, active.getInt("kill_count") + 1);
        active.putInt("kill_count", count);
        if (count >= required) active.putBoolean("objective_complete", true);
        root.put(ACTIVE, active);
        saveRoot(player, root);

        if (count >= required) {
            String cleared = definition.objectiveType() == VillageQuestCatalog.ObjectiveType.STRUCTURE_HOSTILE_CLEAR
                    ? active.getString("target_name") + " is clear enough to report back"
                    : "the area is clear";
            player.sendSystemMessage(
                    Component.literal(definition.title() + ": " + cleared + ". "
                                    + returnInstruction(definition, active.getString("village_name")))
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

    private static Offer selectOffer(
            ServerLevel level,
            ServerPlayer player,
            Villager villager,
            VillageContext village,
            VillageProgressState.Snapshot progress,
            CompoundTag root
    ) {
        for (VillageQuestCatalog.Definition definition : VillageQuestCatalog.forProfession(villager.getVillagerData().getProfession())) {
            if (!definition.issuingTier().equals(village.cell().tier())) continue;

            if (definition.zoneOneCapstone()) {
                // Capstone is an authored ratchet, never an ordinary early roll. Breadth first:
                // COMMUNITY + EXPLORATION + PROFESSION/DANGER, then the serious local problem.
                if (!progress.capstoneEligible() || progress.capstoneComplete()) continue;
            } else if (definition.accomplishmentCategory() != null
                    && progress.categories().contains(definition.accomplishmentCategory())) {
                continue;
            }

            if (isCompleted(root, definition, level, village)) continue;

            PreparedTarget target = prepareTarget(level, player, village, definition);
            if (target != null) return new Offer(definition, target);
        }
        return null;
    }

    private static PreparedTarget prepareTarget(
            ServerLevel level,
            ServerPlayer player,
            VillageContext village,
            VillageQuestCatalog.Definition definition
    ) {
        if (definition.objectiveType() == VillageQuestCatalog.ObjectiveType.STRUCTURE_SURVEY
                || definition.objectiveType() == VillageQuestCatalog.ObjectiveType.STRUCTURE_HOSTILE_CLEAR) {
            NearbyStructureResolver.ResolvedStructure resolved = resolveStructureTarget(level, village, definition);
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

            PlayerKnowledgeState.advance(
                    player,
                    targetKey,
                    PlayerKnowledgeState.Knowledge.KNOWN,
                    PlayerKnowledgeState.Provenance.MAP_RECORD
            );

            return new PreparedTarget(
                    resolved.pos(),
                    resolved.distanceBlocks(),
                    targetName,
                    resolved.id(),
                    targetKey,
                    fact.factId()
            );
        }

        VillageLocalAreaResolver.LocalTarget local = VillageLocalAreaResolver.find(level, village, definition);
        if (local == null) return null;
        VillageLocalFactSavedData.FactKind kind = definition.accomplishmentCategory()
                == VillageProgressState.AccomplishmentCategory.COMMUNITY
                ? VillageLocalFactSavedData.FactKind.COMMUNITY_INCIDENT
                : VillageLocalFactSavedData.FactKind.ROAD;
        VillageLocalFactSavedData.Fact fact = VillageLocalFactSavedData.get(level).rememberLocalIncident(
                village,
                kind,
                definition.id(),
                local.pos(),
                local.label(),
                level.getGameTime()
        );
        return new PreparedTarget(local.pos(), local.distanceBlocks(), local.label(), null, "", fact.factId());
    }

    private static void acceptPending(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        CompoundTag root = root(player);
        if (!root.getCompound(ACTIVE).isEmpty()) return;

        CompoundTag pending = root.getCompound(PENDING);
        if (pending.isEmpty() || !sameDimension(level, pending)) return;
        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(pending.getString("quest_id"));
        if (definition == null) return;

        long age = level.getGameTime() - pending.getLong("created_game_time");
        if (age < 0 || age > PENDING_LIFETIME) {
            root.remove(PENDING);
            saveRoot(player, root);
            player.sendSystemMessage(Component.literal("That local lead has gone stale. Speak to the villager again.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag active = pending.copy();
        active.remove("created_game_time");
        active.putBoolean("objective_complete", false);
        active.putBoolean("surveyed", false);
        active.putInt("kill_count", 0);
        root.put(ACTIVE, active);
        root.remove(PENDING);
        saveRoot(player, root);

        ItemStack contract = new ItemStack(ModItems.VILLAGE_CONTRACT.get());
        contract.setHoverName(Component.literal(definition.title()).withStyle(ChatFormatting.GOLD));
        contract.getOrCreateTag().putString(VillageContractItem.QUEST_ID, definition.id());
        contract.getOrCreateTag().putString(VillageContractItem.TARGET_NAME, active.getString("target_name"));
        contract.getOrCreateTag().putInt(VillageContractItem.TARGET_DISTANCE, active.getInt("target_distance"));
        contract.getOrCreateTag().putString(VillageContractItem.TARGET_DIRECTION, active.getString("target_direction"));
        contract.getOrCreateTag().putString(VillageContractItem.ISSUING_VILLAGE, active.getString("village_name"));
        if (!player.addItem(contract)) player.drop(contract, false);

        for (VillageQuestCatalog.RewardStack stack : definition.acceptanceItems()) giveRewardStack(player, stack);

        boolean atlasMarked = false;
        if (definition.revealAtlasOnAccept()) {
            ResourceLocation targetId = ResourceLocation.tryParse(active.getString("target_structure"));
            atlasMarked = targetId != null && NamedPlaceBridge.revealStructureToAtlas(
                    player,
                    targetId,
                    readPos(active, "target"),
                    active.getString("target_name")
            );
        }

        String prefix = active.getString("village_name");
        if (prefix.isBlank() || "the village".equals(prefix)) prefix = "Accepted";
        else prefix += " — " + definition.title();

        String objective = switch (definition.objectiveType()) {
            case STRUCTURE_SURVEY -> "survey " + active.getString("target_name");
            case STRUCTURE_HOSTILE_CLEAR -> "clear " + definition.requiredKills() + " hostiles at " + active.getString("target_name");
            case LOCAL_HOSTILE_CLEAR -> "clear " + definition.requiredKills() + " hostiles around " + active.getString("target_name");
        };
        player.sendSystemMessage(
                Component.literal(prefix + ": " + objective + ", about " + active.getInt("target_distance")
                                + " blocks " + active.getString("target_direction") + ".")
                        .withStyle(ChatFormatting.GOLD)
        );
        if (atlasMarked) {
            player.sendSystemMessage(
                    Component.literal(active.getString("target_name") + " has been added to your Atlas.")
                            .withStyle(ChatFormatting.AQUA)
            );
        }
    }

    private static void turnInActive(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        CompoundTag root = root(player);
        CompoundTag active = root.getCompound(ACTIVE);
        if (active.isEmpty() || !objectiveComplete(active) || !sameDimension(level, active)) return;

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null) return;

        BlockPos villageAnchor = readPos(active, "village");
        long dx = (long) player.blockPosition().getX() - villageAnchor.getX();
        long dz = (long) player.blockPosition().getZ() - villageAnchor.getZ();
        if (dx * dx + dz * dz > (long) VILLAGE_RETURN_RADIUS * VILLAGE_RETURN_RADIUS) {
            String villageName = active.getString("village_name");
            if (villageName.isBlank()) villageName = "the village that issued this work";
            player.sendSystemMessage(Component.literal("Return to " + villageName + " before turning this in.")
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        giveOrDrop(player, new ItemStack(Items.EMERALD, definition.emeraldReward()));
        for (VillageQuestCatalog.RewardStack stack : definition.rewardItems()) giveRewardStack(player, stack);
        player.giveExperiencePoints(definition.experienceReward());

        String villageKey = active.getString("village_key");
        if (villageKey.isBlank()) {
            VillageContext recovered = VillageContext.resolve(level, villageAnchor);
            villageKey = recovered != null ? recovered.key() : legacyVillageKey(level, active, villageAnchor);
        }

        if (definition.zoneOneCapstone()) {
            VillageProgressState.markZoneOneCapstoneComplete(player, villageKey);
        } else if (definition.accomplishmentCategory() != null) {
            VillageProgressState.recordAccomplishment(
                    player,
                    villageKey,
                    definition.accomplishmentCategory(),
                    definition.id()
            );
        }
        VillageProgressState.Snapshot progress = VillageProgressState.snapshot(player, villageKey);

        String factId = active.getString("fact_id");
        if (!factId.isBlank()) VillageLocalFactSavedData.get(level).resolve(villageKey, factId, level.getGameTime());

        String targetKey = active.getString("target_key");
        if (!targetKey.isBlank()) {
            PlayerKnowledgeState.advance(
                    player,
                    targetKey,
                    PlayerKnowledgeState.Knowledge.CONFIRMED,
                    PlayerKnowledgeState.Provenance.PLAYER_REPORT
            );
        }

        boolean boardSynced = false;
        if (active.getBoolean("has_board") || active.contains("boardX")) {
            boardSynced = BountifulBridge.awardBoardCompletion(level, readPos(active, "board"), player);
        }

        markCompleted(root, definition, level, villageKey, active);
        root.remove(ACTIVE);
        root.remove(PENDING);
        saveRoot(player, root);
        removeContract(player, definition.id());

        String villageName = active.getString("village_name");
        String records = villageName.isBlank() || "the village".equals(villageName)
                ? "The village remembers what you did."
                : villageName + " remembers what you did.";
        player.sendSystemMessage(
                Component.literal("Completed: " + definition.title() + ". " + records
                                + " Standing: " + display(progress.trust()) + ".")
                        .withStyle(ChatFormatting.GREEN)
        );
        if ((active.getBoolean("has_board") || active.contains("boardX")) && !boardSynced) {
            CozyCrazyQuests.LOGGER.debug("Authored quest completed without Bountiful board synchronization for {}", villageKey);
        }
    }

    private static void writePending(
            CompoundTag root,
            ServerLevel level,
            ServerPlayer player,
            Villager villager,
            VillageContext village,
            VillageQuestCatalog.Definition definition,
            PreparedTarget target
    ) {
        CompoundTag pending = new CompoundTag();
        pending.putString("quest_id", definition.id());
        pending.putString("title", definition.title());
        pending.putString("giver_uuid", villager.getUUID().toString());
        pending.putString("giver_profession", professionLabel(villager.getVillagerData().getProfession()));
        pending.putString("objective_type", definition.objectiveType().name());
        pending.putInt("required_kills", definition.requiredKills());
        pending.putString("village_dimension", level.dimension().location().toString());
        pending.putString("board_dimension", level.dimension().location().toString());
        pending.putString("village_key", village.key());
        putPos(pending, "village", village.anchor());
        pending.putString("village_name", village.name());
        pending.putString("village_macro", village.cell().macro());
        pending.putString("village_tier", village.cell().tier());
        pending.putBoolean("has_board", village.hasBoard());
        if (village.hasBoard()) putPos(pending, "board", village.boardPos());
        pending.putString("target_dimension", level.dimension().location().toString());
        pending.putString("target_structure", target.structureId() == null ? "" : target.structureId().toString());
        pending.putString("target_key", target.targetKey());
        pending.putString("fact_id", target.factId());
        putPos(pending, "target", target.pos());
        pending.putInt("target_radius", definition.targetRadiusBlocks());
        pending.putString("target_name", target.displayName());
        pending.putInt("target_distance", target.distanceBlocks());
        pending.putString("target_direction", direction(village.anchor(), target.pos()));
        pending.putLong("created_game_time", level.getGameTime());
        pending.putInt("trust_when_offered", village.legacyBoardTrust(level));
        pending.putString("semantic_trust_when_offered", VillageProgressState.snapshot(player, village.key()).trust().name());
        root.put(PENDING, pending);
    }

    private static NearbyStructureResolver.ResolvedStructure resolveStructureTarget(
            ServerLevel level,
            VillageContext village,
            VillageQuestCatalog.Definition definition
    ) {
        String cacheKey = level.getSeed() + ":" + village.key() + ":" + definition.id();
        CachedTarget cached = TARGET_CACHE.get(cacheKey);
        if (cached != null && level.getGameTime() - cached.checkedAt() <= TARGET_CACHE_LIFETIME) return cached.target();

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
            VillageQuestCatalog.Definition definition
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

    private static boolean sameIssuingVillage(CompoundTag active, ServerLevel level, VillageContext village) {
        if (!sameDimension(level, active)) return false;
        String key = active.getString("village_key");
        if (!key.isBlank()) return key.equals(village.key());
        if (active.contains("boardX") && village.hasBoard()) return village.boardPos().equals(readPos(active, "board"));
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
        if (active.contains("boardX")) completed.putBoolean(legacyCompletionKey(definition.id(), level, readPos(active, "board")), true);
        root.put(COMPLETED, completed);
    }

    private static String completionKey(String questId, String villageKey) {
        return questId + "@village@" + villageKey;
    }

    private static String legacyCompletionKey(String questId, ServerLevel level, BlockPos boardPos) {
        return questId + "@" + level.dimension().location() + "@" + boardPos.getX() + "," + boardPos.getY() + "," + boardPos.getZ();
    }

    private static boolean objectiveComplete(CompoundTag active) {
        return active.getBoolean("objective_complete") || active.getBoolean("surveyed");
    }

    private static String targetSubjectKey(ServerLevel level, ResourceLocation structureId, BlockPos target) {
        return level.dimension().location() + "|structure|" + structureId + "|"
                + Math.floorDiv(target.getX(), 16) + "," + Math.floorDiv(target.getZ(), 16);
    }

    private static String legacyTargetSubjectKey(ServerLevel level, CompoundTag active, BlockPos target) {
        ResourceLocation structureId = ResourceLocation.tryParse(active.getString("target_structure"));
        if (structureId != null) return targetSubjectKey(level, structureId, target);
        return level.dimension().location() + "|place|" + active.getString("target_name") + "|"
                + Math.floorDiv(target.getX(), 16) + "," + Math.floorDiv(target.getZ(), 16);
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

    private static void giveRewardStack(ServerPlayer player, VillageQuestCatalog.RewardStack reward) {
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

    private static String returnInstruction(VillageQuestCatalog.Definition definition, String villageName) {
        if (villageName == null || villageName.isBlank()) villageName = "the issuing village";
        return "Return to " + professionList(definition.giverProfessions()) + " in " + villageName + ".";
    }

    private static String professionList(List<VillagerProfession> professions) {
        List<String> labels = new ArrayList<>();
        for (VillagerProfession profession : professions) labels.add("a " + professionLabel(profession));
        if (labels.isEmpty()) return "a village representative";
        if (labels.size() == 1) return labels.get(0);
        if (labels.size() == 2) return labels.get(0) + " or " + labels.get(1);
        return String.join(", ", labels.subList(0, labels.size() - 1)) + ", or " + labels.get(labels.size() - 1);
    }

    private static String professionLabel(VillagerProfession profession) {
        if (profession == VillagerProfession.ARMORER) return "armorer";
        if (profession == VillagerProfession.BUTCHER) return "butcher";
        if (profession == VillagerProfession.CARTOGRAPHER) return "cartographer";
        if (profession == VillagerProfession.CLERIC) return "cleric";
        if (profession == VillagerProfession.FARMER) return "farmer";
        if (profession == VillagerProfession.FISHERMAN) return "fisherman";
        if (profession == VillagerProfession.FLETCHER) return "fletcher";
        if (profession == VillagerProfession.LEATHERWORKER) return "leatherworker";
        if (profession == VillagerProfession.LIBRARIAN) return "librarian";
        if (profession == VillagerProfession.MASON) return "mason";
        if (profession == VillagerProfession.SHEPHERD) return "shepherd";
        if (profession == VillagerProfession.TOOLSMITH) return "toolsmith";
        if (profession == VillagerProfession.WEAPONSMITH) return "weaponsmith";
        return "villager";
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
    private record Offer(VillageQuestCatalog.Definition definition, PreparedTarget target) {}
}
