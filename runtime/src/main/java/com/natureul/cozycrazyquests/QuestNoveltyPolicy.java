package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;

import java.util.Locale;

/**
 * Small authored-quest recency policy for one village/player relationship.
 *
 * This is deliberately a ranking policy, not a hard lock. A village should strongly prefer a different
 * generated structure instance and a different primary reward family after a turn-in, but an unusually
 * sparse generated world should not become permanently questless merely because the only legal site is
 * the one the player visited last. Exact target repetition is penalized much more heavily than reward
 * repetition so the Amber-Rest-style "go straight back to the same place" sequence loses first.
 */
final class QuestNoveltyPolicy {
    private static final int SAME_REWARD_FAMILY_PENALTY = 10;
    private static final int SAME_TARGET_INSTANCE_PENALTY = 100;

    private QuestNoveltyPolicy() {}

    static int penalty(
            CompoundTag root,
            String villageKey,
            VillageQuestCatalog.Definition definition,
            String targetKey
    ) {
        CompoundTag recent = VillageQuestState.recentCompletion(root, villageKey);
        if (recent.isEmpty()) return 0;

        int penalty = 0;
        String previousTarget = recent.getString("target_key");
        if (targetKey != null && !targetKey.isBlank() && targetKey.equals(previousTarget)) {
            penalty += SAME_TARGET_INSTANCE_PENALTY;
        }

        String family = primaryRewardFamily(definition);
        String previousFamily = recent.getString("reward_family");
        if (!family.isBlank() && family.equals(previousFamily)) {
            penalty += SAME_REWARD_FAMILY_PENALTY;
        }
        return penalty;
    }

    static String primaryRewardFamily(VillageQuestCatalog.Definition definition) {
        if (definition == null || definition.rewardItems().isEmpty()) return "";
        String path = definition.rewardItems().get(0).itemId().getPath().toLowerCase(Locale.ROOT);

        if (containsAny(path, "saddle", "horse_armor", "lead")) return "RIDING";
        if (containsAny(path, "longbow", "crossbow", "bow", "boomerang", "javelin")) return "RANGED";
        if (containsAny(path,
                "sword", "spear", "pike", "lance", "halberd", "glaive", "battleaxe", "dagger",
                "katana", "saber", "rapier", "mace", "hammer", "scythe", "quarterstaff")) return "MELEE";
        if (containsAny(path, "helmet", "chestplate", "leggings", "boots", "shield", "hood", "coat")) return "ARMOR";
        if (containsAny(path, "pickaxe", "shovel", "hoe", "axe", "shears", "brush", "fishing_rod")) return "TOOL";
        if (containsAny(path, "compass", "map", "spyglass", "atlas", "glider")) return "NAVIGATION";
        if (containsAny(path, "thermometer", "waterskin", "water_bucket", "torch", "lantern")) return "SURVIVAL";
        if (containsAny(path, "apple", "carrot", "bread", "pie", "stew", "food")) return "FOOD";
        if (containsAny(path, "ingot", "diamond", "emerald", "obsidian", "paper", "leather")) return "RESOURCE";
        if (containsAny(path, "name_tag", "spawn_egg")) return "ECOLOGY";
        return "MISC:" + path;
    }

    private static boolean containsAny(String path, String... needles) {
        for (String needle : needles) if (path.contains(needle)) return true;
        return false;
    }
}
