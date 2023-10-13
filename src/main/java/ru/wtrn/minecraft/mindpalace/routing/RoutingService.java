package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.block.ModBlocks;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;
import ru.wtrn.minecraft.mindpalace.net.packets.StationListPacket;
import ru.wtrn.minecraft.mindpalace.routing.state.DimensionRoutingState;
import ru.wtrn.minecraft.mindpalace.util.BroadcastUtils;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();
    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private static final ConcurrentHashMap<String, DimensionRoutingState> stateByDimension = new ConcurrentHashMap<>();

    public Collection<RoutingNodeConnection> rebuildGraph(BlockPos startBlockPos, Level level, Integer depth) {
        MinecraftServer server = Objects.requireNonNull(level.getServer());
        BroadcastUtils.broadcastMessage(server, "Rebuilding routes: startPos=%s/%s/%s, depth=%s".formatted(startBlockPos.getX(), startBlockPos.getY(), startBlockPos.getZ(), depth));
        long startTime = System.currentTimeMillis();
        Collection<RoutingNodeConnection> connections = new RoutesGraphBuilder(getRoutingRailBlock(), level).buildGraph(startBlockPos, depth);
        DimensionRoutingState state = getState(level);
        updateGraph(connections, state, level, depth == null);
        long duration = System.currentTimeMillis() - startTime;
        BroadcastUtils.broadcastMessage(server, "Routes rebuild completed in %sms. Discovered %s connection(s)".formatted(duration, connections.size()));
        return connections;
    }

    public Map<String, BlockPos> getStations(Level level) {
        DimensionRoutingState state = getState(level);
        return state.persistentState.getStations();
    }

    @Nullable
    public String getStationName(Level level, BlockPos pos) {
        DimensionRoutingState state = getState(level);
        return state.persistentState.getStationName(pos);
    }

    public GraphPath<BlockPos, RouteRailsEdge> calculateRouteInternal(BlockPos src, String dstName, Level level) {
        DimensionRoutingState state = getState(level);
        GraphPath<BlockPos, RouteRailsEdge> path = calculateRouteInternal(src, dstName, state);
        if (path == null) {
            rebuildGraph(src, level, null);
            path = calculateRouteInternal(src, dstName, state);
        }
        return path;
    }

    public boolean setStationName(BlockPos pos, String name, Level level) {
        DimensionRoutingState state = getState(level);
        state.persistentState.setStationName(pos, name);
        onStateChange(level, state);
        return true;
    }

    public boolean removeStation(String name, Level level) {
        DimensionRoutingState state = getState(level);
        state.persistentState.removeStation(name);
        onStateChange(level, state);
        return true;
    }

    public void onRoutingRailPlaced(BlockPos pos, Level level) {
        logger.info("Placed new routing rail at {}", pos);
        rebuildGraph(pos, level, 1);
    }

    public void onRoutingRailRemoved(BlockPos pos, Level level) {
        logger.info("Removed routing rail at {}", pos);
        DimensionRoutingState state = getState(level);
        state.graph.removeVertex(pos);
    }

    public String getUserDestinationStation(UUID userId, Level level) {
        DimensionRoutingState state = getState(level);
        return state.persistentState.getUserDestinationStationName(userId);
    }

    public boolean setUserDestination(UUID userId, String dstStationName, Level level) {
        DimensionRoutingState state = getState(level);
        if (dstStationName == null) {
            state.persistentState.setUserDestination(userId, null);
            return true;
        }
        if (state.persistentState.getStationPos(dstStationName) == null) {
            return false;
        }
        state.persistentState.setUserDestination(userId, dstStationName);
        onStateChange(level, state);
        return true;
    }

    public void resetCache() {
        stateByDimension.clear();
    }

    private GraphPath<BlockPos, RouteRailsEdge> calculateRouteInternal(BlockPos src, String dstName, DimensionRoutingState state) {
        BlockPos dst = state.persistentState.getStations().get(dstName);
        if (dst == null) {
            return null;
        }
        try {
            return state.shortestPathFinder.getPath(src, dst);
        } catch (Exception e) {
            logger.warn("Route calculation from {} to {} failed", src, dst, e);
            return null;
        }
    }

    private void updateGraph(Collection<RoutingNodeConnection> connections, DimensionRoutingState state, Level level, boolean reset) {
       if (reset) {
           state.initGraph(connections);
       } else {
           state.patchGraph(connections);
       }
       onStateChange(level, state);
    }

    private DimensionRoutingState getState(Level level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            throw new IllegalStateException("Routing service state is not available on client side");
        }
        String dimensionId = level.dimension().location().toString().replace(":", "_");
        Path path = server.getWorldPath(new LevelResource("routing")).resolve(dimensionId + ".json");
        return stateByDimension.computeIfAbsent(dimensionId, (ignored) -> new DimensionRoutingState(path));
    }

    private void onStateChange(Level level, DimensionRoutingState state) {
        state.persistState();
        StationListPacket.sendStationsToLevel(level, state.persistentState.getStations().keySet());
    }

    private RoutingRailBlock getRoutingRailBlock() {
        return (RoutingRailBlock) ModBlocks.ROUTING_RAIL_BLOCK.get();
    }
}
