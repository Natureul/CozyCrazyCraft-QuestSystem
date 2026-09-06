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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
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
    private static final int SOCIAL_ROUTE_RADIUS = 112;
    private static final ResourceLocation GUARD_TYPE = new ResourceLocation("guardvillagers", "guard");
    private static final ResourceLocation GUARD_DIALOGUE = id("guard_local");

    private VillageSocialConversationManager() {}

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (!ConversationBridge.available() || !ConversationBridge.supports(target)) return;

        if (target instanceof Villager villager) {
            VillagerNameService.ensureNamed(level, villager);

            if (ConversationBridge.hasOwnDialogue(villager)) return;
            if (ConversationBridge.hasDialogue(villager)) return;

            ConversationBridge.setDialogue(villager, genericVillagerDialogue(villager));
            return;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (!GUARD_TYPE.equals(entityId)) return;
        VillagerNameService.ensureNamed(level, target);

        if (!player.isShiftKeyDown()) {
            ConversationBridge.clearOwnDialogue(target);
            return;
        }

        if (!ConversationBridge.hasDialogue(target) || ConversationBridge.hasOwnDialogue(target)) {
            ConversationBridge.setDialogue(target, GUARD_DIALOGUE);
        }
    }

    static boolean consumeConversationAction(ServerPlayer player, String action) {
        if ("guard_board".equals(action)) {
            pointToNoticeBoard(player);
            return true;
        }
        if ("route_help".equals(action)) {
            routeToUsefulPerson(player);
            return true;
        }
        return false;
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

    private static void routeToUsefulPerson(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        VillageContext village = VillageContext.resolve(level, player.blockPosition());
        if (village == null) {
            player.sendSystemMessage(Component.literal("No one nearby seems to be speaking for a settlement here.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        VillageProgressState.Snapshot progress = VillageProgressState.snapshot(player, village.key());
        List<VillagerProfession> wanted;
        String reason;

        if (progress.capstoneEligible() && !progress.capstoneComplete()) {
            wanted = List.of(
                    VillagerProfession.MASON,
                    VillagerProfession.LIBRARIAN,
                    VillagerProfession.CARTOGRAPHER,
                    VillagerProfession.WEAPONSMITH,
                    VillagerProfession.ARMORER
            );
            reason = "the larger problem people have started talking about";
        } else if (!progress.categories().contains(VillageProgressState.AccomplishmentCategory.COMMUNITY)) {
            wanted = List.of(VillagerProfession.FARMER, VillagerProfession.SHEPHERD, VillagerProfession.BUTCHER);
            reason = "a local problem around the village";
        } else if (!progress.categories().contains(VillageProgressState.AccomplishmentCategory.EXPLORATION)) {
            wanted = List.of(VillagerProfession.MASON, VillagerProfession.LIBRARIAN, VillagerProfession.CLERIC, VillagerProfession.CARTOGRAPHER);
            reason = "something outside town that needs checking";
        } else if (!progress.categories().contains(VillageProgressState.AccomplishmentCategory.PROFESSION)) {
            wanted = List.of(VillagerProfession.WEAPONSMITH, VillagerProfession.ARMORER, VillagerProfession.FLETCHER, VillagerProfession.TOOLSMITH);
            reason = "practical work for someone willing to travel";
        } else {
            wanted = List.of(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON);
            reason = progress.capstoneComplete()
                    ? "what lies beyond the local roads"
                    : "what the village still needs";
        }

        AABB area = new AABB(player.blockPosition()).inflate(SOCIAL_ROUTE_RADIUS, 48, SOCIAL_ROUTE_RADIUS);
        Optional<Villager> candidate = level.getEntitiesOfClass(Villager.class, area, villager -> {
                    if (villager.isBaby() || !wanted.contains(villager.getVillagerData().getProfession())) return false;
                    VillageContext theirs = VillageContext.resolve(level, villager.blockPosition());
                    return theirs != null && village.key().equals(theirs.key());
                }).stream()
                .min(Comparator.comparingDouble(v -> v.distanceToSqr(player)));

        if (candidate.isEmpty()) {
            player.sendSystemMessage(
                    Component.literal("Nobody nearby fits that problem cleanly. Ask a guard or check the notice board; the village won't lose the lead.")
                            .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        Villager villager = candidate.get();
        VillagerNameService.ensureNamed(level, villager);
        long dx = (long) villager.blockPosition().getX() - player.blockPosition().getX();
        long dz = (long) villager.blockPosition().getZ() - player.blockPosition().getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        String profession = professionLabel(villager.getVillagerData().getProfession());
        String where = distance <= 10 ? "right nearby" : "about " + distance + " blocks " + direction(dx, dz);

        player.sendSystemMessage(
                Component.literal(villager.getDisplayName().getString() + ", the " + profession + ", may know about "
                                + reason + ". They're " + where + ".")
                        .withStyle(ChatFormatting.GOLD)
        );
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
