package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.wtrn.minecraft.mindpalace.entity.RoutingRailBlockEntity;
import ru.wtrn.minecraft.mindpalace.routing.RoutingService;
import ru.wtrn.minecraft.mindpalace.util.RailTraverser;

public class RoutingRailBlock extends RailBlock implements EntityBlock {
    public RoutingRailBlock() {
        super(Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        Direction targetDirection = getTargetDirection(pos, level, cart);
        if (targetDirection != null) {
            Vec3 travelVector = Vec3.atLowerCornerOf(targetDirection.getNormal());
            cart.move(MoverType.SELF, travelVector);
            cart.setDeltaMovement(travelVector);
        }
    }

    private Direction getTargetDirection(BlockPos pos, Level level, AbstractMinecart cart) {
        ServerPlayer player = getPlayerPassenger(cart);
        if (player == null) {
            return null;
        }
        return null;
    }

    private ServerPlayer getPlayerPassenger(AbstractMinecart cart) {
        for (Entity passenger : cart.getPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                return player;
            }
        }
        return null;
    }

    @Override
    public boolean isFlexibleRail(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RoutingRailBlockEntity(pPos, pState);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        RoutingService.INSTANCE.onRoutingRailPlaced(pPos, pLevel);
    }


    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        RoutingService.INSTANCE.onRoutingRailRemoved(pPos, pLevel);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide() || pHand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        long startTimestamp = System.currentTimeMillis();

        Direction direction = pPlayer.getDirection();
        RailTraverser.NextBlock neighbour = findNeighbourRoutingRail(pPos, direction, pLevel);
        String foundNeighbourDetails = null;
        Integer distance = null;
        if (neighbour != null) {
            foundNeighbourDetails = neighbour.pos.toString();
            distance = neighbour.traversedBlocksCount;
        }

        long duration = System.currentTimeMillis() - startTimestamp;

        pPlayer.sendSystemMessage(Component.literal("Direction %s. Found: %s. Distance: %s. Duration: %sms".formatted(direction, foundNeighbourDetails, distance, duration)));
        return InteractionResult.PASS;
    }

    @Nullable
    public RailTraverser.NextBlock findNeighbourRoutingRail(BlockPos pPos, Direction direction, Level level) {
        RailTraverser railTraverser = new RailTraverser(pPos, direction, level);
        RailTraverser.NextBlock result = null;
        for (RailTraverser.NextBlock nextBlock : railTraverser) {
            if (nextBlock.block instanceof RoutingRailBlock) {
                result = nextBlock;
                break;
            }
        }
        return result;
    }
}
