package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jgrapht.graph.DefaultWeightedEdge;

public class RouteRailsEdge extends DefaultWeightedEdge {
    private final Direction direction;

    public Direction getDirection() {
        return direction;
    }

    public BlockPos getSrc() {
        return (BlockPos) super.getSource();
    }

    public BlockPos getDst() {
        return (BlockPos) super.getTarget();
    }

    public int getDistance() {
        return (int) super.getWeight();
    }

    public RouteRailsEdge(Direction direction) {
        this.direction = direction;
    }
}
