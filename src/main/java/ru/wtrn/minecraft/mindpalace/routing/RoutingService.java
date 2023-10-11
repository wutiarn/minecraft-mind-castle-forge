package ru.wtrn.minecraft.mindpalace.routing;

import com.google.gson.Gson;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private static final Gson gson = new Gson();
    private static final Path persistentStatePath = Path.of("./routing.json");
    private final DefaultDirectedWeightedGraph<BlockPos, RouteRailsEdge> graph = new DefaultDirectedWeightedGraph<>(RouteRailsEdge.class);
    private final ShortestPathAlgorithm<BlockPos, RouteRailsEdge> shortestPathFinder = new DijkstraShortestPath<>(graph);
    private final RoutingServicePersistentState state;

    public RoutingService() {
        this.state = loadState();
    }

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

    public boolean setStationName(BlockPos pos, String name) {
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


    public RoutingServicePersistentState loadState() {
        try {
            String state = Files.readString(persistentStatePath);
            return gson.fromJson(state, RoutingServicePersistentState.class);
        } catch (Exception e) {
            logger.error("Failed to load routing state", e);
            return new RoutingServicePersistentState(
                    new HashMap<>(),
                    new HashMap<>()
            );
        }
    }

    public void persistState() {
        try {
            String json = gson.toJson(state);
            Files.write(persistentStatePath, json.getBytes(), StandardOpenOption.WRITE);
        } catch (Exception e) {
            logger.error("Failed to persist routing state", e);
        }
    }
}
