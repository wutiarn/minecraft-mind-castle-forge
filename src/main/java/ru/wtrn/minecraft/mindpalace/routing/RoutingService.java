package ru.wtrn.minecraft.mindpalace.routing;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class RoutingService {
    public static RoutingService INSTANCE = new RoutingService();

    public void rebuildGraph(BlockPos startBlockPos, CommandSourceStack commandSourceStack) {
        boolean isClientSide = commandSourceStack.getLevel().isClientSide();
        commandSourceStack.sendSystemMessage(Component.literal("Rebuilding routes..."));
    }
}
