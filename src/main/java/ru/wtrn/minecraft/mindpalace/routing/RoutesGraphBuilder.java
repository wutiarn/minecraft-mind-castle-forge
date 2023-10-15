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
    private final Set<BlockPos> discoveredNodes = new HashSet<>();
    private final List<RoutingNodeConnection> discoveredConnections = new ArrayList<>();
    private final Queue<PendingNode> pendingNodes = new LinkedList<>();

    private static final List<Direction> scannedDirections = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public RoutesGraphBuilder(RoutingRailBlock block, Level level) {
        this.block = block;
        this.level = level;
    }

    public Collection<RoutingNodeConnection> buildGraph(BlockPos startBlockPos, Integer maxDepth) {
        PendingNode startNode = new PendingNode(startBlockPos, 0);
        pendingNodes.add(startNode);
        discoveredNodes.add(startNode.pos);
        while (true) {
            PendingNode node = pendingNodes.poll();
            if (node == null || (maxDepth != null && node.hopsFromStart > maxDepth)) {
                break;
            }
            scanNodeNeighbors(node);
        }
        return discoveredConnections;
    }

    private void scanNodeNeighbors(PendingNode startNode) {
        for (Direction direction : scannedDirections) {
            RailTraverser.NextBlock found = block.findNeighbourRoutingRail(startNode.pos, direction, level);
            if (found == null) {
                continue;
            }
            boolean newNodeDiscovered = discoveredNodes.add(found.pos);
            RoutingNodeConnection connection = new RoutingNodeConnection(startNode.pos, found.pos, direction, found.traversedBlocksCount);
            discoveredConnections.add(connection);
            if (newNodeDiscovered) {
                pendingNodes.add(new PendingNode(found.pos, startNode.hopsFromStart + 1));
            }
        }
    }

    record PendingNode(BlockPos pos, int hopsFromStart) {
    }
}
