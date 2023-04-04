package ru.wtrn.minecraft.mindpalace.block.entity.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.slf4j.Logger;
import ru.wtrn.minecraft.mindpalace.block.entity.ImageBlockEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageBlockEntityRenderer implements BlockEntityRenderer<ImageBlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ImageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        LOGGER.info("Initializing ImageBlockEntityRenderer");
        try {
            loadTexture();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTexture() throws IOException {
        String url = "https://i.imgur.com/M6UFOku.png";
        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(url)).openConnection(Minecraft.getInstance().getProxy());
        BufferedImage bufferedImage;
        try {
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            bufferedImage = ImageIO.read(inputStream);
        } finally {
            httpURLConnection.disconnect();
        }

        LOGGER.info("Image loaded");
    }


    @Override
    public void render(ImageBlockEntity entity, float pPartialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, int pPackedOverlay) {
//        RenderSystem.enableDepthTest();
//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        RenderSystem.setShaderColor(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
//
//        poseStack.pushPose();
//        poseStack.popPose();
    }
}
