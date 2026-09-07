package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Locale;

/**
 * Occasional local-color Conversations layer for otherwise idle adult villagers.
 *
 * Profession/personality ambient pages remain the default voice. This manager only replaces roughly
 * one in three-to-five idle ambient surfaces (depending on trust), stable per villager/player/day, with
 * a region + radial-tier observation drawn from the Conversation Bible v0.2 context bank. It runs after
 * authored profession/civic quest managers but before the ordinary social fallback, so quest, hint and
 * Gore dialogue always outrank local chatter.
 */
final class RegionalAmbientConversationManager {
    private static final String OWN_PREFIX = CozyCrazyQuests.MOD_ID + ":";
    private static final String REGIONAL_PREFIX = OWN_PREFIX + "ambient_region_";
    private static final String GENERIC_PREFIX = OWN_PREFIX + "villager_";

    private RegionalAmbientConversationManager() {}

    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof Villager villager) || villager.isBaby()) return;
        if (!ConversationBridge.available() || !ConversationBridge.supports(villager)) return;

        VillageContext village = VillageContext.resolve(player.serverLevel(), villager.blockPosition());
        if (village == null) return;

        String current = ConversationBridge.currentDialogueId(villager);
        if (current != null && current.startsWith(OWN_PREFIX)
                && !current.startsWith(GENERIC_PREFIX)
                && !current.startsWith(REGIONAL_PREFIX)) {
            return;
        }
        if (current != null && !current.isBlank() && !current.startsWith(OWN_PREFIX)) return;

        // Bespoke local knowledge outranks ambient color. If a Gore route becomes relevant while an
        // ambient region page is currently attached, clear it now so the social manager can expose Gore.
        if (GoreTunnelLead.adultDialogue(player, villager, village) != null) {
            clearRegionalIfPresent(villager, current);
            return;
        }

        ResourceLocation selected = select(player, villager, village);
        if (selected == null) {
            clearRegionalIfPresent(villager, current);
            return;
        }
        ConversationBridge.setDialogue(villager, selected);
    }

    private static ResourceLocation select(ServerPlayer player, Villager villager, VillageContext village) {
        ZoneBridge.Cell cell = village.cell();
        if (cell == null || !cell.known()) return null;

        String tier = normalizeTier(cell.tier());
        if (tier == null) return null;

        String region;
        if ("hearthlands".equals(tier) && "SHARED_CORE".equals(cell.band())) {
            region = "shared_core";
        } else {
            region = normalizeMacro(cell.macro());
            if (region == null) return null;
        }

        VillageProgressState.Trust trust = VillageProgressState.snapshot(player, village.key()).trust();
        int cadence = switch (trust) {
            case STRANGER -> 5;
            case RECOGNIZED, RELIABLE -> 4;
            case TRUSTED, PROVEN -> 3;
        };

        long day = player.serverLevel().getDayTime() / 24000L;
        int seed = villager.getUUID().hashCode()
                ^ Integer.rotateLeft(player.getUUID().hashCode(), 11)
                ^ Integer.rotateLeft(village.key().hashCode(), 19)
                ^ Long.hashCode(day * 0x9E3779B97F4A7C15L);
        if (Math.floorMod(seed, cadence) != 0) return null;

        return new ResourceLocation(CozyCrazyQuests.MOD_ID, "ambient_region_" + region + "_" + tier);
    }

    private static void clearRegionalIfPresent(Villager villager, String current) {
        if (current != null && current.startsWith(REGIONAL_PREFIX)) {
            ConversationBridge.clearOwnDialogue(villager);
        }
    }

    private static String normalizeMacro(String macro) {
        if (macro == null) return null;
        return switch (macro.toUpperCase(Locale.ROOT)) {
            case "NORTH" -> "north";
            case "EAST" -> "east";
            case "SOUTH" -> "south";
            case "WEST" -> "west";
            default -> null;
        };
    }

    private static String normalizeTier(String tier) {
        if (tier == null) return null;
        return switch (tier.toUpperCase(Locale.ROOT)) {
            case "HEARTHLANDS" -> "hearthlands";
            case "FRONTIER" -> "frontier";
            case "WILDLANDS" -> "wildlands";
            case "DREAD_REACHES", "DREAD" -> "dread_reaches";
            default -> null;
        };
    }
}
