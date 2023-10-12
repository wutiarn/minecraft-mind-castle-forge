package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.routing.RouteRailsEdge;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNode;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DimensionRoutingState {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(HashBiMap.class, (InstanceCreator<HashBiMap<?, ?>>) type -> HashBiMap.create())
            .create();
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    public DefaultDirectedWeightedGraph<BlockPos, RouteRailsEdge> graph;
    public ShortestPathAlgorithm<BlockPos, RouteRailsEdge> shortestPathFinder;
    public final PersistentDimensionRoutingState persistentState;
    private final Path persistentPath;

    public DimensionRoutingState(Path path) {
        this.persistentState = loadState(path);
        this.persistentPath = path;
        resetGraph();
    }

    public static PersistentDimensionRoutingState loadState(Path statePath) {
        try {
            String state = Files.readString(statePath);
            return gson.fromJson(state, PersistentDimensionRoutingState.class);
        } catch (Exception e) {
            logger.error("Failed to load routing state", e);
            return new PersistentDimensionRoutingState(
                    HashBiMap.create(),
                    new HashMap<>()
            );
        }
    }

    public void persistState() {
        try {
            String json = gson.toJson(persistentState);
            //noinspection ResultOfMethodCallIgnored
            persistentPath.getParent().toFile().mkdirs();
            Files.write(persistentPath, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.error("Failed to persist routing state", e);
        }
    }

    public void updateGraph(Collection<RoutingNode> nodes) {
        resetGraph();
        for (RoutingNode discoveredNode : nodes) {
            graph.addVertex(discoveredNode.pos);
            for (Map.Entry<Direction, RoutingNode.Connection> connectionEntry : discoveredNode.connections.entrySet()) {
                RoutingNode.Connection connection = connectionEntry.getValue();
                graph.addVertex(connection.peer().pos);
                RouteRailsEdge edge = new RouteRailsEdge(discoveredNode.pos, connection.peer().pos, connectionEntry.getKey(), connection.distance());
                graph.setEdgeWeight(edge, connection.distance());
                graph.addEdge(discoveredNode.pos, connection.peer().pos, edge);
            }
        }
    }

    private void resetGraph() {
        graph = new DefaultDirectedWeightedGraph<>(RouteRailsEdge.class);
        shortestPathFinder = new DijkstraShortestPath<>(graph);
    }
}
