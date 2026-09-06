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

/** Physical reminder for an authored village contract. It is not a quest log, but it must be unambiguous. */
public final class VillageContractItem extends Item {
    public static final String QUEST_ID = "ccc_quest_id";
    public static final String VILLAGE_KEY = "ccc_village_key";
    public static final String TARGET_NAME = "ccc_target_name";
    public static final String TARGET_DISTANCE = "ccc_target_distance";
    public static final String TARGET_DIRECTION = "ccc_target_direction";
    public static final String TARGET_APPROACH = "ccc_target_approach";
    public static final String ISSUING_VILLAGE = "ccc_issuing_village";

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
            String approach = stack.getTag().getString(TARGET_APPROACH);
            String village = stack.getTag().getString(ISSUING_VILLAGE);
            VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(questId);

            if (!village.isBlank() && !"the village".equals(village)) {
                tooltip.add(Component.literal("Issued in " + village).withStyle(ChatFormatting.GOLD));
            }

            if (definition != null) {
                tooltip.add(Component.literal("Objective: " + objectiveLine(definition, target)).withStyle(ChatFormatting.AQUA));
            } else if (!target.isBlank()) {
                tooltip.add(Component.literal("Objective: Visit " + target).withStyle(ChatFormatting.AQUA));
            }

            if (distance > 0 && !direction.isBlank()) {
                tooltip.add(Component.literal("Location: about " + distance + " blocks " + direction)
                        .withStyle(ChatFormatting.GRAY));
            }

            if ("UNDERGROUND".equals(approach)) {
                tooltip.add(Component.literal("Approach: below ground — search the indicated area for a cave or opening")
                        .withStyle(ChatFormatting.YELLOW));
            } else if ("SUBMERGED".equals(approach)) {
                tooltip.add(Component.literal("Approach: below the waterline — the surface bearing is only a guide")
                        .withStyle(ChatFormatting.YELLOW));
            }

            if (definition != null) {
                tooltip.add(Component.literal("Return to: " + returnLine(definition, village))
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            } else {
                tooltip.add(Component.literal("Return to the village that issued this work")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static String objectiveLine(VillageQuestCatalog.Definition definition, String target) {
        String place = target == null || target.isBlank() ? definition.targetLabel() : target;
        return switch (definition.objectiveType()) {
            case STRUCTURE_SURVEY -> "Survey " + place;
            case STRUCTURE_HOSTILE_CLEAR -> "Clear " + definition.requiredKills() + " hostiles at " + place;
            case LOCAL_HOSTILE_CLEAR -> "Clear " + definition.requiredKills() + " hostiles around " + place;
        };
    }

    private static String returnLine(VillageQuestCatalog.Definition definition, String village) {
        String destination = village == null || village.isBlank() || "the village".equals(village)
                ? "the issuing village"
                : village;
        return professionList(definition.giverProfessions()) + " in " + destination;
    }

    private static String professionList(List<VillagerProfession> professions) {
        List<String> labels = new ArrayList<>();
        for (VillagerProfession profession : professions) labels.add("a " + professionLabel(profession));
        if (labels.isEmpty()) return "a village representative";
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
