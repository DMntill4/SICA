package com.acme.sica.infrastructure.adapter.in.gui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Utilidad robusta para carga asíncrona de imágenes HTTP/HTTPS con User-Agent
 * y generación vectorizada 2D de avatares sin dependencia de fuentes emoji.
 */
public class ImageUtils {

    public static BufferedImage fetchImage(String fotoUrl) {
        if (fotoUrl == null || !fotoUrl.startsWith("data:image")) {
            return null;
        }
        try {
            String base64Data = fotoUrl.substring(fotoUrl.indexOf(",") + 1);
            byte[] bytes = Base64.getDecoder().decode(base64Data);
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }


    public static ImageIcon createVectorAvatarIcon(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo circular oscuro
        g2d.setColor(new Color(30, 41, 59));
        g2d.fillOval(1, 1, width - 2, height - 2);

        // Borde cian brillante
        g2d.setColor(new Color(56, 189, 248));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(1, 1, width - 2, height - 2);

        // Silueta Cabeza (Vector Graphics 2D)
        g2d.setColor(new Color(226, 232, 240));
        int headSize = Math.max(6, width / 3);
        int headX = (width - headSize) / 2;
        int headY = (int) (height * 0.18);
        g2d.fillOval(headX, headY, headSize, headSize);

        // Silueta Hombros (Vector Arc 2D)
        int bodyWidth = (int) (width * 0.65);
        int bodyHeight = (int) (height * 0.50);
        int bodyX = (width - bodyWidth) / 2;
        int bodyY = (int) (height * 0.54);
        g2d.fillArc(bodyX, bodyY, bodyWidth, bodyHeight, 0, 180);

        g2d.dispose();
        return new ImageIcon(img);
    }
}
