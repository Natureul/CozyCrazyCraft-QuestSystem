package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** A readable physical reminder of an authored villager job. */
public final class VillageContractItem extends Item {
    public static final String QUEST_ID = "ccc_quest_id";
    public static final String TARGET_NAME = "ccc_target_name";
    public static final String TARGET_DISTANCE = "ccc_target_distance";
    public static final String TARGET_DIRECTION = "ccc_target_direction";
    public static final String ISSUING_VILLAGE = "ccc_issuing_village";
    public static final String ISSUING_VILLAGER = "ccc_issuing_villager";
    public static final String OBJECTIVE_TEXT = "ccc_objective_text";
    public static final String RETURN_TEXT = "ccc_return_text";

    public VillageContractItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag()) {
            String target = stack.getTag().getString(TARGET_NAME);
            int distance = stack.getTag().getInt(TARGET_DISTANCE);
            String direction = stack.getTag().getString(TARGET_DIRECTION);
            String village = stack.getTag().getString(ISSUING_VILLAGE);
            String villager = stack.getTag().getString(ISSUING_VILLAGER);
            String objective = stack.getTag().getString(OBJECTIVE_TEXT);
            String returnText = stack.getTag().getString(RETURN_TEXT);

            if (!villager.isBlank() && !village.isBlank() && !"the village".equals(village)) {
                tooltip.add(Component.literal("Issued by " + villager + " • " + village)
                        .withStyle(ChatFormatting.GOLD));
            } else if (!villager.isBlank()) {
                tooltip.add(Component.literal("Issued by " + villager).withStyle(ChatFormatting.GOLD));
            } else if (!village.isBlank() && !"the village".equals(village)) {
                tooltip.add(Component.literal("Issued in " + village).withStyle(ChatFormatting.GOLD));
            }

            if (!objective.isBlank()) {
                tooltip.add(Component.literal("Objective: " + objective).withStyle(ChatFormatting.AQUA));
            } else if (!target.isBlank()) {
                // Compatibility for contracts created by 0.3.3 and earlier. Most importantly, do
                // not claim that every old contract belongs to a cartographer.
                tooltip.add(Component.literal("Target: " + target).withStyle(ChatFormatting.AQUA));
            }

            if (!target.isBlank() && !objective.toLowerCase().contains(target.toLowerCase())) {
                tooltip.add(Component.literal("Location: " + target).withStyle(ChatFormatting.DARK_AQUA));
            }
            if (distance > 0 && !direction.isBlank()) {
                tooltip.add(Component.literal("About " + distance + " blocks " + direction)
                        .withStyle(ChatFormatting.GRAY));
            }

            if (returnText.isBlank()) {
                if (!villager.isBlank() && !village.isBlank() && !"the village".equals(village)) {
                    returnText = "Return to " + villager + " in " + village;
                } else if (!village.isBlank() && !"the village".equals(village)) {
                    returnText = "Return to the villager who issued this work in " + village;
                } else {
                    returnText = "Return to the villager who issued this work";
                }
            }
            tooltip.add(Component.literal(returnText)
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
