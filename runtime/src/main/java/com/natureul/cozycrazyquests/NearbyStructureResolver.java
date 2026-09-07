package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * Resolves authored quest targets against the persistent index of real generated structure starts.
 *
 * The 0.4.0 field log showed synchronous structure-locate / Structure Gel paths occurring during quest
 * target resolution. The captured watchdog stall itself was a teleport/chunk-load stall, so it would be
 * incorrect to attribute that watchdog to this quest code. Even so, routine NPC interaction should not
 * be allowed to invoke broad worldgen searches when a bounded generated-start index can answer safely.
 *
 * GeneratedStructureIndexSavedData is populated when real chunks load/generate and persists those starts.
 * This resolver is therefore a bounded in-memory search: no chunk generation, no broad locate, no N+1
 * candidate retry fan-out. If a landmark has not actually been generated/indexed, callers simply treat
 * that structure-dependent offer as unavailable and continue to another quest.
 */
final class NearbyStructureResolver {
    private NearbyStructureResolver() {}

    static ResolvedStructure findNearest(
            ServerLevel level,
            BlockPos origin,
            List<ResourceLocation> structureIds,
            int maxDistanceBlocks
    ) {
        if (structureIds == null || structureIds.isEmpty()) return null;
        if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) return null;
        return GeneratedStructureIndexSavedData.get(level).findNearest(origin, structureIds, maxDistanceBlocks);
    }

    record ResolvedStructure(ResourceLocation id, BlockPos pos, int distanceBlocks) {}
}
