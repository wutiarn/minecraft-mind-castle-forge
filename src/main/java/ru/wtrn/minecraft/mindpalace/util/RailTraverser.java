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
        return direction != null;
    }

    @Override
    public NextBlock next() {
        if (direction == null) {
            throw new NoSuchElementException();
        }
        BlockState prevBlockState = level.getBlockState(previousPos);
        if (!(prevBlockState.getBlock() instanceof BaseRailBlock prevBlock)) {
            throw new NoSuchElementException();
        }

        RailState prevRailState = new RailState(level, previousPos, prevBlockState);
        List<BlockPos> prevConnections = prevRailState.getConnections();

        if (prevConnections.isEmpty()) {
            throw new NoSuchElementException();
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
            throw new NoSuchElementException();
        }

        Vec3 prevDelta = currentPos.getCenter().subtract(previousPos.getCenter());

        BlockState currentBlockState = level.getBlockState(previousPos);
        if (!(currentBlockState.getBlock() instanceof BaseRailBlock currentBlock)) {
            throw new NoSuchElementException();
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
        result.block = currentBlock;
        result.state = currentBlockState;
        result.prevDirection = direction;
        result.nextDirection = nextDirection;
        result.deltaFromPrevious = prevDelta;

        this.previousPos = currentPos;
        this.direction = nextDirection;

        return result;
    }

    private static Direction getDirection(BlockPos currentPos, BlockPos nextPos) {
        return Direction.fromDelta(nextPos.getX() - currentPos.getX(),0, nextPos.getZ() - currentPos.getZ());
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
