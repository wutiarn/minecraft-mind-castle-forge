package ru.wtrn.minecraft.mindpalace.client.texture;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.wtrn.minecraft.mindpalace.config.ModClientConfigs.IMAGES_CLEANUP_DELAY_SECONDS;

public abstract class CachedTexture {
    public static final int NO_TEXTURE = -1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(0,
            new ThreadFactoryBuilder().setNameFormat("mci-texture-worker-%d").setDaemon(true).build());


    protected final String url;
    protected volatile PreparedImage preparedImage = null;
    protected volatile int textureId = NO_TEXTURE;
    protected volatile CachedTexture fallback = null;
    private final AtomicInteger usageCounter = new AtomicInteger();
    private volatile Future<?> downloadFuture = null;
    private volatile ScheduledFuture<?> cleanupFuture = null;

    public CachedTexture(String url) {
        this.url = url;
    }

    public int getTextureId() {
        if (textureId != NO_TEXTURE) {
            return textureId;
        }
        if (preparedImage != null) {
            uploadTexture(preparedImage);
            return textureId;
        }
        if (downloadFuture == null) {
            downloadFuture = executor.submit(() -> {
                try {
                    this.loadImage();
                    LOGGER.info("Image loaded: {}", url);
                } catch (Exception e) {
                    LOGGER.error("Failed to download image for url {}", url, e);
                }
            });
        }
        if (fallback != null) {
            return fallback.getTextureId();
        }
        return NO_TEXTURE;
    }

    public float getAspectRatio() {
        if (preparedImage != null) {
            return preparedImage.width / (float) preparedImage.height;
        }
        return 16 / 9f;
    }

    public synchronized void incrementUsageCounter() {
        usageCounter.incrementAndGet();
        if (cleanupFuture != null) {
            cleanupFuture.cancel(false);
            cleanupFuture = null;
        }
    }

    public synchronized void decrementUsageCounter() {
        int counterValue = usageCounter.decrementAndGet();
        if (counterValue <= 0) {
            if (counterValue < 0) {
                LOGGER.warn("Illegal counter value {} for url {}. Resetting to 0", counterValue, url);
                usageCounter.addAndGet(counterValue * -1);
            }
            if (cleanupFuture != null) {
                cleanupFuture.cancel(false);
            }
            LOGGER.info("Scheduled cleanup for image {}", url);
            cleanupFuture = executor.schedule(this::cleanup, IMAGES_CLEANUP_DELAY_SECONDS.get(), TimeUnit.SECONDS);
            if (textureId != NO_TEXTURE) {
                try {
                    GlStateManager._deleteTexture(textureId);
                    textureId = NO_TEXTURE;
                } catch (Exception e) {
                    LOGGER.error("Failed to delete texture {} for image {}", textureId, url);
                }
            }
        }
    }

    public synchronized void cleanup() {
        boolean onRenderThread = RenderSystem.isOnRenderThread();
        LOGGER.info("Running cleanup for image {} (isLoaded={}, onRenderThread={})", url, textureId != NO_TEXTURE, onRenderThread);
        if (downloadFuture != null && !downloadFuture.isDone()) {
            downloadFuture.cancel(true);
        }
        if (onRenderThread) {
            try {
                GlStateManager._deleteTexture(textureId);
                textureId = NO_TEXTURE;
            } catch (Exception e) {
                LOGGER.error("Failed to delete texture {} for image {}", textureId, url);
            }
        }
        textureId = NO_TEXTURE;
        downloadFuture = null;
        preparedImage = null;
        usageCounter.set(0);
        LOGGER.info("Cleanup completed for image {}", url);
    }

    protected abstract void loadImage() throws Exception;

    protected static PreparedImage prepareImage(BufferedImage image) {
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
        PreparedImage preparedImage = new PreparedImage();
        preparedImage.width = width;
        preparedImage.height = height;
        preparedImage.hasAlpha = hasAlpha;
        preparedImage.byteBuffer = buffer;
        return preparedImage;
    }

    protected void uploadTexture(PreparedImage image) {
        if (cleanupFuture != null) {
            LOGGER.warn("Aborting texture upload due to pending cleanup for image {}", url);
            textureId = NO_TEXTURE;
            return;
        }
        int textureId = GlStateManager._genTexture(); //Generate texture ID
        RenderSystem.bindTexture(textureId); //Bind texture ID

        //Setup wrap mode
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (!image.hasAlpha)
            RenderSystem.pixelStore(GL11.GL_UNPACK_ALIGNMENT, 1);

        // fixes random crash, when values are too high it causes a jvm crash, caused weird behavior when game is paused
        GlStateManager._pixelStore(3314, 0);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);

        //Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, image.hasAlpha ? GL11.GL_RGBA8 : GL11.GL_RGB8, image.width, image.height, 0, image.hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, image.byteBuffer);

        //Return the texture ID so we can bind it later again
        this.textureId = textureId;
    }

    protected static class PreparedImage {
        public int width;
        public int height;
        public boolean hasAlpha;
        public ByteBuffer byteBuffer;
    }
}
