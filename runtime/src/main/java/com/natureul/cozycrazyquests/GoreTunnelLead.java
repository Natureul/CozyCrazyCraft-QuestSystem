package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Small word-of-mouth side chain for Skarrier's Tunnel Gore lair.
 *
 * This deliberately does not turn the lair into radial progression. The structure remains a vertical,
 * underground encounter; the social system merely notices a real nearby instance and lets a child
 * occasionally become the first person to mention it. A mason/toolsmith/librarian can then turn the
 * rumor into a usable lead. Reaching the real lair and returning pays a one-off expedition reward.
 *
 * No structure means no rumor. No global scan runs in the background: the expensive locate happens
 * lazily on the rare child dialogue roll and is cached per village for five Minecraft minutes.
 */
final class GoreTunnelLead {
    private static final ResourceLocation GORE_LAIR = new ResourceLocation("skarrier_mobs", "tunnel_gore_lair_x");
    private static final int SEARCH_RADIUS = 2200;
    private static final int DISCOVERY_RADIUS = 96;
    private static final int VERTICAL_TOLERANCE = 72;
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

    static ResourceLocation adultDialogue(ServerPlayer player, Villager villager, VillageContext village) {
        if (village == null || !suitableAdult(villager.getVillagerData().getProfession())) return null;
        CompoundTag state = state(player);
        if (!sameVillage(state, village) || !state.contains("targetX")) return null;
        if (STAGE_COMPLETE.equals(state.getString("stage"))) return null;
        if (state.getBoolean("surveyed")) return id("gore_tunnel_return");
        if (STAGE_LEAD.equals(state.getString("stage"))) return id("gore_tunnel_adult_active");
        if (STAGE_RUMOR.equals(state.getString("stage"))) return id("gore_tunnel_adult_followup");
        return null;
    }

    static boolean consumeAction(ServerPlayer player, String action) {
        return switch (action) {
            case "child_gore_hint" -> shareChildRumor(player);
            case "gore_tunnel_accept" -> acceptAdultLead(player);
            case "gore_tunnel_turnin" -> turnIn(player);
            default -> false;
        };
    }

    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        CompoundTag state = state(player);
        if (!STAGE_LEAD.equals(state.getString("stage")) || state.getBoolean("surveyed")) return;
        if (!player.serverLevel().dimension().location().toString().equals(state.getString("dimension"))) return;

        BlockPos target = readTarget(state);
        long dx = (long) player.blockPosition().getX() - target.getX();
        long dz = (long) player.blockPosition().getZ() - target.getZ();
        if (dx * dx + dz * dz > (long) DISCOVERY_RADIUS * DISCOVERY_RADIUS) return;
        if (Math.abs(player.blockPosition().getY() - target.getY()) > VERTICAL_TOLERANCE) return;

        state.putBoolean("surveyed", true);
        save(player, state);
        PlayerKnowledgeState.advance(
                player,
                state.getString("target_key"),
                PlayerKnowledgeState.Knowledge.KNOWN,
                PlayerKnowledgeState.Provenance.LOCAL_OBSERVATION
        );
        player.sendSystemMessage(
                Component.literal("You found the place beneath the rumor. Return to a mason, toolsmith, or librarian in "
                                + displayVillage(state) + ".")
                        .withStyle(ChatFormatting.AQUA)
        );
    }

    private static boolean shareChildRumor(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        VillageContext village = VillageContext.resolve(level, player.blockPosition());
        if (village == null) return true;

        CompoundTag state = state(player);
        if (!sameVillage(state, village) || !state.contains("targetX")) {
            NearbyStructureResolver.ResolvedStructure target = resolve(level, village);
            if (target == null) {
                player.displayClientMessage(Component.literal("Whatever the child heard, it is not close enough to place.")
                        .withStyle(ChatFormatting.GRAY), true);
                return true;
            }
            state = new CompoundTag();
            state.putString("village_key", village.key());
            state.putString("village_name", village.name());
            state.putString("dimension", level.dimension().location().toString());
            state.putString("stage", STAGE_RUMOR);
            putTarget(state, target.pos());
            state.putString("target_structure", target.id().toString());
            String targetName = NamedPlaceBridge.structureName(level, target.id(), target.pos());
            state.putString("target_name", targetName);
            state.putString("target_key", targetKey(level, target));
            save(player, state);
        }

        BlockPos target = readTarget(state);
        int distance = roundedDistance(player.blockPosition(), target, 100);
        String where = direction(player.blockPosition(), target);
        PlayerKnowledgeState.advance(
                player,
                state.getString("target_key"),
                PlayerKnowledgeState.Knowledge.RUMOR,
                PlayerKnowledgeState.Provenance.RUMOR_NETWORK
        );
        player.sendSystemMessage(
                Component.literal("The child points " + where + ". \"Somewhere under there. Maybe about " + distance
                                + " blocks? Ask somebody who knows stone.\"")
                        .withStyle(ChatFormatting.GOLD)
        );
        return true;
    }

    private static boolean acceptAdultLead(ServerPlayer player) {
        CompoundTag state = state(player);
        if (!STAGE_RUMOR.equals(state.getString("stage")) || !state.contains("targetX")) return true;

        state.putString("stage", STAGE_LEAD);
        save(player, state);
        PlayerKnowledgeState.advance(
                player,
                state.getString("target_key"),
                PlayerKnowledgeState.Knowledge.LEAD,
                PlayerKnowledgeState.Provenance.PROFESSION_EVIDENCE
        );

        BlockPos target = readTarget(state);
        int distance = roundedDistance(player.blockPosition(), target, 50);
        player.sendSystemMessage(
                Component.literal("Deep-road lead: roughly " + distance + " blocks "
                                + direction(player.blockPosition(), target)
                                + ". The source is underground; do not expect a surface doorway.")
                        .withStyle(ChatFormatting.GOLD)
        );
        giveOrDrop(player, new ItemStack(Items.TORCH, 12));
        return true;
    }

    private static boolean turnIn(ServerPlayer player) {
        CompoundTag state = state(player);
        if (!state.getBoolean("surveyed") || STAGE_COMPLETE.equals(state.getString("stage"))) return true;

        VillageContext village = VillageContext.resolve(player.serverLevel(), player.blockPosition());
        if (village == null || !sameVillage(state, village)) {
            player.displayClientMessage(Component.literal("Return to the village that gave the deep-road lead.")
                    .withStyle(ChatFormatting.GRAY), true);
            return true;
        }

        ItemStack weapon = stack("spartanweaponry:iron_warhammer", "Gorebreaker");
        enchant(weapon, "minecraft:sharpness", 2);
        enchant(weapon, "minecraft:unbreaking", 2);
        giveOrDrop(player, weapon);

        ItemStack boots = stack("minecraft:iron_boots", "Deep-Road Boots");
        enchant(boots, "minecraft:feather_falling", 2);
        enchant(boots, "minecraft:unbreaking", 1);
        giveOrDrop(player, boots);
        giveOrDrop(player, new ItemStack(Items.EMERALD, 8));
        player.giveExperiencePoints(12);

        state.putString("stage", STAGE_COMPLETE);
        save(player, state);
        PlayerKnowledgeState.advance(
                player,
                state.getString("target_key"),
                PlayerKnowledgeState.Knowledge.CONFIRMED,
                PlayerKnowledgeState.Provenance.QUEST_PROOF
        );
        player.sendSystemMessage(
                Component.literal("The village believes the story now. Gorebreaker and the Deep-Road Boots are yours.")
                        .withStyle(ChatFormatting.GREEN)
        );
        return true;
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

    private static boolean suitableAdult(VillagerProfession profession) {
        return profession == VillagerProfession.MASON
                || profession == VillagerProfession.TOOLSMITH
                || profession == VillagerProfession.LIBRARIAN;
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

    private static String displayVillage(CompoundTag state) {
        String name = state.getString("village_name");
        return name.isBlank() || "the village".equalsIgnoreCase(name) ? "the issuing village" : name;
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

    private static ItemStack stack(String itemId, String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(Component.literal(name).withStyle(ChatFormatting.GOLD));
        return stack;
    }

    private static void enchant(ItemStack stack, String enchantId, int level) {
        if (stack.isEmpty()) return;
        var enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantId));
        if (enchantment != null) stack.enchant(enchantment, level);
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!player.addItem(stack)) player.drop(stack, false);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(CozyCrazyQuests.MOD_ID, path);
    }

    private record CachedTarget(NearbyStructureResolver.ResolvedStructure target, long checkedAt) {}
}
