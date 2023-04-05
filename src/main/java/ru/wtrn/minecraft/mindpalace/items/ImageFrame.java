package ru.wtrn.minecraft.mindpalace.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
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
    public static final float frameThickness = 0.031F;
    private boolean initialized = false;

    public ImageFrame(EntityType<ImageFrame> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ImageFrame(EntityType<? extends HangingEntity> pEntityType, Level pLevel, BlockPos pPos, Direction direction) {
        super(pEntityType, pLevel, pPos);
        setDirection(direction);
    }

    @Override
    protected void setDirection(Direction pFacingDirection) {
        super.setDirection(pFacingDirection);
        initialized = true;
        recalculateBoundingBox();
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

        AlignedBox box = new AlignedBox();
        box.setMax(facing.axis, frameThickness);

        float margin = -0.5f;

        Vec2f min = new Vec2f(margin, margin);
        Vec2f max = new Vec2f(xSize + margin, ySize + margin);

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

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.setDirection(Direction.from3DDataValue(pPacket.getData()));
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity pEntity) {
        return false;
    }

    @Override
    protected void recalculateBoundingBox() {
        if (!initialized) {
            super.recalculateBoundingBox();
            return;
        }
        this.setBoundingBox(getBox().getBB(getPos()));
    }
}
