package com.natureul.cozycrazyquests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

import static com.natureul.cozycrazyquests.VillageQuestCatalog.*;

/**
 * Optional Hearthlands recovery work.
 *
 * These are deliberately not progression-category shortcuts. They exist to broaden the verb set
 * beyond "survey" and "kill": a villager knows about a real nearby place, the player physically
 * enters that exact generated instance, recovers a quest-bound object there, and brings it home.
 */
final class HearthlandsRecoveryQuestCatalog {
    private HearthlandsRecoveryQuestCatalog() {}

    static final Definition FROZEN_DISPATCH = recovery(
            "hearthlands_recovery_frozen_dispatch", "The Letter Under Ice",
            profs(VillagerProfession.LIBRARIAN, VillagerProfession.CARTOGRAPHER, VillagerProfession.FLETCHER),
            null, "HEARTHLANDS", 0,
            1150, 72, "the buried ice works", structures("dungeons_enhanced:ice_pit"),
            "Sealed Watch Dispatch", "recovery_frozen_dispatch", false,
            rewards(
                    enchanted("minecraft:iron_pickaxe", 1, "Dispatch Pick", "minecraft:efficiency", 1),
                    reward("minecraft:torch", 16, null)
            ), 5, 7
    );

    static final Definition GREENVEIL_FOLIO = recovery(
            "hearthlands_recovery_greenveil_folio", "Pages Beneath the Vines",
            profs(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            null, "HEARTHLANDS", 0,
            1200, 72, "the overgrown stonework", structures("dungeons_enhanced:jungle_monument"),
            "Water-Stained Survey Folio", "recovery_greenveil_folio", false,
            rewards(
                    enchanted("minecraft:iron_axe", 1, "Folio-Cutter", "minecraft:efficiency", 1),
                    reward("minecraft:paper", 12, null)
            ), 5, 7
    );

    static final Definition SUNSCAR_RUBBING = recovery(
            "hearthlands_recovery_sunscar_rubbing", "The Name Beneath the Sand",
            profs(VillagerProfession.CLERIC, VillagerProfession.LIBRARIAN, VillagerProfession.MASON),
            null, "HEARTHLANDS", 0,
            1200, 72, "the old desert tomb", structures("dungeons_enhanced:desert_tomb"),
            "Carved Funerary Rubbing", "recovery_sunscar_rubbing", false,
            rewards(
                    enchanted("spartanweaponry:iron_boomerang", 1, "Dustscript", "minecraft:unbreaking", 1),
                    reward("minecraft:brush", 1, "Field Brush")
            ), 6, 8
    );

    static final Definition STABLE_LEDGER = recovery(
            "hearthlands_recovery_stable_ledger", "Who Owned the Empty Stalls",
            profs(VillagerProfession.SHEPHERD, VillagerProfession.LEATHERWORKER, VillagerProfession.FARMER),
            null, "HEARTHLANDS", 0,
            1000, 64, "the abandoned stables", structures("dungeons_enhanced:stables"),
            "Weathered Stable Ledger", "recovery_stable_ledger", false,
            rewards(
                    reward("minecraft:saddle", 1, "Roadworn Saddle"),
                    reward("minecraft:lead", 2, null)
            ), 5, 7
    );

    static final List<Definition> ALL = List.of(
            FROZEN_DISPATCH,
            GREENVEIL_FOLIO,
            SUNSCAR_RUBBING,
            STABLE_LEDGER
    );
}
