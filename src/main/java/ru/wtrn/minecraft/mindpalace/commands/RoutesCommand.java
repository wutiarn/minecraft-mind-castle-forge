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
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;
import ru.wtrn.minecraft.mindpalace.routing.RoutingNode;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

public class RoutesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("routes")
                        .executes(RoutesCommand::refreshRoutes)
                        .then(
                                Commands.literal("refresh").executes(RoutesCommand::refreshRoutes)
                        )
                        .then(
                                Commands.literal("stations").executes(RoutesCommand::listStations)
                        )
                        .then(
                                Commands.literal("setName")
                                        .then(
                                                Commands.argument("name", StringArgumentType.string())
                                                        .executes(RoutesCommand::refreshRoutes)
                                        )

                        )
        );
    }

    public static int refreshRoutes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        BlockPos startBlockPos = getTargetedRoutingRailBlockPos(source);
        if (startBlockPos == null) {
            return 1;
        }

        long startTime = System.currentTimeMillis();
        RoutingService.INSTANCE.rebuildGraph(startBlockPos, source.getLevel(), player);
        long duration = System.currentTimeMillis() - startTime;
        context.getSource().sendSuccess(() -> Component.literal("Routes rebuild completed in %sms".formatted(duration)), true);
        return 0;
    }

    public static int setStationName(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        BlockPos startBlockPos = getTargetedRoutingRailBlockPos(source);
        if (startBlockPos == null) {
            return 1;
        }

        boolean success = RoutingService.INSTANCE.setName(startBlockPos, context.getArgument("name", String.class), source);
        if (!success) {
            return 1;
        }
        return 0;
    }

    public static int listStations(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        BlockPos startBlockPos = getTargetedRoutingRailBlockPos(source);
        if (startBlockPos == null) {
            return 1;
        }

        RoutingService.INSTANCE.rebuildGraph(startBlockPos, source.getLevel(), player);
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
