package ru.wtrn.minecraft.mindpalace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs;
import ru.wtrn.minecraft.mindpalace.util.math.vec.VectorUtils;

public class FastRailBlock extends PoweredRailBlock {
    public FastRailBlock() {
        super(BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL), true);
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        super.onMinecartPass(state, level, pos, cart);
        controlSpeed(cart, level, pos);
    }

    protected void controlSpeed(AbstractMinecart cart, Level level, BlockPos pos) {
        final Vec3 cartMotion = cart.getDeltaMovement();

        if (Vec3.ZERO.closerThan(cartMotion, 0.1)) {
            return;
        }
        Vec3 directionVector = getUnitDirectionVector(cartMotion);
        Direction direction = VectorUtils.toHorizontalDirection(directionVector);

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
        SafeJumpPathFinder safeJumpPathFinder = new SafeJumpPathFinder(VectorUtils.toVec3(startPos), level, directionVector, this, maxJumpPath);
        Vec3 safeTravelVector = safeJumpPathFinder.getSafeTravelVector();
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

    private Vec3 findSafePath() {
//        new RailTraverser()
        return null;
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

    private static class SafeJumpPathFinder {
        private final Level level;
        private final Vec3 directionVector;
        private final FastRailBlock fastRailBlock;
        private final double maxPath;
        private double accumulatedPath = 0;
        private Vec3 currentPos;
        private Vec3 resultPath = null;

        public SafeJumpPathFinder(Vec3 startPos, Level level, Vec3 directionVector, FastRailBlock fastRailBlock, double maxPath) {
            this.level = level;
            this.currentPos = startPos;
            this.directionVector = directionVector;
            this.fastRailBlock = fastRailBlock;
            this.maxPath = maxPath;
        }

        public Vec3 getSafeTravelVector() {
            if (resultPath != null) {
                // Don't repeat calculations
                return resultPath;
            }
            resultPath = Vec3.ZERO;
            while (true) {
                if (!processNextBlock()) break;
            }
            return resultPath;
        }

        boolean processNextBlock() {
            BlockState currentBlockState = getBlockState(currentPos);
            if (!currentBlockState.is(fastRailBlock)) {
                return false;
            }
            RailShape railShape = currentBlockState.getValue(fastRailBlock.getShapeProperty());

            Vec3 targetPos = getNeighbourForShape(railShape, currentPos, directionVector);
            BlockState neigborBlockState = getBlockState(targetPos);
            if (!neigborBlockState.is(fastRailBlock)) {
                return false;
            }

            Vec3 delta = targetPos.add(currentPos.scale(-1));
            double deltaLength = VectorUtils.getHorizontalDistance(delta);
            double permittedPathLength = maxPath - accumulatedPath;

            boolean isCompleted = false;
            if (permittedPathLength < deltaLength) {
                double scaleFactor = permittedPathLength / deltaLength;
                delta = delta.scale(scaleFactor);
                deltaLength = deltaLength * scaleFactor;
                isCompleted = true;
            }

            resultPath = resultPath.add(delta);
            accumulatedPath += deltaLength;

            currentPos = targetPos;
            return !isCompleted;
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

            Direction destinationDirection = Direction.fromDelta((int) directionVector.x, (int) directionVector.y, (int) directionVector.z);

            int yValue = 1;
            if (destinationDirection != railDirection) {
                yValue = -1;
            }

            directionVector = new Vec3(directionVector.x, yValue, directionVector.z);
            return currentPos.add(directionVector);
        }
    }
}
