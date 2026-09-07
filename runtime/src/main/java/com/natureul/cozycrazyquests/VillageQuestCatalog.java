package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.ArrayList;
import java.util.List;

/**
 * Executable authored quest catalogue facade.
 *
 * Regional banks are deliberately excluded from the legacy profession-only lookup. A method that has
 * no village cell cannot safely decide whether west/east/north/south content belongs in the current
 * settlement. Region-scoped work is exposed through region-aware helpers instead, preventing a western
 * story from leaking into another macro merely because the same structure mod generated there.
 */
final class VillageQuestCatalog {
    private VillageQuestCatalog() {}

    static final List<Definition> ALL = allDefinitions();

    private static List<Definition> allDefinitions() {
        List<Definition> all = new ArrayList<>();
        all.addAll(HearthlandsQuestCatalog.ALL);
        all.addAll(HearthlandsRecoveryQuestCatalog.ALL);
        all.addAll(FrontierQuestCatalog.ALL);
        all.addAll(FrontierSideQuestCatalog.ALL);
        all.addAll(WildlandsQuestCatalog.ALL);
        all.addAll(HarvestwoodQuestCatalog.ALL);
        return List.copyOf(all);
    }

    static Definition byId(String id) {
        for (Definition definition : ALL) if (definition.id.equals(id)) return definition;
        return null;
    }

    /** Legacy/global authored bank. Region-prefixed definitions require a village cell and are excluded. */
    static List<Definition> forProfession(VillagerProfession profession) {
        return ALL.stream()
                .filter(definition -> !isRegionScoped(definition))
                .filter(definition -> definition.giverProfessions.contains(profession))
                .toList();
    }

    static List<Definition> regionalForProfession(VillagerProfession profession, ZoneBridge.Cell cell) {
        return ALL.stream()
                .filter(VillageQuestCatalog::isRegionScoped)
                .filter(definition -> definition.giverProfessions.contains(profession))
                .filter(definition -> issuesInCell(definition, cell))
                .toList();
    }

    static List<Definition> civicCandidates(ZoneBridge.Cell cell) {
        return ALL.stream()
                .filter(definition -> issuesInCell(definition, cell))
                .toList();
    }

    static boolean issuesInCell(Definition definition, ZoneBridge.Cell cell) {
        if (definition == null || cell == null || !cell.known()) return false;
        if (!definition.issuingTier.equals(cell.tier())) return false;
        String id = definition.id;
        if (id.startsWith("west_")) return "WEST".equals(cell.macro());
        if (id.startsWith("east_")) return "EAST".equals(cell.macro());
        if (id.startsWith("north_")) return "NORTH".equals(cell.macro());
        if (id.startsWith("south_")) return "SOUTH".equals(cell.macro());
        return true;
    }

    static boolean isRegionScoped(Definition definition) {
        if (definition == null) return false;
        String id = definition.id;
        return id.startsWith("west_") || id.startsWith("east_")
                || id.startsWith("north_") || id.startsWith("south_");
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
                targetLabel, "", candidates, kills,
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
                targetLabel, "", candidates, kills,
                id(dialogueStem + "_offer"), id(dialogueStem + "_active"), id(dialogueStem + "_turnin"),
                false, List.of(), rewards, emeralds, xp
        );
    }

    static Definition recovery(
            String id, String title, List<VillagerProfession> professions,
            VillageProgressState.AccomplishmentCategory category, String tier, int maxTierOffset,
            int searchRadius, int targetRadius, String targetLabel, List<ResourceLocation> candidates,
            String recoveryObjectName, String dialogueStem, boolean revealAtlas,
            List<RewardStack> rewards, int emeralds, int xp
    ) {
        return new Definition(
                id, title, professions, category, false, ObjectiveType.STRUCTURE_SURVEY,
                tier, 0, maxTierOffset, true, searchRadius, targetRadius, 0, 0, LocalTerrain.ANY,
                targetLabel, recoveryObjectName, candidates, 0,
                id(dialogueStem + "_offer"), id(dialogueStem + "_active"), id(dialogueStem + "_turnin"),
                revealAtlas, List.of(), rewards, emeralds, xp
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
                targetLabel, "", candidates, kills,
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
                targetLabel, "", List.of(), kills,
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
            String recoveryObjectName,
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
        Definition(
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
            this(
                    id, title, giverProfessions, accomplishmentCategory, zoneOneCapstone, objectiveType,
                    issuingTier, targetMinTierOffset, targetMaxTierOffset, sameMacroRegion,
                    searchRadiusBlocks, targetRadiusBlocks, localTargetMinDistance, localTargetMaxDistance,
                    localTerrain, targetLabel, "", structureCandidates, requiredKills,
                    offerDialogue, activeDialogue, turninDialogue, revealAtlasOnAccept,
                    acceptanceItems, rewardItems, emeraldReward, experienceReward
            );
        }

        boolean accepts(VillagerProfession profession) {
            return giverProfessions.contains(profession);
        }

        boolean isRecovery() {
            return !recoveryObjectName.isBlank();
        }
    }
}
