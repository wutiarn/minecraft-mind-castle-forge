package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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

    public RoutingNode setName(BlockPos pos, String name, CommandSourceStack source) {
        if (state == null) {
            source.sendFailure(Component.literal("Routing state is not initialized"));
            return null;
        }

        RoutingNode node = state.setName(pos, name);
        if (node == null) {
            source.sendFailure(Component.literal("Cannot find routing block at specified location"));
            return null;
        }

        return node;
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
