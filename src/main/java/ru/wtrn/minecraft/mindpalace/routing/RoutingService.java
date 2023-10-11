package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.wtrn.minecraft.mindpalace.block.ModBlocks;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();
    private static final Logger logger = LoggerFactory.getLogger(RoutingNode.class);
    private RoutingServiceState state = new RoutingServiceState(List.of(), new HashMap<>(), new HashMap<>());

    public Collection<RoutingNode> rebuildState(BlockPos startBlockPos, Level level) {
        Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(getRoutingRailBlock(), level).buildGraph(startBlockPos, null);
        state = new RoutingServiceState(discoveredNodes, state.getStations(), state.getDestinationByUserUUID());
        return discoveredNodes;
    }

    public void onRoutingRailPlaced(BlockPos pos, Level level) {
        logger.info("Placed new routing rail at {}", pos);
        rebuildState(pos, level);
    }

    public void onRoutingRailRemoved(BlockPos pos, Level level) {
        logger.info("Removed routing rail at {}", pos);
        state.removeNode(pos);
    }

    public HashMap<String, BlockPos> getStations() {
        return state.getStations();
    }

    public boolean setStationName(BlockPos pos, String name, CommandSourceStack source) {
        state.setName(pos, name);
        return true;
    }


    public GraphPath<BlockPos, RoutingServiceState.RouteRailsEdge> calculateRoute(BlockPos src, String dstName, Level level) {
        GraphPath<BlockPos, RoutingServiceState.RouteRailsEdge> path = state.calculateRoute(src, dstName);
        if (path == null) {
            rebuildState(src, level);
            path = state.calculateRoute(src, dstName);
        }
        return path;
    }

    public boolean setUserDestination(UUID userId, String dstStationName) {
        if (state.getStationPos(dstStationName) == null) {
            return false;
        }
        state.setUserDestination(userId, dstStationName);
        return true;
    }

    public String getUserDestinationStation(UUID userId) {
        return state.getUserDestinationStationName(userId);
    }

    private RoutingRailBlock getRoutingRailBlock() {
        return (RoutingRailBlock) ModBlocks.ROUTING_RAIL_BLOCK.get();
    }
}
