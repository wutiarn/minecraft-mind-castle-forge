package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;
import ru.wtrn.minecraft.mindpalace.util.RailTraverser;

import java.util.*;

public class RoutesGraphBuilder {
    private final RoutingRailBlock block;
    private final Level level;
    private Map<BlockPos, RoutingNode> discoveredNodes = new HashMap<>();
    private Queue<RoutingNode> pendingNodes = new LinkedList<>();

    private static final List<Direction> scannedDirections = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public RoutesGraphBuilder(RoutingRailBlock block, Level level) {
        this.block = block;
        this.level = level;
    }

    public Collection<RoutingNode> buildGraph(BlockPos startBlockPos) {
        RoutingNode startNode = new RoutingNode(startBlockPos);
        discoveredNodes.put(startBlockPos, startNode);
        pendingNodes.add(startNode);
        while (true){
            RoutingNode node = pendingNodes.poll();
            if (node == null) {
                break;
            }
            scanNodeNeighbors(node);
        }
        return discoveredNodes.values();
    }

    private void scanNodeNeighbors(RoutingNode node) {
        for (Direction direction : scannedDirections) {
            RailTraverser.NextBlock found = block.findNeighbourRoutingRail(node.pos, direction, level);
            if (found == null) {
                continue;
            }
            boolean newNodeDiscovered = !discoveredNodes.containsKey(found.pos);
            RoutingNode foundNode = discoveredNodes.computeIfAbsent(found.pos, RoutingNode::new);
            node.addConnection(direction, foundNode);
            if (newNodeDiscovered) {
                pendingNodes.add(foundNode);
            }
        }
    }
}
