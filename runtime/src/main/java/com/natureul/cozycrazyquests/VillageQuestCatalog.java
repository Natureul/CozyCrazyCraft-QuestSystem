package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

final class VillageQuestCatalog {
    static final Definition EMPTY_STALLS = new Definition(
            "hearthlands_community_empty_stalls", "The Empty Stalls",
            List.of(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, false, ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS", 0, 0, true, 896, 56, 0, 0, "the old stables",
            List.of(new ResourceLocation("dungeons_enhanced", "stables")), 0,
            id("community_empty_stalls_offer"), id("community_empty_stalls_active"), id("community_empty_stalls_turnin"),
            false, List.of(), List.of(new RewardStack(new ResourceLocation("minecraft", "saddle"), 1, "Recovered Riding Tack")), 4, 5
    );

    static final Definition SAFE_PASTURE = new Definition(
            "hearthlands_community_safe_pasture", "The Outer Pasture",
            List.of(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.BUTCHER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, false, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 72, 96, 176, "the outer pasture", List.of(), 3,
            id("community_safe_pasture_offer"), id("community_safe_pasture_active"), id("community_safe_pasture_turnin"),
            false, List.of(), List.of(new RewardStack(new ResourceLocation("minecraft", "leather"), 4, null)), 4, 4
    );

    static final Definition OLD_WALLS_OLD_NAMES = new Definition(
            "hearthlands_exploration_old_walls", "Old Walls, Old Names",
            List.of(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, false, ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS", 0, 0, true, 1024, 56, 0, 0, "the old site",
            List.of(
                    new ResourceLocation("valhelsia_structures", "tower_ruin"),
                    new ResourceLocation("dungeons_enhanced", "watch_tower"),
                    new ResourceLocation("dungeons_enhanced", "witch_tower"),
                    new ResourceLocation("dungeons_enhanced", "sunken_shrine"),
                    new ResourceLocation("dungeons_enhanced", "dungeon_variant")
            ), 0,
            id("exploration_old_walls_offer"), id("exploration_old_walls_active"), id("exploration_old_walls_turnin"),
            false, List.of(), List.of(new RewardStack(new ResourceLocation("minecraft", "iron_pickaxe"), 1, "Surveyor's Pick")), 4, 5
    );

    static final Definition FIRST_REAL_MAP = new Definition(
            "hearthlands_cartographer_first_real_map", "The First Real Map",
            List.of(VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, false, ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS", 0, 1, true, 1280, 56, 0, 0, "local landmark",
            List.of(
                    new ResourceLocation("dungeons_enhanced", "watch_tower"),
                    new ResourceLocation("valhelsia_structures", "tower_ruin"),
                    new ResourceLocation("dungeons_enhanced", "stables"),
                    new ResourceLocation("dungeons_enhanced", "witch_tower"),
                    new ResourceLocation("dungeons_enhanced", "sunken_shrine"),
                    new ResourceLocation("dungeons_enhanced", "dungeon_variant"),
                    new ResourceLocation("betterdungeons", "spider_dungeon"),
                    new ResourceLocation("valhelsia_structures", "spawner_dungeon")
            ), 0,
            id("cartographer_first_real_map"), id("cartographer_quest_active"), id("cartographer_quest_turnin"),
            true, List.of(), List.of(new RewardStack(new ResourceLocation("minecraft", "spyglass"), 1, null)), 5, 5
    );

    static final Definition QUIET_WATCH = new Definition(
            "hearthlands_profession_quiet_watch", "The Watch Went Quiet",
            List.of(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, false, ObjectiveType.STRUCTURE_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 1024, 64, 0, 0, "the silent watch",
            List.of(
                    new ResourceLocation("dungeons_enhanced", "watch_tower"),
                    new ResourceLocation("betterdungeons", "spider_dungeon"),
                    new ResourceLocation("valhelsia_structures", "spawner_dungeon")
            ), 4,
            id("profession_quiet_watch_offer"), id("profession_quiet_watch_active"), id("profession_quiet_watch_turnin"),
            false, List.of(), List.of(new RewardStack(new ResourceLocation("minecraft", "crossbow"), 1, "Watch Crossbow")), 5, 5
    );

    static final Definition PATROL_TRIAL = new Definition(
            "hearthlands_profession_patrol_trial", "A Patrol Trial",
            List.of(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, false, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 80, 112, 208, "the patrol road", List.of(), 4,
            id("profession_patrol_trial_offer"), id("profession_patrol_trial_active"), id("profession_patrol_trial_turnin"),
            false, List.of(new RewardStack(new ResourceLocation("minecraft", "iron_sword"), 1, "Patrol Trial Blade")), List.of(), 5, 5
    );

    static final Definition REOPEN_OLD_ROAD = new Definition(
            "hearthlands_capstone_reopen_old_road", "Reopen the Old Road",
            List.of(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            null, true, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 96, 176, 288, "the old road crossing", List.of(), 5,
            id("capstone_old_road_offer"), id("capstone_old_road_active"), id("capstone_old_road_turnin"),
            false, List.of(), List.of(
                    new RewardStack(new ResourceLocation("minecraft", "iron_ingot"), 12, null),
                    new RewardStack(new ResourceLocation("minecraft", "shield"), 1, "Roadwarden's Shield")
            ), 8, 8
    );

    static final List<Definition> ZONE_ONE = List.of(
            EMPTY_STALLS, SAFE_PASTURE, OLD_WALLS_OLD_NAMES, FIRST_REAL_MAP,
            QUIET_WATCH, PATROL_TRIAL, REOPEN_OLD_ROAD
    );

    private VillageQuestCatalog() {}

    static Definition byId(String id) {
        for (Definition definition : ZONE_ONE) if (definition.id.equals(id)) return definition;
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
        STRUCTURE_HOSTILE_CLEAR,
        LOCAL_HOSTILE_CLEAR
    }

    record RewardStack(ResourceLocation itemId, int count, String customName) {}

    record Definition(
            String id,
            String title,
            List<VillagerProfession> giverProfessions,
            VillageProgressState.AccomplishmentCategory accomplishmentCategory,
            boolean zoneOneCapstone,
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
