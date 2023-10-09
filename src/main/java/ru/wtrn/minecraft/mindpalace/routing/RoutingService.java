package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;

import java.util.Collection;
import java.util.stream.Collectors;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();

    public void rebuildGraph(BlockPos startBlockPos, Level level, Player player) {
        player.sendSystemMessage(Component.literal("Rebuilding routes..."));
        RoutingRailBlock routingRailBlock = getRoutingRailAtPos(startBlockPos, level, player);
        Collection<RoutingNode> discoveredNodes = new RoutesGraphBuilder(routingRailBlock, level).buildGraph(startBlockPos, null);
        String debugString = discoveredNodes.stream().map(RoutingNode::toString).collect(Collectors.joining("\n"));
        player.sendSystemMessage(Component.literal("Discovered nodes:\n" + debugString));
    }

    private RoutingRailBlock getRoutingRailAtPos(BlockPos startBlockPos, Level level, Player player) {
        BlockState blockState = level.getBlockState(startBlockPos);
        if (!(blockState.getBlock() instanceof RoutingRailBlock routingRailBlock)) {
            player.sendSystemMessage(Component.literal("Targeted block is not RoutingRailBlock"));
            return null;
        }
        return routingRailBlock;
    }
}
