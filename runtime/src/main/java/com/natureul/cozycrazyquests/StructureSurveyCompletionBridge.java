package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Reliable completion path for structure-survey contracts.
 *
 * The older proximity test compared the player's Y coordinate against the locate result's Y. That is
 * fundamentally unsafe for modded underground structures: map/locate coordinates can point at the
 * surface, a jigsaw anchor, or another navigation position while the actual room is dozens of blocks
 * above or below it. The result was exactly the bad failure mode we want to avoid: the player digs to
 * the marked place, physically enters the correct generated structure, and the quest still refuses to
 * complete.
 *
 * This bridge mirrors CozyCrazyZones' own discovery criterion instead: once the player is inside the
 * bounding box of the exact structure type/instance represented by the contract, the survey is done.
 * The original distance fallback remains in VillageConversationQuestManager for surface landmarks.
 */
final class StructureSurveyCompletionBridge {
    private StructureSurveyCompletionBridge() {}

    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.tickCount % 10 != 0) return;

        CompoundTag root = VillageQuestState.root(player);
        boolean changed = false;

        for (CompoundTag active : VillageQuestState.allActives(root)) {
            if (objectiveComplete(active)) continue;
            VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
            if (definition == null || definition.objectiveType() != VillageQuestCatalog.ObjectiveType.STRUCTURE_SURVEY) continue;
            if (!level.dimension().location().toString().equals(active.getString("target_dimension"))) continue;

            ResourceLocation structureId = ResourceLocation.tryParse(active.getString("target_structure"));
            if (structureId == null) continue;
            BlockPos locate = readPos(active, "target");

            int expectedChunkX = active.contains("target_start_chunk_x")
                    ? active.getInt("target_start_chunk_x")
                    : Integer.MIN_VALUE;
            int expectedChunkZ = active.contains("target_start_chunk_z")
                    ? active.getInt("target_start_chunk_z")
                    : Integer.MIN_VALUE;

            if (!NamedPlaceBridge.insideExactStructure(
                    level,
                    player.blockPosition(),
                    structureId,
                    expectedChunkX,
                    expectedChunkZ,
                    locate
            )) continue;

            active.putBoolean("objective_complete", true);
            active.putBoolean("surveyed", true);
            VillageQuestState.putActive(root, active.getString("village_key"), active);
            changed = true;

            String targetKey = active.getString("target_key");
            if (!targetKey.isBlank()) {
                PlayerKnowledgeState.advance(
                        player,
                        targetKey,
                        PlayerKnowledgeState.Knowledge.CONFIRMED,
                        PlayerKnowledgeState.Provenance.LOCAL_OBSERVATION
                );
            }

            player.displayClientMessage(
                    Component.literal("Survey complete: " + active.getString("target_name") + ". "
                                    + returnInstruction(definition, active.getString("village_name")))
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            CozyCrazyQuests.LOGGER.info(
                    "Completed structure survey '{}' by exact structure occupancy at {}",
                    active.getString("quest_id"),
                    player.blockPosition()
            );
        }

        if (changed) VillageQuestState.save(player, root);
    }

    private static boolean objectiveComplete(CompoundTag active) {
        return active.getBoolean("objective_complete") || active.getBoolean("surveyed");
    }

    private static BlockPos readPos(CompoundTag tag, String prefix) {
        return new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"));
    }

    private static String returnInstruction(VillageQuestCatalog.Definition definition, String villageName) {
        if (villageName == null || villageName.isBlank()) villageName = "the issuing village";
        return "Return to " + professionList(definition.giverProfessions()) + " in " + villageName + ".";
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
