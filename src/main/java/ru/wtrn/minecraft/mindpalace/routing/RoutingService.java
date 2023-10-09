package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;

import java.util.Collection;
import java.util.stream.Collectors;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();

    public void rebuildGraph(BlockPos startBlockPos, CommandSourceStack commandSourceStack) {
        ServerLevel level = commandSourceStack.getLevel();
        boolean isClientSide = level.isClientSide();
        commandSourceStack.sendSystemMessage(Component.literal("Rebuilding routes..."));

        BlockState blockState = level.getBlockState(startBlockPos);
        if (!(blockState.getBlock() instanceof RoutingRailBlock routingRailBlock)) {
            commandSourceStack.sendFailure(Component.literal("Targeted block is not RoutingRailBlock"));
            return;
        }

        Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(routingRailBlock, level).buildGraph(startBlockPos);
        String debugString = discoveredNodes.stream().map(RoutingNode::toString).collect(Collectors.joining("\n"));
        commandSourceStack.sendSuccess(() -> Component.literal("Discovered nodes:\n"+debugString), true);
    }
}
