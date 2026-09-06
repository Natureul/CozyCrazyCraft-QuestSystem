package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;
import java.util.Locale;

final class ZoneBridge {
    private static boolean warned;

    private ZoneBridge() {}

    static String decreeFor(ServerLevel level, BlockPos pos) {
        try {
            Class<?> api = Class.forName("com.natureul.cozycrazyzones.CozyZonesApi");
            Method regionalCellAt = api.getMethod("regionalCellAt", ServerLevel.class, double.class, double.class);
            Object cell = regionalCellAt.invoke(null, level, pos.getX() + 0.5D, pos.getZ() + 0.5D);
            if (cell == null) return "ccc_local_notices";

            String tier = enumName(cell, "radialZone");
            String macro = enumName(cell, "macroRegion");
            String band = enumName(cell, "influenceBand");

            if ("HEARTHLANDS".equals(tier)) {
                if ("SHARED_CORE".equals(band)) return "ccc_local_notices";
                return switch (macro) {
                    case "NORTH" -> "ccc_hearth_north";
                    case "EAST" -> "ccc_hearth_east";
                    case "SOUTH" -> "ccc_hearth_south";
                    case "WEST" -> "ccc_hearth_west";
                    default -> "ccc_local_notices";
                };
            }

            // Frontier/Wildlands/Dread authored Bountiful pools are still being materialized.
            // Do not pretend planned data is live; use a safe generic board until those decrees exist.
            return "ccc_local_notices";
        } catch (Throwable error) {
            if (!warned) {
                warned = true;
                CozyCrazyQuests.LOGGER.warn("Could not classify a bounty board through CozyCrazyZones; falling back to Local Notices", error);
            }
            return "ccc_local_notices";
        }
    }

    private static String enumName(Object record, String accessor) throws Exception {
        Object value = record.getClass().getMethod(accessor).invoke(record);
        return value == null ? "" : value.toString().toUpperCase(Locale.ROOT);
    }
}
