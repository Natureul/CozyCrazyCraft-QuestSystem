package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

/**
 * Authored contracts that bind dialogue to an actual generated structure instance.
 *
 * These are deliberately separate from the tiny Zone-1 social-lattice prototype. A definition is
 * only an eligible story: the runtime still has to locate a real legal instance in the correct
 * radial tier and macro-region before a villager is allowed to offer it.
 */
final class VillageStructureQuestCatalog {
    static final Definition WATCHMANS_SILENCE = new Definition(
            "h1_watchmans_silence",
            "Watchman's Silence",
            List.of(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.FLETCHER),
            VillageProgressState.AccomplishmentCategory.DANGER,
            "HEARTHLANDS",
            "*",
            ObjectiveType.STRUCTURE_HOSTILE_CLEAR,
            1800,
            82,
            0,
            0,
            true,
            List.of(id("dungeons_enhanced", "watch_tower")),
            4,
            null,
            null,
            false,
            List.of(
                    new RewardStack(id("minecraft", "arrow"), 24, null),
                    new RewardStack(id("minecraft", "iron_ingot"), 4, null)
            ),
            6,
            6,
            convo("structure_watchmans_silence_offer"),
            convo("structure_clear_active"),
            convo("structure_clear_turnin")
    );

    static final Definition OLD_STONE_HILL = new Definition(
            "h1_old_stone_hill",
            "Old Stone on the Hill",
            List.of(VillagerProfession.MASON, VillagerProfession.TOOLSMITH, VillagerProfession.LIBRARIAN, VillagerProfession.CARTOGRAPHER),
            VillageProgressState.AccomplishmentCategory.EXPLORATION,
            "HEARTHLANDS",
            "*",
            ObjectiveType.STRUCTURE_SURVEY,
            1800,
            58,
            0,
            0,
            true,
            List.of(id("valhelsia_structures", "tower_ruin")),
            0,
            null,
            null,
            false,
            List.of(new RewardStack(id("minecraft", "lantern"), 4, null)),
            5,
            5,
            convo("structure_old_stone_hill_offer"),
            convo("structure_survey_active"),
            convo("structure_survey_turnin")
    );

    static final Definition EMPTY_STALLS = new Definition(
            "west_h1_empty_stalls",
            "The Empty Stalls",
            List.of(VillagerProfession.FARMER, VillagerProfession.SHEPHERD, VillagerProfession.LEATHERWORKER, VillagerProfession.BUTCHER),
            VillageProgressState.AccomplishmentCategory.COMMUNITY,
            "HEARTHLANDS",
            "WEST",
            ObjectiveType.STRUCTURE_RECOVERY,
            2200,
            72,
            0,
            0,
            true,
            List.of(id("dungeons_enhanced", "stables")),
            0,
            id("cozycrazyquests", "stablemasters_seal"),
            "Stablemaster's Seal",
            false,
            List.of(
                    new RewardStack(id("minecraft", "saddle"), 1, null),
                    new RewardStack(id("minecraft", "hay_block"), 3, null)
            ),
            7,
            7,
            convo("structure_empty_stalls_offer"),
            convo("structure_recovery_active"),
            convo("structure_recovery_turnin")
    );

    static final Definition BURIED_MARKER = new Definition(
            "south_h1_buried_marker",
            "The Buried Marker",
            List.of(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CARTOGRAPHER, VillagerProfession.CLERIC),
            VillageProgressState.AccomplishmentCategory.EXPLORATION,
            "HEARTHLANDS",
            "SOUTH",
            ObjectiveType.STRUCTURE_RECOVERY,
            2300,
            68,
            0,
            0,
            true,
            List.of(id("dungeons_enhanced", "desert_tomb")),
            0,
            id("cozycrazyquests", "sunscar_tomb_tablet"),
            "Buried Tablet",
            false,
            List.of(
                    new RewardStack(id("minecraft", "brush"), 1, null),
                    new RewardStack(id("minecraft", "gold_ingot"), 4, null)
            ),
            8,
            8,
            convo("structure_buried_marker_offer"),
            convo("structure_recovery_active"),
            convo("structure_recovery_turnin")
    );

    static final Definition NORTH_MISSING_EXPEDITION = new Definition(
            "north_f2_missing_expedition",
            "The Missing Expedition",
            List.of(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.DANGER,
            "FRONTIER",
            "NORTH",
            ObjectiveType.STRUCTURE_HOSTILE_CLEAR,
            3000,
            92,
            0,
            0,
            true,
            List.of(
                    id("valhelsia_structures", "castle_ruin"),
                    id("dungeons_enhanced", "tower_of_the_undead")
            ),
            5,
            null,
            null,
            false,
            List.of(
                    new RewardStack(id("minecraft", "compass"), 1, null),
                    new RewardStack(id("minecraft", "cooked_beef"), 12, null)
            ),
            10,
            10,
            convo("structure_missing_expedition_offer"),
            convo("structure_clear_active"),
            convo("structure_clear_turnin")
    );

    static final Definition EAST_SHRINE_UNDER_VINES = new Definition(
            "east_f2_shrine_under_vines",
            "The Shrine Under Vines",
            List.of(VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER, VillagerProfession.MASON),
            VillageProgressState.AccomplishmentCategory.EXPLORATION,
            "FRONTIER",
            "EAST",
            ObjectiveType.STRUCTURE_RECOVERY,
            3000,
            76,
            0,
            0,
            true,
            List.of(id("dungeons_enhanced", "jungle_monument")),
            0,
            id("cozycrazyquests", "greenveil_survey_notes"),
            "Overgrown Survey Notes",
            false,
            List.of(
                    new RewardStack(id("minecraft", "compass"), 1, null),
                    new RewardStack(id("minecraft", "golden_carrot"), 8, null)
            ),
            11,
            11,
            convo("structure_shrine_under_vines_offer"),
            convo("structure_recovery_active"),
            convo("structure_recovery_turnin")
    );

    static final Definition EAST_VINES_HAVE_TEETH = new Definition(
            "east_f2_vines_have_teeth",
            "The Vines Have Teeth",
            List.of(VillagerProfession.FLETCHER, VillagerProfession.WEAPONSMITH, VillagerProfession.LEATHERWORKER, VillagerProfession.ARMORER),
            VillageProgressState.AccomplishmentCategory.DANGER,
            "FRONTIER",
            "EAST",
            ObjectiveType.STRUCTURE_HOSTILE_CLEAR,
            3000,
            88,
            0,
            0,
            true,
            List.of(
                    id("betterjungletemples", "jungle_temple"),
                    id("dungeons_enhanced", "jungle_monument")
            ),
            5,
            null,
            null,
            false,
            List.of(
                    new RewardStack(id("minecraft", "arrow"), 32, null),
                    new RewardStack(id("minecraft", "iron_sword"), 1, "Canopy Guard Blade")
            ),
            10,
            10,
            convo("structure_vines_have_teeth_offer"),
            convo("structure_clear_active"),
            convo("structure_clear_turnin")
    );

    static final Definition SOUTH_FIRST_DIG = new Definition(
            "south_f2_first_dig",
            "The First Dig",
            List.of(VillagerProfession.MASON, VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN),
            VillageProgressState.AccomplishmentCategory.EXPLORATION,
            "FRONTIER",
            "SOUTH",
            ObjectiveType.STRUCTURE_SURVEY,
            3000,
            70,
            0,
            0,
            true,
            List.of(
                    id("betterdeserttemples", "desert_temple"),
                    id("dungeons_enhanced", "desert_temple")
            ),
            0,
            null,
            null,
            false,
            List.of(
                    new RewardStack(id("minecraft", "brush"), 1, null),
                    new RewardStack(id("minecraft", "gold_ingot"), 3, null)
            ),
            9,
            9,
            convo("structure_first_dig_offer"),
            convo("structure_survey_active"),
            convo("structure_survey_turnin")
    );

    static final Definition NORTH_LAST_WARM_CAMP = new Definition(
            "north_w3_last_warm_camp",
            "The Last Warm Camp",
            List.of(VillagerProfession.ARMORER, VillagerProfession.CARTOGRAPHER, VillagerProfession.CLERIC, VillagerProfession.WEAPONSMITH),
            VillageProgressState.AccomplishmentCategory.DANGER,
            "WILDLANDS",
            "NORTH",
            ObjectiveType.STRUCTURE_RECOVERY,
            3800,
            84,
            0,
            0,
            true,
            List.of(id("dungeons_enhanced", "ice_pit")),
            0,
            id("cozycrazyquests", "frostmarch_dispatch"),
            "Frostmarch Dispatch",
            false,
            List.of(
                    new RewardStack(id("minecraft", "golden_apple"), 1, null),
                    new RewardStack(id("minecraft", "cooked_beef"), 16, null)
            ),
            16,
            16,
            convo("structure_last_warm_camp_offer"),
            convo("structure_recovery_active"),
            convo("structure_recovery_turnin")
    );

    static final List<Definition> ALL = List.of(
            WATCHMANS_SILENCE,
            OLD_STONE_HILL,
            EMPTY_STALLS,
            BURIED_MARKER,
            NORTH_MISSING_EXPEDITION,
            EAST_SHRINE_UNDER_VINES,
            EAST_VINES_HAVE_TEETH,
            SOUTH_FIRST_DIG,
            NORTH_LAST_WARM_CAMP
    );

    private VillageStructureQuestCatalog() {}

    static Definition byId(String id) {
        for (Definition definition : ALL) if (definition.id().equals(id)) return definition;
        return null;
    }

    static List<Definition> forProfession(VillagerProfession profession) {
        return ALL.stream().filter(definition -> definition.giverProfessions().contains(profession)).toList();
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    private static ResourceLocation convo(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }

    enum ObjectiveType {
        STRUCTURE_SURVEY,
        STRUCTURE_HOSTILE_CLEAR,
        STRUCTURE_RECOVERY
    }

    record RewardStack(ResourceLocation itemId, int count, String customName) {}

    record Definition(
            String id,
            String title,
            List<VillagerProfession> giverProfessions,
            VillageProgressState.AccomplishmentCategory accomplishmentCategory,
            String issuingTier,
            String issuingMacro,
            ObjectiveType objectiveType,
            int searchRadiusBlocks,
            int targetRadiusBlocks,
            int targetMinTierOffset,
            int targetMaxTierOffset,
            boolean sameMacroRegion,
            List<ResourceLocation> structureCandidates,
            int requiredKills,
            ResourceLocation recoveryItemId,
            String recoveryItemName,
            boolean revealAtlasOnAccept,
            List<RewardStack> rewardItems,
            int emeraldReward,
            int experienceReward,
            ResourceLocation offerDialogue,
            ResourceLocation activeDialogue,
            ResourceLocation turninDialogue
    ) {
        boolean accepts(VillagerProfession profession) {
            return giverProfessions.contains(profession);
        }
    }
}
