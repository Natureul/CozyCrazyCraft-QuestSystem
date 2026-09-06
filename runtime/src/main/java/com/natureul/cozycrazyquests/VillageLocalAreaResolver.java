package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/** Resolve a repeatably named local quest area without creating a fake persistent Atlas landmark. */
final class VillageLocalAreaResolver {
    private static final int[][] WATER_PROBES = {
            {8, 0}, {-8, 0}, {0, 8}, {0, -8},
            {14, 0}, {-14, 0}, {0, 14}, {0, -14},
            {10, 10}, {10, -10}, {-10, 10}, {-10, -10},
            {22, 0}, {-22, 0}, {0, 22}, {0, -22}
    };

    private VillageLocalAreaResolver() {}

    static LocalTarget find(ServerLevel level, VillageContext village, VillageQuestCatalog.Definition definition) {
        int min = Math.max(48, definition.localTargetMinDistance());
        int max = Math.max(min, definition.localTargetMaxDistance());
        long hash = mix(level.getSeed(), village.key().hashCode(), definition.id().hashCode());

        // Water-edge jobs need a few more attempts because they are intentionally withheld from dry villages.
        int attempts = definition.localTerrain() == VillageQuestCatalog.LocalTerrain.WATER_EDGE ? 40 : 16;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int octant = Math.floorMod((int) (hash + attempt * 3L), 8);
            int span = Math.max(1, max - min + 1);
            int distance = min + Math.floorMod((int) (hash >>> (attempt % 24)), span);
            double angle = octant * (Math.PI / 4.0);
            int x = village.anchor().getX() + (int) Math.round(Math.sin(angle) * distance);
            int z = village.anchor().getZ() - (int) Math.round(Math.cos(angle) * distance);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            if (!level.getWorldBorder().isWithinBounds(pos)) continue;
            if (!level.getFluidState(pos.below()).isEmpty()) continue; // quest point itself must be standing ground
            ZoneBridge.Cell targetCell = ZoneBridge.cellAt(level, pos);
            if (!legalCell(village.cell(), targetCell, definition)) continue;
            if (definition.localTerrain() == VillageQuestCatalog.LocalTerrain.WATER_EDGE && !hasWaterNear(level, pos)) continue;

            int actualDistance = (int) Math.round(Math.sqrt(village.anchor().distSqr(pos)));
            return new LocalTarget(pos, actualDistance, definition.targetLabel());
        }
        return null;
    }

    private static boolean hasWaterNear(ServerLevel level, BlockPos center) {
        for (int[] probe : WATER_PROBES) {
            int x = center.getX() + probe[0];
            int z = center.getZ() + probe[1];
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos top = new BlockPos(x, Math.max(level.getMinBuildHeight(), y - 1), z);
            if (!level.getFluidState(top).isEmpty()) return true;
        }
        return false;
    }

    private static boolean legalCell(
            ZoneBridge.Cell villageCell,
            ZoneBridge.Cell targetCell,
            VillageQuestCatalog.Definition definition
    ) {
        if (!villageCell.known() || !targetCell.known()) return false;
        int issuingRank = ZoneBridge.tierRank(villageCell.tier());
        int targetRank = ZoneBridge.tierRank(targetCell.tier());
        if (issuingRank < 0 || targetRank < 0) return false;
        if (targetRank < issuingRank + definition.targetMinTierOffset()) return false;
        if (targetRank > issuingRank + definition.targetMaxTierOffset()) return false;
        return !definition.sameMacroRegion()
                || "SHARED_CORE".equals(villageCell.band())
                || villageCell.macro().equals(targetCell.macro());
    }

    private static long mix(long seed, int a, int b) {
        long value = seed ^ (0x9E3779B97F4A7C15L * a) ^ (0xC2B2AE3D27D4EB4FL * b);
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        return value;
    }

    record LocalTarget(BlockPos pos, int distanceBlocks, String label) {}
}
