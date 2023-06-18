package ru.wtrn.minecraft.mindpalace.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import ru.wtrn.minecraft.mindpalace.http.MciHttpService;
import ru.wtrn.minecraft.mindpalace.entity.ModEntities;

import java.io.IOException;
import java.util.List;

import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.DEFAULT_IMAGE_WIDTH;

public class ImageFrameItem extends Item {

    public ImageFrameItem(Item.Properties pProperties) {
        super(pProperties);
    }

    /**
     * Called when this item is used when targeting a Block
     */
    public InteractionResult useOn(UseOnContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        Direction direction = pContext.getClickedFace();
        BlockPos blockpos1 = blockpos.relative(direction);
        Player player = pContext.getPlayer();
        ItemStack itemstack = pContext.getItemInHand();
        if (player != null && !this.mayPlace(player, direction, itemstack, blockpos1)) {
            return InteractionResult.FAIL;
        } else {
            Level level = pContext.getLevel();
            EntityType<ImageFrame> entityType = ModEntities.IMAGE_FRAME_ENTITY.get();
            ImageFrameItem item = ModItems.IMAGE_FRAME_ITEM.get();
            ImageFrame frame = new ImageFrame(entityType, level, blockpos1, direction);

            if (frame.survives()) {
                long imageId;
                boolean isLatestImage = isLatestImage(itemstack);
                if (isLatestImage) {
                    try {
                        imageId = getLatestImageId();
                    } catch (Exception e) {
                        pContext.getPlayer().sendSystemMessage(
                                Component.literal("Failed to retrieve latest image id from server: " + e)
                        );
                        return InteractionResult.FAIL;
                    }
                } else {
                    imageId = item.getImageId(itemstack);
                }
                frame.setImageId(imageId);
                frame.setTargetSize(getTargetSizeType(itemstack), getTargetSize(itemstack));

                if (!level.isClientSide) {
                    frame.playPlacementSound();
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, frame.position());
                    level.addFreshEntity(frame);
                }

                if (!isLatestImage) {
                    player.getInventory().removeItem(itemstack);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(Player pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
        return !pDirection.getAxis().isVertical() && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
    }

    @Override
    public Component getName(ItemStack pStack) {
        String text;
        if (isLatestImage(pStack)) {
            text = "Latest image";
        } else {
            text = "Image #" + getImageId(pStack);
        }
        return Component.literal(text);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.literal(getTargetSizeType(pStack) + ": " + getTargetSize(pStack)));
    }

    public void setImageId(ItemStack stack, long imageId) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong("imageId", imageId);
    }

    public long getImageId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getLong("imageId");
    }

    public void setTargetSize(ItemStack stack, ImageFrame.TargetSizeType targetSizeSide, int size) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putByte("targetSizeSide", targetSizeSide.id);
        tag.putInt("targetSize", size);
    }

    public ImageFrame.TargetSizeType getTargetSizeType(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        byte value = tag.getByte("targetSizeSide");
        return ImageFrame.TargetSizeType.getForId(value);
    }

    public int getTargetSize(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int value = tag.getInt("targetSize");
        if (value == 0) {
            return DEFAULT_IMAGE_WIDTH.get();
        }
        return value;
    }

    public boolean isLatestImage(ItemStack stack) {
        return getImageId(stack) == 0;
    }

    private long getLatestImageId() throws IOException {
        return MciHttpService.INSTANCE.getLatestImageMetadata().execute().body().id;
    }
}
