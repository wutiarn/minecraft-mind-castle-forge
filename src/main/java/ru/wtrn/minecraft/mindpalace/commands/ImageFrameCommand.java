package ru.wtrn.minecraft.mindpalace.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.command.EnumArgument;
import ru.wtrn.minecraft.mindpalace.items.ImageFrame;
import ru.wtrn.minecraft.mindpalace.items.ImageFrameItem;
import ru.wtrn.minecraft.mindpalace.items.ModItems;

public class ImageFrameCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("i")
                        .then(
                                Commands.argument("image_id", LongArgumentType.longArg()).executes(ImageFrameCommand::giveImage)
                        )
                        .then(
                                Commands.argument("orientation", EnumArgument.enumArgument(Orientation.class))
                                        .then(
                                                Commands.argument("size", IntegerArgumentType.integer(0, 100))
                                                        .executes(ImageFrameCommand::setImageSize)
                                        )

                        )
        );
    }

    public static int giveImage(CommandContext<CommandSourceStack> context) {
        long image_id = context.getArgument("image_id", Long.class);
        ImageFrameItem item = ModItems.IMAGE_FRAME_ITEM.get();
        ItemStack stack = new ItemStack(item, 1);
        item.setImageId(stack, image_id);
        context.getSource().getPlayer().getInventory().add(stack);
        context.getSource().sendSuccess(Component.literal("Given " + item.getName(stack).getString()), true);
        return 0;
    }

    public static int setImageSize(CommandContext<CommandSourceStack> context) {
        Orientation orientation = context.getArgument("orientation", Orientation.class);
        long size = context.getArgument("size", Integer.class);

        ServerPlayer player = context.getSource().getPlayer();
        ItemStack stack = player.getMainHandItem();

        if (!stack.is(ModItems.IMAGE_FRAME_ITEM.get())) {
            context.getSource().sendFailure(Component.literal("Image frame must be in main hand"));
            return 1;
        }

        return 0;
    }

    enum Orientation {
        w(ImageFrame.TargetSizeSide.WIDTH),
        h(ImageFrame.TargetSizeSide.HEIGHT);

        ImageFrame.TargetSizeSide targetSizeSide;

        Orientation(ImageFrame.TargetSizeSide targetSizeSide) {
            this.targetSizeSide = targetSizeSide;
        }
    }
}
