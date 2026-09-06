package com.natureul.cozycrazyquests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Invisible-in-practice handshake item used by Conversations 1.0.5 reply actions.
 *
 * Conversations can give an item with NBT but does not expose a general external action callback.
 * A reply therefore gives one of these tokens with ccc_action=... On the next inventory tick the
 * server consumes the token and dispatches the authoritative CozyCrazy action.
 */
public final class ConversationTokenItem extends Item {
    public static final String ACTION_TAG = "ccc_action";

    public ConversationTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) return;
        if (!stack.hasTag() || !stack.getTag().contains(ACTION_TAG)) return;

        String action = stack.getTag().getString(ACTION_TAG);
        stack.shrink(1);
        CozyCrazyQuests.LOGGER.info(
                "Received Conversations quest action '{}' from player {}",
                action,
                player.getGameProfile().getName()
        );

        if (!VillageSocialConversationManager.consumeConversationAction(player, action)) {
            VillageConversationQuestManager.consumeConversationAction(player, action);
        }
    }
}
