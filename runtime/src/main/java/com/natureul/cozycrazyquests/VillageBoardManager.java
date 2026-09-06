package com.natureul.cozycrazyquests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import java.util.UUID;

public final class VillageBoardManager {
    private static final int PLAYER_CHECK_INTERVAL = 200;
    private static final int MEETING_SEARCH_RADIUS = 56;

    // A same-settlement pair of board/meeting detections in the playtest landed about 65 blocks
    // apart. Ninety-six blocks is deliberately wide enough to coalesce that case without turning
    // this into a general world scan.
    private static final int SETTLEMENT_COALESCE_RADIUS = 96;
    private static final int BOARD_SEARCH_RADIUS = 96;
    private static final int PLACEMENT_RADIUS = 12;
    private static final int VERTICAL_PLACEMENT_SEARCH = 6;
    private static final long RETRY_COOLDOWN = 2400L;

    // A board can exist and still be effectively invisible in a dense or snowy village. Give the
    // player a restrained locator while they are actually in that settlement rather than adding a
    // permanent waypoint or chat spam.
    private static final int HINT_MIN_DISTANCE = 12;
    private static final long HINT_COOLDOWN = 600L;

    private static final Map<String, Long> RETRY_AFTER = new HashMap<>();
    private static final Map<UUID, Long> LAST_HINT_AT = new HashMap<>();

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

        Block boardBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("bountiful", "bountyboard"));
        if (boardBlock == null || boardBlock == Blocks.AIR) {
            RETRY_AFTER.put(retryKey(level, center), gameTime + RETRY_COOLDOWN);
            return;
        }

        VillageBoardSavedData savedData = VillageBoardSavedData.get(level);
        Optional<VillageBoardSavedData.VillageRecord> known =
                savedData.findNearby(center, SETTLEMENT_COALESCE_RADIUS);
        if (known.isPresent()) {
            hintKnownBoard(player, level, known.get().board(), boardBlock, gameTime);
            return;
        }

        String retryKey = retryKey(level, center);
        if (gameTime < RETRY_AFTER.getOrDefault(retryKey, 0L)) return;

        BlockPos existing = findExistingBoard(level, center, boardBlock);
        if (existing != null) {
            savedData.remember(center, existing, VillageBoardSavedData.Status.FOUND_EXISTING, gameTime);
            RETRY_AFTER.remove(retryKey);
            hintKnownBoard(player, level, existing, boardBlock, gameTime);
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
            hintKnownBoard(player, level, placed, boardBlock, gameTime);
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
        // First choice: an outdoor piece of ground beside a village path and close to the bell's
        // elevation. This makes the board read as civic furniture instead of occasionally landing
        // on a roof selected by a heightmap.
        BlockPos preferred = findGroundCandidate(level, center, true);
        if (preferred != null && setBoard(level, center, preferred, boardBlock)) return preferred;

        // Second choice: any safe outdoor ground near the meeting point at roughly the same height.
        BlockPos nearby = findGroundCandidate(level, center, false);
        if (nearby != null && setBoard(level, center, nearby, boardBlock)) return nearby;

        // Last-resort compatibility path for unusually steep/custom villages. Keep the old
        // heightmap strategy, but only after the more legible ground-level searches fail.
        for (int radius = 2; radius <= PLACEMENT_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!safePlacement(level, pos)) continue;
                    if (setBoard(level, center, pos, boardBlock)) return pos;
                }
            }
        }
        return null;
    }

    private static BlockPos findGroundCandidate(ServerLevel level, BlockPos center, boolean requirePathNeighbor) {
        for (int radius = 2; radius <= PLACEMENT_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;

                    // Prefer the bell's own level, then fan upward/downward. This avoids choosing
                    // a roof just because it happens to be the highest motion-blocking surface.
                    for (int step = 0; step <= VERTICAL_PLACEMENT_SEARCH; step++) {
                        int[] offsets = step == 0 ? new int[]{0} : new int[]{step, -step};
                        for (int yOffset : offsets) {
                            BlockPos pos = new BlockPos(x, center.getY() + yOffset, z);
                            if (!safePlacement(level, pos)) continue;
                            if (requirePathNeighbor && !besideVillagePath(level, pos)) continue;
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean besideVillagePath(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborGround = pos.relative(direction).below();
            if (level.getBlockState(neighborGround).is(Blocks.DIRT_PATH)) return true;
        }
        return false;
    }

    private static boolean setBoard(ServerLevel level, BlockPos center, BlockPos pos, Block boardBlock) {
        BlockState state = boardBlock.defaultBlockState();
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction towardCenter = Direction.getNearest(
                    center.getX() - pos.getX(), 0, center.getZ() - pos.getZ());
            if (towardCenter.getAxis().isHorizontal()) {
                state = state.setValue(HorizontalDirectionalBlock.FACING, towardCenter);
            }
        }
        return level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private static boolean safePlacement(ServerLevel level, BlockPos pos) {
        BlockState here = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());

        boolean replaceableGroundCover = here.isAir()
                || here.is(Blocks.SNOW)
                || here.is(Blocks.GRASS)
                || here.is(Blocks.TALL_GRASS);
        if (!replaceableGroundCover || !above.isAir()) return false;
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.below()).isEmpty()) return false;
        if (!below.isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        if (below.is(Blocks.FARMLAND) || below.is(Blocks.DIRT_PATH)) return false;
        if (below.is(Blocks.CHEST) || below.is(Blocks.BARREL) || below.is(Blocks.BELL)) return false;

        // Boards are civic landmarks; hiding one inside a house defeats the guarantee even if the
        // block placement itself is technically safe.
        return level.canSeeSky(pos.above());
    }

    private static void hintKnownBoard(
            ServerPlayer player,
            ServerLevel level,
            BlockPos boardPos,
            Block boardBlock,
            long gameTime
    ) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(
                Math.floorDiv(boardPos.getX(), 16),
                Math.floorDiv(boardPos.getZ(), 16));
        if (chunk == null || !level.getBlockState(boardPos).is(boardBlock)) return;

        long dx = (long) boardPos.getX() - player.blockPosition().getX();
        long dz = (long) boardPos.getZ() - player.blockPosition().getZ();
        long horizontalSq = dx * dx + dz * dz;
        if (horizontalSq <= (long) HINT_MIN_DISTANCE * HINT_MIN_DISTANCE) return;

        long last = LAST_HINT_AT.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2);
        if (gameTime - last < HINT_COOLDOWN) return;
        LAST_HINT_AT.put(player.getUUID(), gameTime);

        int distance = (int) Math.round(Math.sqrt(horizontalSq));
        String direction = compassDirection(dx, dz);
        player.displayClientMessage(
                Component.literal("Village Notice Board  •  " + distance + " blocks " + direction)
                        .withStyle(ChatFormatting.GOLD),
                true
        );
    }

    private static String compassDirection(long dx, long dz) {
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

    private static String retryKey(ServerLevel level, BlockPos center) {
        int cx = Math.floorDiv(center.getX(), SETTLEMENT_COALESCE_RADIUS);
        int cz = Math.floorDiv(center.getZ(), SETTLEMENT_COALESCE_RADIUS);
        return level.dimension().location() + ":" + cx + ":" + cz;
    }
}
