package ru.wtrn.minecraft.mindpalace.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageLoader {
    public static BufferedImage loadImage(InputStream is, int contentLength) throws IOException {
        BufferedInputStream bufferedStream = new BufferedInputStream(is, contentLength);
        bufferedStream.mark(contentLength + 1);
        BufferedImage image = ImageIO.read(bufferedStream);
        bufferedStream.reset();
        return image;
    }
}
