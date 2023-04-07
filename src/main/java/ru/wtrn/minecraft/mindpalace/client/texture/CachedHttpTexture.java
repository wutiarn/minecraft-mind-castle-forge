package ru.wtrn.minecraft.mindpalace.client.texture;

import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CachedHttpTexture extends CachedTexture {
    public CachedHttpTexture(String url) {
        super(url);
        this.fallback = TextureCache.LOADING_TEXTURE;
    }

    @Override
    protected void loadImage() throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(url)).openConnection(Minecraft.getInstance().getProxy());
        try {
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            this.bufferedImage = ImageIO.read(inputStream);
        } catch (Exception e) {
            this.fallback = TextureCache.ERROR_TEXTURE;
            throw e;
        } finally {
            httpURLConnection.disconnect();
        }
    }
}
