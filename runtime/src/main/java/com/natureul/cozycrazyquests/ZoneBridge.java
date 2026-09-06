package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;
import java.util.Locale;

final class ZoneBridge {
    private static boolean warned;

    private ZoneBridge() {}

    static Cell cellAt(ServerLevel level, BlockPos pos) {
        try {
            Class<?> api = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method regionalCellAt = api.getMethod("regionalCellAt", ServerLevel.class, double.class, double.class);
            Object cell = regionalCellAt.invoke(null, level, pos.getX() + 0.5D, pos.getZ() + 0.5D);
            if (cell == null) return Cell.UNKNOWN;

            return new Cell(
                    enumName(cell, "radialZone"),
                    enumName(cell, "macroRegion"),
                    enumName(cell, "influenceBand")
            );
        } catch (Throwable error) {
            if (!warned) {
                warned = true;
                CozyCrazyQuests.LOGGER.warn("Could not classify a position through CozyCrazyZones; authored quests will fail safely", error);
            }
            return Cell.UNKNOWN;
        }
    }

    static String decreeFor(ServerLevel level, BlockPos pos) {
        Cell cell = cellAt(level, pos);
        if (!"HEARTHLANDS".equals(cell.tier())) return "ccc_local_notices";
        if ("SHARED_CORE".equals(cell.band())) return "ccc_local_notices";

        return switch (cell.macro()) {
            case "NORTH" -> "ccc_hearth_north";
            case "EAST" -> "ccc_hearth_east";
            case "SOUTH" -> "ccc_hearth_south";
            case "WEST" -> "ccc_hearth_west";
            default -> "ccc_local_notices";
        };
    }

    static int tierRank(String tier) {
        return switch (tier == null ? "" : tier.toUpperCase(Locale.ROOT)) {
            case "HEARTHLANDS" -> 0;
            case "FRONTIER" -> 1;
            case "WILDLANDS" -> 2;
            case "DREAD_REACHES", "DREAD" -> 3;
            default -> -1;
        };
    }

    private static String enumName(Object record, String accessor) throws Exception {
        Object value = record.getClass().getMethod(accessor).invoke(record);
        return value == null ? "" : value.toString().toUpperCase(Locale.ROOT);
    }

    record Cell(String tier, String macro, String band) {
        static final Cell UNKNOWN = new Cell("", "", "");

        boolean known() {
            return !tier.isBlank();
        }
    }
}
