package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

/**
 * Executable Zone 1 slice of the larger Quest & Reward Bible.
 *
 * Structure-grounded variants come before local fallbacks. If the generated world does not contain
 * the required place, the offer disappears rather than inventing it. The catalogue deliberately
 * covers more ordinary village profession mixes than 0.3.5 so a settlement is much less likely to
 * feel socially empty.
 */
final class VillageQuestCatalog {
    static final Definition EMPTY_STALLS = new Definition(
            "hearthlands_community_empty_stalls", "The Empty Stalls",
            List.of(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, false, ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS", 0, 0, true, 896, 56, 0, 0, LocalTerrain.ANY, "the old stables",
            List.of(new ResourceLocation("dungeons_enhanced", "stables")), 0,
            id("community_empty_stalls_offer"), id("community_empty_stalls_active"), id("community_empty_stalls_turnin"),
            false, List.of(),
            List.of(new RewardStack(new ResourceLocation("minecraft", "saddle"), 1, "Recovered Riding Tack")), 4, 5
    );

    static final Definition SAFE_PASTURE = new Definition(
            "hearthlands_community_safe_pasture", "The Outer Pasture",
            List.of(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.BUTCHER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, false, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 72, 96, 176, LocalTerrain.ANY, "the outer pasture", List.of(), 3,
            id("community_safe_pasture_offer"), id("community_safe_pasture_active"), id("community_safe_pasture_turnin"),
            false, List.of(), List.of(new RewardStack(new ResourceLocation("minecraft", "leather"), 4, null)), 4, 4
    );

    /** Fisherman-heavy community fallback that only exists when the resolver finds actual water. */
    static final Definition WATERLINE_TROUBLE = new Definition(
            "hearthlands_community_waterline", "Trouble at the Waterline",
            List.of(VillagerProfession.FISHERMAN, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, false, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 72, 80, 176, LocalTerrain.WATER_EDGE, "the village waterline", List.of(), 3,
            id("community_waterline_offer"), id("community_waterline_active"), id("community_waterline_turnin"),
            false, List.of(),
            List.of(
                    enchanted(new ResourceLocation("minecraft", "fishing_rod"), 1, "Waterline Rod", "minecraft:unbreaking", 1),
                    new RewardStack(new ResourceLocation("minecraft", "cooked_cod"), 6, null)
            ), 4, 5
    );

    static final Definition OLD_WALLS_OLD_NAMES = new Definition(
            "hearthlands_exploration_old_walls", "Old Walls, Old Names",
            List.of(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, false, ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS", 0, 0, true, 1024, 56, 0, 0, LocalTerrain.ANY, "the old site",
            List.of(
                    new ResourceLocation("valhelsia_structures", "tower_ruin"),
                    new ResourceLocation("dungeons_enhanced", "watch_tower"),
                    new ResourceLocation("dungeons_enhanced", "witch_tower"),
                    new ResourceLocation("dungeons_enhanced", "sunken_shrine"),
                    new ResourceLocation("dungeons_enhanced", "dungeon_variant")
            ), 0,
            id("exploration_old_walls_offer"), id("exploration_old_walls_active"), id("exploration_old_walls_turnin"),
            false, List.of(),
            List.of(
                    new RewardStack(
                            new ResourceLocation("minecraft", "iron_pickaxe"), 1, "Surveyor's Pick",
                            List.of(
                                    new RewardEnchant(new ResourceLocation("minecraft", "efficiency"), 1),
                                    new RewardEnchant(new ResourceLocation("minecraft", "unbreaking"), 1)
                            )
                    ),
                    new RewardStack(new ResourceLocation("minecraft", "iron_ingot"), 6, null)
            ), 8, 10
    );

    /** A real-water exploration branch for fishing settlements instead of forcing every clue through a mason. */
    static final Definition SUNKEN_RECORDS = new Definition(
            "hearthlands_exploration_sunken_records", "What the River Kept",
            List.of(VillagerProfession.FISHERMAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, false, ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS", 0, 0, true, 1024, 64, 0, 0, LocalTerrain.ANY, "the drowned old place",
            List.of(new ResourceLocation("dungeons_enhanced", "sunken_shrine")), 0,
            id("exploration_sunken_records_offer"), id("exploration_sunken_records_active"), id("exploration_sunken_records_turnin"),
            false, List.of(),
            List.of(
                    enchanted(new ResourceLocation("minecraft", "fishing_rod"), 1, "Keeper's Line", "minecraft:luck_of_the_sea", 1),
                    new RewardStack(new ResourceLocation("minecraft", "paper"), 8, null)
            ), 6, 8
    );

    static final Definition FIRST_REAL_MAP = new Definition(
            "hearthlands_cartographer_first_real_map", "The First Real Map",
            List.of(VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, false, ObjectiveType.STRUCTURE_SURVEY,
            "HEARTHLANDS", 0, 1, true, 1280, 56, 0, 0, LocalTerrain.ANY, "local landmark",
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
            true, List.of(),
            List.of(
                    new RewardStack(new ResourceLocation("minecraft", "spyglass"), 1, null),
                    new RewardStack(new ResourceLocation("minecraft", "paper"), 8, null)
            ), 5, 6
    );

    static final Definition QUIET_WATCH = new Definition(
            "hearthlands_profession_quiet_watch", "The Watch Went Quiet",
            List.of(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, false, ObjectiveType.STRUCTURE_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 1024, 64, 0, 0, LocalTerrain.ANY, "the silent watch",
            List.of(
                    new ResourceLocation("dungeons_enhanced", "watch_tower"),
                    new ResourceLocation("betterdungeons", "spider_dungeon"),
                    new ResourceLocation("valhelsia_structures", "spawner_dungeon")
            ), 4,
            id("profession_quiet_watch_offer"), id("profession_quiet_watch_active"), id("profession_quiet_watch_turnin"),
            false, List.of(),
            List.of(enchanted(new ResourceLocation("minecraft", "crossbow"), 1, "Watch Crossbow", "minecraft:quick_charge", 1)),
            6, 8
    );

    static final Definition PATROL_TRIAL = new Definition(
            "hearthlands_profession_patrol_trial", "A Patrol Trial",
            List.of(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, false, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 80, 112, 208, LocalTerrain.ANY, "the patrol road", List.of(), 4,
            id("profession_patrol_trial_offer"), id("profession_patrol_trial_active"), id("profession_patrol_trial_turnin"),
            false,
            List.of(enchanted(new ResourceLocation("minecraft", "iron_sword"), 1, "Patrol Trial Blade", "minecraft:unbreaking", 1)),
            List.of(), 5, 6
    );

    /** Gives leatherworkers a real profession branch even when no stable structure generated nearby. */
    static final Definition BROKEN_CART = new Definition(
            "hearthlands_profession_broken_cart", "The Cart That Didn't Return",
            List.of(VillagerProfession.LEATHERWORKER, VillagerProfession.TOOLSMITH, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.PROFESSION, false, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 80, 104, 208, LocalTerrain.ANY, "the cart trail", List.of(), 4,
            id("profession_broken_cart_offer"), id("profession_broken_cart_active"), id("profession_broken_cart_turnin"),
            false, List.of(),
            List.of(
                    enchanted(new ResourceLocation("minecraft", "iron_axe"), 1, "Roadside Hatchet", "minecraft:efficiency", 1),
                    new RewardStack(new ResourceLocation("minecraft", "leather"), 4, null)
            ), 5, 7
    );

    static final Definition REOPEN_OLD_ROAD = new Definition(
            "hearthlands_capstone_reopen_old_road", "Reopen the Old Road",
            List.of(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            null, true, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 96, 176, 288, LocalTerrain.ANY, "the old road crossing", List.of(), 5,
            id("capstone_old_road_offer"), id("capstone_old_road_active"), id("capstone_old_road_turnin"),
            false, List.of(), List.of(
                    new RewardStack(new ResourceLocation("minecraft", "iron_ingot"), 12, null),
                    enchanted(new ResourceLocation("minecraft", "shield"), 1, "Roadwarden's Shield", "minecraft:unbreaking", 2)
            ), 8, 10
    );

    static final List<Definition> ZONE_ONE = List.of(
            EMPTY_STALLS,
            SAFE_PASTURE,
            WATERLINE_TROUBLE,
            OLD_WALLS_OLD_NAMES,
            SUNKEN_RECORDS,
            FIRST_REAL_MAP,
            QUIET_WATCH,
            PATROL_TRIAL,
            BROKEN_CART,
            REOPEN_OLD_ROAD
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

    private static RewardStack enchanted(ResourceLocation itemId, int count, String name, String enchantId, int level) {
        return new RewardStack(
                itemId,
                count,
                name,
                List.of(new RewardEnchant(ResourceLocation.tryParse(enchantId), level))
        );
    }

    enum ObjectiveType {
        STRUCTURE_SURVEY,
        STRUCTURE_HOSTILE_CLEAR,
        LOCAL_HOSTILE_CLEAR
    }

    enum LocalTerrain {
        ANY,
        WATER_EDGE
    }

    record RewardEnchant(ResourceLocation enchantId, int level) {}

    record RewardStack(ResourceLocation itemId, int count, String customName, List<RewardEnchant> enchants) {
        RewardStack(ResourceLocation itemId, int count, String customName) {
            this(itemId, count, customName, List.of());
        }
    }

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
            LocalTerrain localTerrain,
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
