package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word-of-mouth discovery path for Skarrier's ore-rich Tunnel Gore lair.
 *
 * The lair is the reward. Killing the Gore is explicitly NOT an objective and is not tracked here.
 * A child can very rarely start the rumor, but children are only one route: once the player is at
 * least Recognized in a village, appropriate stone/tool specialists may independently share a real
 * nearby deep-road site as valuable local knowledge. The social lead can mark the surface position
 * on the Atlas, and reaching the real underground structure simply confirms the discovery.
 *
 * No structure means no rumor or reward. The structure locate is lazy and cached per village; there
 * is no background world scan.
 */
final class GoreTunnelLead {
    private static final ResourceLocation GORE_LAIR = new ResourceLocation("skarrier_mobs", "tunnel_gore_lair_x");
    private static final int SEARCH_RADIUS = 2000;
    private static final int DISCOVERY_RADIUS = 104;
    private static final int VERTICAL_TOLERANCE = 80;
    private static final long CACHE_LIFETIME = 6000L;
    private static final String ROOT = "CozyCrazyGoreTunnelLead";
    private static final String STAGE_RUMOR = "RUMOR";
    private static final String STAGE_LEAD = "LEAD";
    private static final String STAGE_COMPLETE = "COMPLETE";

    private static final Map<String, CachedTarget> CACHE = new HashMap<>();

    private GoreTunnelLead() {}

    static boolean hasUsefulRumor(ServerPlayer player, VillageContext village) {
        if (village == null) return false;
        CompoundTag state = state(player);
        if (sameVillage(state, village) && STAGE_COMPLETE.equals(state.getString("stage"))) return false;
        if (sameVillage(state, village) && state.contains("targetX")) return true;
        return resolve(player.serverLevel(), village) != null;
    }

    static boolean completedFor(ServerPlayer player, VillageContext village) {
        CompoundTag state = state(player);
        return village != null && sameVillage(state, village) && STAGE_COMPLETE.equals(state.getString("stage"));
    }

    static ResourceLocation adultDialogue(ServerPlayer player, Villager villager, VillageContext village) {
        if (village == null) return null;
        VillagerProfession profession = villager.getVillagerData().getProfession();
        CompoundTag state = state(player);

        if (sameVillage(state, village) && state.contains("targetX")) {
            if (STAGE_COMPLETE.equals(state.getString("stage"))) return null;
            if (STAGE_LEAD.equals(state.getString("stage")) && usefulSpecialist(profession)) {
                return id("gore_tunnel_adult_active");
            }
            if (STAGE_RUMOR.equals(state.getString("stage")) && rumorInterpreter(profession)) {
                return id("gore_tunnel_adult_followup");
            }
        }

        if (!originSpecialist(profession)) return null;
        VillageProgressState.Trust trust = VillageProgressState.snapshot(player, village.key()).trust();
        if (trust.ordinal() < VillageProgressState.Trust.RECOGNIZED.ordinal()) return null;
        if (resolve(player.serverLevel(), village) == null) return null;
        return id("gore_tunnel_specialist_offer");
    }

    static boolean consumeAction(ServerPlayer player, String action) {
        return switch (action) {
            case "child_gore_hint" -> shareChildRumor(player);
            case "gore_tunnel_accept", "gore_tunnel_specialist_reveal" -> revealSpecialistLead(player);
            default -> false;
        };
    }

    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        CompoundTag state = state(player);
        if (!STAGE_LEAD.equals(state.getString("stage"))) return;
        if (!player.serverLevel().dimension().location().toString().equals(state.getString("dimension"))) return;

        BlockPos target = readTarget(state);
        long dx = (long) player.blockPosition().getX() - target.getX();
        long dz = (long) player.blockPosition().getZ() - target.getZ();
        if (dx * dx + dz * dz > (long) DISCOVERY_RADIUS * DISCOVERY_RADIUS) return;
        if (Math.abs(player.blockPosition().getY() - target.getY()) > VERTICAL_TOLERANCE) return;

        state.putString("stage", STAGE_COMPLETE);
        state.putBoolean("surveyed", true);
        save(player, state);
        PlayerKnowledgeState.advance(
                player,
                state.getString("target_key"),
                PlayerKnowledgeState.Knowledge.CONFIRMED,
                PlayerKnowledgeState.Provenance.LOCAL_OBSERVATION
        );
        player.displayClientMessage(
                Component.literal("You found " + state.getString("target_name")
                                + ". The deep-road story was true; what you do with the tunnels is up to you.")
                        .withStyle(ChatFormatting.AQUA),
                true
        );
    }

    static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        if (oldData.contains(ROOT, Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put(ROOT, oldData.getCompound(ROOT).copy());
        }
    }

    private static boolean shareChildRumor(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        VillageContext village = VillageContext.resolve(level, player.blockPosition());
        if (village == null) return true;

        CompoundTag state = state(player);
        if (!sameVillage(state, village) || !state.contains("targetX")) {
            NearbyStructureResolver.ResolvedStructure target = resolve(level, village);
            if (target == null) return true;
            state = new CompoundTag();
            writeTargetState(state, level, village, target, STAGE_RUMOR);
            save(player, state);
        }

        BlockPos target = readTarget(state);
        int distance = roundedDistance(village.anchor(), target, 200);
        String where = direction(village.anchor(), target);
        PlayerKnowledgeState.advance(
                player,
                state.getString("target_key"),
                PlayerKnowledgeState.Knowledge.RUMOR,
                PlayerKnowledgeState.Provenance.RUMOR_NETWORK
        );
        player.displayClientMessage(
                Component.literal("The child points " + where + ": somewhere underground, maybe " + distance
                                + " blocks out. They insist someone who knows stone has heard it too.")
                        .withStyle(ChatFormatting.GOLD),
                true
        );
        return true;
    }

    private static boolean revealSpecialistLead(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        VillageContext village = VillageContext.resolve(level, player.blockPosition());
        if (village == null) return true;

        CompoundTag state = state(player);
        if (!sameVillage(state, village) || !state.contains("targetX")) {
            NearbyStructureResolver.ResolvedStructure target = resolve(level, village);
            if (target == null) return true;
            state = new CompoundTag();
            writeTargetState(state, level, village, target, STAGE_LEAD);
        } else {
            state.putString("stage", STAGE_LEAD);
        }
        save(player, state);

        PlayerKnowledgeState.advance(
                player,
                state.getString("target_key"),
                PlayerKnowledgeState.Knowledge.KNOWN,
                PlayerKnowledgeState.Provenance.PROFESSION_EVIDENCE
        );

        boolean marked = NamedPlaceBridge.revealStructureToAtlas(
                player,
                GORE_LAIR,
                readTarget(state),
                state.getString("target_name")
        );

        // A small prospector's courtesy; the valuable reward is the ore-rich location itself.
        giveOrDrop(player, new ItemStack(Items.TORCH, 12));

        BlockPos target = readTarget(state);
        int distance = roundedDistance(village.anchor(), target, 50);
        String message = state.getString("target_name") + ": about " + distance + " blocks "
                + direction(village.anchor(), target)
                + ". It is underground; the surface position is a reference, not an entrance.";
        if (marked) message += " I've marked that surface position on your Atlas.";
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    private static void writeTargetState(
            CompoundTag state,
            ServerLevel level,
            VillageContext village,
            NearbyStructureResolver.ResolvedStructure target,
            String stage
    ) {
        state.putString("village_key", village.key());
        state.putString("village_name", village.name());
        state.putString("dimension", level.dimension().location().toString());
        state.putString("stage", stage);
        putTarget(state, target.pos());
        state.putString("target_structure", target.id().toString());
        state.putString("target_name", NamedPlaceBridge.structureName(level, target.id(), target.pos()));
        state.putString("target_key", targetKey(level, target));
    }

    private static NearbyStructureResolver.ResolvedStructure resolve(ServerLevel level, VillageContext village) {
        String key = level.getSeed() + ":" + level.dimension().location() + ":" + village.key();
        CachedTarget cached = CACHE.get(key);
        if (cached != null && level.getGameTime() - cached.checkedAt() <= CACHE_LIFETIME) return cached.target();
        NearbyStructureResolver.ResolvedStructure found = NearbyStructureResolver.findNearest(
                level,
                village.anchor(),
                List.of(GORE_LAIR),
                SEARCH_RADIUS
        );
        CACHE.put(key, new CachedTarget(found, level.getGameTime()));
        return found;
    }

    private static boolean originSpecialist(VillagerProfession profession) {
        return profession == VillagerProfession.MASON
                || profession == VillagerProfession.TOOLSMITH
                || profession == VillagerProfession.WEAPONSMITH;
    }

    private static boolean rumorInterpreter(VillagerProfession profession) {
        return usefulSpecialist(profession)
                || profession == VillagerProfession.CARTOGRAPHER
                || profession == VillagerProfession.LIBRARIAN;
    }

    private static boolean usefulSpecialist(VillagerProfession profession) {
        return profession == VillagerProfession.MASON
                || profession == VillagerProfession.TOOLSMITH
                || profession == VillagerProfession.WEAPONSMITH;
    }

    private static CompoundTag state(ServerPlayer player) {
        return player.getPersistentData().getCompound(ROOT);
    }

    private static void save(ServerPlayer player, CompoundTag state) {
        player.getPersistentData().put(ROOT, state);
    }

    private static boolean sameVillage(CompoundTag state, VillageContext village) {
        return !state.isEmpty() && village.key().equals(state.getString("village_key"));
    }

    private static String targetKey(ServerLevel level, NearbyStructureResolver.ResolvedStructure target) {
        return level.dimension().location() + "|structure|" + target.id() + "|"
                + Math.floorDiv(target.pos().getX(), 16) + "," + Math.floorDiv(target.pos().getZ(), 16);
    }

    private static void putTarget(CompoundTag tag, BlockPos pos) {
        tag.putInt("targetX", pos.getX());
        tag.putInt("targetY", pos.getY());
        tag.putInt("targetZ", pos.getZ());
    }

    private static BlockPos readTarget(CompoundTag tag) {
        return new BlockPos(tag.getInt("targetX"), tag.getInt("targetY"), tag.getInt("targetZ"));
    }

    private static int roundedDistance(BlockPos from, BlockPos to, int step) {
        long dx = (long) to.getX() - from.getX();
        long dz = (long) to.getZ() - from.getZ();
        int exact = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        return Math.max(step, (int) Math.round(exact / (double) step) * step);
    }

    private static String direction(BlockPos from, BlockPos to) {
        long dx = (long) to.getX() - from.getX();
        long dz = (long) to.getZ() - from.getZ();
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

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) player.drop(stack, false);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }

    private record CachedTarget(NearbyStructureResolver.ResolvedStructure target, long checkedAt) {}
}
