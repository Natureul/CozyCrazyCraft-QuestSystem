package com.natureul.cozycrazyquests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

import static com.natureul.cozycrazyquests.VillageQuestCatalog.*;

/** Core Frontier progression plus the four outward regional signature expeditions. */
final class FrontierQuestCatalog {
    private FrontierQuestCatalog() {}

    static final Definition FRONTIER_IGLOO_DISPATCH = structure(
            "frontier_exploration_igloo_dispatch", "The Door in the Snow",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "FRONTIER", 0,
            1700, 64, "the snowbound shelter", structures("minecraft:igloo"), 0,
            "frontier_igloo_dispatch", true,
            rewards(
                    reward("cold_sweat:thermometer", 1, "Trail Thermometer"),
                    enchanted("minecraft:iron_helmet", 1, "Whitecap Hood", "minecraft:unbreaking", 1)
            ), 8, 10
    );

    static final Definition FRONTIER_ICE_MINE = structureClear(
            "frontier_profession_ice_mine", "The Mine That Froze Shut",
            profs(VillagerProfession.TOOLSMITH, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "FRONTIER", 0,
            1800, 88, "the frozen mine",
            structures("bettermineshafts:mineshaft_ice", "bettermineshafts:mineshaft_spruce_snowy"), 6,
            "frontier_ice_mine",
            rewards(
                    reward("cold_sweat:sewing_table", 1, "Expedition Sewing Table"),
                    enchanted("minecraft:iron_pickaxe", 1, "Frostpick", "minecraft:efficiency", 2)
            ), 9, 12
    );

    static final Definition FRONTIER_VINE_SHRINE = structure(
            "frontier_exploration_vine_shrine", "The Shrine Under Vines",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "FRONTIER", 0,
            1800, 72, "the jungle shrine",
            structures("betterjungletemples:jungle_temple", "minecraft:jungle_pyramid"), 0,
            "frontier_vine_shrine", true,
            rewards(
                    reward("backpacked:unlock_token", 1, "Expedition Pack Token"),
                    enchanted("spartanweaponry:iron_saber", 1, "Greenhand", "minecraft:unbreaking", 2)
            ), 9, 12
    );

    static final Definition FRONTIER_WITCH_RING = structureClear(
            "frontier_danger_witch_ring", "The Ring That Never Goes Dark",
            profs(VillagerProfession.CLERIC, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.DANGER, "FRONTIER", 0,
            1800, 88, "the witch ring",
            structures("betterwitchhuts:witch_circle", "betterwitchhuts:witch_hut"), 7,
            "frontier_witch_ring",
            rewards(
                    reward("majruszsaccessories:nature_rune", 1, "Greenveil Nature Rune"),
                    enchanted("minecraft:iron_leggings", 1, "Canopy Striders", "minecraft:protection", 2)
            ), 10, 13
    );

    static final Definition FRONTIER_FIRST_DIG = structure(
            "frontier_exploration_first_dig", "The First Dig",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.MASON, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "FRONTIER", 0,
            1850, 72, "the old desert temple",
            structures("betterdeserttemples:desert_temple", "minecraft:desert_pyramid"), 0,
            "frontier_first_dig", true,
            rewards(
                    reward("majruszsaccessories:ancient_scarab", 1, "Excavator's Scarab"),
                    reward("minecraft:brush", 1, "Field Brush")
            ), 9, 12
    );

    static final Definition FRONTIER_SAVANNA_CARAVAN = structureClear(
            "frontier_profession_savanna_caravan", "The Caravan With No Driver",
            profs(VillagerProfession.LEATHERWORKER, VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "FRONTIER", 0,
            1800, 88, "the abandoned savanna caravan",
            structures("born_in_chaos_v1:clown_caravan_savanna"), 6,
            "frontier_savanna_caravan",
            rewards(
                    reward("cold_sweat:waterskin", 1, "Caravan Waterskin"),
                    enchanted("spartanweaponry:iron_boomerang", 1, "Dustwheel", "minecraft:unbreaking", 2)
            ), 9, 12
    );

    static final Definition FRONTIER_COOLING_RUN = structureClear(
            "frontier_profession_cooling_run", "Heat in the Mine",
            profs(VillagerProfession.TOOLSMITH, VillagerProfession.ARMORER, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "FRONTIER", 0,
            1850, 88, "the hot-country mine",
            structures("bettermineshafts:mineshaft_mesa", "bettermineshafts:mineshaft_red_desert"), 7,
            "frontier_cooling_run",
            rewards(
                    reward("cold_sweat:icebox", 1, "Frontier Icebox"),
                    multiEnchanted("minecraft:iron_boots", 1, "Sunrunner Boots",
                            enchant("minecraft:protection", 1), enchant("minecraft:unbreaking", 2))
            ), 10, 13
    );

    static final Definition FRONTIER_TAIGA_CARAVAN = structureClear(
            "frontier_community_taiga_caravan", "The Cart in the Pines",
            profs(VillagerProfession.FARMER, VillagerProfession.SHEPHERD, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY, "FRONTIER", 0,
            1750, 88, "the abandoned taiga caravan",
            structures("born_in_chaos_v1:clown_caravan_taiga"), 6,
            "frontier_taiga_caravan",
            rewards(
                    reward("majruszsaccessories:tamed_potato_beetle", 1, "Field Beetle"),
                    reward("minecraft:pumpkin_pie", 6, "Road Pies")
            ), 8, 11
    );

    static final Definition FRONTIER_OLD_CASTLE = structureClear(
            "frontier_danger_old_castle", "Smoke in the Old Battlements",
            profs(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.MASON),
            VillageProgressState.AccomplishmentCategory.DANGER, "FRONTIER", 0,
            1800, 96, "the ruined stronghold",
            structures("valhelsia_structures:castle_ruin", "dungeons_enhanced:castle"), 7,
            "frontier_old_castle",
            rewards(
                    enchanted("spartanweaponry:iron_scythe", 1, "Fieldward Scythe", "minecraft:unbreaking", 2),
                    enchanted("minecraft:shield", 1, "Oldwall Shield", "minecraft:unbreaking", 2)
            ), 10, 13
    );

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

    static final Definition FRONTIER_PILLAGER_CAMP = structureClear(
            "frontier_danger_pillager_camp", "Break the Camp Before Nightfall",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            VillageProgressState.AccomplishmentCategory.DANGER, "FRONTIER", 0,
            1750, 96, "the raider camp", structures("dungeons_enhanced:pillager_camp"), 8,
            "frontier_pillager_camp",
            rewards(
                    enchanted("spartanweaponry:iron_halberd", 1, "Campbreaker", "minecraft:unbreaking", 2),
                    enchanted("minecraft:iron_chestplate", 1, "Campcoat", "minecraft:protection", 2)
            ), 10, 13
    );

    static final Definition FRONTIER_PIRATE_SHIP = structure(
            "frontier_exploration_pirate_ship", "A Sail Where No Sail Should Be",
            profs(VillagerProfession.FISHERMAN, VillagerProfession.CARTOGRAPHER, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "FRONTIER", 0,
            1850, 80, "the stranded pirate ship", structures("dungeons_enhanced:pirate_ship"), 0,
            "frontier_pirate_ship", true,
            rewards(
                    enchanted("spartanweaponry:iron_rapier", 1, "Deckhand's Needle", "minecraft:unbreaking", 2),
                    enchanted("minecraft:iron_boots", 1, "Deck Boots", "minecraft:depth_strider", 2)
            ), 9, 12
    );

    static final Definition FRONTIER_LARGE_DUNGEON = structureClear(
            "frontier_danger_large_dungeon", "The Gate Under the Hill",
            profs(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.DANGER, "FRONTIER", 0,
            1900, 104, "the deep gate",
            structures("dungeons_enhanced:large_dungeon", "betterdungeons:skeleton_dungeon", "betterdungeons:zombie_dungeon"), 8,
            "frontier_large_dungeon",
            rewards(
                    enchanted("spartanweaponry:iron_greatsword", 1, "Gatebreaker", "minecraft:unbreaking", 2),
                    enchanted("minecraft:iron_chestplate", 1, "Gateward Mail", "minecraft:protection", 2)
            ), 11, 14
    );

    static final Definition FRONTIER_WHITE_REACH = frontierCapstone(
            "frontier_capstone_white_reach", "The Last Warm Camp",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            2400, 112, "the ice pit beyond the settled road", structures("dungeons_enhanced:ice_pit"), 9,
            "frontier_white_reach",
            rewards(
                    multiEnchanted("spartanweaponry:diamond_pike", 1, "White Reach",
                            enchant("minecraft:unbreaking", 2), enchant("minecraft:sharpness", 2)),
                    multiEnchanted("minecraft:diamond_boots", 1, "White Step",
                            enchant("minecraft:protection", 2), enchant("minecraft:feather_falling", 2)),
                    reward("backpacked:unlock_token", 1, "Deep-North Pack Token")
            ), 14, 18
    );

    static final Definition FRONTIER_GREENWAKE = frontierCapstone(
            "frontier_capstone_greenwake", "Temple of Eight Roots",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            2400, 112, "the Wildlands temple",
            structures("dungeons_enhanced:jungle_monument", "betterjungletemples:jungle_temple"), 9,
            "frontier_greenwake",
            rewards(
                    multiEnchanted("spartanweaponry:diamond_glaive", 1, "Greenwake",
                            enchant("minecraft:unbreaking", 2), enchant("minecraft:sharpness", 2)),
                    multiEnchanted("minecraft:diamond_leggings", 1, "Canopybound Leggings",
                            enchant("minecraft:protection", 2), enchant("minecraft:unbreaking", 1)),
                    reward("backpacked:unlock_token", 1, "Greenveil Pack Token")
            ), 14, 18
    );

    static final Definition FRONTIER_SUNSPIKE = frontierCapstone(
            "frontier_capstone_sunspike", "Beyond the Last Watering Place",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            2450, 112, "the Wildlands desert ruin",
            structures("betterdeserttemples:desert_temple", "dungeons_enhanced:desert_temple", "minecraft:desert_pyramid"), 9,
            "frontier_sunspike",
            rewards(
                    multiEnchanted("spartanweaponry:diamond_lance", 1, "Sunspike",
                            enchant("minecraft:unbreaking", 2), enchant("minecraft:sharpness", 2)),
                    multiEnchanted("minecraft:diamond_boots", 1, "Duststep Boots",
                            enchant("minecraft:protection", 2), enchant("minecraft:unbreaking", 1)),
                    reward("alexsmobs:acacia_blossom", 8, "Sunscar Acacia Blossoms")
            ), 14, 18
    );

    static final Definition FRONTIER_HARVEST_MOON = frontierCapstone(
            "frontier_capstone_harvest_moon", "The Infernal Harvest",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            2400, 112, "the infernal pumpkin grounds", structures("born_in_chaos_v1:infernal_pumpkin"), 9,
            "frontier_harvest_moon",
            rewards(
                    multiEnchanted("spartanweaponry:diamond_scythe", 1, "Harvest Moon",
                            enchant("minecraft:unbreaking", 2), enchant("minecraft:sharpness", 2)),
                    multiEnchanted("minecraft:diamond_chestplate", 1, "Harvest Warden",
                            enchant("minecraft:protection", 2), enchant("minecraft:unbreaking", 1)),
                    reward("majruszsaccessories:tamed_potato_beetle", 1, "Old-Field Beetle")
            ), 14, 18
    );

    static final List<Definition> ALL = List.of(
            FRONTIER_IGLOO_DISPATCH, FRONTIER_ICE_MINE,
            FRONTIER_VINE_SHRINE, FRONTIER_WITCH_RING,
            FRONTIER_FIRST_DIG, FRONTIER_SAVANNA_CARAVAN, FRONTIER_COOLING_RUN,
            FRONTIER_TAIGA_CARAVAN, FRONTIER_OLD_CASTLE,
            FRONTIER_WHITE_REACH, FRONTIER_GREENWAKE, FRONTIER_SUNSPIKE, FRONTIER_HARVEST_MOON,
            FRONTIER_STABLE_ROUTE, FRONTIER_LONG_SURVEY, FRONTIER_DROWNED_SHRINE,
            FRONTIER_WATCHLINE, FRONTIER_CARAVAN_LANCE, FRONTIER_RUIN_BOW,
            FRONTIER_TOMB_ROUTE, FRONTIER_OVERGROWN_ROUTE,
            FRONTIER_PILLAGER_CAMP, FRONTIER_PIRATE_SHIP, FRONTIER_LARGE_DUNGEON
    );
}
