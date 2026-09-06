package com.natureul.cozycrazyquests;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.ModList;

/**
 * Pack-level compatibility policy for Beautify.
 *
 * Beautify injects its Botanist house into every vanilla village street pool at server start using
 * a configurable spawn weight. The pack keeps Beautify's decorative blocks, but does not want that
 * profession/building in the village composition. Running at HIGHEST priority lets us set the
 * loaded Forge config value to zero before Beautify performs its normal-priority pool injection.
 */
final class BeautifyCompat {
    private BeautifyCompat() {}

    static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!ModList.get().isLoaded("beautify")) return;
        try {
            Class<?> configClass = Class.forName("com.github.Pandarix.beautify.util.Config");
            Object value = configClass.getField("BOTANIST_SPAWN_WEIGHT").get(null);
            if (value instanceof ForgeConfigSpec.IntValue spawnWeight) {
                if (spawnWeight.get() != 0) spawnWeight.set(0);
                CozyCrazyQuests.LOGGER.info("Beautify Botanist village-house injection disabled for CozyCrazyCraft");
            }
        } catch (Throwable error) {
            CozyCrazyQuests.LOGGER.warn(
                    "Could not disable Beautify Botanist village-house injection; Beautify may have changed its config API",
                    error
            );
        }
    }
}
