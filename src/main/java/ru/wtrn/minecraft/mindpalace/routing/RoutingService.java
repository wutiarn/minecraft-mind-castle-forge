package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import ru.wtrn.minecraft.mindpalace.block.ModBlocks;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;

import java.util.Collection;
import java.util.stream.Collectors;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();
    private RoutingServiceState state = null;

    public void rebuildGraph(BlockPos startBlockPos, Level level, Player player) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        if (!isRoutingBlock(startBlockPos, level)) {
            server.sendSystemMessage(Component.literal("Targeted block is not RoutingRailBlock"));
        }

        player.sendSystemMessage(Component.literal("Rebuilding routes..."));
        Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(getRoutingRailBlock(), level).buildGraph(startBlockPos, null);
        String debugString = discoveredNodes.stream().map(RoutingNode::toString).collect(Collectors.joining("\n"));
        player.sendSystemMessage(Component.literal("Discovered nodes:\n" + debugString));
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isRoutingBlock(BlockPos startBlockPos, Level level) {
        BlockState blockState = level.getBlockState(startBlockPos);
        return blockState.getBlock() instanceof RoutingRailBlock;
    }

    private RoutingRailBlock getRoutingRailBlock() {
        return (RoutingRailBlock) ModBlocks.ROUTING_RAIL_BLOCK.get();
    }
}
