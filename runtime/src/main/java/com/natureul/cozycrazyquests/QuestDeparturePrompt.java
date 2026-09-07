package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * One-shot departure advice immediately after accepting a structure contract.
 *
 * The paper still carries the first bearing, but that bearing is no longer presented as "walk 500 blocks
 * northwest and hope." The player is explicitly told that the issuing settlement is an information network:
 * talk to locals first, then leave with a better lead. QuestHintNetwork guarantees that useful knowledge is
 * actually available rather than making this flavor text an empty promise.
 */
final class QuestDeparturePrompt {
    private QuestDeparturePrompt() {}

    static void afterAccept(ServerPlayer player) {
        CompoundTag root = VillageQuestState.root(player);
        String villageKey = VillageQuestState.conversationVillage(root);
        if (villageKey.isBlank()) return;

        CompoundTag active = VillageQuestState.activeForVillage(root, villageKey);
        if (active.isEmpty() || active.getString("target_structure").isBlank()) return;

        String villageName = active.getString("village_name");
        String place = villageName.isBlank() || "the village".equalsIgnoreCase(villageName)
                ? "the village"
                : villageName;
        String approach = active.getString("target_approach");

        String specialists = switch (approach) {
            case "UNDERGROUND" -> "a mason, toolsmith, cartographer, librarian, or guard";
            case "SUBMERGED" -> "a fisherman, cartographer, mason, librarian, or guard";
            default -> "a cartographer, mason, fletcher, toolsmith, or guard";
        };

        player.displayClientMessage(
                Component.literal("Before setting out, ask around in " + place + ". " + specialists
                                + " may know more than the first bearing.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                true
        );
    }
}
