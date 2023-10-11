package ru.wtrn.minecraft.mindpalace.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import ru.wtrn.minecraft.mindpalace.commands.argument.StationNameArgumentType;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

import java.util.UUID;

public class GoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("go")
                        .then(
                                Commands.argument("station", new StationNameArgumentType())
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

        boolean success = RoutingService.INSTANCE.setUserDestination(uuid, dstStationName);
        if (!success) {
            source.sendFailure(Component.literal("Failed to find station " + dstStationName));
            return 1;
        }
        source.sendSystemMessage(Component.literal("Destination station set to " + dstStationName));
        return 0;
    }
}
