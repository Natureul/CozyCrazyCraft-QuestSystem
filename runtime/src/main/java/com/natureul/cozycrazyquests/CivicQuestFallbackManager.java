package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * Fills the deliberate gap between vanilla trade professions and village social progression.
 *
 * Vanilla jobs still get first refusal through VillageConversationQuestManager. If that layer has no
 * authored conversation, region-scoped work is offered with an explicit macro check. If the speaker is
 * one of the settlement's deterministic civic contacts, they may also represent ordinary village work
 * even while technically unemployed. No profession, workstation or trade data is ever mutated.
 */
final class CivicQuestFallbackManager {
    private CivicQuestFallbackManager() {}

    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof Villager villager) || villager.isBaby()) return;
        if (!ConversationBridge.available()) return;

        VillageContext village = VillageContext.resolve(level, villager.blockPosition());
        if (village == null) return;

        CompoundTag root = VillageQuestState.root(player);
        VillageQuestState.noteConversation(root, village.key(), villager.getUUID().toString());
        CompoundTag active = VillageQuestState.activeForVillage(root, village.key());
        boolean civic = VillageCivicRoleService.isCivicContact(level, village, villager);

        if (!active.isEmpty()) {
            // The main manager already owns normal profession-compatible active dialogue. Civic contacts
            // are a recovery path when the village otherwise has no valid occupational representative.
            if (ConversationBridge.hasOwnDialogue(villager)) return;
            VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
            if (definition != null && civic && sameIssuingVillage(active, level, village)) {
                ConversationBridge.setDialogue(
                        villager,
                        objectiveComplete(active) ? definition.turninDialogue() : definition.activeDialogue()
                );
            }
            VillageQuestState.save(player, root);
            return;
        }

        // Never replace an offer already selected by the profession-first core manager.
        if (ConversationBridge.hasOwnDialogue(villager)) return;

        VillageProgressState.Snapshot progress = VillageProgressState.snapshot(player, village.key());
        Offer offer = selectRegionalProfessionOffer(level, player, villager, village, progress, root);
        if (offer == null && civic) {
            offer = selectCivicOffer(level, player, village, progress, root);
        }
        if (offer == null) return;

        writePending(root, level, player, villager, village, offer.definition(), offer.target());
        VillageQuestState.save(player, root);
        ConversationBridge.setDialogue(villager, offer.definition().offerDialogue());
    }

    private static Offer selectRegionalProfessionOffer(
            ServerLevel level,
            ServerPlayer player,
            Villager villager,
            VillageContext village,
            VillageProgressState.Snapshot progress,
            CompoundTag root
    ) {
        for (VillageQuestCatalog.Definition definition :
                VillageQuestCatalog.regionalForProfession(villager.getVillagerData().getProfession(), village.cell())) {
            if (!eligible(root, definition, level, village, progress)) continue;
            PreparedTarget target = prepareTarget(level, player, village, definition);
            if (target != null) return new Offer(definition, target);
        }
        return null;
    }

    private static Offer selectCivicOffer(
            ServerLevel level,
            ServerPlayer player,
            VillageContext village,
            VillageProgressState.Snapshot progress,
            CompoundTag root
    ) {
        for (VillageQuestCatalog.Definition definition : VillageQuestCatalog.civicCandidates(village.cell())) {
            if (!eligible(root, definition, level, village, progress)) continue;
            PreparedTarget target = prepareTarget(level, player, village, definition);
            if (target != null) return new Offer(definition, target);
        }
        return null;
    }

    private static boolean eligible(
            CompoundTag root,
            VillageQuestCatalog.Definition definition,
            ServerLevel level,
            VillageContext village,
            VillageProgressState.Snapshot progress
    ) {
        if (!VillageQuestCatalog.issuesInCell(definition, village.cell())) return false;
        if (definition.zoneOneCapstone()) {
            if (!progress.capstoneEligible() || progress.capstoneComplete()) return false;
        } else if (definition.accomplishmentCategory() != null
                && progress.categories().contains(definition.accomplishmentCategory())) {
            return false;
        }
        CompoundTag completed = root.getCompound(VillageQuestState.COMPLETED);
        return !completed.getBoolean(definition.id() + "@village@" + village.key());
    }

    private static PreparedTarget prepareTarget(
            ServerLevel level,
            ServerPlayer player,
            VillageContext village,
            VillageQuestCatalog.Definition definition
    ) {
        if (definition.objectiveType() == VillageQuestCatalog.ObjectiveType.STRUCTURE_SURVEY
                || definition.objectiveType() == VillageQuestCatalog.ObjectiveType.STRUCTURE_HOSTILE_CLEAR) {
            NearbyStructureResolver.ResolvedStructure resolved = nearestLegalStructure(level, village, definition);
            if (resolved == null) return null;

            String targetName = NamedPlaceBridge.structureName(level, resolved.id(), resolved.pos());
            String targetKey = targetSubjectKey(level, resolved.id(), resolved.pos());
            VillageLocalFactSavedData.Fact fact = VillageLocalFactSavedData.get(level).rememberStructureSurvey(
                    village, resolved.id(), resolved.pos(), targetName, level.getGameTime());
            ApproachInfo approach = approachInfo(level, resolved.pos());
            return new PreparedTarget(
                    resolved.pos(), resolved.distanceBlocks(), targetName, resolved.id(), targetKey,
                    fact.factId(), approach.kind(), approach.depthBlocks()
            );
        }

        VillageLocalAreaResolver.LocalTarget local = VillageLocalAreaResolver.find(level, village, definition);
        if (local == null) return null;
        VillageLocalFactSavedData.FactKind kind = definition.accomplishmentCategory()
                == VillageProgressState.AccomplishmentCategory.COMMUNITY
                ? VillageLocalFactSavedData.FactKind.COMMUNITY_INCIDENT
                : VillageLocalFactSavedData.FactKind.ROAD;
        VillageLocalFactSavedData.Fact fact = VillageLocalFactSavedData.get(level).rememberLocalIncident(
                village, kind, definition.id(), local.pos(), local.label(), level.getGameTime());
        return new PreparedTarget(local.pos(), local.distanceBlocks(), local.label(), null, "", fact.factId(), "SURFACE", 0);
    }

    private static NearbyStructureResolver.ResolvedStructure nearestLegalStructure(
            ServerLevel level,
            VillageContext village,
            VillageQuestCatalog.Definition definition
    ) {
        NearbyStructureResolver.ResolvedStructure found = NearbyStructureResolver.findNearest(
                level, village.anchor(), definition.structureCandidates(), definition.searchRadiusBlocks());
        if (found != null && legalTarget(level, village.cell(), found, definition)) return found;

        NearbyStructureResolver.ResolvedStructure nearest = null;
        for (ResourceLocation candidate : definition.structureCandidates()) {
            NearbyStructureResolver.ResolvedStructure alternate = NearbyStructureResolver.findNearest(
                    level, village.anchor(), List.of(candidate), definition.searchRadiusBlocks());
            if (alternate == null || !legalTarget(level, village.cell(), alternate, definition)) continue;
            if (nearest == null || alternate.distanceBlocks() < nearest.distanceBlocks()) nearest = alternate;
        }
        return nearest;
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
        pending.putString("giver_civic_role", VillageCivicRoleService.roleFor(level, village, villager).label());
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
        pending.putString("target_approach", target.approach());
        pending.putInt("target_depth", target.depthBlocks());

        if (target.structureId() != null) {
            NamedPlaceBridge.StructureInstance instance = NamedPlaceBridge.structureInstance(level, target.structureId(), target.pos());
            if (instance != null) {
                pending.putInt("target_start_chunk_x", instance.startChunk().x);
                pending.putInt("target_start_chunk_z", instance.startChunk().z);
            }
        }

        pending.putLong("created_game_time", level.getGameTime());
        pending.putInt("trust_when_offered", village.legacyBoardTrust(level));
        pending.putString("semantic_trust_when_offered", VillageProgressState.snapshot(player, village.key()).trust().name());
        root.put(VillageQuestState.PENDING, pending);
    }

    private static boolean sameIssuingVillage(CompoundTag active, ServerLevel level, VillageContext village) {
        if (!level.dimension().location().toString().equals(active.getString("village_dimension"))) return false;
        String key = active.getString("village_key");
        return key.isBlank() || key.equals(village.key());
    }

    private static boolean objectiveComplete(CompoundTag active) {
        return active.getBoolean("objective_complete") || active.getBoolean("surveyed");
    }

    private static String targetSubjectKey(ServerLevel level, ResourceLocation structureId, BlockPos target) {
        return level.dimension().location() + "|structure|" + structureId + "|"
                + Math.floorDiv(target.getX(), 16) + "," + Math.floorDiv(target.getZ(), 16);
    }

    private static ApproachInfo approachInfo(ServerLevel level, BlockPos target) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, target.getX(), target.getZ());
        int depth = Math.max(0, surfaceY - target.getY());
        BlockPos surfaceBlock = new BlockPos(target.getX(), Math.max(level.getMinBuildHeight(), surfaceY - 1), target.getZ());
        if (!level.getFluidState(surfaceBlock).isEmpty() && depth >= 5) return new ApproachInfo("SUBMERGED", depth);
        if (depth >= 12) return new ApproachInfo("UNDERGROUND", depth);
        return new ApproachInfo("SURFACE", depth);
    }

    private static void putPos(CompoundTag tag, String prefix, BlockPos pos) {
        tag.putInt(prefix + "X", pos.getX());
        tag.putInt(prefix + "Y", pos.getY());
        tag.putInt(prefix + "Z", pos.getZ());
    }

    private static String professionLabel(VillagerProfession profession) {
        if (profession == VillagerProfession.NONE) return "unemployed";
        if (profession == VillagerProfession.NITWIT) return "resident";
        return profession.toString().toLowerCase();
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

    private record PreparedTarget(
            BlockPos pos,
            int distanceBlocks,
            String displayName,
            ResourceLocation structureId,
            String targetKey,
            String factId,
            String approach,
            int depthBlocks
    ) {}

    private record ApproachInfo(String kind, int depthBlocks) {}
    private record Offer(VillageQuestCatalog.Definition definition, PreparedTarget target) {}
}
