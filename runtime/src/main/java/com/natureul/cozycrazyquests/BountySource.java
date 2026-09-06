package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * Small, pack-owned provenance stamp placed on Bountiful bounty items.
 *
 * Bountiful already generates each bounty with the issuing board position, but it does not retain
 * that position as a redemption constraint. We retain only the minimum information needed to make
 * Village Trust local: dimension + exact board block position.
 *
 * Legacy/foreign Bountiful bounties without this tag intentionally fail open. That keeps old
 * playtest items usable and means a reflection/mixin failure cannot strand a player's bounty.
 */
public final class BountySource {
    private static final String ROOT = "CozyCrazySource";
    private static final String DIMENSION = "dimension";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";

    private BountySource() {}

    public static void stamp(ItemStack stack, ServerLevel level, BlockPos boardPos) {
        if (stack == null || stack.isEmpty() || hasSource(stack)) return;

        CompoundTag source = stack.getOrCreateTagElement(ROOT);
        source.putString(DIMENSION, level.dimension().location().toString());
        source.putInt(X, boardPos.getX());
        source.putInt(Y, boardPos.getY());
        source.putInt(Z, boardPos.getZ());
    }

    public static boolean hasSource(ItemStack stack) {
        CompoundTag source = sourceTag(stack);
        return source != null
                && source.contains(DIMENSION, Tag.TAG_STRING)
                && source.contains(X, Tag.TAG_INT)
                && source.contains(Y, Tag.TAG_INT)
                && source.contains(Z, Tag.TAG_INT);
    }

    /**
     * Untagged bounties are legacy-compatible and may be redeemed anywhere. Tagged bounties must
     * return to their exact issuing board in their issuing dimension.
     */
    public static boolean mayRedeemAt(ItemStack stack, ServerLevel level, BlockPos boardPos) {
        CompoundTag source = sourceTag(stack);
        if (source == null || !hasSource(stack)) return true;

        if (!level.dimension().location().toString().equals(source.getString(DIMENSION))) return false;
        return source.getInt(X) == boardPos.getX()
                && source.getInt(Y) == boardPos.getY()
                && source.getInt(Z) == boardPos.getZ();
    }

    public static BlockPos sourcePos(ItemStack stack) {
        CompoundTag source = sourceTag(stack);
        if (source == null || !hasSource(stack)) return null;
        return new BlockPos(source.getInt(X), source.getInt(Y), source.getInt(Z));
    }

    private static CompoundTag sourceTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        return stack.getTagElement(ROOT);
    }
}
