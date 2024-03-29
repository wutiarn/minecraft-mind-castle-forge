package ru.wtrn.minecraft.mindpalace.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class RailTraverser implements Iterable<RailTraverser.NextBlock>, Iterator<RailTraverser.NextBlock> {
    private BlockPos previousPos;
    private Direction direction;
    private final Level level;
    private HashSet<BlockPos> visitedBlocks = new HashSet<>();
    private NextBlock precalculatedNext = null;
    private int traversedBlocksCount = 0;

    public RailTraverser(BlockPos startPos, Direction direction, Level level) {
        this.previousPos = startPos;
        this.direction = direction;
        this.level = level;
    }

    @Override
    public boolean hasNext() {
        if (precalculatedNext != null) {
            return true;
        }
        precalculatedNext = nextImpl();
        return precalculatedNext != null;
    }

    @Override
    public NextBlock next() {
        if (precalculatedNext != null) {
            NextBlock result = precalculatedNext;
            precalculatedNext = null;
            return result;
        }
        NextBlock result = nextImpl();
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    private NextBlock nextImpl() {
        if (direction == null) {
            return null;
        }
        BlockPos previousPos = this.previousPos;
        BlockPos currentPos = previousPos.relative(direction);
        if (visitedBlocks.contains(currentPos)) {
            return null;
        }

        FoundBaseRailBlock foundCurrentBlock = getRailBlockState(currentPos, level);
        if (foundCurrentBlock == null) {
            return null;
        }
        BlockState currentBlockState = foundCurrentBlock.state;
        currentPos = foundCurrentBlock.pos;

        RailState currentRailState = new RailState(level, currentPos, currentBlockState);
        Direction nextDirection = null;
        List<BlockPos> currentRailConnections = currentRailState.getConnections();
        if (currentRailConnections.size() == 2) {
            // Rail has only two connections, one of which must be previous block. And another is the next one.
            for (BlockPos connection : currentRailConnections) {
                if (connection.getX() == previousPos.getX() && connection.getZ() == previousPos.getZ()) {
                    continue;
                }
                nextDirection = getDirection(currentPos, connection);
            }
        }
        BaseRailBlock currentBlock = (BaseRailBlock) currentBlockState.getBlock();
        RailShape railDirection = (currentBlock).getRailDirection(currentBlockState, level, currentPos, null);

        NextBlock result = new NextBlock();
        result.pos = currentPos;
        result.block = currentBlock;
        result.state = currentBlockState;
        result.prevDirection = direction;
        result.nextDirection = nextDirection;
        result.deltaFromPrevious = currentPos.getCenter().subtract(previousPos.getCenter());
        result.traversedBlocksCount = ++traversedBlocksCount;

        this.previousPos = currentPos;
        this.direction = nextDirection;
        switch (railDirection) {
            case SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST -> visitedBlocks.add(currentPos);
        }

        return result;
    }

    private static Direction getDirection(BlockPos currentPos, BlockPos nextPos) {
        return Direction.fromDelta(nextPos.getX() - currentPos.getX(), 0, nextPos.getZ() - currentPos.getZ());
    }

    private FoundBaseRailBlock getRailBlockState(BlockPos pos, Level level) {
        BlockState blockState = level.getBlockState(pos);
        if ((blockState.getBlock() instanceof BaseRailBlock)) {
            return new FoundBaseRailBlock(pos, blockState);
        }

        // Neighbour rails can be one block below...
        blockState = level.getBlockState(pos.below());
        if ((blockState.getBlock() instanceof BaseRailBlock)) {
            return new FoundBaseRailBlock(pos.below(), blockState);
        }

        // ... or one block above
        blockState = level.getBlockState(pos.above());
        if ((blockState.getBlock() instanceof BaseRailBlock)) {
            return new FoundBaseRailBlock(pos.above(), blockState);
        }
        return null;
    }

    @NotNull
    @Override
    public Iterator<NextBlock> iterator() {
        return this;
    }

    public static class NextBlock {
        public BlockPos pos;

        public BaseRailBlock block;
        public BlockState state;
        public Direction prevDirection;
        public Direction nextDirection;
        public Vec3 deltaFromPrevious;
        public int traversedBlocksCount;
    }

    static class FoundBaseRailBlock {
        BlockPos pos;
        BlockState state;

        public FoundBaseRailBlock(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }
    }
}
