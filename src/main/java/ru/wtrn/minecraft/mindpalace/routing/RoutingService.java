package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.block.ModBlocks;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;

import java.util.*;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private final RoutingServiceState state = new RoutingServiceState(new HashMap<>(), new HashMap<>());
    private final DefaultDirectedWeightedGraph<BlockPos, RouteRailsEdge> graph = new DefaultDirectedWeightedGraph<>(RouteRailsEdge.class);
    private final ShortestPathAlgorithm<BlockPos, RouteRailsEdge> shortestPathFinder = new DijkstraShortestPath<>(graph);

    public Collection<RoutingNode> rebuildGraph(BlockPos startBlockPos, Level level) {
        Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(getRoutingRailBlock(), level).buildGraph(startBlockPos, null);
        updateGraph(discoveredNodes);
        return discoveredNodes;
    }

    public HashMap<String, BlockPos> getStations() {
        return state.stations();
    }

    public GraphPath<BlockPos, RouteRailsEdge> calculateRoute(BlockPos src, String dstName, Level level) {
        GraphPath<BlockPos, RouteRailsEdge> path = calculateRoute(src, dstName);
        if (path == null) {
            rebuildGraph(src, level);
            path = calculateRoute(src, dstName);
        }
        return path;
    }

    public boolean setStationName(BlockPos pos, String name, CommandSourceStack source) {
        state.setStationName(pos, name);
        return true;
    }

    public void onRoutingRailPlaced(BlockPos pos, Level level) {
        logger.info("Placed new routing rail at {}", pos);
        rebuildGraph(pos, level);
    }

    public void onRoutingRailRemoved(BlockPos pos, Level level) {
        logger.info("Removed routing rail at {}", pos);
        graph.removeVertex(pos);
    }

    public String getUserDestinationStation(UUID userId) {
        return state.getUserDestinationStationName(userId);
    }

    public boolean setUserDestination(UUID userId, String dstStationName) {
        if (state.getStationPos(dstStationName) == null) {
            return false;
        }
        state.setUserDestination(userId, dstStationName);
        return true;
    }

    private GraphPath<BlockPos, RouteRailsEdge> calculateRoute(BlockPos src, String dstName) {
        BlockPos dst = state.stations().get(dstName);
        if (dst == null) {
            return null;
        }
        try {
            return shortestPathFinder.getPath(src, dst);
        } catch (Exception e) {
            logger.warn("Route calculation from {} to {} failed", src, dst, e);
            return null;
        }
    }

    private void updateGraph(Collection<RoutingNode> nodes) {
        for (RoutingNode discoveredNode : nodes) {
            graph.addVertex(discoveredNode.pos);
            for (Map.Entry<Direction, RoutingNode.Connection> connectionEntry : discoveredNode.connections.entrySet()) {
                RoutingNode.Connection connection = connectionEntry.getValue();
                graph.addVertex(connection.peer().pos);
                graph.addEdge(discoveredNode.pos, connection.peer().pos, new RouteRailsEdge(discoveredNode.pos, connection.peer().pos, connectionEntry.getKey(), connection.distance()));
            }
        }
    }

    private RoutingRailBlock getRoutingRailBlock() {
        return (RoutingRailBlock) ModBlocks.ROUTING_RAIL_BLOCK.get();
    }
}
