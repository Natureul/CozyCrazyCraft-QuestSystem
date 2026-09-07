package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Quest-bound evidence recovered only inside the exact structure assigned to an authored job. */
final class RecoveredEvidence {
    private static final String QUEST_ID = "ccc_recovery_quest";
    private static final String VILLAGE_KEY = "ccc_recovery_village";
    private static final String TARGET_KEY = "ccc_recovery_target";
    private static final String OBJECT_NAME = "ccc_recovery_object";

    private RecoveredEvidence() {}

    static ItemStack create(VillageQuestCatalog.Definition definition, CompoundTag active) {
        ItemStack stack = new ItemStack(ModItems.RECOVERED_EVIDENCE.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(QUEST_ID, definition.id());
        tag.putString(VILLAGE_KEY, active.getString("village_key"));
        tag.putString(TARGET_KEY, active.getString("target_key"));
        tag.putString(OBJECT_NAME, definition.recoveryObjectName());
        String name = definition.recoveryObjectName().isBlank() ? "Recovered Evidence" : definition.recoveryObjectName();
        stack.setHoverName(Component.literal(name).withStyle(ChatFormatting.GOLD));
        return stack;
    }

    static boolean has(ServerPlayer player, CompoundTag active) {
        return findSlot(player, active) >= 0;
    }

    static boolean consume(ServerPlayer player, CompoundTag active) {
        int slot = findSlot(player, active);
        if (slot < 0) return false;
        player.getInventory().getItem(slot).shrink(1);
        return true;
    }

    private static int findSlot(ServerPlayer player, CompoundTag active) {
        String questId = active.getString("quest_id");
        String villageKey = active.getString("village_key");
        String targetKey = active.getString("target_key");
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.RECOVERED_EVIDENCE.get()) || !stack.hasTag()) continue;
            CompoundTag tag = stack.getTag();
            if (!questId.equals(tag.getString(QUEST_ID))) continue;
            if (!villageKey.equals(tag.getString(VILLAGE_KEY))) continue;
            if (!targetKey.isBlank() && !targetKey.equals(tag.getString(TARGET_KEY))) continue;
            return slot;
        }
        return -1;
    }
}
