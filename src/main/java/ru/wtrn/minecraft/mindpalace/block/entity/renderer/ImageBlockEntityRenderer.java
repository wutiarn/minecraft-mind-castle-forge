package ru.wtrn.minecraft.mindpalace.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.slf4j.Logger;
import ru.wtrn.minecraft.mindpalace.block.entity.ImageBlockEntity;

public class ImageBlockEntityRenderer implements BlockEntityRenderer<ImageBlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ImageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        LOGGER.info("Initializing ImageBlockEntityRenderer");
    }

    @Override
    public void render(ImageBlockEntity entity, float p_112308_, PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, int pPackedOverlay) {

    }
}
