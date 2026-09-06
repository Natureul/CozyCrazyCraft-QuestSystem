package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistent per-dimension memory of villages whose civic bounty-board requirement has already
 * been satisfied. This intentionally remembers a settlement even if its board later disappears:
 * breaking a board is treated as player/world modification, not permission for an intrusive
 * automatic respawn loop.
 */
public final class VillageBoardSavedData extends SavedData {
    private static final String DATA_NAME = "cozycrazyquests_village_boards";
    private static final String RECORDS_TAG = "records";

    private final List<VillageRecord> records = new ArrayList<>();

    public VillageBoardSavedData() {}

    public static VillageBoardSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VillageBoardSavedData::load,
                VillageBoardSavedData::new,
                DATA_NAME
        );
    }

    public Optional<VillageRecord> findNearby(BlockPos candidateCenter, int radius) {
        long radiusSq = (long) radius * radius;
        VillageRecord nearest = null;
        long nearestSq = Long.MAX_VALUE;

        for (VillageRecord record : records) {
            long centerSq = horizontalDistanceSq(candidateCenter, record.center());
            long boardSq = horizontalDistanceSq(candidateCenter, record.board());
            long distanceSq = Math.min(centerSq, boardSq);
            if (distanceSq <= radiusSq && distanceSq < nearestSq) {
                nearest = record;
                nearestSq = distanceSq;
            }
        }

        return Optional.ofNullable(nearest);
    }

    public VillageRecord remember(BlockPos center, BlockPos board, Status status, long gameTime) {
        VillageRecord record = new VillageRecord(center.immutable(), board.immutable(), status, gameTime);
        records.add(record);
        setDirty();
        return record;
    }

    public int recordCount() {
        return records.size();
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag list = new ListTag();
        for (VillageRecord record : records) {
            CompoundTag tag = new CompoundTag();
            putPos(tag, "center", record.center());
            putPos(tag, "board", record.board());
            tag.putString("status", record.status().name());
            tag.putLong("firstSeenGameTime", record.firstSeenGameTime());
            list.add(tag);
        }
        root.put(RECORDS_TAG, list);
        return root;
    }

    public static VillageBoardSavedData load(CompoundTag root) {
        VillageBoardSavedData data = new VillageBoardSavedData();
        ListTag list = root.getList(RECORDS_TAG, Tag.TAG_COMPOUND);

        for (Tag raw : list) {
            if (!(raw instanceof CompoundTag tag)) continue;
            if (!hasPos(tag, "center") || !hasPos(tag, "board")) continue;
            if (!tag.contains("status", Tag.TAG_STRING)) continue;

            Status status;
            try {
                status = Status.valueOf(tag.getString("status"));
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            long firstSeen = tag.contains("firstSeenGameTime", Tag.TAG_LONG)
                    ? tag.getLong("firstSeenGameTime")
                    : 0L;
            data.records.add(new VillageRecord(
                    getPos(tag, "center"),
                    getPos(tag, "board"),
                    status,
                    firstSeen
            ));
        }

        return data;
    }

    private static long horizontalDistanceSq(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    private static void putPos(CompoundTag tag, String prefix, BlockPos pos) {
        tag.putInt(prefix + "X", pos.getX());
        tag.putInt(prefix + "Y", pos.getY());
        tag.putInt(prefix + "Z", pos.getZ());
    }

    private static boolean hasPos(CompoundTag tag, String prefix) {
        return tag.contains(prefix + "X", Tag.TAG_INT)
                && tag.contains(prefix + "Y", Tag.TAG_INT)
                && tag.contains(prefix + "Z", Tag.TAG_INT);
    }

    private static BlockPos getPos(CompoundTag tag, String prefix) {
        return new BlockPos(
                tag.getInt(prefix + "X"),
                tag.getInt(prefix + "Y"),
                tag.getInt(prefix + "Z")
        );
    }

    public enum Status {
        FOUND_EXISTING,
        PLACED_REPAIR
    }

    public record VillageRecord(BlockPos center, BlockPos board, Status status, long firstSeenGameTime) {}
}
