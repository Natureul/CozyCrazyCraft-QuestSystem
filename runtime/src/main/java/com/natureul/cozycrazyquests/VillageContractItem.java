package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
            String questId = stack.getTag().getString(QUEST_ID);
            String target = stack.getTag().getString(TARGET_NAME);
            int distance = stack.getTag().getInt(TARGET_DISTANCE);
            String direction = stack.getTag().getString(TARGET_DIRECTION);
            String village = stack.getTag().getString(ISSUING_VILLAGE);
            String villager = stack.getTag().getString(ISSUING_VILLAGER);
            String objective = stack.getTag().getString(OBJECTIVE_TEXT);
            String returnText = stack.getTag().getString(RETURN_TEXT);

            VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(questId);
            if (objective.isBlank() && definition != null) {
                objective = switch (definition.objectiveType()) {
                    case STRUCTURE_SURVEY -> "Survey " + safeTarget(target, definition.targetLabel());
                    case LOCAL_HOSTILE_CLEAR -> "Clear " + Math.max(1, definition.requiredKills())
                            + " hostiles around " + safeTarget(target, definition.targetLabel());
                };
            }

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
                if (!villager.isBlank()) {
                    returnText = !village.isBlank() && !"the village".equals(village)
                            ? "Return to " + villager + " in " + village
                            : "Return to " + villager;
                } else if (definition != null) {
                    String roles = professionList(definition.giverProfessions());
                    returnText = !village.isBlank() && !"the village".equals(village)
                            ? "Return to " + roles + " in " + village
                            : "Return to " + roles + " in the issuing village";
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

    private static String safeTarget(String target, String fallback) {
        if (target != null && !target.isBlank()) return target;
        return fallback == null || fallback.isBlank() ? "the marked place" : fallback;
    }

    private static String professionList(List<VillagerProfession> professions) {
        List<String> labels = new ArrayList<>();
        for (VillagerProfession profession : professions) labels.add("a " + professionLabel(profession));
        if (labels.isEmpty()) return "a villager";
        if (labels.size() == 1) return labels.get(0);
        if (labels.size() == 2) return labels.get(0) + " or " + labels.get(1);
        return String.join(", ", labels.subList(0, labels.size() - 1)) + ", or " + labels.get(labels.size() - 1);
    }

    private static String professionLabel(VillagerProfession profession) {
        if (profession == VillagerProfession.ARMORER) return "armorer";
        if (profession == VillagerProfession.BUTCHER) return "butcher";
        if (profession == VillagerProfession.CARTOGRAPHER) return "cartographer";
        if (profession == VillagerProfession.CLERIC) return "cleric";
        if (profession == VillagerProfession.FARMER) return "farmer";
        if (profession == VillagerProfession.FISHERMAN) return "fisherman";
        if (profession == VillagerProfession.FLETCHER) return "fletcher";
        if (profession == VillagerProfession.LEATHERWORKER) return "leatherworker";
        if (profession == VillagerProfession.LIBRARIAN) return "librarian";
        if (profession == VillagerProfession.MASON) return "mason";
        if (profession == VillagerProfession.SHEPHERD) return "shepherd";
        if (profession == VillagerProfession.TOOLSMITH) return "toolsmith";
        if (profession == VillagerProfession.WEAPONSMITH) return "weaponsmith";
        return "villager";
    }
}
