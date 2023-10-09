package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jgrapht.GraphPath;
import ru.wtrn.minecraft.mindpalace.block.ModBlocks;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();
    private RoutingServiceState state = null;

    public void rebuildGraph(BlockPos startBlockPos, CommandSourceStack source) {
        ServerLevel level = source.getLevel();

        if (!isRoutingBlock(startBlockPos, level)) {
            source.sendFailure(Component.literal("Targeted block is not RoutingRailBlock"));
        }

        source.sendSystemMessage(Component.literal("Rebuilding routes..."));
        Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(getRoutingRailBlock(), level).buildGraph(startBlockPos, null);
        String debugString = discoveredNodes.stream().map(RoutingNode::toString).collect(Collectors.joining("\n"));
        source.sendSystemMessage(Component.literal("Discovered nodes:\n" + debugString));
        state = new RoutingServiceState(discoveredNodes, state);
    }

    public void onRoutingRailPlaced(BlockPos pos, Level level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }
        if (!isRoutingBlock(pos, level)) {
            return;
        }
        server.sendSystemMessage(Component.literal("Placed new routing rail at %s...".formatted(pos)));
        if (this.state != null) {
            Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(getRoutingRailBlock(), level).buildGraph(pos, 1);
            state.performUpdate(discoveredNodes);
        }
    }

    public void onRoutingRailRemoved(BlockPos pos, Level level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }
        server.sendSystemMessage(Component.literal("Removed routing rail at %s...".formatted(pos)));
        if (this.state != null) {
            state.removeNode(pos);
        }
    }

    public boolean setName(BlockPos pos, String name, CommandSourceStack source) {
        if (state == null) {
            source.sendFailure(Component.literal("Routing state is not initialized"));
            return false;
        }

        RoutingNode node = state.setName(pos, name);
        if (node == null) {
            source.sendFailure(Component.literal("Cannot find routing block at specified location"));
            return false;
        }

        return true;
    }

    public boolean listStations(BlockPos pos, CommandSourceStack source) {
        if (state == null) {
            source.sendFailure(Component.literal("Routing state is not initialized"));
            return false;
        }
        String stationsList = state.getStations()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(it -> {
                    BlockPos stationPos = it.getValue().pos;
                    return "%s @ %s/%s/%s".formatted(it.getKey(), stationPos.getX(), stationPos.getY(), stationPos.getZ());
                })
                .collect(Collectors.joining("\n"));
        source.sendSystemMessage(Component.literal("Stations list:\n" + stationsList));
        return true;
    }

    public boolean printRoute(BlockPos pos, String dstStationName, CommandSourceStack source) {
        if (state == null) {
            source.sendFailure(Component.literal("Routing state is not initialized"));
            return false;
        }
        GraphPath<RoutingNode, Long> path = state.calculateRoute(pos, dstStationName);
        if (path == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Path to station %s from %s:");

        List<RoutingNode> vertexList = path.getVertexList();
        for (RoutingNode node : vertexList) {
            sb.append("\n%s/%s/%s".formatted(node.pos.getX(), node.pos.getY(), node.pos.getZ()));
            if (node.name != null) {
                sb.append(" (%s)".formatted(node.name));
            };
        }

        source.sendSystemMessage(Component.literal(sb.toString()));
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isRoutingBlock(BlockPos startBlockPos, Level level) {
        BlockState blockState = level.getBlockState(startBlockPos);
        return blockState.getBlock() instanceof RoutingRailBlock;
    }

    private RoutingRailBlock getRoutingRailBlock() {
        return (RoutingRailBlock) ModBlocks.ROUTING_RAIL_BLOCK.get();
    }
}
