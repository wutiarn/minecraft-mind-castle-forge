package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

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

        float speedFactor = 2.0f;
        Vec3 deltaMovement = cartMotion.multiply(speedFactor, speedFactor, speedFactor);
        final int neighborsToCheck = (int) Math.ceil(getPlaneSqrtDistance(cartMotion));

        Vec3 blockPosVec = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        boolean triggerMove = true;
        for (int i = 1; i <= neighborsToCheck; i++) {
            Vec3 targetPos = blockPosVec.add(deltaMovement);
            BlockState neigborBlockState = level.getBlockState(new BlockPos(targetPos.x, targetPos.y, targetPos.z));
            if (!neigborBlockState.is(this)) {
                triggerMove = false;
                break;
            }
        }

        if (triggerMove) {
            cart.move(MoverType.SELF, deltaMovement);
        }
    }

    @Override
    protected boolean findPoweredRailSignal(Level pLevel, BlockPos pPos, BlockState pState, boolean pSearchForward, int pRecursionCount) {
        return true;
    }

    private static double getPlaneSqrtDistance(Vec3 vec) {
        return Math.sqrt(getPlaneSqrDistance(vec));
    }

    private static double getPlaneSqrDistance(Vec3 vec) {
        return vec.x * vec.x + vec.z * vec.z;
    }

    private static Vec3 getUnitDirectionVector(Vec3 cartMotion) {
        if (Math.abs(cartMotion.x) > 0) {
            return new Vec3(Math.signum(cartMotion.x), 0, 0);
        }
        return new Vec3(0, 0, Math.signum(cartMotion.z));
    }
}
