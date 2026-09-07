package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Last-pass Conversations recovery surface for a seeded recovery object that is no longer in inventory.
 *
 * This intentionally runs after ordinary authored/social selection. It does not auto-create replacement
 * evidence. It only gives the player an explicit choice to void the old generation or go back for it.
 */
final class RecoverySupportConversationManager {
    private RecoverySupportConversationManager() {}

    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof Villager villager) || villager.isBaby()) return;
        if (!ConversationBridge.available() || !ConversationBridge.supports(villager)) return;

        VillageContext village = VillageContext.resolve(level, villager.blockPosition());
        if (village == null) return;
        CompoundTag root = VillageQuestState.root(player);
        CompoundTag active = VillageQuestState.activeForVillage(root, village.key());
        if (!RecoveryQuestRuntime.needsReplacementSupport(player, active)) return;

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        if (definition == null) return;
        boolean issuer = villager.getUUID().toString().equals(active.getString("giver_uuid"));
        boolean validProfession = definition.accepts(villager.getVillagerData().getProfession());
        boolean civic = VillageCivicRoleService.isCivicContact(level, village, villager);
        if (!issuer && !validProfession && !civic) return;

        VillageQuestState.noteConversation(root, village.key(), villager.getUUID().toString());
        VillageQuestState.save(player, root);
        ConversationBridge.setDialogue(villager, VillageQuestCatalog.id("recovery_evidence_missing"));
    }
}
