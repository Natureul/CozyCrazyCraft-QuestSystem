package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

/**
 * Exact kill-credit policy for authored hunts.
 *
 * Ordinary structure-clear contracts intentionally count any monster at the assigned place. Great Hunts
 * are different: a Frostmaw contract must not complete because the player killed a zombie beside the
 * Frostmaw den. Keeping this semantic policy separate from the quest row also lets us refuse to enable
 * encounters whose actual stable entity form has not been audited yet (for example the western
 * Pumpkinhead chains).
 */
final class ExactHuntRegistry {
    private static final Map<String, Spec> BY_QUEST = Map.of(
            "north_w3_sleeping_mountain", new Spec(
                    "Frostmaw",
                    ids("mowziesmobs:frostmaw")
            ),
            "south_w3_sunbird", new Spec(
                    "Umvuthi",
                    ids("mowziesmobs:umvuthi")
            )
    );

    private ExactHuntRegistry() {}

    static Spec forQuest(String questId) {
        return BY_QUEST.get(questId);
    }

    static boolean counts(String questId, EntityType<?> killedType) {
        Spec spec = forQuest(questId);
        return spec == null || spec.matches(killedType);
    }

    private static List<ResourceLocation> ids(String... ids) {
        return java.util.Arrays.stream(ids).map(ResourceLocation::new).toList();
    }

    record Spec(String targetLabel, List<ResourceLocation> entityIds) {
        boolean matches(EntityType<?> type) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            return id != null && entityIds.contains(id);
        }
    }
}
