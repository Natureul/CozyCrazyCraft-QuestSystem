package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

/**
 * Small executable Zone 1 slice of the much larger Quest & Reward Bible.
 *
 * The catalog intentionally starts with three semantically distinct branches rather than importing
 * hundreds of records before the lifecycle is stable: COMMUNITY, EXPLORATION, and PROFESSION.
 * Definitions remain first-clear per player/village; Bountiful owns the repeatable civic layer.
 */
final class VillageQuestCatalog {
    static final Definition SAFE_PASTURE = new Definition(
            "hearthlands_community_safe_pasture",
            "The Outer Pasture",
            List.of(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.BUTCHER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY,
            ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS",
            0,
            0,
            true,
            0,
            72,
            96,
            176,
            "the outer pasture",
            List.of(),
            3,
            id("community_safe_pasture_offer"),
            id("community_safe_pasture_active"),
            id("community_safe_pasture_turnin"),
            false,
            List.of(),
            List.of(new RewardStack(new ResourceLocation("minecraft", "bread"), 8, null)),
            4,
            4
    );

    static final Definition FIRST_REAL_MAP = new Definition(
            "hearthlands_cartographer_first_real_map",
            "The First Real Map",
            List.of(VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION,
            ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS",
            0,
            1,
            true,
            1024,
            56,
            0,
            0,
            "local landmark",
            List.of(
                    new ResourceLocation("dungeons_enhanced", "watch_tower"),
                    new ResourceLocation("valhelsia_structures", "tower_ruin"),
                    new ResourceLocation("dungeons_enhanced", "stables")
            ),
            0,
            id("cartographer_first_real_map"),
            id("cartographer_quest_active"),
            id("cartographer_quest_turnin"),
            true,
            List.of(),
            List.of(new RewardStack(new ResourceLocation("minecraft", "spyglass"), 1, null)),
            5,
            5
    );

    static final Definition PATROL_TRIAL = new Definition(
            "hearthlands_profession_patrol_trial",
            "A Patrol Trial",
            List.of(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION,
            ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS",
            0,
            0,
            true,
            0,
            80,
            112,
            208,
            "the patrol road",
            List.of(),
            4,
            id("profession_patrol_trial_offer"),
            id("profession_patrol_trial_active"),
            id("profession_patrol_trial_turnin"),
            false,
            List.of(new RewardStack(new ResourceLocation("minecraft", "iron_sword"), 1, "Patrol Trial Blade")),
            List.of(),
            5,
            5
    );

    static final List<Definition> ZONE_ONE = List.of(SAFE_PASTURE, FIRST_REAL_MAP, PATROL_TRIAL);

    private VillageQuestCatalog() {}

    static Definition byId(String id) {
        for (Definition definition : ZONE_ONE) {
            if (definition.id.equals(id)) return definition;
        }
        return null;
    }

    static List<Definition> forProfession(VillagerProfession profession) {
        return ZONE_ONE.stream().filter(definition -> definition.giverProfessions.contains(profession)).toList();
    }

    static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }

    enum ObjectiveType {
        STRUCTURE_SURVEY,
        LOCAL_HOSTILE_CLEAR
    }

    record RewardStack(ResourceLocation itemId, int count, String customName) {}

    record Definition(
            String id,
            String title,
            List<VillagerProfession> giverProfessions,
            VillageProgressState.AccomplishmentCategory accomplishmentCategory,
            ObjectiveType objectiveType,
            String issuingTier,
            int targetMinTierOffset,
            int targetMaxTierOffset,
            boolean sameMacroRegion,
            int searchRadiusBlocks,
            int targetRadiusBlocks,
            int localTargetMinDistance,
            int localTargetMaxDistance,
            String targetLabel,
            List<ResourceLocation> structureCandidates,
            int requiredKills,
            ResourceLocation offerDialogue,
            ResourceLocation activeDialogue,
            ResourceLocation turninDialogue,
            boolean revealAtlasOnAccept,
            List<RewardStack> acceptanceItems,
            List<RewardStack> rewardItems,
            int emeraldReward,
            int experienceReward
    ) {
        boolean accepts(VillagerProfession profession) {
            return giverProfessions.contains(profession);
        }
    }
}
