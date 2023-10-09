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
    private Map<BlockPos, DiscoveredNode> discoveredNodes = new HashMap<>();
    private Queue<DiscoveredNode> pendingNodes = new LinkedList<>();

    private static final List<Direction> scannedDirections = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public RoutesGraphBuilder(RoutingRailBlock block, Level level) {
        this.block = block;
        this.level = level;
    }

    public Collection<RoutingNode> buildGraph(BlockPos startBlockPos, Integer maxDepth) {
        DiscoveredNode startNode = new DiscoveredNode(new RoutingNode(startBlockPos), 0);
        discoveredNodes.put(startBlockPos, startNode);
        pendingNodes.add(startNode);
        while (true){
            DiscoveredNode node = pendingNodes.poll();
            if (node == null || (maxDepth != null && node.hopsFromStart > maxDepth)) {
                break;
            }
            scanNodeNeighbors(node);
        }
        return discoveredNodes.values().stream().map(it -> it.node).toList();
    }

    private void scanNodeNeighbors(DiscoveredNode startNode) {
        for (Direction direction : scannedDirections) {
            RailTraverser.NextBlock found = block.findNeighbourRoutingRail(startNode.node.pos, direction, level);
            if (found == null) {
                continue;
            }
            boolean newNodeDiscovered = !discoveredNodes.containsKey(found.pos);
            DiscoveredNode foundNode = discoveredNodes.computeIfAbsent(found.pos, (pos) -> new DiscoveredNode(new RoutingNode(pos), startNode.hopsFromStart + 1));
            startNode.node.addConnection(direction, new RoutingNode.Connection(foundNode.node, found.traversedBlocksCount));
            if (newNodeDiscovered) {
                pendingNodes.add(foundNode);
            }
        }
    }

    record DiscoveredNode(RoutingNode node, int hopsFromStart) {
    }
}
