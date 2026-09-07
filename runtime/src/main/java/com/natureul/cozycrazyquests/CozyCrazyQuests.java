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

        // Pack-level structure compatibility has to run before the source mod mutates village pools.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BeautifyCompat::onServerAboutToStart);

        // Real generated structure starts are indexed as chunks load. Authored NPC targeting reads this
        // bounded persisted index instead of synchronously asking worldgen/Structure Gel to locate things.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, GeneratedStructureIndexSavedData::onChunkLoad);

        MinecraftForge.EVENT_BUS.addListener(VillageBoardManager::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(ProofLootInjector::onLootTableLoad);
        MinecraftForge.EVENT_BUS.addListener(BountyStoryTooltip::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(BountySourceTooltip::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(BountyRedemptionGuard::onRightClickBlock);

        // Profession-authored work gets first refusal. Civic fallback then makes regional work and
        // jobless villages executable without mutating vanilla professions. Regional ambient voice may
        // add local color only to otherwise idle villagers; the ordinary social fallback runs last.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, VillageConversationQuestManager::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, CivicQuestFallbackManager::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, RegionalAmbientConversationManager::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, VillageSocialConversationManager::onEntityInteract);

        // Structure/recovery proof is physical. Survey completion requires sustained real-piece occupancy;
        // recovery evidence is bound to an actual container in the exact assigned structure and only
        // completes after the player takes that quest-bound object. The old manager radius tick is
        // intentionally not registered: locator proximity is never structure-survey completion proof.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, StructureSurveyCompletionBridge::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, RecoveryQuestRuntime::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, RecoveryQuestRuntime::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(GoreTunnelLead::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(VillageConversationQuestManager::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(VillageConversationQuestManager::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(GoreTunnelLead::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(VillageProgressState::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(PlayerKnowledgeState::onPlayerClone);

        LOGGER.info("CozyCrazyQuests runtime V3 loaded");
    }
}
