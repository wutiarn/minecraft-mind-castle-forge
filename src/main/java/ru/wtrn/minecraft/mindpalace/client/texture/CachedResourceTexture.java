package ru.wtrn.minecraft.mindpalace.client.texture;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class CachedResourceTexture extends CachedTexture {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation resourceLocation;
    private boolean loadFailed = false;

    public CachedResourceTexture(String resourceLocation) {
        super(resourceLocation);
        this.resourceLocation = new ResourceLocation(resourceLocation);
    }

    @Override
    public int getTextureId() {
        if (loadFailed) {
            return NO_TEXTURE;
        }
        if (textureId != NO_TEXTURE) {
            return textureId;
        }
        if (preparedImage == null) {
            try {
                this.loadImage();
                LOGGER.info("Image loaded: {}", url);
            } catch (Exception e) {
                loadFailed = true;
                LOGGER.error("Failed to load image from resource {}", url, e);
                return NO_TEXTURE;
            }
        }
        textureId = uploadTexture(preparedImage);
        return textureId;
    }

    @Override
    protected void loadImage() throws Exception {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Failed to find resource " + resourceLocation);
        }
        try (InputStream inputStream = resource.get().open()) {
            BufferedImage image = ImageIO.read(inputStream);
            this.preparedImage = prepareImage(image);
        }
    }
}
