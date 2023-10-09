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
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.util.UUID;

public class GoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("go")
                        .then(
                                Commands.argument("station", StringArgumentType.string())
                                        .executes(GoCommand::setDestinationStation)
                        )
        );
    }

    public static int setDestinationStation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Failed to get current player"));
            return -1;
        }
        UUID uuid = player.getUUID();

        String dstStationName = context.getArgument("station", String.class);

        RoutingService.INSTANCE.setUserDestination(uuid, dstStationName);
        return 0;
    }
}
