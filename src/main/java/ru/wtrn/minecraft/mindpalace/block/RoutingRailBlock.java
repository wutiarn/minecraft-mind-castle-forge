package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import ru.wtrn.minecraft.mindpalace.entity.RoutingRailBlockEntity;
import ru.wtrn.minecraft.mindpalace.util.RailTraverser;

public class RoutingRailBlock extends RailBlock implements EntityBlock {
    public RoutingRailBlock() {
        super(Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        super.onMinecartPass(state, level, pos, cart);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext);
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
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide() || pHand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        long startTimestamp = System.currentTimeMillis();

        Direction direction = pPlayer.getDirection();
        RailTraverser.NextBlock neighbour = findNeighbourRoutingRail(pPos, direction, pLevel);
        String foundNeighbourDetails = null;
        if (neighbour != null) {
            foundNeighbourDetails = neighbour.pos.toString();
        }

        long duration = System.currentTimeMillis() - startTimestamp;

        pPlayer.sendSystemMessage(Component.literal("Routing report completed in %dms. Direction %s. Found: %s.".formatted(duration, direction, foundNeighbourDetails)));
        return InteractionResult.PASS;
    }

    @Nullable
    private RailTraverser.NextBlock findNeighbourRoutingRail(BlockPos pPos, Direction direction, Level level) {
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
