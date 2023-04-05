package ru.wtrn.minecraft.mindpalace.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.wtrn.minecraft.mindpalace.util.math.base.Axis;
import ru.wtrn.minecraft.mindpalace.util.math.base.Facing;
import ru.wtrn.minecraft.mindpalace.util.math.box.AlignedBox;
import ru.wtrn.minecraft.mindpalace.util.math.vec.Vec2f;

public class ImageFrame extends HangingEntity {

    private final float xSize = 10f;
    private final float ySize = 10f;
    public static final float frameThickness = 0.031F;
    private boolean initialized = false;
    private long imageId = 0;

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
    public void dropItem(@Nullable Entity pBrokenEntity) {
        ImageFrameItem item = ModItems.IMAGE_FRAME_ITEM.get();
        ItemStack stack = new ItemStack(item, 1);
        item.setImageId(stack, imageId);
        this.spawnAtLocation(stack);
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        ItemStack mainHandItem = pPlayer.getMainHandItem();
        if (mainHandItem.is(Items.STICK)) {
            kill();
            dropItem(null);
        }
        if (pPlayer.isLocalPlayer()) {
            pPlayer.sendSystemMessage(Component.literal("Image ID: " + imageId));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void recalculateBoundingBox() {
        if (!initialized) {
            super.recalculateBoundingBox();
            return;
        }
        this.setBoundingBox(getBox().getBB(getPos()));
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

    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putByte("facing", (byte)this.direction.get2DDataValue());
        pCompound.putLong("imageId", imageId);
        super.addAdditionalSaveData(pCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.direction = Direction.from2DDataValue(pCompound.getByte("facing"));
        this.imageId = pCompound.getLong("imageId");
        super.readAdditionalSaveData(pCompound);
        this.setDirection(this.direction);
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
    public void playPlacementSound() {

    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean survives() {
        return true;
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }
}
