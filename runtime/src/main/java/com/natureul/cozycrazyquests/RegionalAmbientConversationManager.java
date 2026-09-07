package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Locale;

/**
 * Contextual Conversations layer for otherwise idle adult villagers.
 *
 * Profession/personality ambient pages remain the default voice. This manager occasionally replaces
 * an idle ambient surface with either (a) a recent village-completion acknowledgement or (b) a
 * region + radial-tier observation drawn from Conversation Bible v0.2. Selection is deterministic for
 * the villager/player/day so a villager does not reroll personality every click. Authored quests, hints,
 * civic work and bespoke Gore knowledge outrank this layer.
 */
final class RegionalAmbientConversationManager {
    private static final String OWN_PREFIX = CozyCrazyQuests.MOD_ID + ":";
    private static final String REGIONAL_PREFIX = OWN_PREFIX + "ambient_region_";
    private static final String COMPLETION_PREFIX = OWN_PREFIX + "ambient_completion_";
    private static final String GENERIC_PREFIX = OWN_PREFIX + "villager_";
    private static final long COMPLETION_MEMORY_TICKS = 72000L;

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
                && !current.startsWith(REGIONAL_PREFIX)
                && !current.startsWith(COMPLETION_PREFIX)) {
            return;
        }
        if (current != null && !current.isBlank() && !current.startsWith(OWN_PREFIX)) return;

        // Bespoke local knowledge outranks ambient color. If a Gore route becomes relevant while an
        // ambient context page is attached, clear it now so the social manager can expose Gore.
        if (GoreTunnelLead.adultDialogue(player, villager, village) != null) {
            clearContextIfPresent(villager, current);
            return;
        }

        ResourceLocation selected = recentCompletionDialogue(player, villager, village);
        if (selected == null) selected = regionalDialogue(player, villager, village);
        if (selected == null) {
            clearContextIfPresent(villager, current);
            return;
        }
        ConversationBridge.setDialogue(villager, selected);
    }

    /** Village-wide memory: recent completed work can surface from residents other than the giver. */
    private static ResourceLocation recentCompletionDialogue(
            ServerPlayer player,
            Villager villager,
            VillageContext village
    ) {
        CompoundTag root = VillageQuestState.root(player);
        CompoundTag recent = VillageQuestState.recentCompletion(root, village.key());
        if (recent.isEmpty()) return null;

        String questId = recent.getString("quest_id");
        if (questId.isBlank()) return null;
        long age = player.serverLevel().getGameTime() - recent.getLong("completed_game_time");
        if (age < 0 || age > COMPLETION_MEMORY_TICKS) return null;

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(questId);
        if (definition == null || definition.accomplishmentCategory() == null) return null;

        long day = player.serverLevel().getDayTime() / 24000L;
        int seed = villager.getUUID().hashCode()
                ^ Integer.rotateLeft(player.getUUID().hashCode(), 5)
                ^ Integer.rotateLeft(questId.hashCode(), 13)
                ^ Long.hashCode(day * 0xD1B54A32D192ED03L);
        if (Math.floorMod(seed, 3) != 0) return null;

        String category = definition.accomplishmentCategory().name().toLowerCase(Locale.ROOT);
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, "ambient_completion_" + category);
    }

    /** Region/tier local color: frequent enough to give place identity, rare enough to preserve professions. */
    private static ResourceLocation regionalDialogue(
            ServerPlayer player,
            Villager villager,
            VillageContext village
    ) {
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

    private static void clearContextIfPresent(Villager villager, String current) {
        if (current != null && (current.startsWith(REGIONAL_PREFIX) || current.startsWith(COMPLETION_PREFIX))) {
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
