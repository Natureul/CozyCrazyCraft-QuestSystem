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
 * Ambient/social layer beneath authored quest dialogue.
 *
 * The quest manager runs first. If it did not select a giver/turn-in conversation, ordinary residents
 * can still supply local knowledge, referrals, rumors, or normal profession chatter. Guards now use
 * ordinary right click; sneak-right-click is deliberately left alone for Carry On and other entity
 * interaction mods.
 *
 * Ordinary professional chatter is deliberately stable-but-varied: a villager's UUID chooses one of
 * three authored voice variants for that profession. The same person therefore keeps their conversational
 * flavor across reloads and profession interactions, while two farmers are no longer guaranteed to recite
 * the identical opening line. This is a light personality layer, not a personality stereotype system.
 */
public final class VillageSocialConversationManager {
    private static final int BOARD_DIRECTION_RADIUS = 192;
    private static final int SOCIAL_ROUTE_RADIUS = 176;
    private static final ResourceLocation GUARD_TYPE = new ResourceLocation("guardvillagers", "guard");

    private VillageSocialConversationManager() {}

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (!ConversationBridge.available() || !ConversationBridge.supports(target)) return;

        if (target instanceof Villager villager) {
            VillagerNameService.ensureNamed(level, villager);

            // Authored giver/turn-in dialogue always wins.
            if (ConversationBridge.hasOwnDialogue(villager)) return;
            if (ConversationBridge.hasDialogue(villager)) return;

            VillageContext village = VillageContext.resolve(level, villager.blockPosition());
            if (village != null) {
                ResourceLocation hint = VillageConversationQuestManager.socialHintDialogue(player, villager, village);
                if (hint != null) {
                    ConversationBridge.setDialogue(villager, hint);
                    return;
                }
            }

            ConversationBridge.setDialogue(villager, genericVillagerDialogue(player, villager, village));
            return;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (!GUARD_TYPE.equals(entityId)) return;
        VillagerNameService.ensureNamed(level, target);

        // Carry On uses sneak-right-click in this pack. Leave that gesture entirely alone.
        if (player.isShiftKeyDown()) {
            ConversationBridge.clearOwnDialogue(target);
            return;
        }

        VillageContext village = VillageContext.resolve(level, target.blockPosition());
        if (village != null) {
            ResourceLocation hint = VillageConversationQuestManager.socialHintDialogue(player, target, village);
            if (hint != null) {
                ConversationBridge.setDialogue(target, hint);
                return;
            }
        }

        ResourceLocation dialogue = findUsefulPerson(player, village).isPresent()
                ? id("guard_local")
                : id("guard_local_quiet");
        ConversationBridge.setDialogue(target, dialogue);
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
        if ("buy_hint".equals(action)) {
            return VillageConversationQuestManager.buyCurrentHint(player);
        }
        if ("mark_active_target".equals(action)) {
            return VillageConversationQuestManager.markCurrentTargetOnAtlas(player);
        }
        return false;
    }

    private static ResourceLocation genericVillagerDialogue(
            ServerPlayer player,
            Villager villager,
            VillageContext village
    ) {
        if (villager.isBaby()) return id("villager_child");

        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE) {
            return findUsefulPerson(player, village).isPresent()
                    ? id("villager_unemployed")
                    : id("villager_unemployed_quiet");
        }
        if (profession == VillagerProfession.NITWIT) {
            return findUsefulPerson(player, village).isPresent()
                    ? id("villager_nitwit")
                    : id("villager_nitwit_quiet");
        }

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
        else path = "unemployed";
        return ambientVariant(villager, "villager_" + path);
    }

    /**
     * Stable per-person ambient voice. Variant 0 is the original conversation; variants 1 and 2
     * are Bible-authored alternates. UUID hashing means the person does not change voice because the
     * player reopened the screen or reloaded the world.
     */
    private static ResourceLocation ambientVariant(Villager villager, String basePath) {
        int variant = Math.floorMod(villager.getUUID().hashCode(), 3);
        return switch (variant) {
            case 1 -> id(basePath + "_v2");
            case 2 -> id(basePath + "_v3");
            default -> id(basePath);
        };
    }

    private static void routeToUsefulPerson(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        VillageContext village = VillageContext.resolve(level, player.blockPosition());
        Optional<Villager> candidate = findUsefulPerson(player, village);

        // No system-chat failure bark. The quiet conversation page already told the player the local
        // resident does not know a suitable person and suggested the guard/board in-world.
        if (candidate.isEmpty()) return;

        Villager villager = candidate.get();
        VillagerNameService.ensureNamed(level, villager);
        long dx = (long) villager.blockPosition().getX() - player.blockPosition().getX();
        long dz = (long) villager.blockPosition().getZ() - player.blockPosition().getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        String profession = professionLabel(villager.getVillagerData().getProfession());
        String where = distance <= 10 ? "right nearby" : "about " + distance + " blocks " + direction(dx, dz);

        player.sendSystemMessage(
                Component.literal(villager.getDisplayName().getString() + ", the " + profession
                                + ", is the person I'd ask. They're " + where + ".")
                        .withStyle(ChatFormatting.GOLD)
        );
    }

    private static Optional<Villager> findUsefulPerson(ServerPlayer player, VillageContext village) {
        if (village == null) return Optional.empty();
        ServerLevel level = player.serverLevel();
        VillageProgressState.Snapshot progress = VillageProgressState.snapshot(player, village.key());
        List<VillagerProfession> wanted;

        if (progress.capstoneEligible() && !progress.capstoneComplete()) {
            wanted = List.of(
                    VillagerProfession.MASON,
                    VillagerProfession.LIBRARIAN,
                    VillagerProfession.CARTOGRAPHER,
                    VillagerProfession.WEAPONSMITH,
                    VillagerProfession.ARMORER
            );
        } else if (!progress.categories().contains(VillageProgressState.AccomplishmentCategory.COMMUNITY)) {
            wanted = List.of(
                    VillagerProfession.FARMER,
                    VillagerProfession.SHEPHERD,
                    VillagerProfession.BUTCHER,
                    VillagerProfession.FISHERMAN,
                    VillagerProfession.LEATHERWORKER
            );
        } else if (!progress.categories().contains(VillageProgressState.AccomplishmentCategory.EXPLORATION)) {
            wanted = List.of(
                    VillagerProfession.MASON,
                    VillagerProfession.LIBRARIAN,
                    VillagerProfession.CLERIC,
                    VillagerProfession.CARTOGRAPHER,
                    VillagerProfession.FISHERMAN
            );
        } else if (!progress.categories().contains(VillageProgressState.AccomplishmentCategory.PROFESSION)) {
            wanted = List.of(
                    VillagerProfession.WEAPONSMITH,
                    VillagerProfession.ARMORER,
                    VillagerProfession.FLETCHER,
                    VillagerProfession.TOOLSMITH,
                    VillagerProfession.LEATHERWORKER
            );
        } else if (!progress.categories().contains(VillageProgressState.AccomplishmentCategory.DANGER)) {
            wanted = List.of(
                    VillagerProfession.WEAPONSMITH,
                    VillagerProfession.ARMORER,
                    VillagerProfession.FLETCHER,
                    VillagerProfession.TOOLSMITH,
                    VillagerProfession.CLERIC
            );
        } else {
            wanted = List.of(VillagerProfession.CARTOGRAPHER, VillagerProfession.LIBRARIAN, VillagerProfession.MASON);
        }

        AABB area = new AABB(village.anchor()).inflate(SOCIAL_ROUTE_RADIUS, 64, SOCIAL_ROUTE_RADIUS);
        return level.getEntitiesOfClass(Villager.class, area, villager -> {
                    if (villager.isBaby() || !wanted.contains(villager.getVillagerData().getProfession())) return false;
                    VillageContext theirs = VillageContext.resolve(level, villager.blockPosition());
                    return theirs != null && village.key().equals(theirs.key());
                }).stream()
                .min(Comparator.comparingDouble(v -> v.distanceToSqr(player)));
    }

    private static void pointToNoticeBoard(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Optional<VillageBoardSavedData.VillageRecord> record = VillageBoardSavedData.get(level)
                .findNearby(player.blockPosition(), BOARD_DIRECTION_RADIUS);
        if (record.isEmpty()) {
            player.displayClientMessage(Component.literal("No posted board is close enough to point out.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        BlockPos board = record.get().board();
        long dx = (long) board.getX() - player.blockPosition().getX();
        long dz = (long) board.getZ() - player.blockPosition().getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        player.displayClientMessage(
                Component.literal("Notice board: about " + distance + " blocks " + direction(dx, dz) + ".")
                        .withStyle(ChatFormatting.GOLD),
                true
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
