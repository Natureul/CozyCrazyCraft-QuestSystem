package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

import java.util.Locale;
import java.util.Optional;

/**
 * Stable-enough social identity for an inhabited settlement.
 *
 * A village is not its bounty board. Conversations and authored progression must still work while
 * a board is missing, undiscovered, destroyed, or waiting for the board manager to record it. The
 * nearest meeting POI supplies the civic anchor when available; CozyCrazyZones supplies the
 * persistent settlement name. Bountiful is retained only as an optional civic-service bridge.
 *
 * Crucially, resolving a conversation never performs a structure locate. Village names are read
 * only from CozyCrazyZones' already-assigned in-memory ledger so a right click cannot force worldgen.
 */
record VillageContext(
        String key,
        String name,
        BlockPos anchor,
        ZoneBridge.Cell cell,
        VillageBoardSavedData.VillageRecord boardRecord
) {
    private static final int MEETING_RADIUS = 96;
    private static final int NAME_RADIUS = 256;
    private static final int BOARD_RADIUS = 192;

    static VillageContext resolve(ServerLevel level, BlockPos origin) {
        Optional<BlockPos> meeting = level.getPoiManager().findClosest(
                holder -> holder.is(PoiTypes.MEETING),
                pos -> true,
                origin,
                MEETING_RADIUS,
                PoiManager.Occupancy.ANY
        ).filter(level::isVillage).map(BlockPos::immutable);

        BlockPos anchor = meeting.orElse(origin.immutable());
        String name = VillageNameCacheBridge.nearestAssigned(level, anchor, NAME_RADIUS);
        boolean namedVillage = name != null && !name.isBlank() && !"the village".equalsIgnoreCase(name);
        boolean villageHere = level.isVillage(anchor) || level.isVillage(origin) || namedVillage;
        if (!villageHere) return null;

        if (!namedVillage) name = "the village";
        ZoneBridge.Cell cell = ZoneBridge.cellAt(level, anchor);
        VillageBoardSavedData.VillageRecord board = VillageBoardSavedData.get(level)
                .findNearby(anchor, BOARD_RADIUS)
                .orElse(null);

        return new VillageContext(
                stableKey(level, name, anchor, namedVillage, meeting.orElse(null), board),
                name,
                anchor,
                cell,
                board
        );
    }

    boolean hasBoard() {
        return boardRecord != null;
    }

    BlockPos boardPos() {
        return boardRecord == null ? null : boardRecord.board();
    }

    int legacyBoardTrust(ServerLevel level) {
        return hasBoard() ? BountifulBridge.boardCompletedCount(level, boardRecord.board()) : 0;
    }

    private static String stableKey(
            ServerLevel level,
            String name,
            BlockPos anchor,
            boolean namedVillage,
            BlockPos meeting,
            VillageBoardSavedData.VillageRecord board
    ) {
        String dimension = level.dimension().location().toString();
        if (namedVillage) {
            return dimension + "|name|" + slug(name);
        }

        // Prefer a physical civic object over coordinate bucketing. Two settlements can be unusually
        // close together; their bell/meeting POIs remain distinct even when a coarse 128-block grid does not.
        if (meeting != null) {
            return dimension + "|meeting|" + meeting.getX() + "," + meeting.getY() + "," + meeting.getZ();
        }
        if (board != null) {
            BlockPos pos = board.board();
            return dimension + "|board|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        }

        // Emergency compatibility path for an inhabited POI cluster before its meeting point or board is
        // available. This is intentionally finer than the old 128-block bucket and should be rare.
        return dimension + "|fallback|" + Math.floorDiv(anchor.getX(), 48) + "," + Math.floorDiv(anchor.getZ(), 48);
    }

    private static String slug(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
