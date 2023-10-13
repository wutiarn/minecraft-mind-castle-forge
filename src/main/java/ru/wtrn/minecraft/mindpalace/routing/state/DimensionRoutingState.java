package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import net.minecraft.core.BlockPos;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.routing.RouteRailsEdge;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNodeConnection;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

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
        initGraph(persistentState.getConnections());
    }

    public static PersistentDimensionRoutingState loadState(Path statePath) {
        try {
            String state = Files.readString(statePath);
            return gson.fromJson(state, PersistentDimensionRoutingState.class);
        } catch (Exception e) {
            logger.error("Failed to load routing state", e);
            return new PersistentDimensionRoutingState(
                    HashBiMap.create(),
                    new HashMap<>(),
                    List.of()
            );
        }
    }

    public void persistState() {
        try {
            persistentState.setConnections(dumpGraphConnections());
            String json = gson.toJson(persistentState);
            //noinspection ResultOfMethodCallIgnored
            persistentPath.getParent().toFile().mkdirs();
            Files.write(persistentPath, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.error("Failed to persist routing state", e);
        }
    }

    public void initGraph(Collection<RoutingNodeConnection> connections) {
        graph = new DefaultDirectedWeightedGraph<>(RouteRailsEdge.class);
        shortestPathFinder = new DijkstraShortestPath<>(graph);
        if (connections == null) {
            return;
        }
        for (RoutingNodeConnection connection : connections) {
            addConnection(connection);
        }
    }

    public void patchGraph(Collection<RoutingNodeConnection> connections) {
        for (RoutingNodeConnection connection : connections) {
            if (!graph.containsVertex(connection.src)) {
                continue;
            }
            Set<RouteRailsEdge> outgoingEdges = graph.outgoingEdgesOf(connection.src);
            graph.removeAllEdges(outgoingEdges);
        }
        for (RoutingNodeConnection connection : connections) {
            addConnection(connection);
        }
    }

    private void addConnection(RoutingNodeConnection connection) {
        graph.addVertex(connection.src);
        graph.addVertex(connection.dst);
        RouteRailsEdge edge = new RouteRailsEdge(connection.direction);
        graph.setEdgeWeight(edge, connection.distance);
        graph.addEdge(connection.src, connection.dst, edge);
    }

    private Collection<RoutingNodeConnection> dumpGraphConnections() {
        Set<RouteRailsEdge> edgeSet = graph.edgeSet();
        ArrayList<RoutingNodeConnection> connections = new ArrayList<>(edgeSet.size());
        for (RouteRailsEdge edge : edgeSet) {
            connections.add(new RoutingNodeConnection(edge.getSrc(), edge.getDst(), edge.getDirection(), edge.getDistance()));
        }
        return connections;
    }
}
