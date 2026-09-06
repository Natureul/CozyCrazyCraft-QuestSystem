package com.natureul.cozycrazyquests;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves authored quest targets against the actual generated-world structure registry.
 *
 * This class intentionally does no background polling. A locate is performed only when an NPC
 * needs to decide whether a structure-dependent quest can be offered, and callers cache the
 * result. That keeps "villagers know what's near them" from becoming another permanent tick cost.
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

        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> holders = new ArrayList<>();

        for (ResourceLocation id : structureIds) {
            ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, id);
            Optional<Holder.Reference<Structure>> holder = registry.getHolder(key);
            holder.ifPresent(holders::add);
        }
        if (holders.isEmpty()) return null;

        int radiusChunks = Math.max(1, (maxDistanceBlocks + 15) / 16);
        Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator().findNearestMapStructure(
                level,
                HolderSet.direct(holders),
                origin,
                radiusChunks,
                false
        );
        if (result == null) return null;

        BlockPos pos = result.getFirst();
        long dx = (long) pos.getX() - origin.getX();
        long dz = (long) pos.getZ() - origin.getZ();
        long distanceSq = dx * dx + dz * dz;
        if (distanceSq > (long) maxDistanceBlocks * maxDistanceBlocks) return null;

        ResourceLocation resolvedId = registry.getKey(result.getSecond().value());
        if (resolvedId == null) return null;

        return new ResolvedStructure(resolvedId, pos.immutable(), (int) Math.round(Math.sqrt(distanceSq)));
    }

    record ResolvedStructure(ResourceLocation id, BlockPos pos, int distanceBlocks) {}
}
