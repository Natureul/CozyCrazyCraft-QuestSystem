package com.natureul.cozycrazyquests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

import static com.natureul.cozycrazyquests.VillageQuestCatalog.*;

/**
 * First executable Wildlands slice.
 *
 * This catalog is intentionally smaller than the design Bible. Only entries whose encounter or recovery
 * site has a verified structure binding are live here. West's Pumpkinhead route and the deferred eastern
 * abomination remain out until their spawn/structure mechanisms are authoritative; the runtime should be
 * asymmetric rather than fabricate symmetry.
 */
final class WildlandsQuestCatalog {
    private WildlandsQuestCatalog() {}

    static final Definition SLEEPING_MOUNTAIN = structureClear(
            "north_w3_sleeping_mountain", "The Sleeping Mountain",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            VillageProgressState.AccomplishmentCategory.DANGER, "WILDLANDS", 0,
            2400, 144, "the Frostmaw den", structures("mowziesmobs:frostmaw_spawn"), 1,
            "wildlands_sleeping_mountain",
            rewards(
                    multiEnchanted("spartanweaponry:pike_diamond", 1, "White Reach",
                            enchant("minecraft:sharpness", 3), enchant("minecraft:unbreaking", 2))
            ),
            14, 18
    );

    static final Definition LAST_WARM_CAMP = recovery(
            "north_w3_last_warm_camp", "The Last Warm Camp",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.TOOLSMITH, VillagerProfession.LEATHERWORKER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "WILDLANDS", 0,
            2200, 112, "the abandoned ice camp", structures("dungeons_enhanced:ice_pit"),
            "Marked Stove Plate", "wildlands_last_warm_camp", true,
            rewards(
                    reward("cold_sweat:waterskin", 1, "Coastbound Waterskin"),
                    reward("cold_sweat:thermometer", 1, "Quartermaster's Thermometer")
            ),
            11, 14
    );

    static final Definition TEMPLE_EIGHT_ROOTS = recovery(
            "east_w3_temple_eight_roots", "Temple of Eight Roots",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            VillageProgressState.AccomplishmentCategory.EXPLORATION, "WILDLANDS", 0,
            2400, 120, "the temple swallowed by the canopy",
            structures("betterjungletemples:jungle_temple", "dungeons_enhanced:jungle_monument"),
            "Seed Reliquary", "wildlands_temple_eight_roots", true,
            rewards(
                    multiEnchanted("spartanweaponry:glaive_diamond", 1, "Greenwake",
                            enchant("minecraft:sharpness", 3), enchant("minecraft:unbreaking", 2))
            ),
            13, 17
    );

    static final Definition SUNBIRD = structureClear(
            "south_w3_sunbird", "The Sunbird",
            profs(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER),
            VillageProgressState.AccomplishmentCategory.DANGER, "WILDLANDS", 0,
            2400, 144, "the Umvuthana grove", structures("mowziesmobs:umvuthana_grove"), 1,
            "wildlands_sunbird",
            rewards(
                    multiEnchanted("spartanweaponry:lance_diamond", 1, "Sunspike",
                            enchant("minecraft:sharpness", 3), enchant("minecraft:unbreaking", 2))
            ),
            14, 18
    );

    static final List<Definition> ALL = List.of(
            SLEEPING_MOUNTAIN,
            LAST_WARM_CAMP,
            TEMPLE_EIGHT_ROOTS,
            SUNBIRD
    );
}
