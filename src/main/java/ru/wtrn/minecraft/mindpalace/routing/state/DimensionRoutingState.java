package ru.wtrn.minecraft.mindpalace.routing.state;

import com.google.gson.Gson;
import net.minecraft.core.BlockPos;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.routing.RouteRailsEdge;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class DimensionRoutingState {
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private final String dimensionId;
    public final DefaultDirectedWeightedGraph<BlockPos, RouteRailsEdge> graph = new DefaultDirectedWeightedGraph<>(RouteRailsEdge.class);
    public final ShortestPathAlgorithm<BlockPos, RouteRailsEdge> shortestPathFinder = new DijkstraShortestPath<>(graph);
    public final PersistentDimensionRoutingState persistentState;

    public DimensionRoutingState(String dimensionId) {
        this.dimensionId = dimensionId;
        this.persistentState = loadState(dimensionId);
    }

    public static PersistentDimensionRoutingState loadState(String dimensionId) {
        try {
            String state = Files.readString(getPersistentFile(dimensionId));
            return gson.fromJson(state, PersistentDimensionRoutingState.class);
        } catch (Exception e) {
            logger.error("Failed to load routing state", e);
            return new PersistentDimensionRoutingState(
                    new HashMap<>(),
                    new HashMap<>()
            );
        }
    }

    public void persistState() {
        try {
            String json = gson.toJson(persistentState);
            Files.write(getPersistentFile(dimensionId), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.error("Failed to persist routing state", e);
        }
    }

    private static Path getPersistentFile(String dimensionId) {
        Path baseDir = Path.of("routing");
        //noinspection ResultOfMethodCallIgnored
        baseDir.toFile().mkdirs();
        dimensionId = dimensionId.replace(":", "_");
        return baseDir.resolve(dimensionId + ".json");
    }
}
