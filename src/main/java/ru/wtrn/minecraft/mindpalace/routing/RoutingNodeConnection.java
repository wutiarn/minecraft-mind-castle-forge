package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import ru.wtrn.minecraft.mindpalace.util.BlockPosUtil;

import java.util.Arrays;

public class RoutingNodeConnection {
    private String src;
    private String dst;
    private Direction direction;
    private int distance;

    public RoutingNodeConnection(BlockPos src, BlockPos dst, Direction direction, int distance) {
        this.src = BlockPosUtil.blockPosToString(src);
        this.dst = BlockPosUtil.blockPosToString(dst);
        this.direction = direction;
        this.distance = distance;
    }

    public BlockPos getSrc() {
        return BlockPosUtil.blockPosFromString(src);
    }

    public BlockPos getDst() {
        return BlockPosUtil.blockPosFromString(dst);
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


}
