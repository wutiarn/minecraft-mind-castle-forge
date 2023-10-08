package ru.wtrn.minecraft.mindpalace.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RailTraverser {
    private BlockPos previousPos;
    private Direction direction;
    private final Level level;

    public RailTraverser(BlockPos startPos, Direction direction, Level level) {
        this.previousPos = startPos;
        this.direction = direction;
        this.level = level;
    }

    public NextBlock next() {
        if (direction == null) {
            return null;
        }
        BlockState prevBlockState = level.getBlockState(previousPos);
        if (!(prevBlockState.getBlock() instanceof BaseRailBlock prevBlock)) {
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

        Vec3 prevDelta = previousPos.getCenter().subtract(currentPos.getCenter());

        BlockState currentBlockState = level.getBlockState(previousPos);
        if (!(currentBlockState.getBlock() instanceof BaseRailBlock currentBlock)) {
            return null;
        }

        RailState currentRailState = new RailState(level, previousPos, prevBlockState);
        Direction nextDirection = null;
        List<BlockPos> currentRailConnections = currentRailState.getConnections();
        if (currentRailConnections.size() == 2) {
            // Rail has only two connections, one of which must be previous block. And another is the next one.
            for (BlockPos connection : currentRailConnections) {
                if (connection.equals(previousPos)) {
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

    public static class NextBlock {
        public BlockPos pos;

        public BaseRailBlock block;
        public BlockState state;
        public Direction prevDirection;
        public Direction nextDirection;
        public Vec3 deltaFromPrevious;
    }
}
