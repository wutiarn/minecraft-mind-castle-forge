package ru.wtrn.minecraft.mindpalace.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jgrapht.GraphPath;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;
import ru.wtrn.minecraft.mindpalace.commands.argument.StationNameArgumentType;
import ru.wtrn.minecraft.mindpalace.routing.RouteRailsEdge;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNodeConnection;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;
import ru.wtrn.minecraft.mindpalace.util.BlockPosUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        StationNameArgumentType stationNameArgumentType = new StationNameArgumentType();
        dispatcher.register(
                Commands.literal("station")
                        .executes(StationCommand::refreshRoutes)
                        .then(
                                Commands.literal("refresh").executes(StationCommand::refreshRoutes)
                        )
                        .then(
                                Commands.literal("list").executes(StationCommand::listStations)
                        )
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("name", StringArgumentType.string())
                                                        .executes(StationCommand::setStationName)
                                        )

                        )
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("name", stationNameArgumentType)
                                                        .executes(StationCommand::removeStation)
                                        )

                        )
                        .then(
                                Commands.literal("route")
                                        .executes(StationCommand::printRoute)
                                        .then(
                                                Commands.argument("station", stationNameArgumentType)
                                                        .executes(StationCommand::printRoute)
                                        )

                        )
                        .then(
                                Commands.literal("bridge")
                                        .then(
                                                Commands.argument("destination", stationNameArgumentType)
                                                        .executes((ctx) -> handleBridgeCommand(ctx, false))
                                        )
                        )
                        .then(
                                Commands.literal("removeBridge")
                                        .then(
                                                Commands.argument("destination", stationNameArgumentType)
                                                        .executes((ctx) -> handleBridgeCommand(ctx, true))
                                        )
                        )
                        .then(
                                Commands.literal("listBridges")
                                        .executes(StationCommand::listBridges)
                        )
                        .then(
                                Commands.literal("launchblock")
                                        .then(
                                                Commands.argument("destination", stationNameArgumentType)
                                                        .executes(StationCommand::handleLaunchblockCommand)
                                        )
                        )
        );
    }

    public static int refreshRoutes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        source.sendSystemMessage(Component.literal("Rebuilding routes..."));
        List<BlockPos> startBlocks = RoutingService.INSTANCE.getStations(level).values().stream().map(BlockPosUtil::blockPosFromString).collect(Collectors.toList());
        Collection<RoutingNodeConnection> discoveredNodes = RoutingService.INSTANCE.rebuildGraph(startBlocks, level, null, true);
        String debugString = discoveredNodes.stream().map(RoutingNodeConnection::toString).collect(Collectors.joining("\n"));
        source.sendSystemMessage(Component.literal("Discovered connections:\n" + debugString));
        return 0;
    }

    public static int setStationName(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        BlockPos pos = getTargetedRoutingRailBlockPos(source);
        if (pos == null) {
            return 1;
        }

        String name = context.getArgument("name", String.class);
        boolean success = RoutingService.INSTANCE.setStationName(pos, name, source.getLevel());
        if (!success) {
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Station name set to " + name), true);
        return 0;
    }

    public static int removeStation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = context.getArgument("name", String.class);
        boolean removed = RoutingService.INSTANCE.removeStation(name, source.getLevel());
        if (!removed) {
            source.sendFailure(Component.literal("Station %s not found".formatted(name)));
            return 1;
        }
        source.sendSystemMessage(Component.literal("Station %s removed".formatted(name)));
        return 0;
    }

    public static int listStations(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String stationsList = RoutingService.INSTANCE.getStations(source.getLevel())
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(it -> {
                    return "%s @ %s".formatted(it.getKey(), it.getValue());
                })
                .collect(Collectors.joining("\n"));
        source.sendSystemMessage(Component.literal("Stations list:\n" + stationsList));
        return 0;
    }

    public static int printRoute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        BlockPos pos = getTargetedRoutingRailBlockPos(source);
        if (pos == null) {
            return 1;
        }
        String dstStation;
         try {
            dstStation = context.getArgument("station", String.class);
        } catch (Exception e) {
             ServerPlayer player = source.getPlayer();
             if (player == null) {
                 source.sendFailure(Component.literal("This command can be invoked only by player"));
                 return 1;
             }
             dstStation = RoutingService.INSTANCE.getUserDestinationStation(player.getUUID(), source.getLevel());
        };
        if (dstStation == null) {
            source.sendFailure(Component.literal("No destination station specified (in command or using /go command before)"));
            return 1;
        }

        GraphPath<BlockPos, RouteRailsEdge> path = RoutingService.INSTANCE.calculateRoute(pos, dstStation, source.getLevel());
        if (path == null) {
            source.sendFailure(Component.literal("No path found to " + dstStation));
            return 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Path to station %s:".formatted(dstStation));

        List<RouteRailsEdge> vertexList = path.getEdgeList();
        for (RouteRailsEdge edge : vertexList) {
            BlockPos dstPos = edge.getDst();
            sb.append("\n%s %s blocks to %s/%s/%s".formatted(edge.getDirection(), edge.getDistance(), dstPos.getX(), dstPos.getY(), dstPos.getZ()));
        }

        source.sendSystemMessage(Component.literal(sb.toString()));

        return 0;
    }

    private static int handleBridgeCommand(CommandContext<CommandSourceStack> context, boolean isRemoval) {
        CommandSourceStack source = context.getSource();

        BlockPos pos = getTargetedRoutingRailBlockPos(source);
        if (pos == null) {
            return 1;
        }
        ServerLevel level = source.getLevel();
        String srcStation = RoutingService.INSTANCE.getStationName(level, pos);
        if (srcStation == null) {
            source.sendFailure(Component.literal("Targeted block must have station name"));
            return 1;
        }
        String dstStation = context.getArgument("destination", String.class);

        if (isRemoval) {
            boolean removed = RoutingService.INSTANCE.removeBridge(srcStation, dstStation, level);
            if (removed) {
                source.sendSystemMessage(Component.literal("Removed bridge from %s to %s".formatted(srcStation, dstStation)));
                return 0;
            } else {
                source.sendFailure(Component.literal("Failed to remove bridge from %s to %s: not bridged".formatted(srcStation, dstStation)));
                return 1;
            }
        } else {
            RoutingService.INSTANCE.addBridge(srcStation, dstStation, level);
            source.sendSystemMessage(Component.literal("Station %s bridged to %s".formatted(srcStation, dstStation)));
            return 0;
        }
    }

    private static int handleLaunchblockCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        BlockPos pos = getTargetedRoutingRailBlockPos(source);
        if (pos == null) {
            return 1;
        }
        ServerLevel level = source.getLevel();
        String dstStation = context.getArgument("destination", String.class);
        RoutingService.INSTANCE.setLaunchBlockDestinationStation(pos, dstStation, level);
        if (dstStation != null) {
            source.sendSystemMessage(Component.literal("Destination station %s set for launch block at %s".formatted(dstStation, BlockPosUtil.blockPosToString(pos))));
        } else {
            source.sendSystemMessage(Component.literal("Destination station removed for launch block at %s".formatted(BlockPosUtil.blockPosToString(pos))));
        }
        return 0;
    }

    private static int listBridges(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String stationsList = RoutingService.INSTANCE.getBridgedStations(source.getLevel())
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(it -> "%s -> %s".formatted(it.getKey(), String.join(", ", it.getValue())))
                .collect(Collectors.joining("\n"));
        source.sendSystemMessage(Component.literal("Bridged stations:\n" + stationsList));
        return 0;
    }

    private static BlockPos getTargetedRoutingRailBlockPos(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();

        if (player == null) {
            source.sendFailure(Component.literal("This command can be invoked only by player"));
            return null;
        }
        HitResult hitResult = player.pick(20, 0, false);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            source.sendFailure(Component.literal("You should look at routing rail for this command"));
            return null;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();

        BlockState blockState = level.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof RoutingRailBlock routingRailBlock)) {
            source.sendFailure(Component.literal("Targeted block is not RoutingRailBlock"));
            return null;
        }
        return blockPos;
    }
}
