package ru.wtrn.minecraft.mindpalace.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import ru.wtrn.minecraft.mindpalace.block.RoutingRailBlock;
import ru.wtrn.minecraft.mindpalace.client.texture.TextureCache;
import ru.wtrn.minecraft.mindpalace.entity.ImageFrame;
import ru.wtrn.minecraft.mindpalace.items.ImageFrameItem;
import ru.wtrn.minecraft.mindpalace.items.ModItems;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;

public class RoutesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("routes")
                        .executes(RoutesCommand::refreshRoutes)
                        .then(
                                Commands.literal("refresh").executes(RoutesCommand::refreshRoutes)
                        )
        );
    }

    public static int refreshRoutes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();

        if (player == null) {
            source.sendFailure(Component.literal("This command can be invoked only by player"));
            return 1;
        }
        HitResult hitResult = player.pick(20, 0, false);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            source.sendFailure(Component.literal("You should look at routing rail for this command"));
            return 1;
        }

        BlockPos startBlockPos = blockHitResult.getBlockPos();

        BlockState blockState = level.getBlockState(startBlockPos);
        if (!(blockState.getBlock() instanceof RoutingRailBlock routingRailBlock)) {
            source.sendFailure(Component.literal("Targeted block is not RoutingRailBlock"));
            return 1;
        }

        long startTime = System.currentTimeMillis();
        RoutingService.INSTANCE.rebuildGraph(startBlockPos, source.getLevel(), player);

        long duration = System.currentTimeMillis() - startTime;

        context.getSource().sendSuccess(() -> Component.literal("Routes rebuild completed in %sms".formatted(duration)), true);

        return 0;
    }
}
