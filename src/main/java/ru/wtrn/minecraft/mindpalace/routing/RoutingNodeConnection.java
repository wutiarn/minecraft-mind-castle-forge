package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class RoutingNodeConnection {
    public BlockPos src;
    public BlockPos dst;
    public Direction direction;
    public int distance;

    public RoutingNodeConnection(BlockPos src, BlockPos dst, Direction direction, int distance) {
        this.src = src;
        this.dst = dst;
        this.direction = direction;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return src.getX() + "/" + src.getY() + "/" + src.getZ() +
                " > " + direction + " " + distance +
                " > " + dst.getX() + "/" + dst.getY() + "/" + dst.getZ();
    }
}
