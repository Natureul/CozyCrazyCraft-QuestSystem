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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection-only bridge into CozyCrazyZones' persistent named-place and Atlas systems.
 *
 * CozyCrazyZones already owns the world-global identity of villages and named structures. Quests
 * must reuse that identity rather than inventing a second name for the same place. Keeping this
 * bridge reflective preserves the quest runtime's graceful fallback when CozyCrazyZones is absent
 * or a future version changes an integration surface.
 */
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

            Class<?> profileClass = Class.forName("com.natureul.cozycrazyzones.StructureDiscoveryProfile");
            Method classify = profileClass.getMethod("classify", Registry.class, Structure.class, ResourceLocation.class);
            Object profile = classify.invoke(null, registry, structure, structureId);
            if (profile == null) return fallbackStructureName(structureId);

            Class<?> zonesApi = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method regionalCellAt = zonesApi.getMethod("regionalCellAt", ServerLevel.class, double.class, double.class);
            Object cell = regionalCellAt.invoke(null, level, locatedPos.getX() + 0.5D, locatedPos.getZ() + 0.5D);
            if (cell == null) return fallbackStructureName(structureId);

            Class<?> namesClass = Class.forName("com.natureul.cozycrazyzones.StructureNameSavedData");
            Method get = namesClass.getMethod("get", ServerLevel.class);
            Object names = get.invoke(null, level);
            Method getOrAssign = namesClass.getMethod(
                    "getOrAssign",
                    profileClass,
                    cell.getClass(),
                    long.class,
                    ResourceLocation.class,
                    ChunkPos.class
            );
            Object value = getOrAssign.invoke(
                    names,
                    profile,
                    cell,
                    level.getSeed(),
                    structureId,
                    new ChunkPos(locatedPos)
            );
            if (value instanceof String name && !name.isBlank()) return name;
        } catch (Throwable error) {
            if (!warnedStructureName) {
                warnedStructureName = true;
                CozyCrazyQuests.LOGGER.warn(
                        "Could not resolve a CozyCrazyZones structure place-name; quest will use a generic landmark name",
                        error
                );
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
            Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator().findNearestMapStructure(
                    level,
                    villages.get(),
                    origin,
                    radiusChunks,
                    false
            );
            if (result == null) return "the village";

            BlockPos start = result.getFirst();
            long dx = (long) start.getX() - origin.getX();
            long dz = (long) start.getZ() - origin.getZ();
            if (dx * dx + dz * dz > (long) maxDistanceBlocks * maxDistanceBlocks) return "the village";

            Class<?> zonesApi = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method macroRegionAt = zonesApi.getMethod("macroRegionAt", ServerLevel.class, double.class, double.class);
            Object macro = macroRegionAt.invoke(null, level, start.getX() + 0.5D, start.getZ() + 0.5D);
            if (macro == null) return "the village";

            Class<?> namesClass = Class.forName("com.natureul.cozycrazyzones.VillageNameSavedData");
            Method get = namesClass.getMethod("get", ServerLevel.class);
            Object names = get.invoke(null, level);
            Method getOrAssign = namesClass.getMethod(
                    "getOrAssign",
                    macro.getClass(),
                    long.class,
                    ChunkPos.class
            );
            Object value = getOrAssign.invoke(names, macro, level.getSeed(), new ChunkPos(start));
            if (value instanceof String name && !name.isBlank()) return name;
        } catch (Throwable error) {
            if (!warnedVillageName) {
                warnedVillageName = true;
                CozyCrazyQuests.LOGGER.warn(
                        "Could not resolve the CozyCrazyZones village name for an authored quest",
                        error
                );
            }
        }
        return "the village";
    }

    /**
     * Reveal an already-resolved quest target through CozyCrazyZones' own Atlas marker ledger.
     * The marker is persisted even if the player is not currently holding an Atlas; the Zones
     * service installs pending markers when an Atlas becomes available.
     */
    static boolean revealStructureToAtlas(
            ServerPlayer player,
            ResourceLocation structureId,
            BlockPos locatedPos,
            String name
    ) {
        try {
            ServerLevel level = player.serverLevel();
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Structure structure = registry.get(structureId);
            if (structure == null) return false;

            Class<?> profileClass = Class.forName("com.natureul.cozycrazyzones.StructureDiscoveryProfile");
            Method classify = profileClass.getMethod("classify", Registry.class, Structure.class, ResourceLocation.class);
            Object profile = classify.invoke(null, registry, structure, structureId);
            if (profile == null) return false;

            Class<?> zonesApi = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method regionalCellAt = zonesApi.getMethod("regionalCellAt", ServerLevel.class, double.class, double.class);
            Object cell = regionalCellAt.invoke(null, level, locatedPos.getX() + 0.5D, locatedPos.getZ() + 0.5D);
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
            String discoveryKey = String.valueOf(keyFor.invoke(null, structureId, new ChunkPos(locatedPos)));

            Class<?> markerService = Class.forName("com.natureul.cozycrazyzones.AtlasDiscoveryMarkerService");
            Method enqueue = markerService.getMethod(
                    "enqueue",
                    ServerPlayer.class,
                    String.class,
                    category.getClass(),
                    String.class,
                    BlockPos.class,
                    MapDecoration.Type.class
            );
            enqueue.invoke(null, player, discoveryKey, category, name, locatedPos, mapIcon);
            return true;
        } catch (Throwable error) {
            if (!warnedAtlas) {
                warnedAtlas = true;
                CozyCrazyQuests.LOGGER.warn(
                        "Could not reveal an authored quest target through the CozyCrazyZones Atlas marker service",
                        error
                );
            }
            return false;
        }
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
}
