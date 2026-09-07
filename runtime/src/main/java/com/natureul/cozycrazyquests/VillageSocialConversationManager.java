package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
 * Village civic roles are independent of vanilla trade professions. A settlement full of unemployed
 * adults therefore still has people who can route the player without the quest system silently asking
 * the player to manufacture workstations just to make the village function.
 *
 * Ambient, hint and Gore pages are refreshable social state, not permanent entity identity. Before
 * falling back to generic chatter we clear only those known refreshable CozyCrazyQuests IDs. Authored
 * quest offer/active/turn-in pages from the higher-priority managers remain sticky and are never cleared.
 */
public final class VillageSocialConversationManager {
    private static final int BOARD_DIRECTION_RADIUS = 192;
    private static final int SOCIAL_ROUTE_RADIUS = 176;
    private static final ResourceLocation GUARD_TYPE = new ResourceLocation("guardvillagers", "guard");
    private static final String OWN_PREFIX = CozyCrazyQuests.MOD_ID + ":";

    private VillageSocialConversationManager() {}

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (!ConversationBridge.available() || !ConversationBridge.supports(target)) return;

        if (target instanceof Villager villager) {
            VillagerNameService.ensureNamed(level, villager);
            VillageContext village = VillageContext.resolve(level, villager.blockPosition());

            if (villager.isBaby()) {
                clearRefreshableSocialDialogue(villager);
                if (!ConversationBridge.hasOwnDialogue(villager) && !ConversationBridge.hasDialogue(villager)) {
                    ConversationBridge.setDialogue(villager, childDialogue(player, villager, village));
                }
                return;
            }

            // Do not replace the actual giver's active reminder with the same generic hint page every
            // specialist can say. Civic representatives also keep their authored reminder when vanilla
            // profession compatibility was precisely what the fallback layer was compensating for.
            if (village != null && ConversationBridge.hasOwnDialogue(villager)
                    && preserveAuthoredActiveVoice(player, level, village, villager)) {
                return;
            }

            if (village != null) {
                ResourceLocation activeHint = QuestHintNetwork.dialogue(player, villager, village);
                if (activeHint != null) {
                    ConversationBridge.setDialogue(villager, activeHint);
                    return;
                }
            }

            // A previous click may have left this entity on an ambient, hint or earlier Gore-stage page.
            // Re-evaluate those pages now; never clear authored quest pages selected by higher-priority code.
            clearRefreshableSocialDialogue(villager);
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

        clearRefreshableSocialDialogue(target);
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

    private static boolean preserveAuthoredActiveVoice(
            ServerPlayer player,
            ServerLevel level,
            VillageContext village,
            Villager villager
    ) {
        CompoundTag root = VillageQuestState.root(player);
        CompoundTag active = VillageQuestState.activeForVillage(root, village.key());
        if (active.isEmpty()) return false;
        if (villager.getUUID().toString().equals(active.getString("giver_uuid"))) return true;

        VillageQuestCatalog.Definition definition = VillageQuestCatalog.byId(active.getString("quest_id"));
        return definition != null
                && !definition.accepts(villager.getVillagerData().getProfession())
                && VillageCivicRoleService.isCivicContact(level, village, villager);
    }

    private static ResourceLocation genericVillagerDialogue(
            ServerPlayer player,
            Villager villager,
            VillageContext village
    ) {
        if (village != null) {
            VillageCivicRoleService.Role role = VillageCivicRoleService.roleFor(player.serverLevel(), village, villager);
            if (role.isCivicContact()) return id(role.dialoguePath());
        }

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

    /** Only social-state pages are refreshable; authored quest pages selected earlier in the event are not. */
    private static void clearRefreshableSocialDialogue(LivingEntity entity) {
        String current = ConversationBridge.currentDialogueId(entity);
        if (!isRefreshableSocialDialogue(current)) return;
        ConversationBridge.clearOwnDialogue(entity);
    }

    private static boolean isRefreshableSocialDialogue(String dialogueId) {
        if (dialogueId == null || !dialogueId.startsWith(OWN_PREFIX)) return false;
        String path = dialogueId.substring(OWN_PREFIX.length());
        return path.startsWith("villager_")
                || path.startsWith("hint_")
                || path.startsWith("gore_tunnel_")
                || path.startsWith("guard_local")
                || "civic_steward".equals(path)
                || "civic_roadwarden".equals(path)
                || "civic_quartermaster".equals(path)
                || "civic_watch_contact".equals(path);
    }

    private static void routeToUsefulPerson(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        VillageContext village = VillageContext.resolve(level, player.blockPosition());
        Optional<Villager> candidate = findUsefulPerson(player, village);
        if (candidate.isEmpty()) return;

        Villager villager = candidate.get();
        VillagerNameService.ensureNamed(level, villager);
        long dx = (long) villager.blockPosition().getX() - player.blockPosition().getX();
        long dz = (long) villager.blockPosition().getZ() - player.blockPosition().getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        String label = usefulLabel(level, village, villager);
        String where = distance <= 10 ? "nearby" : distance + " " + shortDirection(dx, dz);
        String name = villager.getDisplayName().getString();
        String message = name + " • " + label + " • " + where;
        if (message.length() > 45) message = name + " • " + where;
        if (message.length() > 45) {
            if (name.length() > 26) name = name.substring(0, 23) + "...";
            message = name + " • " + where;
        }

        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.GOLD), true);
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
        List<Villager> adults = level.getEntitiesOfClass(Villager.class, area, villager -> {
                    if (villager.isBaby()) return false;
                    VillageContext theirs = VillageContext.resolve(level, villager.blockPosition());
                    return theirs != null && village.key().equals(theirs.key());
                });

        Optional<Villager> professionContact = adults.stream()
                .filter(villager -> wanted.contains(villager.getVillagerData().getProfession()))
                .min(Comparator.comparingDouble(v -> v.distanceToSqr(player)));
        if (professionContact.isPresent()) return professionContact;

        List<Villager> civic = adults.stream()
                .filter(villager -> VillageCivicRoleService.isCivicContact(level, village, villager))
                .sorted(Comparator.comparingDouble(v -> v.distanceToSqr(player)))
                .toList();
        if (civic.isEmpty()) return Optional.empty();

        String currentSpeaker = VillageQuestState.conversationSpeaker(VillageQuestState.root(player));
        for (Villager villager : civic) {
            if (!villager.getUUID().toString().equals(currentSpeaker)) return Optional.of(villager);
        }
        return Optional.of(civic.get(0));
    }

    private static String usefulLabel(ServerLevel level, VillageContext village, Villager villager) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession != VillagerProfession.NONE && profession != VillagerProfession.NITWIT) {
            return professionLabel(profession);
        }
        VillageCivicRoleService.Role role = VillageCivicRoleService.roleFor(level, village, villager);
        return role.isCivicContact() ? role.label() : "resident";
    }

    private static void pointToNoticeBoard(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Optional<VillageBoardSavedData.VillageRecord> record = VillageBoardSavedData.get(level)
                .findNearby(player.blockPosition(), BOARD_DIRECTION_RADIUS);
        if (record.isEmpty()) {
            player.displayClientMessage(Component.literal("No nearby notice board.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        BlockPos board = record.get().board();
        long dx = (long) board.getX() - player.blockPosition().getX();
        long dz = (long) board.getZ() - player.blockPosition().getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        player.displayClientMessage(
                Component.literal("Board • " + distance + " " + shortDirection(dx, dz))
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

    private static String shortDirection(long dx, long dz) {
        double angle = Math.atan2(dx, -dz);
        int octant = Math.floorMod((int) Math.round(angle / (Math.PI / 4.0)), 8);
        return switch (octant) {
            case 0 -> "N";
            case 1 -> "NE";
            case 2 -> "E";
            case 3 -> "SE";
            case 4 -> "S";
            case 5 -> "SW";
            case 6 -> "W";
            default -> "NW";
        };
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }
}
