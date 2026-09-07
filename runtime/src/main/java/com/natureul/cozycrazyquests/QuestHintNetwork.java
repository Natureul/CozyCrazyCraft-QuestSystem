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
 * Daggerfall-style social navigation for an already accepted authored contract.
 *
 * The contract gives the player a credible starting bearing, not an instruction to blindly walk
 * hundreds of blocks and hope. Once work is active, ordinary residents can know nothing, repeat a
 * rumor, refer the player to somebody better informed, narrow the approach, identify the place by
 * name, or (where plausible) mark it on the Atlas. At least one loaded adult in the issuing village
 * is designated as a fallback guide so bad NPC rolls cannot make the whole settlement useless.
 *
 * The original giver is part of this network too. Before completion, asking the person who issued the
 * contract for more help is a sensible player action and must not dead-end into a generic reminder.
 * Completion/turn-in dialogue still wins because this network returns nothing once the objective is done.
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

        if (!(speaker instanceof Villager villager)) {
            return id("hint_structure_guard");
        }

        VillagerProfession profession = villager.getVillagerData().getProfession();
        String approach = active.getString("target_approach");
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
        if (active.isEmpty() || objectiveComplete(active) || active.getString("target_structure").isBlank()) return false;

        Optional<Villager> specialist = findSpecialist(level, player, village, active);
        if (specialist.isPresent()) {
            Villager villager = specialist.get();
            VillagerNameService.ensureNamed(level, villager);
            long dx = (long) villager.blockPosition().getX() - player.blockPosition().getX();
            long dz = (long) villager.blockPosition().getZ() - player.blockPosition().getZ();
            int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
            String where = distance <= 10 ? "right nearby" : "about " + distance + " blocks " + direction(dx, dz);
            player.displayClientMessage(
                    Component.literal("Ask " + villager.getDisplayName().getString() + ", the "
                                    + professionLabel(villager.getVillagerData().getProfession()) + ". They're " + where + ".")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
            return true;
        }

        // No specialist loaded? The village still must not strand the player. Give a usable lead now.
        return giveLead(player, PlayerKnowledgeState.Knowledge.LEAD, 100, false);
    }

    static boolean consumeAction(ServerPlayer player, String action) {
        return switch (action) {
            case "quest_hint_rumor" -> giveLead(player, PlayerKnowledgeState.Knowledge.RUMOR, 200, true);
            case "quest_hint_lead" -> giveLead(player, PlayerKnowledgeState.Knowledge.LEAD, 100, true);
            case "quest_hint_identify" -> giveLead(player, PlayerKnowledgeState.Knowledge.KNOWN, 50, false);
            case "quest_hint_referral" -> routeForActiveQuest(player);
            default -> false;
        };
    }

    private static boolean giveLead(
            ServerPlayer player,
            PlayerKnowledgeState.Knowledge knowledge,
            int rounding,
            boolean descriptiveOnly
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

        BlockPos village = readPos(active, "village");
        BlockPos target = readPos(active, "target");
        int distance = roundedDistance(village, target, rounding);
        String bearing = direction(
                (long) target.getX() - village.getX(),
                (long) target.getZ() - village.getZ()
        );
        String approach = active.getString("target_approach");
        String subject = descriptiveOnly ? descriptiveSubject(active) : active.getString("target_name");
        if (subject.isBlank()) subject = descriptiveSubject(active);

        String extra = switch (approach) {
            case "UNDERGROUND" -> " It's underground; the surface bearing is only where to begin looking for a descent. Once you are actually inside the place, the survey counts — do not chase an imaginary depth below it.";
            case "SUBMERGED" -> " It's below the waterline; search the water around that bearing rather than the shore alone.";
            default -> " Look for the landmark itself once you're in that area.";
        };

        player.displayClientMessage(
                Component.literal(subject + ": roughly " + distance + " blocks " + bearing + "." + extra)
                        .withStyle(knowledge == PlayerKnowledgeState.Knowledge.KNOWN ? ChatFormatting.AQUA : ChatFormatting.GOLD),
                true
        );
        return true;
    }

    private static Optional<Villager> findSpecialist(
            ServerLevel level,
            ServerPlayer player,
            VillageContext village,
            CompoundTag active
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

        AABB area = new AABB(village.anchor()).inflate(SOCIAL_RADIUS, 64, SOCIAL_RADIUS);
        return level.getEntitiesOfClass(Villager.class, area, villager ->
                        !villager.isBaby()
                                && wanted.contains(villager.getVillagerData().getProfession()))
                .stream()
                .min(Comparator.comparingInt(v -> wanted.indexOf(v.getVillagerData().getProfession()) * 10000
                        + (int) Math.min(9999, v.distanceToSqr(player))));
    }

    private static boolean isFallbackGuide(
            ServerLevel level,
            VillageContext village,
            Villager speaker,
            String questId
    ) {
        AABB area = new AABB(village.anchor()).inflate(SOCIAL_RADIUS, 64, SOCIAL_RADIUS);
        return level.getEntitiesOfClass(Villager.class, area, villager -> !villager.isBaby()).stream()
                .min(Comparator.comparingInt(villager -> Math.floorMod(villager.getUUID().hashCode() ^ questId.hashCode(), Integer.MAX_VALUE)))
                .map(villager -> villager.getUUID().equals(speaker.getUUID()))
                .orElse(false);
    }

    private static String descriptiveSubject(CompoundTag active) {
        ResourceLocation id = ResourceLocation.tryParse(active.getString("target_structure"));
        if (id == null) return "The place you're looking for";
        String path = id.getPath().replace('_', ' ');
        if (path.endsWith(" x")) path = path.substring(0, path.length() - 2);
        return "The " + path;
    }

    private static boolean objectiveComplete(CompoundTag active) {
        return active.getBoolean("objective_complete") || active.getBoolean("surveyed");
    }

    private static BlockPos readPos(CompoundTag tag, String prefix) {
        return new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z"));
    }

    private static int roundedDistance(BlockPos from, BlockPos to, int step) {
        long dx = (long) to.getX() - from.getX();
        long dz = (long) to.getZ() - from.getZ();
        int exact = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        return Math.max(step, (int) Math.round(exact / (double) step) * step);
    }

    private static String professionLabel(VillagerProfession profession) {
        ResourceLocation key = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
        return key == null ? "villager" : key.getPath().replace('_', ' ');
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
