package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.routing.RouteRailsEdge;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNodeConnection;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;
import ru.wtrn.minecraft.mindpalace.util.BlockPosUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.ibm.icu.text.PluralRules.Operand.v;
import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.ROUTING_BRIDGE_GRAPH_WEIGHT;

public class DimensionRoutingState {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(HashBiMap.class, (InstanceCreator<HashBiMap<?, ?>>) type -> HashBiMap.create())
            .registerTypeAdapter(SetMultimap.class, (InstanceCreator<SetMultimap<?, ?>>) type -> Multimaps.newSetMultimap(new HashMap<>(), HashSet::new))
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
            return new PersistentDimensionRoutingState();
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
        try {
            dumpGraphToGraphViz();
        } catch (Exception e) {
            logger.error("Failed to dump graph to graphviz file", e);
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
        applyBridges();
    }

    public void patchGraph(Collection<RoutingNodeConnection> connections) {
        for (RoutingNodeConnection connection : connections) {
            BlockPos src = connection.getSrc();
            if (!graph.containsVertex(src)) {
                continue;
            }
            Set<RouteRailsEdge> outgoingEdges = graph.outgoingEdgesOf(src);
            if (!outgoingEdges.isEmpty()) {
                List<RouteRailsEdge> edgesToRemove = List.copyOf(outgoingEdges);
                graph.removeAllEdges(edgesToRemove);
            }
        }
        for (RoutingNodeConnection connection : connections) {
            addConnection(connection);
        }
        applyBridges();
    }

    public void addBridge(String firstStation, String secondStation) {
        persistentState.addBridge(firstStation, secondStation);
        applyBridge(firstStation, secondStation);
        persistState();
    }

    public boolean removeBridge(String firstStation, String secondStation) {
        boolean removed = persistentState.removeBridge(firstStation, secondStation);
        if (!removed) {
            return false;
        }

        BlockPos firstStationPos = persistentState.getStationPos(firstStation);
        BlockPos secondStationPos = persistentState.getStationPos(secondStation);

        if (firstStationPos != null && secondStationPos != null) {
            graph.removeEdge(firstStationPos, secondStationPos);
            graph.removeEdge(secondStationPos, firstStationPos);
        }

        persistState();
        return true;
    }

    private void applyBridges() {
        HashMap<String, HashSet<String>> bridgedStations = persistentState.getBridgedStations();
        for (Map.Entry<String, HashSet<String>> entry : bridgedStations.entrySet()) {
            String srcStation = entry.getKey();
            HashSet<String> dstStations = entry.getValue();
            BlockPos srcStationPos = persistentState.getStationPos(srcStation);
            if (srcStationPos == null) {
                logger.warn("Failed to setup bridge with source station %s. Targets: %s".formatted(srcStation, dstStations));
                continue;
            }
            for (String dstStation : dstStations) {
                applyBridge(srcStation, dstStation);
            }
        }
    }

    private void applyBridge(String srcStation, String dstStation) {
        BlockPos srcStationPos = persistentState.getStationPos(srcStation);
        if (srcStationPos == null) {
            return;
        }
        BlockPos dstStationPos = persistentState.getStationPos(dstStation);
        if (dstStationPos == null) {
            return;
        }
        Integer weight = ROUTING_BRIDGE_GRAPH_WEIGHT.get();

        logger.info("Applying bridge {} ({}) -> {} ({}), weight = {}", srcStation, BlockPosUtil.blockPosToString(srcStationPos), dstStation, BlockPosUtil.blockPosToString(dstStationPos), weight);

        RouteRailsEdge edge = new RouteRailsEdge(null);
        graph.setEdgeWeight(edge, weight);
        graph.addEdge(srcStationPos, dstStationPos, edge);
    }

    private void addConnection(RoutingNodeConnection connection) {
        BlockPos src = connection.getSrc();
        graph.addVertex(src);
        BlockPos dst = connection.getDst();
        graph.addVertex(dst);
        RouteRailsEdge edge = new RouteRailsEdge(connection.getDirection());
        graph.setEdgeWeight(edge, connection.getDistance());
        graph.addEdge(src, dst, edge);
    }

    private Collection<RoutingNodeConnection> dumpGraphConnections() {
        Set<RouteRailsEdge> edgeSet = graph.edgeSet();
        ArrayList<RoutingNodeConnection> connections = new ArrayList<>(edgeSet.size());
        for (RouteRailsEdge edge : edgeSet) {
            connections.add(new RoutingNodeConnection(edge.getSrc(), edge.getDst(), edge.getDirection(), edge.getDistance()));
        }
        return connections;
    }

    private void dumpGraphToGraphViz() throws IOException {
        DOTExporter<BlockPos, RouteRailsEdge> exporter =
                new DOTExporter<>((v) ->
                        "b" + BlockPosUtil.blockPosToString(v)
                                .replace("/", "_")
                                .replace("-", "n")
                );
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(BlockPosUtil.blockPosToString(v)));
            return map;
        });
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(e.getDirection() + " " + e.getDistance()));
            return map;
        });
        Writer writer = new StringWriter();
        exporter.exportGraph(graph, writer);

        Path graphvizPath = getGraphvizPath();
        Files.write(graphvizPath, writer.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Path getGraphvizPath() {
        String primaryFileName = persistentPath.getFileName().toString();
        int index = primaryFileName.lastIndexOf(".");
        String filename = primaryFileName.substring(0, index) + ".dot";
        return persistentPath.getParent().resolve(filename);
    }
}
