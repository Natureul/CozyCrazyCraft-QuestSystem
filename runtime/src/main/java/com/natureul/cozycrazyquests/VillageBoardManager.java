package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class VillageBoardManager {
    private static final int PLAYER_CHECK_INTERVAL = 200;
    private static final int MEETING_SEARCH_RADIUS = 56;
    private static final int BOARD_SEARCH_RADIUS = 64;
    private static final int BOARD_SEARCH_VERTICAL = 10;
    private static final int PLACEMENT_RADIUS = 12;
    private static final long RETRY_COOLDOWN = 2400L;

    private static final Set<String> SATISFIED = new HashSet<>();
    private static final Map<String, Long> RETRY_AFTER = new HashMap<>();

    private VillageBoardManager() {}

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        long gameTime = level.getGameTime();
        int offset = Math.floorMod(player.getId(), PLAYER_CHECK_INTERVAL);
        if (Math.floorMod((int) (gameTime % PLAYER_CHECK_INTERVAL), PLAYER_CHECK_INTERVAL) != offset) return;

        Optional<BlockPos> meeting = level.getPoiManager().findClosest(
                holder -> holder.is(PoiTypes.MEETING),
                pos -> true,
                player.blockPosition(),
                MEETING_SEARCH_RADIUS,
                PoiManager.Occupancy.ANY
        );
        if (meeting.isEmpty()) return;

        BlockPos center = meeting.get();
        if (!level.isVillage(center)) return;

        String key = settlementKey(level, center);
        if (SATISFIED.contains(key)) return;
        if (gameTime < RETRY_AFTER.getOrDefault(key, 0L)) return;

        Block boardBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("bountiful", "bountyboard"));
        if (boardBlock == null || boardBlock == Blocks.AIR) {
            RETRY_AFTER.put(key, gameTime + RETRY_COOLDOWN);
            return;
        }

        BlockPos existing = findExistingBoard(level, center, boardBlock);
        if (existing != null) {
            SATISFIED.add(key);
            RETRY_AFTER.remove(key);
            CozyCrazyQuests.LOGGER.debug("Village at {} already has bounty board at {}", center, existing);
            return;
        }

        BlockPos placed = placeCivicBoard(level, center, boardBlock);
        if (placed != null) {
            SATISFIED.add(key);
            RETRY_AFTER.remove(key);
            CozyCrazyQuests.LOGGER.info("Added civic bounty board at {} for boardless village centered near {}", placed, center);
        } else {
            RETRY_AFTER.put(key, gameTime + RETRY_COOLDOWN);
        }
    }

    private static BlockPos findExistingBoard(ServerLevel level, BlockPos center, Block boardBlock) {
        int minY = Math.max(level.getMinBuildHeight(), center.getY() - BOARD_SEARCH_VERTICAL);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, center.getY() + BOARD_SEARCH_VERTICAL);

        for (int dx = -BOARD_SEARCH_RADIUS; dx <= BOARD_SEARCH_RADIUS; dx++) {
            for (int dz = -BOARD_SEARCH_RADIUS; dz <= BOARD_SEARCH_RADIUS; dz++) {
                if (dx * dx + dz * dz > BOARD_SEARCH_RADIUS * BOARD_SEARCH_RADIUS) continue;
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (level.getBlockState(pos).is(boardBlock)) return pos;
                }
            }
        }
        return null;
    }

    private static BlockPos placeCivicBoard(ServerLevel level, BlockPos center, Block boardBlock) {
        for (int radius = 2; radius <= PLACEMENT_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!safePlacement(level, pos)) continue;

                    BlockState state = boardBlock.defaultBlockState();
                    if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                        Direction towardCenter = Direction.getNearest(center.getX() - x, 0, center.getZ() - z);
                        if (towardCenter.getAxis().isHorizontal()) {
                            state = state.setValue(HorizontalDirectionalBlock.FACING, towardCenter);
                        }
                    }

                    if (level.setBlock(pos, state, Block.UPDATE_ALL)) return pos;
                }
            }
        }
        return null;
    }

    private static boolean safePlacement(ServerLevel level, BlockPos pos) {
        BlockState here = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());

        if (!here.isAir() || !above.isAir()) return false;
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.below()).isEmpty()) return false;
        if (!below.isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        if (below.is(Blocks.FARMLAND) || below.is(Blocks.DIRT_PATH)) return false;
        if (below.is(Blocks.CHEST) || below.is(Blocks.BARREL) || below.is(Blocks.BELL)) return false;
        return true;
    }

    private static String settlementKey(ServerLevel level, BlockPos center) {
        // Meeting POIs in the same village can move slightly as the village changes. Coarsen the
        // key so nearby bells/meeting points converge on one runtime record instead of duplicating boards.
        int cx = Math.floorDiv(center.getX(), 64);
        int cz = Math.floorDiv(center.getZ(), 64);
        return level.dimension().location() + ":" + cx + ":" + cz;
    }
}
