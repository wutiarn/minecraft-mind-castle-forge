package ru.wtrn.minecraft.mindpalace.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import ru.wtrn.minecraft.mindpalace.items.ImageFrameItem;
import ru.wtrn.minecraft.mindpalace.items.ModItems;

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
        ImageFrameItem item = ModItems.IMAGE_FRAME_ITEM.get();
        ItemStack stack = new ItemStack(item, 1);
        item.setImageId(stack, image_id);
        context.getSource().getPlayer().getInventory().add(stack);
        context.getSource().sendSuccess(Component.literal("Given " + item.getName(stack)), true);
        return 0;
    }
}
