package ru.wtrn.minecraft.mindpalace.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class ImageLoader {
    public static BufferedImage loadImage(byte[] bytes) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes));
        ExifIFD0Directory exifMetadata = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        int orientationTag = ExifIFD0Directory.TAG_ORIENTATION;
        if (exifMetadata == null || !exifMetadata.containsTag(orientationTag)) {
            return image;
        }

        int orientation = exifMetadata.getInt(orientationTag);
        int rotationAngle = getRotationAngle(orientation);

        image = rotateImage(image, rotationAngle);

        return image;
    }

    /**
     * See com.drew.metadata.exif.makernotes.PanasonicMakernoteDescriptor#getRotationDescription() for reference
     */
    private static int getRotationAngle(int exifOrientation) {
        return switch (exifOrientation) {
            case 3 -> 180;
            case 6 -> 90;
            case 8 -> 270;
            default -> 0;
        };
    }

    /**
     * Reference: https://stackoverflow.com/a/66189875
     */
    private static BufferedImage rotateImage(BufferedImage src, int cwRotationAngle) {
        if (cwRotationAngle == 0) {
            return src;
        }
        int w;
        int h;
        if (cwRotationAngle != 180) {
            w = src.getHeight();
            h = src.getWidth();
        } else {
            w = src.getWidth();
            h = src.getHeight();
        }

        BufferedImage dst = new BufferedImage(w, h, src.getType());
        Graphics2D graphic = dst.createGraphics();
        graphic.translate((w - src.getWidth())/2.0, (h - src.getHeight())/2.0);
        graphic.rotate(Math.toRadians(cwRotationAngle), src.getWidth()/2.0, src.getHeight()/2.0);
        graphic.drawImage(src, null, 0, 0);
        graphic.dispose();
        return dst;
    }
}
