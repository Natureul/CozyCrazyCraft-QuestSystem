package com.natureul.cozycrazyquests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

import static com.natureul.cozycrazyquests.VillageQuestCatalog.*;

/**
 * Optional Hearthlands recovery work.
 *
 * These are deliberately not progression-category shortcuts. They broaden the verb set beyond
 * "survey" and "kill", but each uses a different structure family from the core Hearthlands bank.
 * That keeps recovery work from becoming an immediate re-run of the structure the player just saw.
 */
final class HearthlandsRecoveryQuestCatalog {
    private HearthlandsRecoveryQuestCatalog() {}

    static final Definition FROZEN_DISPATCH = recovery(
            "hearthlands_recovery_frozen_dispatch", "The Letter in the Rafters",
            profs(VillagerProfession.LIBRARIAN, VillagerProfession.CARTOGRAPHER, VillagerProfession.FLETCHER),
            null, "HEARTHLANDS", 0,
            1050, 56, "the empty miner's shelter", structures("dungeons_enhanced:miners_house"),
            "Sealed Watch Dispatch", "recovery_frozen_dispatch", false,
            rewards(
                    reward("cold_sweat:thermometer", 1, "Trail Thermometer"),
                    reward("minecraft:torch", 12, null)
            ), 5, 7
    );

    static final Definition GREENVEIL_FOLIO = recovery(
            "hearthlands_recovery_greenveil_folio", "Plans Left Behind",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            null, "HEARTHLANDS", 0,
            1050, 56, "the empty field house", structures("valhelsia_structures:player_house"),
            "Water-Stained Survey Folio", "recovery_greenveil_folio", false,
            rewards(
                    enchanted("minecraft:iron_shovel", 1, "Foundation Trowel", "minecraft:efficiency", 1),
                    reward("minecraft:paper", 10, null)
            ), 5, 7
    );

    static final Definition SUNSCAR_RUBBING = recovery(
            "hearthlands_recovery_sunscar_rubbing", "The Mark Beside the Sand",
            profs(VillagerProfession.CLERIC, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            null, "HEARTHLANDS", 0,
            1100, 64, "the small desert ruin", structures("towns_and_towers:mimic_desert"),
            "Carved Funerary Rubbing", "recovery_sunscar_rubbing", false,
            rewards(
                    enchanted("spartanweaponry:iron_boomerang", 1, "Dustscript", "minecraft:unbreaking", 1),
                    reward("minecraft:brush", 1, "Field Brush")
            ), 6, 8
    );

    static final Definition STABLE_LEDGER = recovery(
            "hearthlands_recovery_stable_ledger", "The Paper Trail from the Stalls",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.LEATHERWORKER, VillagerProfession.FARMER),
            null, "HEARTHLANDS", 0,
            900, 48, "the old hay store", structures("dungeons_enhanced:hay_storage"),
            "Weathered Stable Ledger", "recovery_stable_ledger", false,
            rewards(
                    enchanted("minecraft:iron_hoe", 1, "Stablekeeper's Hoe", "minecraft:unbreaking", 1),
                    reward("minecraft:name_tag", 1, "Route Keeper's Tag")
            ), 5, 7
    );

    static final List<Definition> ALL = List.of(
            FROZEN_DISPATCH,
            GREENVEIL_FOLIO,
            SUNSCAR_RUBBING,
            STABLE_LEDGER
    );
}
