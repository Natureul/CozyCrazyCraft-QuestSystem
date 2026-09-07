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
 * can still supply local knowledge, referrals, rumors, or normal profession chatter. Guards use
 * ordinary right click; sneak-right-click is deliberately left alone for Carry On and other entity
 * interaction mods.
 *
 * An important exception is an active structure contract: a non-giver who plausibly knows the route
 * may replace the generic active reminder with QuestHintNetwork dialogue. This is intentional. It
 * lets a cartographer, mason, librarian, guard, etc. actually help even when their profession also
 * happens to be a legal turn-in profession for that quest.
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
            VillageContext village = VillageContext.resolve(level, villager.blockPosition());

            // Children never become paid informants or generic quest hint machines. Their useful
            // moments are deliberately rare and handled by the child dialogue bank below.
            if (villager.isBaby()) {
                if (!ConversationBridge.hasOwnDialogue(villager) && !ConversationBridge.hasDialogue(villager)) {
                    ConversationBridge.setDialogue(villager, childDialogue(player, villager, village));
                }
                return;
            }

            if (village != null) {
                // This deliberately runs before the "quest manager already attached something" guard.
                // It fixes cases where a second cartographer/fisherman/etc. inherited the generic active
                // reminder and therefore could not actually give the player the clue they came to ask for.
                ResourceLocation activeHint = QuestHintNetwork.dialogue(player, villager, village);
                if (activeHint != null) {
                    ConversationBridge.setDialogue(villager, activeHint);
                    return;
                }
            }

            // Authored offers, original-giver reminders and completed turn-ins still win.
            if (ConversationBridge.hasOwnDialogue(villager)) return;
            if (ConversationBridge.hasDialogue(villager)) return;

            if (village != null) {
                ResourceLocation deepRoad = GoreTunnelLead.adultDialogue(player, villager, village);
                if (deepRoad != null) {
                    ConversationBridge.setDialogue(villager, deepRoad);
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
            ResourceLocation hint = QuestHintNetwork.dialogue(player, target, village);
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
            if (!QuestHintNetwork.routeForActiveQuest(player)) routeToUsefulPerson(player);
            return true;
        }
        if ("buy_hint".equals(action)) {
            return VillageConversationQuestManager.buyCurrentHint(player);
        }
        if ("mark_active_target".equals(action)) {
            return VillageConversationQuestManager.markCurrentTargetOnAtlas(player);
        }
        if (QuestHintNetwork.consumeAction(player, action)) return true;
        if (GoreTunnelLead.consumeAction(player, action)) return true;
        return false;
    }

    private static ResourceLocation genericVillagerDialogue(
            ServerPlayer player,
            Villager villager,
            VillageContext village
    ) {
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
     * Child chatter changes only once per Minecraft day, not every click. Only three out of thirty-two
     * day/person rolls are potentially useful. The rarest branch mentions the Tunnel Gore only when
     * the resolver has found a real lair inside the bounded local search radius. If the player already
     * followed that rumor through, the same rare slot becomes a small continuity reaction instead.
     */
    private static ResourceLocation childDialogue(
            ServerPlayer player,
            Villager villager,
            VillageContext village
    ) {
        long day = player.serverLevel().getDayTime() / 24000L;
        int seed = villager.getUUID().hashCode()
                ^ Integer.rotateLeft(player.getUUID().hashCode(), 7)
                ^ Long.hashCode(day * 0x9E3779B97F4A7C15L);
        int roll = Math.floorMod(seed, 32);

        if (roll == 0 && village != null) {
            if (GoreTunnelLead.completedFor(player, village)) return id("villager_child_gore_after");
            if (GoreTunnelLead.hasUsefulRumor(player, village)) return id("villager_child_gore_rumor");
        }
        if (roll == 1 && findUsefulPerson(player, village).isPresent()) {
            return id("villager_child_helpful_person");
        }
        if (roll == 2 && village != null && village.hasBoard()) {
            return id("villager_child_helpful_board");
        }

        int variant = Math.floorMod(seed >>> 5, 6);
        return switch (variant) {
            case 1 -> id("villager_child_v2");
            case 2 -> id("villager_child_v3");
            case 3 -> id("villager_child_v4");
            case 4 -> id("villager_child_v5");
            case 5 -> id("villager_child_v6");
            default -> id("villager_child");
        };
    }

    /** Stable per-person ambient voice; a villager does not change personality on every click. */
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

        player.displayClientMessage(
                Component.literal(villager.getDisplayName().getString() + ", the " + profession
                                + ", is the person I'd ask. They're " + where + ".")
                        .withStyle(ChatFormatting.GOLD),
                true
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
