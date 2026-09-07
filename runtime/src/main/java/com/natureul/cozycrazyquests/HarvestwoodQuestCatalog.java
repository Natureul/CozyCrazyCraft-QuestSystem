package com.natureul.cozycrazyquests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

import static com.natureul.cozycrazyquests.VillageQuestCatalog.*;

/**
 * Harvestwood / WEST executable authored content.
 *
 * This catalog deliberately stays inside the existing quest primitives: generated-structure survey,
 * structure-bound recovery evidence, and bounded hostile clears. It does not own boss spawning,
 * structure completion, navigation, hints, or exact-entity encounter routing.
 *
 * Sir Pumpkinhead and Lord Pumpkinhead are intentionally absent as kill objectives. Their protected
 * encounter path remains a trust/runtime integration concern; this catalog builds evidence and readiness
 * around them without turning either boss into routine villager work.
 */
final class HarvestwoodQuestCatalog {
    private HarvestwoodQuestCatalog() {}

    static final Definition WEST_FARMSTEAD_GUARDIAN = structure(
            "west_h1_farmstead_guardian", "A Guardian in the Field",
            profs(VillagerProfession.FARMER, VillagerProfession.LIBRARIAN, VillagerProfession.CARTOGRAPHER),
            null, "HEARTHLANDS", 0,
            1400, 72, "the inhabited harvest farm", structures("born_in_chaos_v1:farm"), 0,
            "west_farmstead_guardian", false,
            rewards(
                    enchanted("minecraft:iron_hoe", 1, "Fieldwake Hoe", "minecraft:unbreaking", 1),
                    reward("minecraft:pumpkin_pie", 4, null)
            ), 5, 7
    );

    static final Definition WEST_FOREST_WATCH_LEDGER = recovery(
            "west_h1_forest_watch_ledger", "The Fog-Watch Ledger",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.FLETCHER, VillagerProfession.LIBRARIAN),
            null, "HEARTHLANDS", 0,
            1350, 72, "the forest observation tower", structures("born_in_chaos_v1:observation_tower_forest"),
            "Fog-Watch Ledger", "west_forest_watch_ledger", false,
            rewards(
                    enchanted("minecraft:iron_axe", 1, "Hedgerow Axe", "minecraft:efficiency", 1),
                    reward("minecraft:lantern", 4, null)
            ), 5, 7
    );

    static final Definition WEST_FRONTIER_WATCH_DISPATCH = recovery(
            "west_f2_watch_dispatch", "Two Towers, One Story",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.FLETCHER, VillagerProfession.LIBRARIAN),
            null, "FRONTIER", 0,
            1800, 80, "the western observation tower", structures("born_in_chaos_v1:observation_tower_forest"),
            "Western Watch Dispatch", "west_frontier_watch_dispatch", false,
            rewards(
                    enchanted("spartanweaponry:iron_longbow", 1, "Oldwood Longbow", "minecraft:unbreaking", 2),
                    reward("minecraft:spyglass", 1, "Roadwatch Glass")
            ), 9, 12
    );

    static final Definition WEST_FRONTIER_HOUND_ROAD = structureClear(
            "west_f2_hound_road", "The Road Around the Mound",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH),
            null, "FRONTIER", 0,
            1850, 96, "the hound mound on the timber road", structures("born_in_chaos_v1:mound_of_hounds"), 6,
            "west_frontier_hound_road",
            rewards(
                    multiEnchanted("spartanweaponry:iron_battleaxe", 1, "Roadcutter Battleaxe",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    reward("minecraft:lantern", 4, null)
            ), 10, 13
    );

    static final Definition WEST_FRONTIER_FORGE_STAMP = recovery(
            "west_f2_forge_stamp", "The Old-Road Maker's Stamp",
            profs(VillagerProfession.WEAPONSMITH, VillagerProfession.TOOLSMITH, VillagerProfession.ARMORER),
            null, "FRONTIER", 0,
            1900, 80, "the frontier forge", structures("valhelsia_structures:forge"),
            "Old-Road Maker's Stamp", "west_frontier_forge_stamp", false,
            rewards(
                    enchanted("spartanweaponry:iron_scythe", 1, "Harvest-Road Scythe", "minecraft:unbreaking", 2),
                    reward("minecraft:iron_ingot", 6, null)
            ), 10, 13
    );

    static final Definition WEST_FRONTIER_ROUTE_PLATE = recovery(
            "west_f2_route_plate", "Where the Timber Road Split",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.MASON, VillagerProfession.TOOLSMITH),
            null, "FRONTIER", 0,
            1900, 88, "the old-road ruin", structures(
                    "valhelsia_structures:castle_ruin",
                    "valhelsia_structures:tower_ruin"
            ),
            "Timber-Road Plate", "west_frontier_route_plate", false,
            rewards(
                    multiEnchanted("minecraft:iron_axe", 1, "Roadwright Axe",
                            enchant("minecraft:efficiency", 3), enchant("minecraft:unbreaking", 2)),
                    reward("minecraft:lantern", 6, null)
            ), 9, 12
    );

    static final Definition WEST_WILDLANDS_HOUND_MOUND = structureClear(
            "west_w3_hound_mound", "The Road Goes Quiet",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            VillageProgressState.AccomplishmentCategory.DANGER, "WILDLANDS", 0,
            2400, 112, "the deep-west hound mound", structures("born_in_chaos_v1:mound_of_hounds"), 7,
            "west_wildlands_hound_mound",
            rewards(
                    multiEnchanted("minecraft:diamond_axe", 1, "Old-Growth Road Axe",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    reward("minecraft:golden_carrot", 8, null)
            ), 14, 18
    );

    static final Definition WEST_WILDLANDS_INFERNAL_EVIDENCE = recovery(
            "west_w3_infernal_evidence", "Ash Where No Barn Burned",
            profs(VillagerProfession.CLERIC, VillagerProfession.LIBRARIAN, VillagerProfession.FARMER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "WILDLANDS", 0,
            2400, 112, "the infernal pumpkin site", structures("born_in_chaos_v1:infernal_pumpkin"),
            "Charred Spirit Ledger", "west_wildlands_infernal_evidence", false,
            rewards(
                    multiEnchanted("minecraft:diamond_axe", 1, "Ashwood Axe",
                            enchant("minecraft:efficiency", 3), enchant("minecraft:unbreaking", 2)),
                    reward("minecraft:water_bucket", 1, "Spiritkeeper's Water")
            ), 13, 17
    );

    static final Definition WEST_WILDLANDS_HEADLESS_WITNESS = recovery(
            "west_w3_headless_witness", "Three Roads, One Rider",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC),
            null, "WILDLANDS", 0,
            2500, 96, "the last western watch", structures("born_in_chaos_v1:observation_tower_forest"),
            "Last Headless-Road Ledger", "west_wildlands_headless_witness", false,
            rewards(
                    multiEnchanted("minecraft:diamond_helmet", 1, "Frontiersman's Hood",
                            enchant("minecraft:protection", 3), enchant("minecraft:unbreaking", 2)),
                    reward("minecraft:compass", 1, "Headless-Road Compass")
            ), 14, 18
    );

    static final Definition WEST_DREAD_LAST_ROAD_PLATE = recovery(
            "west_d4_last_road_plate", "The Last Reliable Road",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.MASON, VillagerProfession.LIBRARIAN),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "DREAD_REACHES", 0,
            3000, 120, "the dark tower on the last road", structures(
                    "born_in_chaos_v1:dark_tower_forest",
                    "born_in_chaos_v1:dark_tower_plain",
                    "born_in_chaos_v1:dark_tower_taiga"
            ),
            "Last Reliable Route Plate", "west_dread_last_road_plate", false,
            rewards(
                    multiEnchanted("minecraft:diamond_axe", 1, "Last-Road Axe",
                            enchant("minecraft:efficiency", 4), enchant("minecraft:unbreaking", 3)),
                    reward("minecraft:lantern", 8, null)
            ), 18, 22
    );

    static final Definition WEST_DREAD_TRANSMUTERS_FOLIO = recovery(
            "west_d4_transmuters_folio", "What the Old Alchemists Changed",
            profs(VillagerProfession.CLERIC, VillagerProfession.LIBRARIAN, VillagerProfession.TOOLSMITH),
            VillageProgressState.AccomplishmentCategory.PROFESSION, "DREAD_REACHES", 0,
            3100, 120, "the Born in Chaos dark tower", structures(
                    "born_in_chaos_v1:dark_tower_forest",
                    "born_in_chaos_v1:dark_tower_plain",
                    "born_in_chaos_v1:dark_tower_taiga"
            ),
            "Black-Alchemist Folio", "west_dread_transmuters_folio", false,
            rewards(
                    multiEnchanted("minecraft:diamond_pickaxe", 1, "Black-Road Pick",
                            enchant("minecraft:efficiency", 4), enchant("minecraft:unbreaking", 3)),
                    reward("minecraft:obsidian", 8, null)
            ), 18, 22
    );

    static final Definition WEST_DREAD_ONE_JOURNEY_CACHE = structureClear(
            "west_d4_one_journey_cache", "Made for One Journey",
            profs(VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.DANGER, "DREAD_REACHES", 0,
            3200, 128, "the dark-tower approach", structures(
                    "born_in_chaos_v1:dark_tower_forest",
                    "born_in_chaos_v1:dark_tower_plain",
                    "born_in_chaos_v1:dark_tower_taiga"
            ), 8,
            "west_dread_one_journey_cache",
            rewards(
                    multiEnchanted("minecraft:diamond_chestplate", 1, "One-Journey Coat",
                            enchant("minecraft:protection", 4), enchant("minecraft:unbreaking", 3)),
                    reward("minecraft:golden_apple", 2, null)
            ), 20, 24
    );

    static final List<Definition> ALL = List.of(
            WEST_FARMSTEAD_GUARDIAN,
            WEST_FOREST_WATCH_LEDGER,
            WEST_FRONTIER_WATCH_DISPATCH,
            WEST_FRONTIER_HOUND_ROAD,
            WEST_FRONTIER_FORGE_STAMP,
            WEST_FRONTIER_ROUTE_PLATE,
            WEST_WILDLANDS_HOUND_MOUND,
            WEST_WILDLANDS_INFERNAL_EVIDENCE,
            WEST_WILDLANDS_HEADLESS_WITNESS,
            WEST_DREAD_LAST_ROAD_PLATE,
            WEST_DREAD_TRANSMUTERS_FOLIO,
            WEST_DREAD_ONE_JOURNEY_CACHE
    );
}
