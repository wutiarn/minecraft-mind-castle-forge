package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs;

public class FastRailBlock extends PoweredRailBlock {
    public FastRailBlock() {
        super(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(0.7F).sound(SoundType.METAL), true);
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        super.onMinecartPass(state, level, pos, cart);
        controlSpeed(cart, level, pos);
    }

    protected void controlSpeed(AbstractMinecart cart, Level level, BlockPos pos) {
        final Vec3 cartMotion = cart.getDeltaMovement();

        Vec3 directionVector = getUnitDirectionVector(cartMotion);

        Double highSpeed = ModCommonConfigs.FAST_RAILS_HIGH_SPEED.get();
        final Double baseSpeed = ModCommonConfigs.FAST_RAILS_BASE_SPEED.get();
        final int neighborsToCheck = (int) Math.ceil(highSpeed);

        NeighbourRailIterator neighbourRailIterator = new NeighbourRailIterator(cart.position(), level, directionVector, this);
        for (int i = 1; i <= neighborsToCheck; i++) {
            if (!neighbourRailIterator.hasNext()) {
                highSpeed = Math.min(i - 1.0, highSpeed);
                break;
            }
        }

        cart.setDeltaMovement(directionVector.scale(baseSpeed));
        double additionalMoveSpeed = highSpeed - baseSpeed;
        if (additionalMoveSpeed > 0) {
            cart.move(MoverType.SELF, directionVector.scale(additionalMoveSpeed));
        }
    }

    @Override
    protected boolean findPoweredRailSignal(Level pLevel, BlockPos pPos, BlockState pState, boolean pSearchForward, int pRecursionCount) {
        return true;
    }

    private static Vec3 getUnitDirectionVector(Vec3 cartMotion) {
        if (Math.abs(cartMotion.x) > 0) {
            return new Vec3(Math.signum(cartMotion.x), 0, 0);
        }
        return new Vec3(0, 0, Math.signum(cartMotion.z));
    }

    private static class NeighbourRailIterator {
        private final Level level;
        private final Vec3 startPos;
        private final Vec3 directionVector;
        private final Block targetBlockType;

        public NeighbourRailIterator(Vec3 startPos, Level level, Vec3 directionVector, Block targetBlockType) {
            this.level = level;
            this.startPos = startPos;
            this.directionVector = directionVector;
            this.targetBlockType = targetBlockType;
        }

        boolean hasNext() {
            Vec3 targetPos = startPos.add(directionVector);
            BlockState neigborBlockState = level.getBlockState(new BlockPos(targetPos.x, targetPos.y, targetPos.z));
            return neigborBlockState.is(targetBlockType);
        }
    }
}
