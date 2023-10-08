package ru.wtrn.minecraft.mindpalace.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

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
        BlockPos currentPos = previousPos.relative(direction);
        BlockState currentBlockState = level.getBlockState(currentPos);
        if (!(currentBlockState.getBlock() instanceof BaseRailBlock currentBlock)) {
            return null;
        }

        RailShape railDirection = currentBlock.getRailDirection(currentBlockState, level, previousPos, null);

        RailState railState = new RailState(level, currentPos, currentBlockState);
        List<BlockPos> connections = railState.getConnections();

        if (connections.size() != 2) {
            return null;
        }

        //noinspection OptionalGetWithoutIsPresent
        BlockPos nextBlockPos = connections.stream().filter(it -> it.equals(previousPos)).findFirst().get();
        Direction nextDirection = getNextDirection(currentPos, nextBlockPos);

        this.previousPos = currentPos;
        this.direction = nextDirection;

        return new NextBlock(currentPos, currentBlockState);
    }

    private static Direction getNextDirection(BlockPos currentPos, BlockPos nextPos) {
        return Direction.fromDelta(nextPos.getX() - currentPos.getX(),nextPos.getY() - currentPos.getY(), nextPos.getZ() - currentPos.getZ());
    }

    public static class NextBlock {
        public BlockPos pos;
        public BlockState state;

        public NextBlock(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }
    }
}
