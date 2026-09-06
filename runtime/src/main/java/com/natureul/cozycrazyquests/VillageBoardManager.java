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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class VillageBoardManager {
    private static final int PLAYER_CHECK_INTERVAL = 200;
    private static final int MEETING_SEARCH_RADIUS = 56;

    // A same-settlement pair of board/meeting detections in the playtest landed about 65 blocks
    // apart. Ninety-six blocks is deliberately wide enough to coalesce that case without turning
    // this into a general world scan.
    private static final int SETTLEMENT_COALESCE_RADIUS = 96;
    private static final int BOARD_SEARCH_RADIUS = 96;
    private static final int PLACEMENT_RADIUS = 12;
    private static final long RETRY_COOLDOWN = 2400L;

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

        VillageBoardSavedData savedData = VillageBoardSavedData.get(level);
        if (savedData.findNearby(center, SETTLEMENT_COALESCE_RADIUS).isPresent()) return;

        String retryKey = retryKey(level, center);
        if (gameTime < RETRY_AFTER.getOrDefault(retryKey, 0L)) return;

        Block boardBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("bountiful", "bountyboard"));
        if (boardBlock == null || boardBlock == Blocks.AIR) {
            RETRY_AFTER.put(retryKey, gameTime + RETRY_COOLDOWN);
            return;
        }

        BlockPos existing = findExistingBoard(level, center, boardBlock);
        if (existing != null) {
            savedData.remember(center, existing, VillageBoardSavedData.Status.FOUND_EXISTING, gameTime);
            RETRY_AFTER.remove(retryKey);
            CozyCrazyQuests.LOGGER.info(
                    "Recorded existing civic bounty board at {} for village centered near {} ({} saved settlements)",
                    existing, center, savedData.recordCount()
            );
            return;
        }

        BlockPos placed = placeCivicBoard(level, center, boardBlock);
        if (placed != null) {
            savedData.remember(center, placed, VillageBoardSavedData.Status.PLACED_REPAIR, gameTime);
            RETRY_AFTER.remove(retryKey);
            CozyCrazyQuests.LOGGER.info(
                    "Added civic bounty board at {} for boardless village centered near {} ({} saved settlements)",
                    placed, center, savedData.recordCount()
            );
        } else {
            RETRY_AFTER.put(retryKey, gameTime + RETRY_COOLDOWN);
        }
    }

    /**
     * Search only block entities in chunks that are already loaded. Bountiful boards are block
     * entities, so this is both more accurate and dramatically cheaper than touching every block
     * in a 96-block-radius cylinder. We deliberately do not load/generate chunks for this check.
     */
    private static BlockPos findExistingBoard(ServerLevel level, BlockPos center, Block boardBlock) {
        int minChunkX = Math.floorDiv(center.getX() - BOARD_SEARCH_RADIUS, 16);
        int maxChunkX = Math.floorDiv(center.getX() + BOARD_SEARCH_RADIUS, 16);
        int minChunkZ = Math.floorDiv(center.getZ() - BOARD_SEARCH_RADIUS, 16);
        int maxChunkZ = Math.floorDiv(center.getZ() + BOARD_SEARCH_RADIUS, 16);
        long radiusSq = (long) BOARD_SEARCH_RADIUS * BOARD_SEARCH_RADIUS;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                    long dx = (long) pos.getX() - center.getX();
                    long dz = (long) pos.getZ() - center.getZ();
                    if (dx * dx + dz * dz > radiusSq) continue;
                    if (level.getBlockState(pos).is(boardBlock)) return pos.immutable();
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

    private static String retryKey(ServerLevel level, BlockPos center) {
        int cx = Math.floorDiv(center.getX(), SETTLEMENT_COALESCE_RADIUS);
        int cz = Math.floorDiv(center.getZ(), SETTLEMENT_COALESCE_RADIUS);
        return level.dimension().location() + ":" + cx + ":" + cz;
    }
}
