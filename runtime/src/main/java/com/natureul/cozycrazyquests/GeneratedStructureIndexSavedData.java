package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persistent index of structure starts that the world has actually generated/loaded.
 *
 * Authored quests must never call Minecraft/Structure Gel's broad structure locator from an ordinary
 * villager click. That path can synchronously perform generation checks for minutes. Chunk load already
 * gives us authoritative generated StructureStart data at essentially zero additional worldgen cost, so
 * we remember those starts once and quest target selection becomes a bounded in-memory nearest search.
 *
 * This index deliberately contains only real generated starts. If a requested landmark has not been
 * generated/indexed yet, the structure-dependent offer is unavailable and the village falls through to
 * other work rather than freezing the integrated server trying to prove a hypothetical future structure.
 */
final class GeneratedStructureIndexSavedData extends SavedData {
    private static final String DATA_NAME = "cozycrazyquests_generated_structure_index_v1";
    private static final String ENTRIES = "entries";

    private final Map<String, Entry> byKey = new HashMap<>();
    private final Map<ResourceLocation, List<Entry>> byStructure = new HashMap<>();

    private GeneratedStructureIndexSavedData() {}

    private static GeneratedStructureIndexSavedData load(CompoundTag tag) {
        GeneratedStructureIndexSavedData data = new GeneratedStructureIndexSavedData();
        ListTag list = tag.getList(ENTRIES, Tag.TAG_COMPOUND);
        for (Tag raw : list) {
            if (!(raw instanceof CompoundTag entryTag)) continue;
            ResourceLocation id = ResourceLocation.tryParse(entryTag.getString("structure"));
            if (id == null) continue;
            Entry entry = new Entry(
                    id,
                    entryTag.getInt("start_x"),
                    entryTag.getInt("start_z"),
                    new BlockPos(entryTag.getInt("x"), entryTag.getInt("y"), entryTag.getInt("z"))
            );
            data.putLoaded(entry);
        }
        return data;
    }

    static GeneratedStructureIndexSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                GeneratedStructureIndexSavedData::load,
                GeneratedStructureIndexSavedData::new,
                DATA_NAME
        );
    }

    static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ChunkAccess chunk = event.getChunk();
        Map<Structure, StructureStart> starts = chunk.getAllStarts();
        if (starts.isEmpty()) return;

        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        GeneratedStructureIndexSavedData data = get(level);
        boolean changed = false;
        for (Map.Entry<Structure, StructureStart> raw : starts.entrySet()) {
            StructureStart start = raw.getValue();
            if (start == null || !start.isValid()) continue;
            ResourceLocation id = registry.getKey(raw.getKey());
            if (id == null) continue;
            changed |= data.remember(id, start);
        }
        if (changed) data.setDirty();
    }

    NearbyStructureResolver.ResolvedStructure findNearest(
            BlockPos origin,
            Collection<ResourceLocation> candidates,
            int maxDistanceBlocks
    ) {
        if (candidates == null || candidates.isEmpty()) return null;
        long maxSq = (long) maxDistanceBlocks * maxDistanceBlocks;
        Entry nearest = null;
        long nearestSq = Long.MAX_VALUE;

        for (ResourceLocation id : candidates) {
            for (Entry entry : byStructure.getOrDefault(id, List.of())) {
                long dx = (long) entry.pos().getX() - origin.getX();
                long dz = (long) entry.pos().getZ() - origin.getZ();
                long sq = dx * dx + dz * dz;
                if (sq > maxSq || sq >= nearestSq) continue;
                nearest = entry;
                nearestSq = sq;
            }
        }
        if (nearest == null) return null;
        return new NearbyStructureResolver.ResolvedStructure(
                nearest.structureId(),
                nearest.pos(),
                (int) Math.round(Math.sqrt(nearestSq))
        );
    }

    int countFor(Set<ResourceLocation> candidates) {
        int count = 0;
        for (ResourceLocation id : candidates) count += byStructure.getOrDefault(id, List.of()).size();
        return count;
    }

    private boolean remember(ResourceLocation id, StructureStart start) {
        ChunkPos startChunk = start.getChunkPos();
        BoundingBox box = start.getBoundingBox();
        BlockPos center = new BlockPos(
                (box.minX() + box.maxX()) / 2,
                (box.minY() + box.maxY()) / 2,
                (box.minZ() + box.maxZ()) / 2
        );
        Entry entry = new Entry(id, startChunk.x, startChunk.z, center);
        String key = key(entry);
        Entry previous = byKey.get(key);
        if (entry.equals(previous)) return false;
        if (previous != null) removeLoaded(previous);
        putLoaded(entry);
        return true;
    }

    private void putLoaded(Entry entry) {
        byKey.put(key(entry), entry);
        byStructure.computeIfAbsent(entry.structureId(), ignored -> new ArrayList<>()).add(entry);
    }

    private void removeLoaded(Entry entry) {
        List<Entry> entries = byStructure.get(entry.structureId());
        if (entries == null) return;
        entries.remove(entry);
        if (entries.isEmpty()) byStructure.remove(entry.structureId());
    }

    private static String key(Entry entry) {
        return entry.structureId() + "@" + entry.startChunkX() + "," + entry.startChunkZ();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Entry entry : byKey.values()) {
            CompoundTag out = new CompoundTag();
            out.putString("structure", entry.structureId().toString());
            out.putInt("start_x", entry.startChunkX());
            out.putInt("start_z", entry.startChunkZ());
            out.putInt("x", entry.pos().getX());
            out.putInt("y", entry.pos().getY());
            out.putInt("z", entry.pos().getZ());
            list.add(out);
        }
        tag.put(ENTRIES, list);
        return tag;
    }

    private record Entry(ResourceLocation structureId, int startChunkX, int startChunkZ, BlockPos pos) {}
}
