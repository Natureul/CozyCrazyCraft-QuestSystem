package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

/**
 * Executable authored quest catalogue.
 *
 * The catalogue is intentionally larger than the number of quests a player sees at once. Village
 * semantic progression still surfaces only a small set of COMMUNITY / EXPLORATION / PROFESSION /
 * DANGER branches. The larger library exists so different professions, terrain and generated
 * structures produce different stories instead of every settlement exposing the same three jobs.
 */
final class VillageQuestCatalog {
    private VillageQuestCatalog() {}

    // ---------------------------------------------------------------------
    // HEARTHLANDS — COMMUNITY
    // ---------------------------------------------------------------------

    static final Definition EMPTY_STALLS = structure(
            "hearthlands_community_empty_stalls", "The Empty Stalls",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, "HEARTHLANDS", 0,
            896, 56, "the old stables", structures("dungeons_enhanced:stables"), 0,
            "community_empty_stalls", false,
            rewards(reward("minecraft:saddle", 1, "Recovered Riding Tack")), 4, 5
    );

    static final Definition SAFE_PASTURE = local(
            "hearthlands_community_safe_pasture", "The Outer Pasture",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.BUTCHER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, "HEARTHLANDS",
            72, 96, 176, LocalTerrain.ANY, "the outer pasture", 3,
            "community_safe_pasture", rewards(reward("minecraft:leather", 4, null)), 4, 4
    );

    static final Definition WATERLINE_TROUBLE = local(
            "hearthlands_community_waterline", "Trouble at the Waterline",
            profs(VillagerProfession.FISHERMAN, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, "HEARTHLANDS",
            72, 80, 176, LocalTerrain.WATER_EDGE, "the village waterline", 3,
            "community_waterline",
            rewards(
                    enchanted("minecraft:fishing_rod", 1, "Waterline Rod", "minecraft:unbreaking", 1),
                    reward("minecraft:cooked_cod", 6, null)
            ), 4, 5
    );

    // ---------------------------------------------------------------------
    // HEARTHLANDS — SITUATED / REGIONAL EXPLORATION
    // ---------------------------------------------------------------------

    static final Definition BELOW_WHITE_SHELF = structure(
            "hearthlands_exploration_below_white_shelf", "Below the White Shelf",
            profs(VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1100, 72, "the buried ice works", structures("dungeons_enhanced:ice_pit"), 0,
            "north_below_white_shelf", false,
            rewards(
                    enchanted("minecraft:iron_boots", 1, "Shelfwalker Boots", "minecraft:feather_falling", 1),
                    reward("minecraft:torch", 16, null)
            ), 6, 8
    );

    static final Definition STONE_UNDER_VINES = structure(
            "hearthlands_exploration_stone_under_vines", "Stone Under the Vines",
            profs(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1150, 64, "the overgrown stonework", structures("dungeons_enhanced:jungle_monument"), 0,
            "east_stone_under_vines", false,
            rewards(
                    enchanted("minecraft:iron_axe", 1, "Vine-Cutter", "minecraft:efficiency", 1),
                    reward("minecraft:scaffolding", 8, null)
            ), 6, 8
    );

    static final Definition STONE_UNDER_SUN = structure(
            "hearthlands_exploration_stone_under_sun", "Stone Under the Sun",
            profs(VillagerProfession.MASON, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1150, 64, "the old desert stone", structures("dungeons_enhanced:desert_tomb"), 0,
            "south_stone_under_sun", false,
            rewards(
                    enchanted("minecraft:iron_helmet", 1, "Sunroad Helm", "minecraft:unbreaking", 1),
                    reward("minecraft:compass", 1, "Road Compass")
            ), 6, 8
    );

    static final Definition WITCHLIGHT_RECORDS = structure(
            "hearthlands_exploration_witchlight_records", "Smoke Above the Old Road",
            profs(VillagerProfession.CLERIC, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1050, 64, "the old witch tower", structures("dungeons_enhanced:witch_tower"), 0,
            "west_witchlight_records", false,
            rewards(
                    enchanted("minecraft:iron_pickaxe", 1, "Old-Stone Pick", "minecraft:unbreaking", 1),
                    reward("minecraft:lantern", 4, null)
            ), 6, 8
    );

    static final Definition OLD_WALLS_OLD_NAMES = structure(
            "hearthlands_exploration_old_walls", "Old Walls, Old Names",
            profs(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1024, 56, "the old site",
            structures(
                    "valhelsia_structures:tower_ruin",
                    "dungeons_enhanced:watch_tower",
                    "dungeons_enhanced:witch_tower",
                    "dungeons_enhanced:sunken_shrine",
                    "dungeons_enhanced:dungeon_variant"
            ), 0, "exploration_old_walls", false,
            rewards(
                    multiEnchanted("minecraft:iron_pickaxe", 1, "Surveyor's Pick",
                            enchant("minecraft:efficiency", 1), enchant("minecraft:unbreaking", 1)),
                    reward("minecraft:iron_ingot", 6, null)
            ), 8, 10
    );

    static final Definition SUNKEN_RECORDS = structure(
            "hearthlands_exploration_sunken_records", "What the River Kept",
            profs(VillagerProfession.FISHERMAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1024, 64, "the drowned old place", structures("dungeons_enhanced:sunken_shrine"), 0,
            "exploration_sunken_records", false,
            rewards(
                    enchanted("minecraft:fishing_rod", 1, "Keeper's Line", "minecraft:luck_of_the_sea", 1),
                    reward("minecraft:paper", 8, null)
            ), 6, 8
    );

    static final Definition LANTERNS_BELOW = structureClear(
            "hearthlands_exploration_lanterns_below", "Lanterns Below",
            profs(VillagerProfession.CLERIC, VillagerProfession.MASON, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1000, 72, "the buried chamber", structures("valhelsia_structures:spawner_room"), 4,
            "exploration_lanterns_below",
            rewards(
                    enchanted("minecraft:iron_pickaxe", 1, "Lamplighter's Pick", "minecraft:efficiency", 1),
                    reward("minecraft:lantern", 6, null)
            ), 6, 8
    );

    static final Definition FIRST_REAL_MAP = structure(
            "hearthlands_cartographer_first_real_map", "The First Real Map",
            profs(VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 1,
            1280, 56, "local landmark",
            structures(
                    "dungeons_enhanced:watch_tower",
                    "valhelsia_structures:tower_ruin",
                    "dungeons_enhanced:stables",
                    "dungeons_enhanced:witch_tower",
                    "dungeons_enhanced:sunken_shrine",
                    "dungeons_enhanced:dungeon_variant",
                    "betterdungeons:spider_dungeon",
                    "valhelsia_structures:spawner_dungeon"
            ), 0, "cartographer_first_real_map", true,
            rewards(reward("minecraft:spyglass", 1, null), reward("minecraft:paper", 8, null)), 5, 6,
            id("cartographer_quest_active"), id("cartographer_quest_turnin")
    );

    // ---------------------------------------------------------------------
    // HEARTHLANDS — PROFESSION / PRACTICAL WORK
    // ---------------------------------------------------------------------

    static final Definition WATCHLINE_PIKE = structureClear(
            "hearthlands_profession_watchline_pike", "The Watchline Below",
            profs(VillagerProfession.FLETCHER, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1100, 80, "the buried watchline", structures("dungeons_enhanced:ice_pit"), 4,
            "north_watchline_pike",
            rewards(enchanted("spartanweaponry:iron_pike", 1, "Watchline Pike", "minecraft:unbreaking", 1)), 6, 8
    );

    static final Definition CANOPY_STAFF = structureClear(
            "hearthlands_profession_canopy_staff", "The Path Under the Canopy",
            profs(VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1150, 80, "the overgrown route", structures("dungeons_enhanced:jungle_monument"), 4,
            "east_canopy_staff",
            rewards(enchanted("spartanweaponry:iron_quarterstaff", 1, "Canopy Staff", "minecraft:unbreaking", 1)), 6, 8
    );

    static final Definition ROADRUNNER_SPEAR = structureClear(
            "hearthlands_profession_roadrunner_spear", "The Road Past the Tomb",
            profs(VillagerProfession.FLETCHER, VillagerProfession.LEATHERWORKER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1150, 80, "the exposed road", structures("dungeons_enhanced:desert_tomb"), 4,
            "south_roadrunner_spear",
            rewards(enchanted("spartanweaponry:iron_spear", 1, "Roadrunner Spear", "minecraft:unbreaking", 1)), 6, 8
    );

    static final Definition WOODCUTTER_BATTLEAXE = structureClear(
            "hearthlands_profession_woodcutter_battleaxe", "Cut the Witchlight",
            profs(VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1050, 80, "the witch-road tower", structures("dungeons_enhanced:witch_tower"), 4,
            "west_woodcutter_battleaxe",
            rewards(multiEnchanted("spartanweaponry:iron_battleaxe", 1, "Woodcutter Battleaxe",
                    enchant("minecraft:efficiency", 1), enchant("minecraft:unbreaking", 1))), 6, 8
    );

    static final Definition BRING_BACK_MAIL = structure(
            "hearthlands_profession_bring_back_mail", "Bring Back the Mail",
            profs(VillagerProfession.ARMORER, VillagerProfession.LEATHERWORKER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            900, 56, "the old stable yard", structures("dungeons_enhanced:stables"), 0,
            "profession_bring_back_mail", false,
            rewards(
                    enchanted("minecraft:chainmail_chestplate", 1, "Road-Mended Mail", "minecraft:unbreaking", 1),
                    reward("minecraft:iron_ingot", 4, null)
            ), 5, 7
    );

    static final Definition QUIET_WATCH = structureClear(
            "hearthlands_profession_quiet_watch", "The Watch Went Quiet",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1024, 64, "the silent watch",
            structures("dungeons_enhanced:watch_tower", "betterdungeons:spider_dungeon", "valhelsia_structures:spawner_dungeon"), 4,
            "profession_quiet_watch",
            rewards(enchanted("minecraft:crossbow", 1, "Watch Crossbow", "minecraft:quick_charge", 1)), 6, 8
    );

    static final Definition RUINED_ROAD = structureClear(
            "hearthlands_profession_ruined_road", "The Road Through the Ruin",
            profs(VillagerProfession.MASON, VillagerProfession.TOOLSMITH, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1000, 72, "the ruined road", structures("valhelsia_structures:tower_ruin"), 4,
            "profession_ruined_road",
            rewards(
                    enchanted("minecraft:iron_shovel", 1, "Roadwright's Spade", "minecraft:unbreaking", 1),
                    reward("minecraft:iron_ingot", 4, null)
            ), 5, 7
    );

    static final Definition PATROL_TRIAL = local(
            "hearthlands_profession_patrol_trial", "A Patrol Trial",
            profs(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS",
            80, 112, 208, LocalTerrain.ANY, "the patrol road", 4,
            "profession_patrol_trial", rewards(), 5, 6,
            rewards(enchanted("minecraft:iron_sword", 1, "Patrol Trial Blade", "minecraft:unbreaking", 1))
    );

    static final Definition BROKEN_CART = local(
            "hearthlands_profession_broken_cart", "The Cart That Didn't Return",
            profs(VillagerProfession.LEATHERWORKER, VillagerProfession.TOOLSMITH, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS",
            80, 104, 208, LocalTerrain.ANY, "the cart trail", 4,
            "profession_broken_cart",
            rewards(
                    enchanted("minecraft:iron_axe", 1, "Roadside Hatchet", "minecraft:efficiency", 1),
                    reward("minecraft:leather", 4, null)
            ), 5, 7
    );

    // ---------------------------------------------------------------------
    // HEARTHLANDS CAPSTONE
    // ---------------------------------------------------------------------

    static final Definition REOPEN_OLD_ROAD = new Definition(
            "hearthlands_capstone_reopen_old_road", "Reopen the Old Road",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON,
                    VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            null, true, ObjectiveType.LOCAL_HOSTILE_CLEAR,
            "HEARTHLANDS", 0, 0, true, 0, 96, 176, 288, LocalTerrain.ANY,
            "the old road crossing", List.of(), 5,
            id("capstone_old_road_offer"), id("capstone_old_road_active"), id("capstone_old_road_turnin"),
            false, List.of(),
            rewards(reward("minecraft:iron_ingot", 12, null),
                    enchanted("minecraft:shield", 1, "Roadwarden's Shield", "minecraft:unbreaking", 2)),
            8, 10
    );

    // ---------------------------------------------------------------------
    // FRONTIER — DESTINATION-HEAVY EXPEDITION WORK
    // ---------------------------------------------------------------------

    static final Definition FRONTIER_STABLE_ROUTE = structure(
            "frontier_community_stable_route", "The Long Stable Road",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.LEATHERWORKER, VillagerProfession.FARMER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, "FRONTIER", 0,
            1500, 64, "the frontier stables", structures("dungeons_enhanced:stables"), 0,
            "frontier_stable_route", false,
            rewards(reward("minecraft:iron_horse_armor", 1, "Roadwarden Horse Armor"), reward("minecraft:lead", 2, null)),
            8, 10
    );

    static final Definition FRONTIER_LONG_SURVEY = structure(
            "frontier_exploration_long_survey", "Beyond the Last Good Signpost",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "FRONTIER", 1,
            1800, 72, "the distant landmark",
            structures("dungeons_enhanced:watch_tower", "valhelsia_structures:tower_ruin",
                    "dungeons_enhanced:dungeon_variant", "dungeons_enhanced:witch_tower"), 0,
            "frontier_long_survey", true,
            rewards(reward("minecraft:spyglass", 1, "Frontier Glass"), reward("minecraft:compass", 1, null)),
            9, 12
    );

    static final Definition FRONTIER_DROWNED_SHRINE = structureClear(
            "frontier_exploration_drowned_shrine", "A Light Under the Water",
            profs(VillagerProfession.FISHERMAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "FRONTIER", 0,
            1600, 80, "the drowned shrine", structures("dungeons_enhanced:sunken_shrine"), 6,
            "frontier_drowned_shrine",
            rewards(enchanted("minecraft:iron_boots", 1, "Riverwarden Boots", "minecraft:depth_strider", 1),
                    reward("minecraft:prismarine_shard", 8, null)), 9, 12
    );

    static final Definition FRONTIER_WATCHLINE = structureClear(
            "frontier_danger_watchline", "Hold the Watchline",
            profs(VillagerProfession.FLETCHER, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.DANGER, "FRONTIER", 0,
            1600, 88, "the exposed watchline",
            structures("dungeons_enhanced:watch_tower", "betterdungeons:spider_dungeon", "valhelsia_structures:spawner_dungeon"), 6,
            "frontier_watchline",
            rewards(enchanted("spartanweaponry:iron_heavy_crossbow", 1, "Frontier Heavy Crossbow", "minecraft:unbreaking", 2)),
            10, 12
    );

    static final Definition FRONTIER_CARAVAN_LANCE = structureClear(
            "frontier_profession_caravan_lance", "The Empty Hitching Post",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.LEATHERWORKER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "FRONTIER", 0,
            1500, 80, "the frontier stable road", structures("dungeons_enhanced:stables"), 6,
            "frontier_caravan_lance",
            rewards(enchanted("spartanweaponry:iron_lance", 1, "Caravan Lance", "minecraft:unbreaking", 2)),
            9, 12
    );

    static final Definition FRONTIER_RUIN_BOW = structureClear(
            "frontier_profession_ruin_bow", "Shots Across the Ruins",
            profs(VillagerProfession.FLETCHER, VillagerProfession.CARTOGRAPHER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "FRONTIER", 0,
            1700, 88, "the ruined sightline",
            structures("valhelsia_structures:tower_ruin", "dungeons_enhanced:watch_tower"), 6,
            "frontier_ruin_bow",
            rewards(enchanted("spartanweaponry:iron_longbow", 1, "Survey Longbow", "minecraft:power", 2)),
            9, 12
    );

    static final Definition FRONTIER_TOMB_ROUTE = structureClear(
            "frontier_danger_tomb_route", "The Road Around the Dead",
            profs(VillagerProfession.CLERIC, VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.DANGER, "FRONTIER", 0,
            1700, 88, "the tomb road", structures("dungeons_enhanced:desert_tomb"), 6,
            "frontier_tomb_route",
            rewards(enchanted("spartanweaponry:iron_javelin", 1, "Long-Sun Javelin", "minecraft:unbreaking", 2)),
            10, 13
    );

    static final Definition FRONTIER_OVERGROWN_ROUTE = structureClear(
            "frontier_danger_overgrown_route", "The Green Road Closed",
            profs(VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH, VillagerProfession.ARMORER),
            VillageProgressState.AccomplishmentCategory.DANGER, "FRONTIER", 0,
            1700, 88, "the overgrown road", structures("dungeons_enhanced:jungle_monument"), 6,
            "frontier_overgrown_route",
            rewards(enchanted("spartanweaponry:iron_glaive", 1, "Greenroad Glaive", "minecraft:unbreaking", 2)),
            10, 13
    );

    static final List<Definition> ALL = List.of(
            // Hearthlands community
            EMPTY_STALLS, SAFE_PASTURE, WATERLINE_TROUBLE,
            // Hearthlands exploration, situated first
            BELOW_WHITE_SHELF, STONE_UNDER_VINES, STONE_UNDER_SUN, WITCHLIGHT_RECORDS,
            LANTERNS_BELOW, SUNKEN_RECORDS, OLD_WALLS_OLD_NAMES, FIRST_REAL_MAP,
            // Hearthlands profession, situated first
            WATCHLINE_PIKE, CANOPY_STAFF, ROADRUNNER_SPEAR, WOODCUTTER_BATTLEAXE,
            BRING_BACK_MAIL, QUIET_WATCH, RUINED_ROAD, PATROL_TRIAL, BROKEN_CART,
            REOPEN_OLD_ROAD,
            // Frontier
            FRONTIER_STABLE_ROUTE, FRONTIER_LONG_SURVEY, FRONTIER_DROWNED_SHRINE,
            FRONTIER_WATCHLINE, FRONTIER_CARAVAN_LANCE, FRONTIER_RUIN_BOW,
            FRONTIER_TOMB_ROUTE, FRONTIER_OVERGROWN_ROUTE
    );

    static Definition byId(String id) {
        for (Definition definition : ALL) if (definition.id.equals(id)) return definition;
        return null;
    }

    static List<Definition> forProfession(VillagerProfession profession) {
        return ALL.stream().filter(definition -> definition.giverProfessions.contains(profession)).toList();
    }

    static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }

    // ---------------------------------------------------------------------
    // Compact authoring helpers. Keep quest rows readable as this Bible grows.
    // ---------------------------------------------------------------------

    private static Definition structure(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier, int maxTierOffset,
            int searchRadius, int targetRadius, String targetLabel, List<ResourceLocation> candidates, int kills,
            String dialogueStem, boolean revealAtlas, List<RewardStack> rewards, int emeralds, int xp
    ) {
        return structure(id, title, professions, category, tier, maxTierOffset, searchRadius, targetRadius,
                targetLabel, candidates, kills, dialogueStem, revealAtlas, rewards, emeralds, xp,
                id(dialogueStem + "_active"), id(dialogueStem + "_turnin"));
    }

    private static Definition structure(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier, int maxTierOffset,
            int searchRadius, int targetRadius, String targetLabel, List<ResourceLocation> candidates, int kills,
            String dialogueStem, boolean revealAtlas, List<RewardStack> rewards, int emeralds, int xp,
            ResourceLocation activeDialogue, ResourceLocation turninDialogue
    ) {
        return new Definition(
                id, title, professions, category, false, ObjectiveType.STRUCTURE_SURVEY,
                tier, 0, maxTierOffset, true, searchRadius, targetRadius, 0, 0, LocalTerrain.ANY,
                targetLabel, candidates, kills,
                id(dialogueStem + ("cartographer_first_real_map".equals(dialogueStem) ? "" : "_offer")),
                activeDialogue, turninDialogue, revealAtlas, List.of(), rewards, emeralds, xp
        );
    }

    private static Definition structureClear(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier, int maxTierOffset,
            int searchRadius, int targetRadius, String targetLabel, List<ResourceLocation> candidates, int kills,
            String dialogueStem, List<RewardStack> rewards, int emeralds, int xp
    ) {
        return new Definition(
                id, title, professions, category, false, ObjectiveType.STRUCTURE_HOSTILE_CLEAR,
                tier, 0, maxTierOffset, true, searchRadius, targetRadius, 0, 0, LocalTerrain.ANY,
                targetLabel, candidates, kills,
                id(dialogueStem + "_offer"), id(dialogueStem + "_active"), id(dialogueStem + "_turnin"),
                false, List.of(), rewards, emeralds, xp
        );
    }

    private static Definition local(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier,
            int targetRadius, int minDistance, int maxDistance, LocalTerrain terrain, String targetLabel, int kills,
            String dialogueStem, List<RewardStack> rewards, int emeralds, int xp
    ) {
        return local(id, title, professions, category, tier, targetRadius, minDistance, maxDistance, terrain,
                targetLabel, kills, dialogueStem, rewards, emeralds, xp, List.of());
    }

    private static Definition local(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier,
            int targetRadius, int minDistance, int maxDistance, LocalTerrain terrain, String targetLabel, int kills,
            String dialogueStem, List<RewardStack> rewards, int emeralds, int xp, List<RewardStack> acceptanceItems
    ) {
        return new Definition(
                id, title, professions, category, false, ObjectiveType.LOCAL_HOSTILE_CLEAR,
                tier, 0, 0, true, 0, targetRadius, minDistance, maxDistance, terrain,
                targetLabel, List.of(), kills,
                id(dialogueStem + "_offer"), id(dialogueStem + "_active"), id(dialogueStem + "_turnin"),
                false, acceptanceItems, rewards, emeralds, xp
        );
    }

    @SafeVarargs
    private static List<VillagerProfession> profs(VillagerProfession... professions) {
        return List.of(professions);
    }

    private static List<ResourceLocation> structures(String... ids) {
        return java.util.Arrays.stream(ids).map(ResourceLocation::new).toList();
    }

    private static List<RewardStack> rewards(RewardStack... rewards) {
        return List.of(rewards);
    }

    private static RewardStack reward(String id, int count, String name) {
        return new RewardStack(new ResourceLocation(id), count, name);
    }

    private static RewardEnchant enchant(String id, int level) {
        return new RewardEnchant(new ResourceLocation(id), level);
    }

    private static RewardStack enchanted(String itemId, int count, String name, String enchantId, int level) {
        return new RewardStack(new ResourceLocation(itemId), count, name,
                List.of(new RewardEnchant(new ResourceLocation(enchantId), level)));
    }

    private static RewardStack multiEnchanted(String itemId, int count, String name, RewardEnchant... enchants) {
        return new RewardStack(new ResourceLocation(itemId), count, name, List.of(enchants));
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
