package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * Resolves authored quest targets against the persistent index of real generated structure starts.
 *
 * The old implementation called ChunkGenerator.findNearestMapStructure synchronously from villager
 * interaction. A September 0.4.0 field test captured one Structure Gel locate holding the server thread
 * for roughly nine minutes. Normal NPC interaction must never enter that worldgen search again.
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
