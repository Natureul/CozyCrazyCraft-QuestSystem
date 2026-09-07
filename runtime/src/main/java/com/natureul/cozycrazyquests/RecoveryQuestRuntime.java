package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Turn-in guard for recovery-specialized structure contracts. */
final class RecoveryQuestRuntime {
    private RecoveryQuestRuntime() {}

    /**
     * Runs immediately before the normal authored turn-in action. Ordinary quests pass through.
     * Recovery jobs consume their quest-bound evidence; if the player lost it, the contract becomes
     * active again so returning to the exact structure can issue a replacement instead of hard-locking.
     */
    static boolean beforeTurnIn(ServerPlayer player) {
        CompoundTag root = VillageQuestState.root(player);
        String villageKey = VillageQuestState.conversationVillage(root);
        if (villageKey.isBlank()) {
            VillageContext village = VillageContext.resolve(player.serverLevel(), player.blockPosition());
            if (village != null) villageKey = village.key();
        }

        CompoundTag active = VillageQuestState.activeForVillage(root, villageKey);
        if (active.isEmpty()) return true;
        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null || !definition.isRecovery()) return true;

        if (RecoveredEvidence.consume(player, active)) return true;

        active.putBoolean("objective_complete", false);
        active.putBoolean("recovery_collected", false);
        active.putBoolean("visited_target", false);
        VillageQuestState.putActive(root, villageKey, active);
        VillageQuestState.save(player, root);

        String object = definition.recoveryObjectName().isBlank()
                ? "the recovered evidence"
                : definition.recoveryObjectName();
        player.sendSystemMessage(
                Component.literal("You no longer have " + object + ". Return to "
                                + active.getString("target_name") + " and recover it again.")
                        .withStyle(ChatFormatting.GOLD)
        );
        return false;
    }
}
