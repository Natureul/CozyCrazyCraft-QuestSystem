package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class BountyRedemptionGuard {
    private static final ResourceLocation BOARD_ID = new ResourceLocation("bountiful", "bountyboard");
    private static final ResourceLocation BOUNTY_ID = new ResourceLocation("bountiful", "bounty");

    private BountyRedemptionGuard() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockState clicked = level.getBlockState(event.getPos());
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(clicked.getBlock());
        if (!BOARD_ID.equals(blockId)) return;

        ItemStack held = event.getEntity().getItemInHand(event.getHand());
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(held.getItem());
        if (!BOUNTY_ID.equals(itemId)) return;

        if (BountySource.mayRedeemAt(held, level, event.getPos())) return;

        event.getEntity().sendSystemMessage(
                Component.literal("This notice belongs to another village. Return it to the board that issued it.")
                        .withStyle(ChatFormatting.GOLD)
        );
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);
    }
}
