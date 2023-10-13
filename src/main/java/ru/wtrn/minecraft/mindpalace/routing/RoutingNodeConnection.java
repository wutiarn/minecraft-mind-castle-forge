package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Arrays;

public class RoutingNodeConnection {
    private String src;
    private String dst;
    private Direction direction;
    private int distance;

    public RoutingNodeConnection(BlockPos src, BlockPos dst, Direction direction, int distance) {
        this.src = blockPosToString(src);
        this.dst = blockPosToString(dst);
        this.direction = direction;
        this.distance = distance;
    }

    public BlockPos getSrc() {
        return blockPosFromString(src);
    }

    public BlockPos getDst() {
        return blockPosFromString(dst);
    }

    public Direction getDirection() {
        return direction;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return src + " > " + direction + " " + distance + " > " + dst;
    }

    private String blockPosToString(BlockPos pos) {
        return pos.getX() + "/" + pos.getY() + "/" + pos.getZ();
    }

    private BlockPos blockPosFromString(String posStr) {
        int[] split = Arrays.stream(posStr.split("/")).mapToInt(Integer::parseInt).toArray();
        return new BlockPos(split[0], split[1], split[2]);
    }
}
