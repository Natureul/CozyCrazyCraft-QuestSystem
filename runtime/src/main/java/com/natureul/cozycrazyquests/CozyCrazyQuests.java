package com.natureul.cozycrazyquests;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
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
        MinecraftForge.EVENT_BUS.addListener(BountySourceTooltip::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(BountyRedemptionGuard::onRightClickBlock);

        // The compact Zone-1 social lattice gets first refusal. Location-bound structure work runs
        // immediately after it and only attaches when no higher-priority core authored dialogue is
        // already selected. The social layer then supplies ambient profession conversation.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, VillageConversationQuestManager::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, VillageStructureConversationQuestManager::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, VillageSocialConversationManager::onEntityInteract);

        MinecraftForge.EVENT_BUS.addListener(VillageConversationQuestManager::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(VillageConversationQuestManager::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(VillageConversationQuestManager::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(VillageStructureConversationQuestManager::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(VillageStructureConversationQuestManager::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(VillageStructureConversationQuestManager::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(VillageProgressState::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(PlayerKnowledgeState::onPlayerClone);

        LOGGER.info("CozyCrazyQuests runtime V3 loaded");
    }
}
