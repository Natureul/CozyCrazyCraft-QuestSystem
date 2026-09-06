package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;

import java.util.UUID;

/**
 * Upgrades 0.3.3-era village papers in place.
 *
 * The first authored quest manager already persisted the giver UUID/profession and objective data,
 * but the old contract tooltip ignored those fields and hard-coded "cartographer". This tiny bridge
 * means an existing playtest save does not need to abandon/reaccept the quest just to receive the
 * corrected paper. Once a contract has the clarified tags this becomes a no-op.
 */
final class CoreContractClarifier {
    private static final String ROOT = "CozyCrazyVillagerQuests";
    private static final String ACTIVE = "active";

    private CoreContractClarifier() {}

    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        enrich(player);
    }

    static void enrich(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        CompoundTag active = root.getCompound(ACTIVE);
        if (active.isEmpty()) return;

        String questId = active.getString("quest_id");
        if (questId.isBlank()) return;

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.VILLAGE_CONTRACT.get()) || !stack.hasTag()) continue;
            CompoundTag tag = stack.getTag();
            if (!questId.equals(tag.getString(VillageContractItem.QUEST_ID))) continue;
            if (!tag.getString(VillageContractItem.OBJECTIVE_TEXT).isBlank()
                    && !tag.getString(VillageContractItem.RETURN_TEXT).isBlank()) return;

            String giverName = resolveGiverName(player.serverLevel(), active);
            if (!giverName.isBlank()) tag.putString(VillageContractItem.ISSUING_VILLAGER, giverName);

            String target = active.getString("target_name");
            String objectiveType = active.getString("objective_type");
            int requiredKills = Math.max(1, active.getInt("required_kills"));
            String objective = "STRUCTURE_SURVEY".equals(objectiveType)
                    ? "Survey " + target
                    : "Clear " + requiredKills + " hostiles around " + target;
            tag.putString(VillageContractItem.OBJECTIVE_TEXT, objective);

            String village = active.getString("village_name");
            String giverForReturn = !giverName.isBlank()
                    ? giverName
                    : article(active.getString("giver_profession"));
            String returnText = !village.isBlank() && !"the village".equalsIgnoreCase(village)
                    ? "Return to " + giverForReturn + " in " + village
                    : "Return to " + giverForReturn + " in the issuing village";
            tag.putString(VillageContractItem.RETURN_TEXT, returnText);
            return;
        }
    }

    private static String resolveGiverName(ServerLevel level, CompoundTag active) {
        String raw = active.getString("giver_uuid");
        if (raw.isBlank()) return "";
        try {
            Entity entity = level.getEntity(UUID.fromString(raw));
            return entity == null ? "" : entity.getDisplayName().getString();
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }

    private static String article(String profession) {
        if (profession == null || profession.isBlank()) return "the villager who issued this work";
        char first = Character.toLowerCase(profession.charAt(0));
        String prefix = "aeiou".indexOf(first) >= 0 ? "an " : "a ";
        return prefix + profession.toLowerCase();
    }
}
