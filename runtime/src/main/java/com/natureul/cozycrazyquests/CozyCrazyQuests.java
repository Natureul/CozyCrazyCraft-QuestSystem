package com.natureul.cozycrazyquests;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CozyCrazyQuests.MOD_ID)
public final class CozyCrazyQuests {
    public static final String MOD_ID = "cozycrazyquests";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CozyCrazyQuests() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);

        MinecraftForge.EVENT_BUS.addListener(VillageBoardManager::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(ProofLootInjector::onLootTableLoad);
        MinecraftForge.EVENT_BUS.addListener(BountyStoryTooltip::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(BountyRedemptionGuard::onRightClickBlock);
        LOGGER.info("CozyCrazyQuests runtime V2 loaded");
    }
}
