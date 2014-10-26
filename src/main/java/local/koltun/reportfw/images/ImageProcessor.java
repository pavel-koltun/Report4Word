package local.koltun.reportfw.images;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageProcessor {
    public static byte[] getBytesFromImage(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        byte[] bytes = IOUtils.toByteArray(is);
        is.close();

        return bytes;
    }

    public static BufferedImage toBufferedImage(Image image, String imageFormatName) {
        if (image instanceof BufferedImage)
            return (BufferedImage) image;

        int type = BufferedImage.TYPE_INT_RGB;
        if (imageFormatName.equals("png")) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);

        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();

        return bufferedImage;
    }
}
