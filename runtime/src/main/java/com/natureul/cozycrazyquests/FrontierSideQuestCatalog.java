package com.natureul.cozycrazyquests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

import static com.natureul.cozycrazyquests.VillageQuestCatalog.*;

/**
 * Optional one-time Frontier adventures.
 *
 * These intentionally have no semantic accomplishment category. They add places, dialogue and named
 * equipment without satisfying COMMUNITY / EXPLORATION / PROFESSION / DANGER on the player's behalf.
 * A real eligible structure must still exist before the conversation can be selected.
 */
final class FrontierSideQuestCatalog {
    private FrontierSideQuestCatalog() {}

    static final Definition RIME_MINE = structureClear(
            "frontier_side_rime_mine", "Iron Under Rime",
            profs(VillagerProfession.TOOLSMITH, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            null, "FRONTIER", 0,
            1850, 96, "the frozen side-shaft",
            structures("bettermineshafts:mineshaft_ice", "bettermineshafts:mineshaft_spruce_snowy"), 7,
            "frontier_side_rime_mine",
            rewards(
                    multiEnchanted("spartanweaponry:iron_warhammer", 1, "Rimehammer",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:iron_chestplate", 1, "Minewarden Coat", "minecraft:protection", 2)
            ), 9, 12
    );

    static final Definition OVERGROWN_MINE = structureClear(
            "frontier_side_overgrown_mine", "Roots in the Shaft",
            profs(VillagerProfession.TOOLSMITH, VillagerProfession.FLETCHER, VillagerProfession.ARMORER),
            null, "FRONTIER", 0,
            1850, 96, "the root-choked mine",
            structures("bettermineshafts:mineshaft_overgrown", "bettermineshafts:mineshaft_jungle", "bettermineshafts:mineshaft_lush"), 7,
            "frontier_side_overgrown_mine",
            rewards(
                    multiEnchanted("spartanweaponry:iron_glaive", 1, "Rootline",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:iron_leggings", 1, "Vineguard Leggings", "minecraft:projectile_protection", 2)
            ), 9, 12
    );

    static final Definition DESERT_TEMPLE = structureClear(
            "frontier_side_desert_temple", "Sand in the Doorway",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.LEATHERWORKER),
            null, "FRONTIER", 0,
            1850, 96, "the sand-choked temple", structures("dungeons_enhanced:desert_temple"), 7,
            "frontier_side_desert_temple",
            rewards(
                    multiEnchanted("spartanweaponry:iron_boomerang", 1, "Sunwheel",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    multiEnchanted("minecraft:iron_boots", 1, "Dustguard Boots",
                            enchant("minecraft:protection", 1), enchant("minecraft:feather_falling", 2))
            ), 9, 12
    );

    static final Definition TAIGA_SALVAGE = structureClear(
            "frontier_side_taiga_salvage", "What the Cart Left Behind",
            profs(VillagerProfession.LEATHERWORKER, VillagerProfession.SHEPHERD, VillagerProfession.FLETCHER),
            null, "FRONTIER", 0,
            1800, 96, "the abandoned pine-road cart", structures("born_in_chaos_v1:clown_caravan_taiga"), 6,
            "frontier_side_taiga_salvage",
            rewards(
                    multiEnchanted("spartanweaponry:iron_halberd", 1, "Pinehook",
                            enchant("minecraft:sharpness", 1), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:shield", 1, "Cartwarden Shield", "minecraft:unbreaking", 2)
            ), 8, 11
    );

    static final Definition UNDEAD_TOWER = structureClear(
            "frontier_side_undead_tower", "The Bell in the Dead Tower",
            profs(VillagerProfession.CLERIC, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            null, "FRONTIER", 0,
            1900, 104, "the tower of the undead", structures("dungeons_enhanced:tower_of_the_undead"), 8,
            "frontier_side_undead_tower",
            rewards(
                    multiEnchanted("spartanweaponry:iron_flanged_mace", 1, "Bellringer",
                            enchant("minecraft:smite", 3), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:iron_helmet", 1, "Towerwatch Helm", "minecraft:protection", 2)
            ), 10, 13
    );

    static final Definition TALL_WITCH = structureClear(
            "frontier_side_tall_witch", "A Hut Above the Reeds",
            profs(VillagerProfession.CLERIC, VillagerProfession.FLETCHER, VillagerProfession.LEATHERWORKER),
            null, "FRONTIER", 0,
            1850, 96, "the tall witch hut", structures("dungeons_enhanced:tall_witch_hut"), 7,
            "frontier_side_tall_witch",
            rewards(
                    multiEnchanted("spartanweaponry:iron_rapier", 1, "Hexneedle",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    multiEnchanted("minecraft:iron_boots", 1, "Bogstep Boots",
                            enchant("minecraft:depth_strider", 2), enchant("minecraft:unbreaking", 1))
            ), 9, 12
    );

    static final Definition FIREWELL = structureClear(
            "frontier_side_firewell", "Ash Around the Well",
            profs(VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH, VillagerProfession.TOOLSMITH),
            null, "FRONTIER", 0,
            1850, 96, "the firewell", structures("born_in_chaos_v1:firewell"), 7,
            "frontier_side_firewell",
            rewards(
                    multiEnchanted("spartanweaponry:iron_battle_hammer", 1, "Ashmaul",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:iron_chestplate", 1, "Cinderplate", "minecraft:protection", 2)
            ), 10, 13
    );

    static final Definition HOUND_MOUND = structureClear(
            "frontier_side_hound_mound", "Tracks Around the Mound",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            null, "FRONTIER", 0,
            1850, 104, "the mound of hounds", structures("born_in_chaos_v1:mound_of_hounds"), 8,
            "frontier_side_hound_mound",
            rewards(
                    multiEnchanted("spartanweaponry:iron_spear", 1, "Houndspike",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:chainmail_chestplate", 1, "Trailmail", "minecraft:protection", 2)
            ), 10, 13
    );

    static final Definition FORGE = structureClear(
            "frontier_side_forge", "The Forge With No Smith",
            profs(VillagerProfession.TOOLSMITH, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            null, "FRONTIER", 0,
            1850, 96, "the abandoned forge", structures("valhelsia_structures:forge"), 6,
            "frontier_side_forge",
            rewards(
                    multiEnchanted("spartanweaponry:iron_battle_hammer", 1, "Forge-Bell",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    multiEnchanted("minecraft:iron_boots", 1, "Smith's Greaves",
                            enchant("minecraft:protection", 2), enchant("minecraft:unbreaking", 1))
            ), 9, 12
    );

    static final Definition CASTLE_RUIN = structureClear(
            "frontier_side_castle_ruin", "The Broken Gate",
            profs(VillagerProfession.MASON, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            null, "FRONTIER", 0,
            1900, 104, "the broken castle gate", structures("valhelsia_structures:castle_ruin"), 7,
            "frontier_side_castle_ruin",
            rewards(
                    multiEnchanted("spartanweaponry:iron_halberd", 1, "Gatehook",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:shield", 1, "Oldgate Shield", "minecraft:unbreaking", 2)
            ), 10, 13
    );

    static final Definition SKELETON_DUNGEON = structureClear(
            "frontier_side_skeleton_dungeon", "Arrows Below",
            profs(VillagerProfession.FLETCHER, VillagerProfession.ARMORER, VillagerProfession.CLERIC),
            null, "FRONTIER", 0,
            1900, 104, "the skeleton dungeon", structures("betterdungeons:skeleton_dungeon"), 8,
            "frontier_side_skeleton_dungeon",
            rewards(
                    multiEnchanted("spartanweaponry:iron_heavy_crossbow", 1, "Bonebolt",
                            enchant("minecraft:quick_charge", 1), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:chainmail_chestplate", 1, "Rattlecoat", "minecraft:projectile_protection", 3)
            ), 10, 13
    );

    static final Definition ZOMBIE_DUNGEON = structureClear(
            "frontier_side_zombie_dungeon", "The Door That Keeps Opening",
            profs(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.CLERIC),
            null, "FRONTIER", 0,
            1900, 104, "the zombie dungeon", structures("betterdungeons:zombie_dungeon"), 8,
            "frontier_side_zombie_dungeon",
            rewards(
                    multiEnchanted("spartanweaponry:iron_greatsword", 1, "Gravecleaver",
                            enchant("minecraft:sharpness", 2), enchant("minecraft:unbreaking", 2)),
                    enchanted("minecraft:iron_chestplate", 1, "Deadward Mail", "minecraft:protection", 2)
            ), 10, 13
    );

    static final List<Definition> ALL = List.of(
            RIME_MINE, OVERGROWN_MINE, DESERT_TEMPLE, TAIGA_SALVAGE,
            UNDEAD_TOWER, TALL_WITCH, FIREWELL, HOUND_MOUND,
            FORGE, CASTLE_RUIN, SKELETON_DUNGEON, ZOMBIE_DUNGEON
    );
}
