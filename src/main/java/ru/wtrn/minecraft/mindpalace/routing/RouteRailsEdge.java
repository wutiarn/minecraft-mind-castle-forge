package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedIntrusiveEdgesSpecifics;

import java.util.Objects;

public class RouteRailsEdge extends DefaultWeightedEdge {
    private final BlockPos src;
    private final BlockPos dst;
    private final Direction direction;
    private final int distance;

    public RouteRailsEdge(BlockPos src, BlockPos dst, Direction direction, int distance) {
        this.src = src;
        this.dst = dst;
        this.direction = direction;
        this.distance = distance;
    }

    public BlockPos getSrc() {
        return src;
    }

    public BlockPos getDst() {
        return dst;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    protected Object getSource() {
        return src;
    }

    @Override
    protected Object getTarget() {
        return dst;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteRailsEdge edge = (RouteRailsEdge) o;
        return Objects.equals(src, edge.src) && Objects.equals(dst, edge.dst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst);
    }
}
