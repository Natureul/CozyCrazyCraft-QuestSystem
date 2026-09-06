package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class BountySourceTooltip {
    private static final ResourceLocation BOUNTY_ID = new ResourceLocation("bountiful", "bounty");

    private BountySourceTooltip() {}

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (!BOUNTY_ID.equals(itemId) || !BountySource.hasSource(stack)) return;

        event.getToolTip().add(
                Component.literal("Return to the board that issued this notice")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC)
        );
    }
}
