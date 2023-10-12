package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
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

    public Collection<RoutingNode> rebuildGraph(BlockPos startBlockPos, Level level) {
        MinecraftServer server = Objects.requireNonNull(level.getServer());
        BroadcastUtils.broadcastMessage(server, "Rebuilding routes...");
        long startTime = System.currentTimeMillis();
        Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(getRoutingRailBlock(), level).buildGraph(startBlockPos, null);
        DimensionRoutingState state = getState(level);
        updateGraph(discoveredNodes, state);
        long duration = System.currentTimeMillis() - startTime;
        BroadcastUtils.broadcastMessage(server, "Routes rebuild completed in %sms".formatted(duration));
        return discoveredNodes;
    }

    public Map<String, BlockPos> getStations(Level level) {
        DimensionRoutingState state = getState(level);
        return state.persistentState.stations();
    }

    @Nullable
    public String getStationName(Level level, BlockPos pos) {
        DimensionRoutingState state = getState(level);
        return state.persistentState.getStationName(pos);
    }

    public GraphPath<BlockPos, RouteRailsEdge> calculateRoute(BlockPos src, String dstName, Level level) {
        DimensionRoutingState state = getState(level);
        GraphPath<BlockPos, RouteRailsEdge> path = calculateRoute(src, dstName, state);
        if (path == null) {
            rebuildGraph(src, level);
            path = calculateRoute(src, dstName, level);
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

    private GraphPath<BlockPos, RouteRailsEdge> calculateRoute(BlockPos src, String dstName, DimensionRoutingState state) {
        BlockPos dst = state.persistentState.stations().get(dstName);
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

    private void updateGraph(Collection<RoutingNode> nodes, DimensionRoutingState state) {
       state.updateGraph(nodes);
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
        StationListPacket.sendStationsToLevel(level, state.persistentState.stations().keySet());
    }

    private RoutingRailBlock getRoutingRailBlock() {
        return (RoutingRailBlock) ModBlocks.ROUTING_RAIL_BLOCK.get();
    }
}
