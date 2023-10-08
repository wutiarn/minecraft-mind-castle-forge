package ru.wtrn.minecraft.mindpalace.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class RailTraverser implements Iterable<RailTraverser.NextBlock>, Iterator<RailTraverser.NextBlock> {
    private BlockPos previousPos;
    private Direction direction;
    private final Level level;

    public RailTraverser(BlockPos startPos, Direction direction, Level level) {
        this.previousPos = startPos;
        this.direction = direction;
        this.level = level;
    }

    @Override
    public boolean hasNext() {
        return nextImpl(false) != null;
    }

    @Override
    public NextBlock next() {
        NextBlock nextBlock = nextImpl(true);
        if (nextBlock == null) {
            throw new NoSuchElementException();
        }
        return nextBlock;
    }

    private NextBlock nextImpl(boolean updateState) {
        if (direction == null) {
            return null;
        }
        BlockState prevBlockState = getRailBlockState(previousPos, level);
        if (prevBlockState == null) {
            return null;
        }

        RailState prevRailState = new RailState(level, previousPos, prevBlockState);
        List<BlockPos> prevConnections = prevRailState.getConnections();

        if (prevConnections.isEmpty()) {
            return null;
        }

        BlockPos currentPos = null;
        for (BlockPos connection : prevConnections) {
            Direction connectionDirection = getDirection(previousPos, connection);
            if (connectionDirection == direction) {
                currentPos = connection;
                break;
            }
        }
        if (currentPos == null) {
            return null;
        }

        Vec3 prevDelta = currentPos.getCenter().subtract(previousPos.getCenter());

        BlockState currentBlockState = getRailBlockState(currentPos, level);
        if (currentBlockState == null) {
            return null;
        }

        RailState currentRailState = new RailState(level, currentPos, prevBlockState);
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

        NextBlock result = new NextBlock();
        result.pos = currentPos;
        result.block = (BaseRailBlock) currentBlockState.getBlock();
        result.state = currentBlockState;
        result.prevDirection = direction;
        result.nextDirection = nextDirection;
        result.deltaFromPrevious = prevDelta;

        if (updateState) {
            this.previousPos = currentPos;
            this.direction = nextDirection;
        }

        return result;
    }

    private static Direction getDirection(BlockPos currentPos, BlockPos nextPos) {
        return Direction.fromDelta(nextPos.getX() - currentPos.getX(), 0, nextPos.getZ() - currentPos.getZ());
    }

    private BlockState getRailBlockState(BlockPos pos, Level level) {
        BlockState blockState = level.getBlockState(pos);
        if ((blockState.getBlock() instanceof BaseRailBlock)) {
            return blockState;
        }

        // Check one block below due to strange implementation of net.minecraft.world.level.block.RailState.getConnections
        blockState = level.getBlockState(pos.below());
        if ((blockState.getBlock() instanceof BaseRailBlock)) {
            return blockState;
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
    }
}
