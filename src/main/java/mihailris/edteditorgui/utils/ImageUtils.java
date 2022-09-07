package mihailris.edteditorgui.utils;

import mihailris.edteditorgui.uicomponents.TextEditor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class ImageUtils {
    public static Image loadColored(String path, Color tint){
        BufferedImage image;
        try {
            image = ImageIO.read(Objects.requireNonNull(TextEditor.class.getResource(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Graphics graphics = image.getGraphics();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color imageColor = new Color(image.getRGB(x, y), true);
                int a = imageColor.getAlpha();
                graphics.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), a));
                graphics.fillRect(x, y, 1, 1);
            }
        }
        graphics.dispose();
        return image;
    }
}
