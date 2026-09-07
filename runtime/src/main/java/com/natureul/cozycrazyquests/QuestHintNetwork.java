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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Daggerfall-style social navigation for authored village progression.
 *
 * NPC speech belongs in Conversations. Runtime actions here only change knowledge, create a justified Atlas
 * marker, or emit tiny state feedback. RUMOR and LEAD never silently reveal an exact marker; KNOWN may mark
 * a destination when the speaker's conversation establishes reliable knowledge. Underground/submerged KNOWN
 * markers use a surface approach rather than the chamber/structure center.
 */
final class QuestHintNetwork {
    private static final int SOCIAL_RADIUS = 176;

    private QuestHintNetwork() {}

    static ResourceLocation dialogue(ServerPlayer player, LivingEntity speaker, VillageContext village) {
        if (village == null) return null;
        CompoundTag root = VillageQuestState.root(player);
        CompoundTag active = VillageQuestState.activeForVillage(root, village.key());
        if (active.isEmpty() || objectiveComplete(active)) return null;
        if (active.getString("target_structure").isBlank()) return null;

        VillageQuestState.noteConversation(root, village.key(), speaker.getUUID().toString());
        VillageQuestState.save(player, root);

        String approach = active.getString("target_approach");
        boolean underground = "UNDERGROUND".equals(approach) || "SUBMERGED".equals(approach);
        PlayerKnowledgeState.Knowledge current = PlayerKnowledgeState.knowledge(player, active.getString("target_key"));
        if (current == PlayerKnowledgeState.Knowledge.CONFIRMED) {
            return id(underground ? "hint_underground_confirmed" : "hint_structure_confirmed");
        }
        if (current == PlayerKnowledgeState.Knowledge.KNOWN) {
            return id(underground ? "hint_underground_known" : "hint_structure_known");
        }

        if (!(speaker instanceof Villager villager)) return id("hint_structure_guard");

        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.CARTOGRAPHER) return id("hint_cartographer_target");
        if (profession == VillagerProfession.MASON) return id("hint_structure_mason");
        if (profession == VillagerProfession.LIBRARIAN || profession == VillagerProfession.CLERIC) {
            return id("hint_structure_records");
        }
        if (profession == VillagerProfession.FISHERMAN && "SUBMERGED".equals(approach)) {
            return id("hint_structure_water");
        }
        if (profession == VillagerProfession.TOOLSMITH
                || profession == VillagerProfession.WEAPONSMITH
                || profession == VillagerProfession.ARMORER
                || profession == VillagerProfession.FLETCHER) {
            return id("hint_structure_route");
        }

        if (isFallbackGuide(player.serverLevel(), village, villager, active.getString("quest_id"))) {
            return id("hint_structure_lead");
        }

        int roll = Math.floorMod(villager.getUUID().hashCode() * 31 + active.getString("quest_id").hashCode(), 100);
        if (roll < 20) return id("hint_structure_unknown");
        if (roll < 45) return id("hint_structure_rumor");
        if (roll < 70) return id("hint_structure_referral");
        return id("hint_structure_lead");
    }

    static boolean routeForActiveQuest(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        VillageContext village = VillageContext.resolve(level, player.blockPosition());
        if (village == null) return false;
        CompoundTag root = VillageQuestState.root(player);
        CompoundTag active = VillageQuestState.activeForVillage(root, village.key());
        if (active.isEmpty() || objectiveComplete(active) || active.getString("target_structure").isBlank()) {
            return routeForVillageProgress(player, level, village, root);
        }

        Optional<Villager> specialist = findSpecialist(level, player, village, active, root);
        if (specialist.isPresent()) {
            Villager villager = specialist.get();
            VillagerNameService.ensureNamed(level, villager);
            player.displayClientMessage(
                    Component.literal(compactReferral(player, villager)).withStyle(ChatFormatting.GOLD),
                    true
            );
            return true;
        }

        return giveLead(player, PlayerKnowledgeState.Knowledge.LEAD, 100);
    }

    static boolean consumeAction(ServerPlayer player, String action) {
        return switch (action) {
            case "quest_hint_rumor" -> giveLead(player, PlayerKnowledgeState.Knowledge.RUMOR, 200);
            case "quest_hint_lead" -> giveLead(player, PlayerKnowledgeState.Knowledge.LEAD, 100);
            case "quest_hint_identify" -> giveLead(player, PlayerKnowledgeState.Knowledge.KNOWN, 50);
            case "quest_hint_referral" -> routeForActiveQuest(player);
            default -> false;
        };
    }

    private static boolean giveLead(
            ServerPlayer player,
            PlayerKnowledgeState.Knowledge knowledge,
            int rounding
    ) {
        CompoundTag root = VillageQuestState.root(player);
        String villageKey = VillageQuestState.conversationVillage(root);
        if (villageKey.isBlank()) {
            VillageContext village = VillageContext.resolve(player.serverLevel(), player.blockPosition());
            if (village != null) villageKey = village.key();
        }
        CompoundTag active = VillageQuestState.activeForVillage(root, villageKey);
        if (active.isEmpty() || active.getString("target_structure").isBlank()) return false;

        String targetKey = active.getString("target_key");
        if (!targetKey.isBlank()) {
            PlayerKnowledgeState.Provenance provenance = knowledge == PlayerKnowledgeState.Knowledge.KNOWN
                    ? PlayerKnowledgeState.Provenance.PROFESSION_EVIDENCE
                    : PlayerKnowledgeState.Provenance.VILLAGE_REPORT;
            PlayerKnowledgeState.advance(player, targetKey, knowledge, provenance);
        }

        boolean marked = knowledge == PlayerKnowledgeState.Knowledge.KNOWN && markKnownTarget(player, active);
        String status = switch (knowledge) {
            case UNKNOWN -> "No reliable lead.";
            case RUMOR -> "Rumor: " + compactRoute(active, rounding);
            case LEAD -> "Lead: " + compactRoute(active, rounding);
            case KNOWN -> marked ? "Atlas marked." : "Place identified.";
            case CONFIRMED -> "Already confirmed.";
        };
        player.displayClientMessage(
                Component.literal(status)
                        .withStyle(knowledge.ordinal() >= PlayerKnowledgeState.Knowledge.KNOWN.ordinal()
                                ? ChatFormatting.AQUA : ChatFormatting.GOLD),
                true
        );
        return true;
    }

    /** Only the KNOWN path calls this method; RUMOR and LEAD cannot reveal an Atlas marker. */
    private static boolean markKnownTarget(ServerPlayer player, CompoundTag active) {
        ResourceLocation structureId = ResourceLocation.tryParse(active.getString("target_structure"));
        if (structureId == null) return false;
        BlockPos target = readPos(active, "target");
        String approach = active.getString("target_approach");
        BlockPos navigationAnchor = ("UNDERGROUND".equals(approach) || "SUBMERGED".equals(approach))
                ? NamedPlaceBridge.surfaceApproach(player.serverLevel(), structureId, target, readPos(active, "village"))
                : target;
        boolean marked = NamedPlaceBridge.revealStructureToAtlas(
                player,
                structureId,
                target,
                active.getString("target_name"),
                navigationAnchor
        );
        String targetKey = active.getString("target_key");
        if (marked && !targetKey.isBlank()) {
            PlayerKnowledgeState.advance(
                    player,
                    targetKey,
                    PlayerKnowledgeState.Knowledge.KNOWN,
                    PlayerKnowledgeState.Provenance.MAP_RECORD
            );
        }
        return marked;
    }

    private static Optional<Villager> findSpecialist(
            ServerLevel level,
            ServerPlayer player,
            VillageContext village,
            CompoundTag active,
            CompoundTag root
    ) {
        String approach = active.getString("target_approach");
        List<VillagerProfession> wanted = switch (approach) {
            case "UNDERGROUND" -> List.of(
                    VillagerProfession.MASON,
                    VillagerProfession.TOOLSMITH,
                    VillagerProfession.CARTOGRAPHER,
                    VillagerProfession.LIBRARIAN,
                    VillagerProfession.CLERIC
            );
            case "SUBMERGED" -> List.of(
                    VillagerProfession.FISHERMAN,
                    VillagerProfession.CARTOGRAPHER,
                    VillagerProfession.MASON,
                    VillagerProfession.LIBRARIAN,
                    VillagerProfession.CLERIC
            );
            default -> List.of(
                    VillagerProfession.CARTOGRAPHER,
                    VillagerProfession.MASON,
                    VillagerProfession.LIBRARIAN,
                    VillagerProfession.FLETCHER,
                    VillagerProfession.TOOLSMITH
            );
        };

        String currentSpeaker = VillageQuestState.conversationSpeaker(root);
        AABB area = new AABB(village.anchor()).inflate(SOCIAL_RADIUS, 64, SOCIAL_RADIUS);
        List<Villager> candidates = level.getEntitiesOfClass(Villager.class, area, villager -> {
                    if (villager.isBaby() || !wanted.contains(villager.getVillagerData().getProfession())) return false;
                    VillageContext theirs = VillageContext.resolve(level, villager.blockPosition());
                    return theirs != null && village.key().equals(theirs.key());
                });

        Optional<Villager> other = candidates.stream()
                .filter(villager -> !villager.getUUID().toString().equals(currentSpeaker))
                .min(Comparator.comparingInt(v -> wanted.indexOf(v.getVillagerData().getProfession()) * 10000
                        + (int) Math.min(9999, v.distanceToSqr(player))));
        if (other.isPresent()) return other;

        return candidates.stream()
                .min(Comparator.comparingInt(v -> wanted.indexOf(v.getVillagerData().getProfession()) * 10000
                        + (int) Math.min(9999, v.distanceToSqr(player))));
    }

    private static boolean routeForVillageProgress(
            ServerPlayer player,
            ServerLevel level,
            VillageContext village,
            CompoundTag root
    ) {
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

        String currentSpeaker = VillageQuestState.conversationSpeaker(root);
        AABB area = new AABB(village.anchor()).inflate(SOCIAL_RADIUS, 64, SOCIAL_RADIUS);
        List<Villager> adults = level.getEntitiesOfClass(Villager.class, area, villager -> {
                    if (villager.isBaby()) return false;
                    VillageContext theirs = VillageContext.resolve(level, villager.blockPosition());
                    return theirs != null && village.key().equals(theirs.key());
                });

        Optional<Villager> professionContact = adults.stream()
                .filter(villager -> !villager.getUUID().toString().equals(currentSpeaker))
                .filter(villager -> wanted.contains(villager.getVillagerData().getProfession()))
                .min(Comparator.comparingInt(v -> wanted.indexOf(v.getVillagerData().getProfession()) * 10000
                        + (int) Math.min(9999, v.distanceToSqr(player))));

        Optional<Villager> contact = professionContact.isPresent()
                ? professionContact
                : adults.stream()
                        .filter(villager -> !villager.getUUID().toString().equals(currentSpeaker))
                        .filter(villager -> VillageCivicRoleService.isCivicContact(level, village, villager))
                        .min(Comparator.comparingDouble(v -> v.distanceToSqr(player)));

        if (contact.isPresent()) {
            Villager villager = contact.get();
            VillagerNameService.ensureNamed(level, villager);
            player.displayClientMessage(
                    Component.literal(compactReferral(player, villager)).withStyle(ChatFormatting.GOLD),
                    true
            );
            return true;
        }

        String fallback = village.hasBoard() ? "Try the notice board." : "No other contact nearby.";
        player.displayClientMessage(Component.literal(fallback).withStyle(ChatFormatting.GRAY), true);
        return true;
    }

    private static boolean isFallbackGuide(
            ServerLevel level,
            VillageContext village,
            Villager speaker,
            String questId
    ) {
        AABB area = new AABB(village.anchor()).inflate(SOCIAL_RADIUS, 64, SOCIAL_RADIUS);
        return level.getEntitiesOfClass(Villager.class, area, villager -> {
                    if (villager.isBaby()) return false;
                    VillageContext theirs = VillageContext.resolve(level, villager.blockPosition());
                    return theirs != null && village.key().equals(theirs.key());
                }).stream()
                .min(Comparator.comparingInt(villager -> Math.floorMod(villager.getUUID().hashCode() ^ questId.hashCode(), Integer.MAX_VALUE)))
                .map(villager -> villager.getUUID().equals(speaker.getUUID()))
                .orElse(false);
    }

    private static String compactReferral(ServerPlayer player, Villager villager) {
        long dx = (long) villager.blockPosition().getX() - player.blockPosition().getX();
        long dz = (long) villager.blockPosition().getZ() - player.blockPosition().getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        String name = villager.getDisplayName().getString();
        String profession = professionLabel(villager.getVillagerData().getProfession());
        if (villager.getVillagerData().getProfession() == VillagerProfession.NONE
                || villager.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
            VillageContext village = VillageContext.resolve(player.serverLevel(), villager.blockPosition());
            if (village != null) {
                VillageCivicRoleService.Role role = VillageCivicRoleService.roleFor(player.serverLevel(), village, villager);
                if (role.isCivicContact()) profession = role.label();
            }
        }
        String route = distance <= 10 ? "nearby" : distance + " " + shortDirection(dx, dz);
        String message = name + " • " + profession + " • " + route;
        if (message.length() <= 45) return message;
        message = name + " • " + route;
        if (message.length() <= 45) return message;
        if (name.length() > 26) name = name.substring(0, 23) + "...";
        return name + " • " + route;
    }

    private static String compactRoute(CompoundTag active, int rounding) {
        int distance = Math.max(0, active.getInt("target_distance"));
        String direction = shortDirection(active.getString("target_direction"));
        if (distance <= 0) return direction.isBlank() ? "local area" : direction;

        String range;
        if (rounding > 1 && distance < rounding) {
            range = "<" + rounding;
        } else if (rounding > 1) {
            int rounded = Math.max(rounding, (int) Math.round(distance / (double) rounding) * rounding);
            range = "~" + rounded;
        } else {
            range = Integer.toString(distance);
        }
        return direction.isBlank() ? range : range + " " + direction;
    }

    private static String shortDirection(String direction) {
        return switch (direction == null ? "" : direction.toLowerCase()) {
            case "north" -> "N";
            case "northeast" -> "NE";
            case "east" -> "E";
            case "southeast" -> "SE";
            case "south" -> "S";
            case "southwest" -> "SW";
            case "west" -> "W";
            case "northwest" -> "NW";
            default -> "";
        };
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

    private static boolean objectiveComplete(CompoundTag active) {
        return active.getBoolean("objective_complete") || active.getBoolean("surveyed");
    }

    private static BlockPos readPos(CompoundTag tag, String prefix) {
        return new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"));
    }

    private static String professionLabel(VillagerProfession profession) {
        ResourceLocation key = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
        return key == null ? "villager" : key.getPath().replace('_', ' ');
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }
}
