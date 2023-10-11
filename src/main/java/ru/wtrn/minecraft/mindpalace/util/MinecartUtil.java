package ru.wtrn.minecraft.mindpalace.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import ru.wtrn.minecraft.mindpalace.entity.FastMinecart;

public class MinecartUtil {
    public static FastMinecart spawnAndRide(Level level, Player player, BlockPos pos) {
        FastMinecart minecart = new FastMinecart(level, pos.getX() + 0.5, pos.getY() + 0.0625, pos.getZ() + 0.5);
        level.addFreshEntity(minecart);
        level.gameEvent(GameEvent.ENTITY_PLACE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));
        minecart.interact(player, InteractionHand.MAIN_HAND);
        return minecart;
    }
}
