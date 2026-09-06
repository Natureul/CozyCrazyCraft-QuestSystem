package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class BountyStoryTooltip {
    private static final int WRAP = 48;

    private BountyStoryTooltip() {}

    public static void onTooltip(ItemTooltipEvent event) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (key == null || !"bountiful".equals(key.getNamespace()) || !"bounty".equals(key.getPath())) return;

        List<String> objectiveIds = BountifulStoryBridge.bountyObjectiveIds(event.getItemStack());
        if (objectiveIds.size() != 1) return;

        StoryCatalog.Card card = StoryCatalog.get(objectiveIds.get(0));
        if (card == null) return;

        List<Component> tooltip = event.getToolTip();
        if (tooltip.isEmpty()) return;

        tooltip.set(0, Component.literal(card.title()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        int index = 1;
        tooltip.add(index++, Component.literal(card.noticeClass() + "  •  " + card.issuer())
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        if (card.trust() > 0) {
            tooltip.add(index++, Component.literal("Village Trust " + card.trust() + "+")
                .withStyle(ChatFormatting.DARK_GREEN));
        }
        for (String line : wrap(card.body(), WRAP)) {
            tooltip.add(index++, Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(index, Component.empty());
    }

    public static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) return lines;
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }
}
