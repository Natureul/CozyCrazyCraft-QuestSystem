package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reflection-only bridge into CozyCrazyZones' persistent named-place and Atlas systems. */
final class NamedPlaceBridge {
    private static boolean warnedStructureName;
    private static boolean warnedVillageName;
    private static boolean warnedAtlas;

    private NamedPlaceBridge() {}

    static String structureName(ServerLevel level, ResourceLocation structureId, BlockPos locatedPos) {
        try {
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Structure structure = registry.get(structureId);
            if (structure == null) return fallbackStructureName(structureId);

            StructureIdentity identity = structureIdentity(level, structure, locatedPos);

            Class<?> profileClass = Class.forName("com.natureul.cozycrazyzones.StructureDiscoveryProfile");
            Method classify = profileClass.getMethod("classify", Registry.class, Structure.class, ResourceLocation.class);
            Object profile = classify.invoke(null, registry, structure, structureId);
            if (profile == null) return fallbackStructureName(structureId);

            Class<?> zonesApi = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method regionalCellAt = zonesApi.getMethod("regionalCellAt", ServerLevel.class, double.class, double.class);
            Object cell = regionalCellAt.invoke(null, level, identity.markerPos().getX() + 0.5D, identity.markerPos().getZ() + 0.5D);
            if (cell == null) return fallbackStructureName(structureId);

            Class<?> namesClass = Class.forName("com.natureul.cozycrazyzones.StructureNameSavedData");
            Method get = namesClass.getMethod("get", ServerLevel.class);
            Object names = get.invoke(null, level);
            Method getOrAssign = namesClass.getMethod("getOrAssign", profileClass, cell.getClass(), long.class, ResourceLocation.class, ChunkPos.class);
            Object value = getOrAssign.invoke(names, profile, cell, level.getSeed(), structureId, identity.startChunk());
            if (value instanceof String name && !name.isBlank()) return name;
        } catch (Throwable error) {
            if (!warnedStructureName) {
                warnedStructureName = true;
                CozyCrazyQuests.LOGGER.warn("Could not resolve a CozyCrazyZones structure place-name; quest will use a generic landmark name", error);
            }
        }
        return fallbackStructureName(structureId);
    }

    /** Resolve nearby village naming from the persisted generated-start index; never invoke worldgen locate. */
    static String nearestVillageName(ServerLevel level, BlockPos origin, int maxDistanceBlocks) {
        try {
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Optional<HolderSet.Named<Structure>> villages = registry.getTag(StructureTags.VILLAGE);
            if (villages.isEmpty()) return "the village";

            List<ResourceLocation> villageIds = new ArrayList<>();
            for (Holder<Structure> holder : villages.get()) {
                ResourceLocation id = registry.getKey(holder.value());
                if (id != null) villageIds.add(id);
            }
            NearbyStructureResolver.ResolvedStructure result = GeneratedStructureIndexSavedData.get(level)
                    .findNearest(origin, villageIds, maxDistanceBlocks);
            if (result == null) return "the village";

            Structure structure = registry.get(result.id());
            if (structure == null) return "the village";
            StructureIdentity identity = structureIdentity(level, structure, result.pos());

            Class<?> zonesApi = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method macroRegionAt = zonesApi.getMethod("macroRegionAt", ServerLevel.class, double.class, double.class);
            Object macro = macroRegionAt.invoke(null, level, identity.markerPos().getX() + 0.5D, identity.markerPos().getZ() + 0.5D);
            if (macro == null) return "the village";

            Class<?> namesClass = Class.forName("com.natureul.cozycrazyzones.VillageNameSavedData");
            Method get = namesClass.getMethod("get", ServerLevel.class);
            Object names = get.invoke(null, level);
            Method getOrAssign = namesClass.getMethod("getOrAssign", macro.getClass(), long.class, ChunkPos.class);
            Object value = getOrAssign.invoke(names, macro, level.getSeed(), identity.startChunk());
            if (value instanceof String name && !name.isBlank()) return name;
        } catch (Throwable error) {
            if (!warnedVillageName) {
                warnedVillageName = true;
                CozyCrazyQuests.LOGGER.warn("Could not resolve the CozyCrazyZones village name for an authored quest", error);
            }
        }
        return "the village";
    }

    static boolean revealStructureToAtlas(ServerPlayer player, ResourceLocation structureId, BlockPos locatedPos, String name) {
        return revealStructureToAtlas(player, structureId, locatedPos, name, null);
    }

    /**
     * Reveal a generated structure while allowing quest code to supply a player-safe navigation anchor.
     * The discovery key still belongs to the exact generated structure start; only the rendered Atlas
     * position is overridden. This keeps objective identity separate from navigation geometry.
     */
    static boolean revealStructureToAtlas(
            ServerPlayer player,
            ResourceLocation structureId,
            BlockPos locatedPos,
            String name,
            BlockPos navigationAnchor
    ) {
        try {
            ServerLevel level = player.serverLevel();
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Structure structure = registry.get(structureId);
            if (structure == null) return false;

            StructureIdentity identity = structureIdentity(level, structure, locatedPos);
            BlockPos markerPos = navigationAnchor == null ? identity.markerPos() : navigationAnchor.immutable();

            Class<?> profileClass = Class.forName("com.natureul.cozycrazyzones.StructureDiscoveryProfile");
            Method classify = profileClass.getMethod("classify", Registry.class, Structure.class, ResourceLocation.class);
            Object profile = classify.invoke(null, registry, structure, structureId);
            if (profile == null) return false;

            Class<?> zonesApi = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method regionalCellAt = zonesApi.getMethod("regionalCellAt", ServerLevel.class, double.class, double.class);
            Object cell = regionalCellAt.invoke(null, level, markerPos.getX() + 0.5D, markerPos.getZ() + 0.5D);
            if (cell == null) return false;

            Method categoryAccessor = profileClass.getMethod("category");
            Object category = categoryAccessor.invoke(profile);
            if (category == null) return false;

            Class<?> symbolPolicy = Class.forName("com.natureul.cozycrazyzones.RegionalMapSymbolPolicy");
            Method iconFor = symbolPolicy.getMethod("iconFor", profileClass, cell.getClass());
            Object icon = iconFor.invoke(null, profile, cell);
            if (!(icon instanceof MapDecoration.Type mapIcon)) return false;

            Class<?> nameData = Class.forName("com.natureul.cozycrazyzones.StructureNameSavedData");
            Method keyFor = nameData.getMethod("keyFor", ResourceLocation.class, ChunkPos.class);
            String discoveryKey = String.valueOf(keyFor.invoke(null, structureId, identity.startChunk()));

            Class<?> markerService = Class.forName("com.natureul.cozycrazyzones.AtlasDiscoveryMarkerService");
            Method enqueue = markerService.getMethod("enqueue", ServerPlayer.class, String.class, category.getClass(), String.class, BlockPos.class, MapDecoration.Type.class);
            enqueue.invoke(null, player, discoveryKey, category, name, markerPos, mapIcon);
            return true;
        } catch (Throwable error) {
            if (!warnedAtlas) {
                warnedAtlas = true;
                CozyCrazyQuests.LOGGER.warn("Could not reveal an authored quest target through the CozyCrazyZones Atlas marker service", error);
            }
            return false;
        }
    }

    /**
     * Returns a surface navigation anchor on or immediately beside the generated structure footprint,
     * biased toward the caller's origin. This is intentionally not used as completion proof. It is a
     * route marker for underground/submerged/large structures whose locator or center would be misleading.
     */
    static BlockPos surfaceApproach(ServerLevel level, ResourceLocation structureId, BlockPos locatedPos, BlockPos from) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure structure = registry.get(structureId);
        if (structure == null) return surfaceAt(level, locatedPos.getX(), locatedPos.getZ());

        StructureStart start = level.structureManager().getStructureAt(locatedPos, structure);
        if (start == null || !start.isValid()) return surfaceAt(level, locatedPos.getX(), locatedPos.getZ());

        BoundingBox box = start.getBoundingBox();
        int x = clamp(from.getX(), box.minX(), box.maxX());
        int z = clamp(from.getZ(), box.minZ(), box.maxZ());

        if (from.getX() >= box.minX() && from.getX() <= box.maxX()
                && from.getZ() >= box.minZ() && from.getZ() <= box.maxZ()) {
            int west = from.getX() - box.minX();
            int east = box.maxX() - from.getX();
            int north = from.getZ() - box.minZ();
            int south = box.maxZ() - from.getZ();
            int nearest = Math.min(Math.min(west, east), Math.min(north, south));
            if (nearest == west) x = box.minX();
            else if (nearest == east) x = box.maxX();
            else if (nearest == north) z = box.minZ();
            else z = box.maxZ();
        }
        return surfaceAt(level, x, z);
    }

    static StructureInstance structureInstance(ServerLevel level, ResourceLocation structureId, BlockPos locatedPos) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure structure = registry.get(structureId);
        if (structure == null) return null;

        StructureStart start = level.structureManager().getStructureAt(locatedPos, structure);
        if (start == null || !start.isValid()) return null;

        BoundingBox box = start.getBoundingBox();
        BlockPos marker = new BlockPos(
                (box.minX() + box.maxX()) / 2,
                locatedPos.getY(),
                (box.minZ() + box.maxZ()) / 2
        );
        return new StructureInstance(start.getChunkPos(), marker);
    }

    static boolean insideExactStructure(
            ServerLevel level,
            BlockPos playerPos,
            ResourceLocation structureId,
            int expectedStartChunkX,
            int expectedStartChunkZ,
            BlockPos legacyLocatePos
    ) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure structure = registry.get(structureId);
        if (structure == null) return false;

        StructureStart current = level.structureManager().getStructureAt(playerPos, structure);
        if (current == null || !current.isValid()) return false;
        if (!insideAnyPiece(current, playerPos)) return false;

        ChunkPos currentStart = current.getChunkPos();
        if (expectedStartChunkX != Integer.MIN_VALUE && expectedStartChunkZ != Integer.MIN_VALUE) {
            return currentStart.x == expectedStartChunkX && currentStart.z == expectedStartChunkZ;
        }

        StructureIdentity expected = structureIdentity(level, structure, legacyLocatePos);
        if (expected.startChunk().equals(currentStart)) return true;

        BoundingBox box = current.getBoundingBox();
        long cx = ((long) box.minX() + box.maxX()) / 2L;
        long cz = ((long) box.minZ() + box.maxZ()) / 2L;
        long dx = cx - legacyLocatePos.getX();
        long dz = cz - legacyLocatePos.getZ();
        return dx * dx + dz * dz <= 192L * 192L;
    }

    private static boolean insideAnyPiece(StructureStart start, BlockPos pos) {
        for (var piece : start.getPieces()) {
            if (piece.getBoundingBox().isInside(pos)) return true;
        }
        return false;
    }

    private static BlockPos surfaceAt(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        return new BlockPos(x, Math.max(level.getMinBuildHeight() + 1, y), z);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static StructureIdentity structureIdentity(ServerLevel level, Structure structure, BlockPos locatedPos) {
        StructureStart start = level.structureManager().getStructureAt(locatedPos, structure);
        if (start != null && start.isValid()) {
            BoundingBox box = start.getBoundingBox();
            BlockPos marker = new BlockPos((box.minX() + box.maxX()) / 2, locatedPos.getY(), (box.minZ() + box.maxZ()) / 2);
            return new StructureIdentity(start.getChunkPos(), marker);
        }
        return new StructureIdentity(new ChunkPos(locatedPos), locatedPos.immutable());
    }

    private static String fallbackStructureName(ResourceLocation id) {
        String[] words = id.getPath().replace('/', '_').split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.isEmpty() ? "local landmark" : out.toString();
    }

    record StructureInstance(ChunkPos startChunk, BlockPos markerPos) {}
    private record StructureIdentity(ChunkPos startChunk, BlockPos markerPos) {}
}
