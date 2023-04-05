package ru.wtrn.minecraft.mindpalace.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class GiveImageFrameCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("i")
                        .then(
                                Commands.argument("image_id", LongArgumentType.longArg()).executes(GiveImageFrameCommand::execute)
                        )
        );
    }

    public static int execute(CommandContext<CommandSourceStack> context) {
        long image_id = context.getArgument("image_id", Long.class);
        return 0;
    }
}
