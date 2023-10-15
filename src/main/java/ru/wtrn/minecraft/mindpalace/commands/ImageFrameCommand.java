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
import ru.wtrn.minecraft.mindpalace.client.texture.TextureCache;
import ru.wtrn.minecraft.mindpalace.entity.ImageFrame;
import ru.wtrn.minecraft.mindpalace.items.ImageFrameItem;
import ru.wtrn.minecraft.mindpalace.items.ModItems;

public class ImageFrameCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("i")
                        .executes(ImageFrameCommand::giveLatestImage)
                        .then(
                                Commands.argument("imageId", LongArgumentType.longArg()).executes(ImageFrameCommand::giveImage)
                        )
                        .then(
                                Commands.argument("targetSide", EnumArgument.enumArgument(TargetSide.class))
                                        .then(
                                                Commands.argument("size", IntegerArgumentType.integer(0, 100))
                                                        .executes(ImageFrameCommand::setImageSize)
                                        )

                        )
                        .then(
                                Commands.literal("reload").executes(ImageFrameCommand::reloadTextures)
                        )
        );
    }

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("i")
                        .then(
                                Commands.literal("reload").executes(ImageFrameCommand::reloadTextures)
                        )
        );
    }

    public static int giveLatestImage(CommandContext<CommandSourceStack> context) {
        doGiveImage(context, 0);
        return 0;
    }

    public static int giveImage(CommandContext<CommandSourceStack> context) {
        long imageId = context.getArgument("imageId", Long.class);
        doGiveImage(context, imageId);
        return 0;
    }

    private static void doGiveImage(CommandContext<CommandSourceStack> context, long imageId) {
        ImageFrameItem item = ModItems.IMAGE_FRAME_ITEM.get();
        ItemStack stack = new ItemStack(item, 1);
        item.setImageId(stack, imageId);
        context.getSource().getPlayer().getInventory().add(stack);
        context.getSource().sendSuccess(() -> Component.literal("Given " + item.getName(stack).getString()), true);
    }

    public static int setImageSize(CommandContext<CommandSourceStack> context) {
        TargetSide targetSide = context.getArgument("targetSide", TargetSide.class);
        int size = context.getArgument("size", Integer.class);

        ServerPlayer player = context.getSource().getPlayer();
        ItemStack stack = player.getMainHandItem();
        ImageFrameItem item = ModItems.IMAGE_FRAME_ITEM.get();
        if (!stack.is(item)) {
            context.getSource().sendFailure(Component.literal("Image frame must be in main hand"));
            return 1;
        }

        item.setTargetSize(stack, targetSide.targetSizeSide, size);

        return 0;
    }

    public static int reloadTextures(CommandContext<CommandSourceStack> context) {
        TextureCache.forceCleanup();
        context.getSource().sendSuccess(() -> Component.literal("Image cache cleanup completed"), true);
        return 0;
    }

    enum TargetSide {
        w(ImageFrame.TargetSizeType.WIDTH),
        h(ImageFrame.TargetSizeType.HEIGHT);

        ImageFrame.TargetSizeType targetSizeSide;

        TargetSide(ImageFrame.TargetSizeType targetSizeSide) {
            this.targetSizeSide = targetSizeSide;
        }
    }
}
