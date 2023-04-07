package ru.wtrn.minecraft.mindpalace.client.texture;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedTexture {
    public static final int NO_TEXTURE = -1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 5,
            1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("mci-texture-loader-%d").setDaemon(true).build());

    private final String url;
    private volatile BufferedImage bufferedImage = null;
    private volatile int textureId = NO_TEXTURE;
    private final AtomicInteger usageCounter = new AtomicInteger();
    private volatile Future<?> downloadFuture = null;

    public CachedTexture(String url) {
        this.url = url;

    }

    public int getTextureId() {
        if (textureId != NO_TEXTURE) {
            return textureId;
        }
        if (bufferedImage != null) {
            textureId = uploadTexture(bufferedImage);
            return textureId;
        }
        if (downloadFuture == null) {
            downloadFuture = executor.submit(() -> {
                try {
                    this.download();
                } catch (Exception e) {
                    LOGGER.error("Failed to download image for url {}", url, e);
                }
            });
        }
        return NO_TEXTURE;
    }

    public void incrementUsageCounter() {
        usageCounter.incrementAndGet();
    }

    public void decrementUsageCounter() {
        usageCounter.decrementAndGet();
    }

    public int getUsageCounter() {
        return usageCounter.get();
    }

    public void cleanup() {
        if (textureId == NO_TEXTURE && downloadFuture == null) {
            return;
        }
        if (downloadFuture != null && !downloadFuture.isDone()) {
            downloadFuture.cancel(true);
            waitForInitialization();
        }
        if (textureId != NO_TEXTURE) {
            GlStateManager._deleteTexture(textureId);
        }
        textureId = 0;
    }

    private void waitForInitialization() {
        try {
            downloadFuture.get();
        } catch (CancellationException e) {
            // ignore
        } catch (Exception e) {
            LOGGER.error("Failed to wait until initializationFuture completion", e);
        }
    }

    private void download() throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(url)).openConnection(Minecraft.getInstance().getProxy());
        try {
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            this.bufferedImage = ImageIO.read(inputStream);
        } finally {
            httpURLConnection.disconnect();
        }
    }

    private static int uploadTexture(BufferedImage image) {
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
}
