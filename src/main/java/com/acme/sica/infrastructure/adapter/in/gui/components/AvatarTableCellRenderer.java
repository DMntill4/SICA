package com.acme.sica.infrastructure.adapter.in.gui.components;

import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom TableCellRenderer que renderiza miniaturas de Avatares / Fotos de Perfil
 * dentro de las filas de tablas de Swing con cache de alto rendimiento.
 */
public class AvatarTableCellRenderer extends DefaultTableCellRenderer {

    private static final Map<String, ImageIcon> ICON_CACHE = new ConcurrentHashMap<>();
    private static final ImageIcon DEFAULT_ICON = createDefaultIcon();

    public AvatarTableCellRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        label.setText("");

        if (value instanceof ImageIcon icon) {
            label.setIcon(icon);
            return label;
        }

        if (value == null || value.toString().trim().isEmpty()) {
            label.setIcon(DEFAULT_ICON);
            return label;
        }

        String url = value.toString().trim();
        if (ICON_CACHE.containsKey(url)) {
            label.setIcon(ICON_CACHE.get(url));
            return label;
        }

        label.setIcon(DEFAULT_ICON);

        // Carga asíncrona para no congelar la UI
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    BufferedImage img = ImageUtils.fetchImage(url);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(scaled);
                        ICON_CACHE.put(url, scaledIcon);
                        return scaledIcon;
                    }
                } catch (Exception ignored) {}
                return DEFAULT_ICON;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon res = get();
                    if (res != null) {
                        table.repaint();
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();

        return label;
    }

    private static ImageIcon createDefaultIcon() {
        return ImageUtils.createVectorAvatarIcon(28, 28);
    }
}

