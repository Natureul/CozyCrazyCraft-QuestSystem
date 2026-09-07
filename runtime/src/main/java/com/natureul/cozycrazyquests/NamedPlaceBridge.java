package com.natureul.cozycrazyquests;

import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.lang.reflect.Method;
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

    static String nearestVillageName(ServerLevel level, BlockPos origin, int maxDistanceBlocks) {
        try {
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Optional<HolderSet.Named<Structure>> villages = registry.getTag(StructureTags.VILLAGE);
            if (villages.isEmpty()) return "the village";

            int radiusChunks = Math.max(1, (maxDistanceBlocks + 15) / 16);
            Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator().findNearestMapStructure(level, villages.get(), origin, radiusChunks, false);
            if (result == null) return "the village";

            BlockPos locatedPos = result.getFirst();
            long dx = (long) locatedPos.getX() - origin.getX();
            long dz = (long) locatedPos.getZ() - origin.getZ();
            if (dx * dx + dz * dz > (long) maxDistanceBlocks * maxDistanceBlocks) return "the village";

            StructureIdentity identity = structureIdentity(level, result.getSecond().value(), locatedPos);

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
        try {
            ServerLevel level = player.serverLevel();
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Structure structure = registry.get(structureId);
            if (structure == null) return false;

            StructureIdentity identity = structureIdentity(level, structure, locatedPos);

            Class<?> profileClass = Class.forName("com.natureul.cozycrazyzones.StructureDiscoveryProfile");
            Method classify = profileClass.getMethod("classify", Registry.class, Structure.class, ResourceLocation.class);
            Object profile = classify.invoke(null, registry, structure, structureId);
            if (profile == null) return false;

            Class<?> zonesApi = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method regionalCellAt = zonesApi.getMethod("regionalCellAt", ServerLevel.class, double.class, double.class);
            Object cell = regionalCellAt.invoke(null, level, identity.markerPos().getX() + 0.5D, identity.markerPos().getZ() + 0.5D);
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
            enqueue.invoke(null, player, discoveryKey, category, name, identity.markerPos(), mapIcon);
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
     * Stable identity for the exact generated structure instance represented by a locate result.
     *
     * This method is intentionally strict. If StructureManager cannot prove that the locator position
     * belongs to a real StructureStart, return null instead of fabricating a start chunk from the locate
     * point. A fabricated identity can make a perfectly valid modded structure impossible to complete
     * when its /locate navigation point sits outside its actual bounding box.
     */
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

    /**
     * True only when the player is physically inside the exact generated structure instance assigned
     * to the quest. This mirrors CozyCrazyZones discovery logic and avoids brittle Y-distance checks
     * for underground structures whose locate coordinate may be nowhere near the room the player enters.
     */
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
        if (current == null || !current.isValid() || !current.getBoundingBox().isInside(playerPos)) return false;

        ChunkPos currentStart = current.getChunkPos();
        if (expectedStartChunkX != Integer.MIN_VALUE && expectedStartChunkZ != Integer.MIN_VALUE) {
            return currentStart.x == expectedStartChunkX && currentStart.z == expectedStartChunkZ;
        }

        // Compatibility for contracts accepted by older builds or locators that could not prove an
        // exact start chunk at offer time.
        StructureIdentity expected = structureIdentity(level, structure, legacyLocatePos);
        if (expected.startChunk().equals(currentStart)) return true;

        // Some modded locators return a navigation position outside the actual bounding box. If the
        // player is inside a matching instance whose box is still centered near the original locate,
        // accept it rather than making a legitimate discovery impossible to report.
        BoundingBox box = current.getBoundingBox();
        long cx = ((long) box.minX() + box.maxX()) / 2L;
        long cz = ((long) box.minZ() + box.maxZ()) / 2L;
        long dx = cx - legacyLocatePos.getX();
        long dz = cz - legacyLocatePos.getZ();
        return dx * dx + dz * dz <= 192L * 192L;
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
