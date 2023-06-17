package ru.wtrn.minecraft.mindpalace.client.texture;

import net.minecraft.client.Minecraft;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.wtrn.minecraft.mindpalace.http.MciHttpService;
import ru.wtrn.minecraft.mindpalace.util.ImageLoader;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CachedHttpTexture extends CachedTexture {

    public CachedHttpTexture(String url) {
        super(url);
        this.fallbackSupplier = TextureCache.LOADING_TEXTURE;
    }

    @Override
    protected void loadImage() throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = MciHttpService.HTTP_CLIENT.newCall(request).execute()) {
            InputStream inputStream = response.body().byteStream();
            long contentLength = response.body().contentLength();
            BufferedImage image = ImageLoader.loadImage(inputStream, (int) contentLength);
            this.preparedImage = prepareImage(image);
        } catch (Exception e) {
            this.fallbackSupplier = TextureCache.ERROR_TEXTURE;
            throw e;
        }
    }
}
