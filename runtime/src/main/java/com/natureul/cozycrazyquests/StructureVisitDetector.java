package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Completion detector for authored structure visits.
 *
 * Vanilla /locate-style structure positions are good navigation anchors, but their Y coordinate is not a
 * reliable "stand here" coordinate for large or deeply generated structures. Some mod structures report a
 * start/reference point well below rooms the player is already physically exploring. Survey completion should
 * therefore prefer the generated StructureStart's actual bounding box and only fall back to a forgiving
 * underground-arrival envelope when a third-party structure cannot be resolved through StructureManager.
 */
final class StructureVisitDetector {
    private StructureVisitDetector() {}

    static boolean reached(
            ServerLevel level,
            BlockPos playerPos,
            ResourceLocation structureId,
            BlockPos locatePos,
            int horizontalRadius,
            String approach
    ) {
        if (structureId != null && insideGeneratedStructure(level, playerPos, structureId)) return true;

        long dx = (long) playerPos.getX() - locatePos.getX();
        long dz = (long) playerPos.getZ() - locatePos.getZ();
        if (dx * dx + dz * dz > (long) horizontalRadius * horizontalRadius) return false;

        if ("UNDERGROUND".equals(approach)) {
            // Fallback for mod structures whose StructureStart lookup does not expose the occupied room the
            // player is standing in. We intentionally do NOT require the locate Y. Being substantially below
            // the local surface in the correct horizontal footprint is enough to prove the player made the
            // underground approach, while standing on the surface above it is not.
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, playerPos.getX(), playerPos.getZ());
            return playerPos.getY() <= surfaceY - 10;
        }

        if ("SUBMERGED".equals(approach)) {
            // A submerged survey should count once the player is in/under the water column at the correct
            // footprint rather than requiring a locator's arbitrary Y reference.
            return !level.getFluidState(playerPos).isEmpty()
                    || !level.getFluidState(playerPos.above()).isEmpty();
        }

        return true;
    }

    private static boolean insideGeneratedStructure(ServerLevel level, BlockPos playerPos, ResourceLocation structureId) {
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureId);
        if (structure == null) return false;
        StructureStart start = level.structureManager().getStructureAt(playerPos, structure);
        return start != null && start.isValid();
    }
}
