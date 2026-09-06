package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class VillageContractItem extends Item {
    public static final String QUEST_ID = "ccc_quest_id";
    public static final String TARGET_NAME = "ccc_target_name";
    public static final String TARGET_DISTANCE = "ccc_target_distance";
    public static final String TARGET_DIRECTION = "ccc_target_direction";
    public static final String ISSUING_VILLAGE = "ccc_issuing_village";

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

            if (!village.isBlank() && !"the village".equals(village)) {
                tooltip.add(Component.literal("Issued in " + village).withStyle(ChatFormatting.GOLD));
            }
            if (!target.isBlank()) {
                tooltip.add(Component.literal("Survey: " + target).withStyle(ChatFormatting.AQUA));
            }
            if (distance > 0 && !direction.isBlank()) {
                tooltip.add(Component.literal("About " + distance + " blocks " + direction)
                        .withStyle(ChatFormatting.GRAY));
            }
            String returnLine = !village.isBlank() && !"the village".equals(village)
                    ? "Return to a cartographer in " + village
                    : "Return to a cartographer in the issuing village";
            tooltip.add(Component.literal(returnLine)
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
