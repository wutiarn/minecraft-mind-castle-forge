package ru.wtrn.minecraft.mindpalace.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import ru.wtrn.minecraft.mindpalace.block.ImageBlock;
import ru.wtrn.minecraft.mindpalace.util.math.base.Axis;
import ru.wtrn.minecraft.mindpalace.util.math.base.Facing;
import ru.wtrn.minecraft.mindpalace.util.math.box.AlignedBox;
import ru.wtrn.minecraft.mindpalace.util.math.vec.Vec2f;

public class ImageBlockEntity extends BlockEntity {

    private final float xSize = 10f;
    private final float ySize = 10f;

    public ImageBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.IMAGE_BLOCK.get(), blockPos, blockState);
    }

    public Direction getDirection() {
        return Direction.WEST;
    }

    public AlignedBox getBox() {
        Direction direction = getDirection();
        Facing facing = Facing.get(direction);
        AlignedBox box = ImageBlock.box(direction);

        Vec2f min = new Vec2f(0, 0);
        Vec2f max = new Vec2f(xSize, ySize);

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, min.x);
        box.setMax(one, max.x);

        box.setMin(two, min.y);
        box.setMax(two, max.y);
        return box;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getBox().getBB(getBlockPos());
    }
}
