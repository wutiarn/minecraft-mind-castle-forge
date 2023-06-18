package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs;
import ru.wtrn.minecraft.mindpalace.util.math.base.Facing;
import ru.wtrn.minecraft.mindpalace.util.math.vec.VectorUtils;

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

        NeighbourRailIterator neighbourRailIterator = new NeighbourRailIterator(cart.position(), level, directionVector, this);
        int safeTravelBlocks = (int) Math.ceil(highSpeed - baseSpeed);
        Vec3 safeTravelVector = neighbourRailIterator.getSafeTravelVector(safeTravelBlocks);

        if (!safeTravelVector.closerThan(Vec3.ZERO, 0.1)) {
            cart.move(MoverType.SELF, safeTravelVector);
        }
        cart.setDeltaMovement(directionVector.scale(baseSpeed));
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
        private final Vec3 directionVector;
        private final FastRailBlock fastRailBlock;
        private Vec3 startPos;
        private Vec3 currentPos;

        public NeighbourRailIterator(Vec3 startPos, Level level, Vec3 directionVector, FastRailBlock fastRailBlock) {
            this.level = level;
            this.startPos = startPos;
            this.currentPos = startPos;
            this.directionVector = directionVector;
            this.fastRailBlock = fastRailBlock;
        }

        public Vec3 getSafeTravelVector(int blocksLimit) {
            for (int i = 0; i < blocksLimit; i++) {
                if (!findNext()) {
                    break;
                }
            }
            return currentPos.add(startPos.scale(-1));
        }

        boolean findNext() {
            BlockState currentBlockState = getBlockState(currentPos);
            if (!currentBlockState.is(fastRailBlock)) {
                return false;
            }
            RailShape railShape = currentBlockState.getValue(fastRailBlock.getShapeProperty());

            Vec3 targetPos = getNeighbourForShape(railShape, currentPos, directionVector);
            BlockState neigborBlockState = getBlockState(targetPos);
            if (!neigborBlockState.is(fastRailBlock)) {
                return false;
            };
            currentPos = targetPos;
            return true;
        }

        private BlockState getBlockState(Vec3 pos) {
            BlockPos blockPos = VectorUtils.toBlockPos(pos);
            return level.getBlockState(blockPos);
        }

        private static Vec3 getNeighbourForShape(RailShape railShape, Vec3 currentPos, Vec3 directionVector) {
            if (!railShape.isAscending()) {
                return currentPos.add(directionVector);
            }

            Direction railDirection = null;
            switch (railShape) {
                case ASCENDING_EAST -> railDirection = Direction.EAST;
                case ASCENDING_WEST -> railDirection = Direction.WEST;
                case ASCENDING_NORTH -> railDirection = Direction.NORTH;
                case ASCENDING_SOUTH -> railDirection = Direction.SOUTH;
            }

            Direction destinationDirection = Direction.fromNormal((int) directionVector.x, (int) directionVector.y, (int) directionVector.z);

            int yValue = 1;
            if (destinationDirection != railDirection) {
                yValue = -1;
            }

            directionVector = new Vec3(directionVector.x, yValue, directionVector.z);
            return currentPos.add(directionVector);
        }
    }
}
