package ru.wtrn.minecraft.mindpalace.items;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import retrofit2.Call;
import ru.wtrn.minecraft.mindpalace.client.texture.CachedTexture;
import ru.wtrn.minecraft.mindpalace.client.texture.TextureCache;
import ru.wtrn.minecraft.mindpalace.http.MciHttpService;
import ru.wtrn.minecraft.mindpalace.http.model.MciImageMetadata;
import ru.wtrn.minecraft.mindpalace.util.CachedAction;
import ru.wtrn.minecraft.mindpalace.util.math.base.Axis;
import ru.wtrn.minecraft.mindpalace.util.math.base.Facing;
import ru.wtrn.minecraft.mindpalace.util.math.box.AlignedBox;
import ru.wtrn.minecraft.mindpalace.util.math.vec.Vec2f;

import java.time.Duration;
import java.util.function.Supplier;

import static ru.wtrn.minecraft.mindpalace.config.ModClientConfigs.IMAGES_LOAD_DISTANCE;
import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.DEFAULT_IMAGE_WIDTH;
import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.MCI_SERVER_URL;

public class ImageFrame extends HangingEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long NO_IMAGE = -1L;
    public static final float frameThickness = 0.031F;
    private boolean initialized = false;
    private static final EntityDataAccessor<Long> DATA_IMAGE_ID = SynchedEntityData.defineId(ImageFrame.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Byte> DATA_TARGET_SIZE_TYPE = SynchedEntityData.defineId(ImageFrame.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_TARGET_SIZE = SynchedEntityData.defineId(ImageFrame.class, EntityDataSerializers.INT);
    private final CachedAction<?> doTickAction = new CachedAction<>(Duration.ofSeconds(1), () -> {
        this.doTick();
        return null;
    });

    private final CachedAction<Long> getImageIdAction = new CachedAction<>(Duration.ofSeconds(30), () -> getEntityData().get(DATA_IMAGE_ID));
    private final CachedAction<AlignedBox> getBoxAction = new CachedAction<>(Duration.ofSeconds(30), this::doGetBox);

    @OnlyIn(Dist.CLIENT)
    private Supplier<CachedTexture> cachedTextureSupplier;

    @OnlyIn(Dist.CLIENT)
    private int lastTextureId;

    @OnlyIn(Dist.CLIENT)
    private long lastTextureImageId;


    public ImageFrame(EntityType<ImageFrame> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        if (pLevel.isClientSide) {
            resetTexture();
        }
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
            String textureKey = getTextureKey(imageId);
            setTexture(imageId, textureKey);
        }
        int textureId = cachedTextureSupplier.get().getTextureId();
        if (initialized && textureId != this.lastTextureId) {
            this.lastTextureId = textureId;
            recalculateBoundingBox();
        }
        return textureId;
    }

    private void resetTexture() {
        cachedTextureSupplier = TextureCache.LOADING_TEXTURE;
        lastTextureId = CachedTexture.NO_TEXTURE;
        lastTextureImageId = NO_IMAGE;
    }

    @OnlyIn(Dist.CLIENT)
    private static String getTextureKey(long imageId) {
        return MCI_SERVER_URL.get() + "/storage/i/" + imageId;
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
        item.setTargetSize(stack, getTargetSizeType(), getTargetSize());
        if (pBrokenEntity instanceof Player player) {
            if (player.getMainHandItem().isEmpty()) {
                Inventory inventory = player.getInventory();
                inventory.setPickedItem(stack);
            }
            if (player.isLocalPlayer()) {
                Long imageId = getImageIdAction.invoke();
                String textureKey = getTextureKey(imageId);
                TextureCache.cleanup(textureKey);
                resetTexture();
            }
        } else {
            this.spawnAtLocation(stack);
        }
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        long imageId = getImageId();
        if (pPlayer.isShiftKeyDown()) {
            kill();
            dropItem(pPlayer);
            if (pPlayer.isLocalPlayer()) {
                pPlayer.sendSystemMessage(Component.literal("Destroyed image #" + imageId));
            }
            return InteractionResult.SUCCESS;
        }
        if (pPlayer.isLocalPlayer()) {
            if (pPlayer.getMainHandItem().is(Items.STICK)) {
                pPlayer.sendSystemMessage(Component.literal("Reloading texture for image #" + imageId));
                TextureCache.cleanup(getTextureKey(imageId));
                return InteractionResult.SUCCESS;
            }
            try {
                Call<MciImageMetadata> metadata = MciHttpService.INSTANCE.getImageMetadata(imageId);
                MutableComponent component = metadata.execute().body().toChatInfo();
                pPlayer.sendSystemMessage(component);
            } catch (Exception e) {
                LOGGER.error("Failed to get image {} metadata", imageId, e);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> pKey) {
        if (DATA_TARGET_SIZE_TYPE.equals(pKey) || DATA_TARGET_SIZE.equals(pKey)) {
            recalculateBoundingBox();
            return;
        }
        if (DATA_IMAGE_ID.equals(pKey)) {
            getImageIdAction.invalidate();
        }
    }

    @Override
    protected void recalculateBoundingBox() {
        if (!initialized) {
            super.recalculateBoundingBox();
            return;
        }
        getBoxAction.invalidate();
        this.setBoundingBox(getBox().getBB(position()));
    }

    @OnlyIn(Dist.CLIENT)
    private synchronized void setTexture(long imageId, String textureKey) {
        cachedTextureSupplier = TextureCache.getSupplier(textureKey);
        lastTextureImageId = imageId;
    }

    public AlignedBox getBox() {
        AlignedBox cached = getBoxAction.invoke();
        return new AlignedBox(cached);
    }

    public AlignedBox doGetBox() {
        float margin = -0.5f;
        float aspectRatio;
        if (level.isClientSide && this.cachedTextureSupplier != null) {
            aspectRatio = cachedTextureSupplier.get().getAspectRatio();
        } else {
            aspectRatio = 16 / 9f;
        }

        float xSize;
        float ySize;
        if (getTargetSizeType() == TargetSizeType.WIDTH) {
            xSize = this.getTargetSize();
            ySize = xSize / aspectRatio;
        } else {
            ySize = this.getTargetSize();
            xSize = ySize * aspectRatio;
        }

        Direction direction = getDirection();
        Facing facing = Facing.get(direction);

        AlignedBox box = new AlignedBox();
        box.setMax(facing.axis, frameThickness);

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
        SynchedEntityData entityData = this.getEntityData();
        entityData.define(DATA_IMAGE_ID, NO_IMAGE);
        entityData.define(DATA_TARGET_SIZE_TYPE, TargetSizeType.WIDTH.id);
        entityData.define(DATA_TARGET_SIZE, DEFAULT_IMAGE_WIDTH.get());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.setDirection(Direction.from3DDataValue(pPacket.getData()));
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putByte("facing", (byte) this.direction.get2DDataValue());
        pCompound.putLong("imageId", getImageId());
        pCompound.putByte("targetSizeType", getTargetSizeType().id);
        pCompound.putInt("targetSize", getTargetSize());
        super.addAdditionalSaveData(pCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.direction = Direction.from2DDataValue(pCompound.getByte("facing"));
        setImageId(pCompound.getLong("imageId"));
        TargetSizeType targetSizeType = TargetSizeType.getForId(pCompound.getByte("targetSizeType"));
        setTargetSize(targetSizeType, pCompound.getInt("targetSize"));
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

    public TargetSizeType getTargetSizeType() {
        Byte id = getEntityData().get(DATA_TARGET_SIZE_TYPE);
        return TargetSizeType.getForId(id);
    }

    public int getTargetSize() {
        int value = getEntityData().get(DATA_TARGET_SIZE);
        if (value == 0) {
            return DEFAULT_IMAGE_WIDTH.get();
        }
        return value;
    }

    public void setTargetSize(ImageFrame.TargetSizeType targetSizeSide, int size) {
        SynchedEntityData entityData = getEntityData();
        entityData.set(DATA_TARGET_SIZE_TYPE, targetSizeSide.id);
        entityData.set(DATA_TARGET_SIZE, size);
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
