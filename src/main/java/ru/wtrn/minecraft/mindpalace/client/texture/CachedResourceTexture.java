package ru.wtrn.minecraft.mindpalace.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class CachedResourceTexture extends CachedTexture {

    private final ResourceLocation resourceLocation;

    public CachedResourceTexture(String resourceLocation) {
        super(resourceLocation);
        this.resourceLocation = new ResourceLocation(resourceLocation);
    }

    @Override
    protected void loadImage() throws Exception {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Failed to find resource " + resourceLocation);
        }
        try (InputStream inputStream = resource.get().open()) {
            this.bufferedImage = ImageIO.read(inputStream);
        }
    }
}
