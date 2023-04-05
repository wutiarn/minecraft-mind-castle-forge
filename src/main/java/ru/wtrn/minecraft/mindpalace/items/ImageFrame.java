package ru.wtrn.minecraft.mindpalace.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.wtrn.minecraft.mindpalace.block.ImageBlock;
import ru.wtrn.minecraft.mindpalace.util.math.base.Axis;
import ru.wtrn.minecraft.mindpalace.util.math.base.Facing;
import ru.wtrn.minecraft.mindpalace.util.math.box.AlignedBox;
import ru.wtrn.minecraft.mindpalace.util.math.vec.Vec2f;

public class ImageFrame extends HangingEntity {

    private final float xSize = 10f;
    private final float ySize = 10f;

    public ImageFrame(EntityType<ImageFrame> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ImageFrame(EntityType<? extends HangingEntity> pEntityType, Level pLevel, BlockPos pPos, Direction direction) {
        super(pEntityType, pLevel, pPos);
        setDirection(direction);
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public void dropItem(@Nullable Entity pBrokenEntity) {

    }

    @Override
    public void playPlacementSound() {

    }

    @Override
    public boolean survives() {
        return super.survives();
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

        if (facing == Facing.EAST || facing == Facing.NORTH) {
            min.x -= xSize - 1;
            max.x -= xSize - 1;
        }

        box.setMin(one, min.x);
        box.setMax(one, max.x);

        box.setMin(two, min.y);
        box.setMax(two, max.y);
        return box;
    }

}
