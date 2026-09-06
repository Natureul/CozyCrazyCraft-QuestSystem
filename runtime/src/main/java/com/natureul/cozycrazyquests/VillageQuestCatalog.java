package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

/**
 * Small semantic catalog for the first Conversations proof-of-concept.
 *
 * The long-term catalog will be data-driven using the Quest & Reward Master Bible fields. Keeping
 * this definition explicit for 0.3.0 lets us validate the lifecycle (eligibility -> exact target ->
 * dialogue -> completion -> Village Trust) before importing hundreds of authored records.
 */
final class VillageQuestCatalog {
    static final Definition FIRST_REAL_MAP = new Definition(
            "hearthlands_cartographer_first_real_map",
            "The First Real Map",
            VillagerProfession.CARTOGRAPHER,
            "HEARTHLANDS",
            0,
            1,
            true,
            1024,
            56,
            List.of(
                    new ResourceLocation("dungeons_enhanced", "watch_tower"),
                    new ResourceLocation("valhelsia_structures", "tower_ruin"),
                    new ResourceLocation("dungeons_enhanced", "stables")
            ),
            5,
            5
    );

    private VillageQuestCatalog() {}

    record Definition(
            String id,
            String title,
            VillagerProfession giverProfession,
            String issuingTier,
            int targetMinTierOffset,
            int targetMaxTierOffset,
            boolean sameMacroRegion,
            int searchRadiusBlocks,
            int targetRadiusBlocks,
            List<ResourceLocation> structureCandidates,
            int emeraldReward,
            int experienceReward
    ) {}
}
