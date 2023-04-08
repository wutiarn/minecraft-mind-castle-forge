package ru.wtrn.minecraft.mindpalace.items;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import ru.wtrn.minecraft.mindpalace.client.texture.CachedTexture;
import ru.wtrn.minecraft.mindpalace.client.texture.TextureCache;
import ru.wtrn.minecraft.mindpalace.util.CachedAction;
import ru.wtrn.minecraft.mindpalace.util.math.base.Axis;
import ru.wtrn.minecraft.mindpalace.util.math.base.Facing;
import ru.wtrn.minecraft.mindpalace.util.math.box.AlignedBox;
import ru.wtrn.minecraft.mindpalace.util.math.vec.Vec2f;

import java.time.Duration;
import java.util.function.Supplier;

import static ru.wtrn.minecraft.mindpalace.config.ModClientConfigs.IMAGES_LOAD_DISTANCE;
import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.DEFAULT_IMAGE_WIDTH;

public class ImageFrame extends HangingEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long NO_IMAGE = -1L;
    private final int targetSize = DEFAULT_IMAGE_WIDTH.get();
    private final TargetSizeType targetSizeType = TargetSizeType.WIDTH;
    public static final float frameThickness = 0.031F;
    private boolean initialized = false;
    private static final EntityDataAccessor<Long> DATA_IMAGE_ID = SynchedEntityData.defineId(ImageFrame.class, EntityDataSerializers.LONG);
    private final CachedAction<?> doTickAction = new CachedAction<>(Duration.ofSeconds(1), () -> {
        this.doTick();
        return null;
    });

    private final CachedAction<Long> getImageIdAction = new CachedAction<>(Duration.ofSeconds(1), () -> getEntityData().get(DATA_IMAGE_ID));

    @OnlyIn(Dist.CLIENT)
    private Supplier<CachedTexture> cachedTextureSupplier = TextureCache.LOADING_TEXTURE;

    private int lastTextureId = CachedTexture.NO_TEXTURE;

    @OnlyIn(Dist.CLIENT)
    private long lastTextureImageId = NO_IMAGE;


    public ImageFrame(EntityType<ImageFrame> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * This constructor is only used by ru.wtrn.minecraft.mindpalace.items.ImageFrameItem#useOn.
     * So we don't initialize textures here to avoid usage counter issues.
     */
    public ImageFrame(EntityType<? extends HangingEntity> pEntityType, Level pLevel, BlockPos pPos, Direction direction) {
        super(pEntityType, pLevel, pPos);
        setDirection(direction);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            doTickAction.invoke();
        }
    }

    private void doTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        Vec3 playerPosition = player.position();
        if (!playerPosition.closerThan(this.position(), IMAGES_LOAD_DISTANCE.get())) {
             return;
        }
        // Prepare texture if user is close to entity, even if it is not rendered yet
         getTextureId();
    }

    @OnlyIn(Dist.CLIENT)
    public int getTextureId() {
        long imageId = getImageIdAction.invoke();
        if (imageId != this.lastTextureImageId) {
            String textureKey = "http://100.64.1.3:8094/storage/i/" + imageId;
            setTexture(imageId, textureKey);
        }
        int textureId = cachedTextureSupplier.get().getTextureId();
        if (initialized && textureId != this.lastTextureId) {
            this.lastTextureId = textureId;
            recalculateBoundingBox();
        }
        return textureId;
    }

    @Override
    protected void setDirection(Direction pFacingDirection) {
        super.setDirection(pFacingDirection);
        initialized = true;
    }

    @Override
    public void dropItem(@Nullable Entity pBrokenEntity) {
        this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        ImageFrameItem item = ModItems.IMAGE_FRAME_ITEM.get();
        ItemStack stack = new ItemStack(item, 1);
        item.setImageId(stack, getImageId());
        if (pBrokenEntity instanceof Player) {
            Inventory inventory = ((Player) pBrokenEntity).getInventory();
            inventory.add(stack);
        } else {
            this.spawnAtLocation(stack);
        }
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        ItemStack mainHandItem = pPlayer.getMainHandItem();
        if (mainHandItem.is(Items.STICK)) {
            kill();
            dropItem(pPlayer);
        }
        if (!pPlayer.isLocalPlayer()) {
            long imageId = getImageId();
            MutableComponent component = Component.literal("Image ID: " + getImageId())
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mci.wtrn.ru/i/" + imageId)));
            pPlayer.sendSystemMessage(component);
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

    private synchronized void setTexture(long imageId, String textureKey) {
        cachedTextureSupplier = TextureCache.getSupplier(textureKey);
        lastTextureImageId = imageId;
    }

    public AlignedBox getBox() {
        float aspectRatio;
        if (level.isClientSide && this.cachedTextureSupplier != null) {
            aspectRatio = cachedTextureSupplier.get().getAspectRatio();
        } else {
            aspectRatio = 16 / 9f;
        }

        float xSize;
        float ySize;
        if (targetSizeType == TargetSizeType.WIDTH) {
            xSize = this.targetSize;
            ySize = xSize / aspectRatio;
        } else {
            ySize = this.targetSize;
            xSize = ySize * aspectRatio;
        }

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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_IMAGE_ID, NO_IMAGE);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.setDirection(Direction.from3DDataValue(pPacket.getData()));
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putByte("facing", (byte) this.direction.get2DDataValue());
        pCompound.putLong("imageId", getImageId());
        super.addAdditionalSaveData(pCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.direction = Direction.from2DDataValue(pCompound.getByte("facing"));
        setImageId(pCompound.getLong("imageId"));
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
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
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
        return getEntityData().get(DATA_IMAGE_ID);
    }

    public void setImageId(long imageId) {
        getEntityData().set(DATA_IMAGE_ID, imageId);
    }

    public enum TargetSizeType {
        WIDTH(1),
        HEIGHT(2);

        public final byte id;

        TargetSizeType(int id) {
            this.id = (byte) id;
        }

        public static TargetSizeType getForId(byte id) {
            //noinspection SwitchStatementWithTooFewBranches
            return switch (id) {
                case 2 -> HEIGHT;
                default -> WIDTH;
            };
        }
    }
}
