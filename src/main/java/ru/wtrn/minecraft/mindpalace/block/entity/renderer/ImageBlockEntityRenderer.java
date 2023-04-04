package ru.wtrn.minecraft.mindpalace.block.entity.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import ru.wtrn.minecraft.mindpalace.block.entity.ImageBlockEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

public class ImageBlockEntityRenderer implements BlockEntityRenderer<ImageBlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int textureId = -1;
    private BufferedImage bufferedImage;

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

    private int getTextureId() {
        if (bufferedImage == null) {
            return -1;
        }
        if (textureId == -1) {
            LOGGER.info("Uploading image to texture");
            textureId = uploadTexture(bufferedImage);
            LOGGER.info("Image uploaded to texture");
        }
        return textureId;
    }

    private int uploadTexture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        boolean hasAlpha = false;
        if (image.getColorModel().hasAlpha()) {
            for (int pixel : pixels) {
                if ((pixel >> 24 & 0xFF) < 0xFF) {
                    hasAlpha = true;
                    break;
                }
            }
        }
        int bytesPerPixel = hasAlpha ? 4 : 3;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
            buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
            buffer.put((byte) (pixel & 0xFF)); // Blue component
            if (hasAlpha) {
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
            }
        }
        buffer.flip();

        int textureID = GlStateManager._genTexture(); //Generate texture ID
        RenderSystem.bindTexture(textureID); //Bind texture ID

        //Setup wrap mode
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (!hasAlpha)
            RenderSystem.pixelStore(GL11.GL_UNPACK_ALIGNMENT, 1);

        // fixes random crash, when values are too high it causes a jvm crash, caused weird behavior when game is paused
        GlStateManager._pixelStore(3314, 0);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);

        //Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA8 : GL11.GL_RGB8, width, height, 0, hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);

        //Return the texture ID so we can bind it later again
        return textureID;
    }


    @Override
    public void render(ImageBlockEntity entity, float pPartialTick, PoseStack pose, MultiBufferSource multiBufferSource, int pPackedLight, int pPackedOverlay) {
        int textureId = getTextureId();

//        RenderSystem.enableDepthTest();
//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
////        RenderSystem.setShaderColor(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
//        RenderSystem.bindTexture(textureId);
//        RenderSystem.setShaderTexture(0, textureId);
//
//        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
//
//        pose.pushPose();
//        pose.translate(0.5, 0.5, 0.5);
//        pose.popPose();
//
//        Matrix4f mat = pose.last().pose();
//        Matrix3f mat3f = pose.last().normal();
//
//        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
//        Tesselator tesselator = Tesselator.getInstance();
//        BufferBuilder builder = tesselator.getBuilder();
//        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
//
//        builder
//                .vertex(mat, 0, 0, 0)
//                .uv(0, 0)
//                .normal(mat3f, 0, 0, 0)
//                .endVertex();
//
//        builder
//                .vertex(mat, 0, 0, 1)
//                .uv(0, 1)
//                .normal(mat3f, 0, 0, 1)
//                .endVertex();
//
//        builder
//                .vertex(mat, 1, 0, 1)
//                .uv(1, 1)
//                .normal(mat3f, 1, 0, 1)
//                .endVertex();
//
//        builder
//                .vertex(mat, 1, 0, 0)
//                .uv(1, 0)
//                .normal(mat3f, 1, 0, 0)
//                .endVertex();
//
//        tesselator.end();

    }
}
