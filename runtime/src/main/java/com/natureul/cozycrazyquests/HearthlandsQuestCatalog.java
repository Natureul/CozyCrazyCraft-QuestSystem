package com.natureul.cozycrazyquests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

import static com.natureul.cozycrazyquests.VillageQuestCatalog.*;

/** Hearthlands authored work: settlement help, local discovery, profession trials and the first capstone. */
final class HearthlandsQuestCatalog {
    private HearthlandsQuestCatalog() {}

    static final Definition EMPTY_STALLS = structure(
            "hearthlands_community_empty_stalls", "The Empty Stalls",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, "HEARTHLANDS", 0,
            896, 56, "the old stables", structures("dungeons_enhanced:stables"), 0,
            "community_empty_stalls", false,
            rewards(
                    enchanted("minecraft:iron_shovel", 1, "Stableyard Spade", "minecraft:unbreaking", 1),
                    reward("minecraft:golden_carrot", 4, null)
            ), 4, 5
    );

    static final Definition SAFE_PASTURE = local(
            "hearthlands_community_safe_pasture", "The Outer Pasture",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.FARMER, VillagerProfession.BUTCHER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, "HEARTHLANDS",
            72, 96, 176, LocalTerrain.ANY, "the outer pasture", 3,
            "community_safe_pasture",
            rewards(
                    enchanted("minecraft:shears", 1, "Pasture Shears", "minecraft:unbreaking", 1),
                    reward("minecraft:pumpkin_pie", 4, null)
            ), 4, 4
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

    static final Definition BELOW_WHITE_SHELF = structure(
            "hearthlands_exploration_below_white_shelf", "The Cellar Under the Hill",
            profs(VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1100, 64, "the old cellar", structures("dungeons_enhanced:dungeon_variant"), 0,
            "north_below_white_shelf", false,
            rewards(
                    enchanted("minecraft:iron_boots", 1, "Hillwalker Boots", "minecraft:feather_falling", 1),
                    reward("minecraft:torch", 12, null)
            ), 5, 6
    );

    static final Definition STONE_UNDER_VINES = structure(
            "hearthlands_exploration_stone_under_vines", "Stone in the Green",
            profs(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1050, 56, "the old stone circle", structures("dungeons_enhanced:druid_circle"), 0,
            "east_stone_under_vines", false,
            rewards(
                    enchanted("minecraft:iron_axe", 1, "Brush-Cutter", "minecraft:efficiency", 1),
                    reward("minecraft:scaffolding", 8, null)
            ), 5, 6
    );

    static final Definition STONE_UNDER_SUN = structure(
            "hearthlands_exploration_stone_under_sun", "Stone Under the Sun",
            profs(VillagerProfession.MASON, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1150, 64, "the old desert stone", structures("dungeons_enhanced:desert_tomb"), 0,
            "south_stone_under_sun", false,
            rewards(
                    reward("cold_sweat:waterskin", 1, "Road Waterskin"),
                    reward("minecraft:compass", 1, "Road Compass")
            ), 6, 7
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
            ), 6, 7
    );

    static final Definition OLD_WALLS_OLD_NAMES = structure(
            "hearthlands_exploration_old_walls", "Old Walls, Old Names",
            profs(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1024, 56, "the ruined building",
            structures("dungeons_enhanced:ruined_building"), 0,
            "exploration_old_walls", false,
            rewards(
                    multiEnchanted("minecraft:iron_pickaxe", 1, "Surveyor's Pick",
                            enchant("minecraft:efficiency", 1), enchant("minecraft:unbreaking", 1)),
                    reward("minecraft:bricks", 12, null)
            ), 6, 8
    );

    static final Definition SUNKEN_RECORDS = structure(
            "hearthlands_exploration_sunken_records", "What the River Kept",
            profs(VillagerProfession.FISHERMAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1024, 64, "the drowned old place", structures("dungeons_enhanced:sunken_shrine"), 0,
            "exploration_sunken_records", false,
            rewards(
                    enchanted("minecraft:iron_boots", 1, "Fordwalker Boots", "minecraft:depth_strider", 1),
                    reward("minecraft:paper", 8, null)
            ), 6, 7
    );

    static final Definition LANTERNS_BELOW = structureClear(
            "hearthlands_exploration_lanterns_below", "Lanterns Below",
            profs(VillagerProfession.CLERIC, VillagerProfession.MASON, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 0,
            1000, 72, "the buried chamber", structures("valhelsia_structures:spawner_room"), 4,
            "exploration_lanterns_below",
            rewards(
                    enchanted("minecraft:shield", 1, "Chamber Guard", "minecraft:unbreaking", 1),
                    reward("minecraft:lantern", 4, null)
            ), 6, 8
    );

    static final Definition FIRST_REAL_MAP = structure(
            "hearthlands_cartographer_first_real_map", "The First Real Map",
            profs(VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "HEARTHLANDS", 1,
            1280, 56, "local landmark",
            structures(
                    "dungeons_enhanced:mushroom_house",
                    "valhelsia_structures:big_tree",
                    "valhelsia_structures:witch_hut",
                    "born_in_chaos_v1:observation_tower_forest",
                    "born_in_chaos_v1:observation_tower_plains"
            ), 0, "cartographer_first_real_map", true,
            rewards(reward("minecraft:spyglass", 1, "Survey Glass"), reward("minecraft:paper", 8, null)), 5, 6,
            id("cartographer_quest_active"), id("cartographer_quest_turnin")
    );

    static final Definition WATCHLINE_PIKE = structureClear(
            "hearthlands_profession_watchline_pike", "Hold the Watchline",
            profs(VillagerProfession.FLETCHER, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1050, 72, "the old watch tower", structures("dungeons_enhanced:watch_tower"), 4,
            "north_watchline_pike",
            rewards(enchanted("spartanweaponry:iron_pike", 1, "Watchline Pike", "minecraft:unbreaking", 1)), 6, 8
    );

    static final Definition CANOPY_STAFF = local(
            "hearthlands_profession_canopy_staff", "Keep the Lane Open",
            profs(VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS",
            80, 104, 208, LocalTerrain.ANY, "the wooded lane", 4,
            "east_canopy_staff",
            rewards(enchanted("spartanweaponry:iron_quarterstaff", 1, "Lane Staff", "minecraft:unbreaking", 1)), 5, 7
    );

    static final Definition ROADRUNNER_SPEAR = local(
            "hearthlands_profession_roadrunner_spear", "Test the Long Reach",
            profs(VillagerProfession.FLETCHER, VillagerProfession.LEATHERWORKER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS",
            80, 112, 224, LocalTerrain.ANY, "the exposed road", 4,
            "south_roadrunner_spear",
            rewards(enchanted("spartanweaponry:iron_spear", 1, "Road Spear", "minecraft:unbreaking", 1)), 5, 7
    );

    static final Definition WOODCUTTER_BATTLEAXE = local(
            "hearthlands_profession_woodcutter_battleaxe", "Clear the Timber Track",
            profs(VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS",
            80, 104, 208, LocalTerrain.ANY, "the timber track", 4,
            "west_woodcutter_battleaxe",
            rewards(multiEnchanted("spartanweaponry:iron_battleaxe", 1, "Roadcutter Battleaxe",
                    enchant("minecraft:efficiency", 1), enchant("minecraft:unbreaking", 1))), 5, 7
    );

    static final Definition BRING_BACK_MAIL = local(
            "hearthlands_profession_bring_back_mail", "Field-Test the Mail",
            profs(VillagerProfession.ARMORER, VillagerProfession.LEATHERWORKER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS",
            80, 112, 224, LocalTerrain.ANY, "the rough road", 4,
            "profession_bring_back_mail",
            rewards(reward("minecraft:iron_ingot", 4, null)), 5, 7,
            rewards(enchanted("minecraft:chainmail_chestplate", 1, "Road-Mended Mail", "minecraft:unbreaking", 1))
    );

    static final Definition QUIET_WATCH = structureClear(
            "hearthlands_profession_quiet_watch", "Silk Across the Road",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1024, 64, "the spider dungeon",
            structures("betterdungeons:spider_dungeon"), 4,
            "profession_quiet_watch",
            rewards(enchanted("minecraft:crossbow", 1, "Roadwatch Crossbow", "minecraft:quick_charge", 1)), 6, 8
    );

    static final Definition RUINED_ROAD = structureClear(
            "hearthlands_profession_ruined_road", "The Road Through the Ruin",
            profs(VillagerProfession.MASON, VillagerProfession.TOOLSMITH, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "HEARTHLANDS", 0,
            1000, 72, "the tower ruin", structures("valhelsia_structures:tower_ruin"), 4,
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

    static final List<Definition> ALL = List.of(
            EMPTY_STALLS, SAFE_PASTURE, WATERLINE_TROUBLE,
            BELOW_WHITE_SHELF, STONE_UNDER_VINES, STONE_UNDER_SUN, WITCHLIGHT_RECORDS,
            LANTERNS_BELOW, SUNKEN_RECORDS, OLD_WALLS_OLD_NAMES, FIRST_REAL_MAP,
            WATCHLINE_PIKE, CANOPY_STAFF, ROADRUNNER_SPEAR, WOODCUTTER_BATTLEAXE,
            BRING_BACK_MAIL, QUIET_WATCH, RUINED_ROAD, PATROL_TRIAL, BROKEN_CART,
            REOPEN_OLD_ROAD
    );
}
