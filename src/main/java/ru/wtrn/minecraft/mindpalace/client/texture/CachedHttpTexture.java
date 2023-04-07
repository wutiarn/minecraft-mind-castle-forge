package ru.wtrn.minecraft.mindpalace.client.texture;

import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CachedHttpTexture extends CachedTexture {
    public CachedHttpTexture(String url) {
        super(url);
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
        } finally {
            httpURLConnection.disconnect();
        }
    }
}
