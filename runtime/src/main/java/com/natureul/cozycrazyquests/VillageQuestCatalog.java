package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.ArrayList;
import java.util.List;

/**
 * Executable authored quest catalogue facade.
 *
 * The definitions are split by progression layer so the Bible can keep growing without turning one
 * source file into a wall of hundreds of quest rows. The facade preserves the original runtime API:
 * callers still ask for a quest by id or profession, while authored banks can grow independently.
 */
final class VillageQuestCatalog {
    private VillageQuestCatalog() {}

    static final List<Definition> ALL = allDefinitions();

    private static List<Definition> allDefinitions() {
        List<Definition> all = new ArrayList<>();
        all.addAll(HearthlandsQuestCatalog.ALL);
        all.addAll(FrontierQuestCatalog.ALL);
        all.addAll(FrontierSideQuestCatalog.ALL);
        return List.copyOf(all);
    }

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

    static Definition structure(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier, int maxTierOffset,
            int searchRadius, int targetRadius, String targetLabel, List<ResourceLocation> candidates, int kills,
            String dialogueStem, boolean revealAtlas, List<RewardStack> rewards, int emeralds, int xp
    ) {
        return structure(id, title, professions, category, tier, maxTierOffset, searchRadius, targetRadius,
                targetLabel, candidates, kills, dialogueStem, revealAtlas, rewards, emeralds, xp,
                id(dialogueStem + "_active"), id(dialogueStem + "_turnin"));
    }

    static Definition structure(
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

    static Definition structureClear(
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

    static Definition frontierCapstone(
            String id, String title, List<VillagerProfession> professions,
            int searchRadius, int targetRadius, String targetLabel, List<ResourceLocation> candidates, int kills,
            String dialogueStem, List<RewardStack> rewards, int emeralds, int xp
    ) {
        return new Definition(
                id, title, professions, null, true, ObjectiveType.STRUCTURE_HOSTILE_CLEAR,
                "FRONTIER", 1, 1, true, searchRadius, targetRadius, 0, 0, LocalTerrain.ANY,
                targetLabel, candidates, kills,
                id(dialogueStem + "_offer"), id(dialogueStem + "_active"), id(dialogueStem + "_turnin"),
                true, List.of(), rewards, emeralds, xp
        );
    }

    static Definition local(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier,
            int targetRadius, int minDistance, int maxDistance, LocalTerrain terrain, String targetLabel, int kills,
            String dialogueStem, List<RewardStack> rewards, int emeralds, int xp
    ) {
        return local(id, title, professions, category, tier, targetRadius, minDistance, maxDistance, terrain,
                targetLabel, kills, dialogueStem, rewards, emeralds, xp, List.of());
    }

    static Definition local(
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
    static List<VillagerProfession> profs(VillagerProfession... professions) {
        return List.of(professions);
    }

    static List<ResourceLocation> structures(String... ids) {
        return java.util.Arrays.stream(ids).map(ResourceLocation::new).toList();
    }

    static List<RewardStack> rewards(RewardStack... rewards) {
        return List.of(rewards);
    }

    static RewardStack reward(String id, int count, String name) {
        return new RewardStack(new ResourceLocation(id), count, name);
    }

    static RewardEnchant enchant(String id, int level) {
        return new RewardEnchant(new ResourceLocation(id), level);
    }

    static RewardStack enchanted(String itemId, int count, String name, String enchantId, int level) {
        return new RewardStack(new ResourceLocation(itemId), count, name,
                List.of(new RewardEnchant(new ResourceLocation(enchantId), level)));
    }

    static RewardStack multiEnchanted(String itemId, int count, String name, RewardEnchant... enchants) {
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
