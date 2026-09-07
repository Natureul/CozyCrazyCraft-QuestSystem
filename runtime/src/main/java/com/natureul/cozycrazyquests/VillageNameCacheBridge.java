package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Read-only, generation-free bridge into CozyCrazyZones' already-assigned village-name ledger.
 *
 * NPC interaction must never call findNearestMapStructure merely to obtain a settlement name: that
 * operation can synchronously force structure/chunk work and turn a right click into a multi-second
 * server stall. CozyCrazyZones already persists village names by structure-start ChunkPos, so this
 * bridge scans that in-memory ledger only. Missing names simply fall back to "the village" until a
 * later cache refresh; conversation is more important than eagerly inventing/loading geography.
 */
final class VillageNameCacheBridge {
    private static final long CACHE_LIFETIME = 1200L;
    private static final Map<String, CachedName> CACHE = new HashMap<>();
    private static boolean warned;

    private VillageNameCacheBridge() {}

    static String nearestAssigned(ServerLevel level, BlockPos origin, int maxDistanceBlocks) {
        String cacheKey = level.dimension().location() + "|"
                + Math.floorDiv(origin.getX(), 64) + "," + Math.floorDiv(origin.getZ(), 64);
        CachedName cached = CACHE.get(cacheKey);
        if (cached != null && level.getGameTime() - cached.checkedAt() <= CACHE_LIFETIME) {
            return cached.name();
        }

        String found = lookup(level, origin, maxDistanceBlocks);
        CACHE.put(cacheKey, new CachedName(found, level.getGameTime()));
        return found;
    }

    private static String lookup(ServerLevel level, BlockPos origin, int maxDistanceBlocks) {
        try {
            Class<?> namesClass = Class.forName("com.natureul.cozycrazyzones.VillageNameSavedData");
            Method get = namesClass.getMethod("get", ServerLevel.class);
            Method getIfAssigned = namesClass.getMethod("getIfAssigned", ChunkPos.class);
            Object names = get.invoke(null, level);

            ChunkPos center = new ChunkPos(origin);
            int radiusChunks = Math.max(1, (maxDistanceBlocks + 15) / 16);
            long maxSq = (long) maxDistanceBlocks * maxDistanceBlocks;
            long bestSq = Long.MAX_VALUE;
            String best = null;

            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
                    ChunkPos candidate = new ChunkPos(center.x + dx, center.z + dz);
                    Object value = getIfAssigned.invoke(names, candidate);
                    if (!(value instanceof String name) || name.isBlank()) continue;

                    long bx = candidate.getMiddleBlockX();
                    long bz = candidate.getMiddleBlockZ();
                    long ddx = bx - origin.getX();
                    long ddz = bz - origin.getZ();
                    long distanceSq = ddx * ddx + ddz * ddz;
                    if (distanceSq <= maxSq && distanceSq < bestSq) {
                        bestSq = distanceSq;
                        best = name;
                    }
                }
            }
            return best == null ? "the village" : best;
        } catch (Throwable error) {
            if (!warned) {
                warned = true;
                CozyCrazyQuests.LOGGER.warn(
                        "Could not read CozyCrazyZones' assigned village-name ledger; using generic village names",
                        error
                );
            }
            return "the village";
        }
    }

    private record CachedName(String name, long checkedAt) {}
}
