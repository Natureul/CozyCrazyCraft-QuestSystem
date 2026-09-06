package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

/**
 * Ambient/social layer that sits underneath authored quest dialogue.
 *
 * The quest manager runs first at HIGHEST priority. If it attached an authored quest conversation,
 * this layer leaves it alone. Otherwise ordinary villagers still receive a profession conversation
 * so a "wrong" click is useful rather than silent. Guard Villagers guards use sneak-right-click for
 * conversation so their normal right-click inventory/follow controls remain intact.
 */
public final class VillageSocialConversationManager {
    private static final int BOARD_DIRECTION_RADIUS = 192;
    private static final ResourceLocation GUARD_TYPE = new ResourceLocation("guardvillagers", "guard");
    private static final ResourceLocation GUARD_DIALOGUE = id("guard_local");

    private VillageSocialConversationManager() {}

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (!ConversationBridge.available() || !ConversationBridge.supports(target)) return;

        if (target instanceof Villager villager) {
            // Name belongs to the person, not the job. This therefore runs even when the quest
            // manager already selected a higher-priority authored conversation.
            VillagerNameService.ensureNamed(level, villager);

            // Authored quest/turn-in dialogue selected by VillageConversationQuestManager wins.
            if (ConversationBridge.hasOwnDialogue(villager)) return;
            // Respect any conversation deliberately attached by another mod/datapack.
            if (ConversationBridge.hasDialogue(villager)) return;

            ConversationBridge.setDialogue(villager, genericVillagerDialogue(villager));
            return;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (!GUARD_TYPE.equals(entityId)) return;
        VillagerNameService.ensureNamed(level, target);

        if (!player.isShiftKeyDown()) {
            // Do not hijack Guard Villagers' normal right-click UI. If this guard was spoken to on a
            // previous click, clear our dialogue before Conversations' NORMAL-priority hook runs.
            ConversationBridge.clearOwnDialogue(target);
            return;
        }

        if (!ConversationBridge.hasDialogue(target) || ConversationBridge.hasOwnDialogue(target)) {
            ConversationBridge.setDialogue(target, GUARD_DIALOGUE);
        }
    }

    static boolean consumeConversationAction(ServerPlayer player, String action) {
        if (!"guard_board".equals(action)) return false;
        pointToNoticeBoard(player);
        return true;
    }

    private static ResourceLocation genericVillagerDialogue(Villager villager) {
        if (villager.isBaby()) return id("villager_child");

        VillagerProfession profession = villager.getVillagerData().getProfession();
        String path;
        if (profession == VillagerProfession.ARMORER) path = "armorer";
        else if (profession == VillagerProfession.BUTCHER) path = "butcher";
        else if (profession == VillagerProfession.CARTOGRAPHER) path = "cartographer";
        else if (profession == VillagerProfession.CLERIC) path = "cleric";
        else if (profession == VillagerProfession.FARMER) path = "farmer";
        else if (profession == VillagerProfession.FISHERMAN) path = "fisherman";
        else if (profession == VillagerProfession.FLETCHER) path = "fletcher";
        else if (profession == VillagerProfession.LEATHERWORKER) path = "leatherworker";
        else if (profession == VillagerProfession.LIBRARIAN) path = "librarian";
        else if (profession == VillagerProfession.MASON) path = "mason";
        else if (profession == VillagerProfession.SHEPHERD) path = "shepherd";
        else if (profession == VillagerProfession.TOOLSMITH) path = "toolsmith";
        else if (profession == VillagerProfession.WEAPONSMITH) path = "weaponsmith";
        else if (profession == VillagerProfession.NITWIT) path = "nitwit";
        else path = "unemployed";
        return id("villager_" + path);
    }

    private static void pointToNoticeBoard(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Optional<VillageBoardSavedData.VillageRecord> record = VillageBoardSavedData.get(level)
                .findNearby(player.blockPosition(), BOARD_DIRECTION_RADIUS);
        if (record.isEmpty()) {
            player.sendSystemMessage(
                    Component.literal("The guard doesn't know of a posted notice board here yet.")
                            .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        BlockPos board = record.get().board();
        long dx = (long) board.getX() - player.blockPosition().getX();
        long dz = (long) board.getZ() - player.blockPosition().getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        player.sendSystemMessage(
                Component.literal("Village Notice Board: about " + distance + " blocks " + direction(dx, dz) + ".")
                        .withStyle(ChatFormatting.GOLD)
        );
    }

    private static String direction(long dx, long dz) {
        double angle = Math.atan2(dx, -dz);
        int octant = Math.floorMod((int) Math.round(angle / (Math.PI / 4.0)), 8);
        return switch (octant) {
            case 0 -> "north";
            case 1 -> "northeast";
            case 2 -> "east";
            case 3 -> "southeast";
            case 4 -> "south";
            case 5 -> "southwest";
            case 6 -> "west";
            default -> "northwest";
        };
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }
}
