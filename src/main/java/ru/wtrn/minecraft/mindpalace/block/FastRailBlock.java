package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs;
import ru.wtrn.minecraft.mindpalace.util.RailTraverser;
import ru.wtrn.minecraft.mindpalace.util.math.vec.VectorUtils;

public class FastRailBlock extends RailBlock {
    public FastRailBlock() {
        super(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        super.onMinecartPass(state, level, pos, cart);
        controlSpeed(cart, level, pos);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide() || pHand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        AbstractMinecart minecart = AbstractMinecart.createMinecart(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.0625, pPos.getZ() + 0.5, AbstractMinecart.Type.RIDEABLE);
        pLevel.addFreshEntity(minecart);
        pLevel.gameEvent(GameEvent.ENTITY_PLACE, pPos, GameEvent.Context.of(pPlayer, pLevel.getBlockState(pPos)));
        minecart.interact(pPlayer, InteractionHand.MAIN_HAND);

        return InteractionResult.PASS;
    }

    protected void controlSpeed(AbstractMinecart cart, Level level, BlockPos pos) {
        final Vec3 cartMotion = cart.getDeltaMovement();

        if (Vec3.ZERO.closerThan(cartMotion, 0.01)) {
            return;
        }
        Vec3 directionVector = getUnitDirectionVector(cartMotion);
        if (!isOccupiedByPlayer(cart)) {
            cart.kill();
            return;
        }

        Double highSpeed = ModCommonConfigs.FAST_RAILS_HIGH_SPEED.get();
        final Double baseSpeed = ModCommonConfigs.FAST_RAILS_BASE_SPEED.get();

        double maxJumpPath = highSpeed - baseSpeed;
        if (maxJumpPath > 0) {
            performJump(pos, maxJumpPath, cart, level, directionVector);
        }
        cart.setDeltaMovement(directionVector.scale(baseSpeed));
    }

    private void performJump(BlockPos startPos, double maxJumpPath, AbstractMinecart cart, Level level, Vec3 directionVector) {
        Direction direction = VectorUtils.toHorizontalDirection(directionVector);
        Vec3 safeTravelVector = findSafePath(startPos, direction, level, maxJumpPath);
        // Avoid collisions if cart is on long fast rails track
        cart.noPhysics = maxJumpPath - VectorUtils.getHorizontalDistance(safeTravelVector) < 0.01;

        if (Vec3.ZERO.closerThan(safeTravelVector, 0.1)) {
            return;
        }
        cart.move(MoverType.SELF, safeTravelVector);
    }

    private boolean isOccupiedByPlayer(AbstractMinecart cart) {
        for (Entity passenger : cart.getPassengers()) {
            if (passenger instanceof Player) {
                return true;
            }
        }
        return false;
    }

    private Vec3 findSafePath(BlockPos startPos, Direction direction, Level level, double maxPath) {
        RailTraverser traverser = new RailTraverser(startPos, direction, level);
        Vec3 resultPath = Vec3.ZERO;
        double accumulatedPath = 0;
        boolean isCompleted = false;

        for (RailTraverser.NextBlock nextBlock : traverser) {
            if (!(nextBlock.block instanceof FastRailBlock)) {
                break;
            }
            Vec3 delta = nextBlock.deltaFromPrevious;
            double deltaLength = VectorUtils.getHorizontalDistance(delta);
            double permittedPathLength = maxPath - accumulatedPath;

            if (permittedPathLength < deltaLength) {
                double scaleFactor = permittedPathLength / deltaLength;
                delta = delta.scale(scaleFactor);
                deltaLength = deltaLength * scaleFactor;
                isCompleted = true;
            }

            resultPath = resultPath.add(delta);
            accumulatedPath += deltaLength;
            if (isCompleted) {
                break;
            }
        }
        return resultPath;
    }

    @Override
    public boolean isFlexibleRail(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    private static Vec3 getUnitDirectionVector(Vec3 cartMotion) {
        if (Math.abs(cartMotion.x) > 0) {
            return new Vec3(Math.signum(cartMotion.x), 0, 0);
        }
        return new Vec3(0, 0, Math.signum(cartMotion.z));
    }
}
