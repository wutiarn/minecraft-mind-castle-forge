package ru.wtrn.minecraft.mindpalace.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ImageLoader {
    public static BufferedImage loadImage(InputStream is, int contentLength) throws Exception {
        BufferedInputStream bufferedStream = new BufferedInputStream(is, contentLength);
        bufferedStream.mark(contentLength + 1);
        BufferedImage image = ImageIO.read(bufferedStream);
        bufferedStream.reset();
        Metadata metadata = ImageMetadataReader.readMetadata(bufferedStream, contentLength);
        ExifIFD0Directory exifMetadata = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        int orientationTag = ExifIFD0Directory.TAG_ORIENTATION;
        if (exifMetadata == null || !exifMetadata.containsTag(orientationTag)) {
            return image;
        }

        int orientation = exifMetadata.getInt(orientationTag);
        int rotationAngle = getRotationAngle(orientation);

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
}
