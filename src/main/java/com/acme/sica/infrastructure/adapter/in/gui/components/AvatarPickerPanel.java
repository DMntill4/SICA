package com.acme.sica.infrastructure.adapter.in.gui.components;

import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.Random;

/**
 * Componente Reutilizable de Selección y Previsualización de Avatar / Foto de Perfil SICA.
 * Permite subir una imagen local de la PC (con conversión a Base64) o generar avatares aleatorios de IA,
 * mostrando una vista previa circular en tiempo real.
 */
public class AvatarPickerPanel extends JPanel {

    private final JLabel lblPreview;
    private String fotoUrl;
    private static final Random RANDOM = new Random();

    // Colección de semillas para fotos aleatorias realistas de IA
    private static final String[] RANDOM_AVATARS = {
            "https://i.pravatar.cc/150?img=1",
            "https://i.pravatar.cc/150?img=3",
            "https://i.pravatar.cc/150?img=5",
            "https://i.pravatar.cc/150?img=8",
            "https://i.pravatar.cc/150?img=12",
            "https://i.pravatar.cc/150?img=32",
            "https://i.pravatar.cc/150?img=47",
            "https://i.pravatar.cc/150?img=60"
    };


    public AvatarPickerPanel() {
        this(null);
    }

    public AvatarPickerPanel(String initialFotoUrl) {
        setLayout(new BorderLayout(12, 0));
        setOpaque(false);

        // Previsualización Circular de la Foto
        lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(80, 80));
        lblPreview.setMinimumSize(new Dimension(80, 80));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setVerticalAlignment(SwingConstants.CENTER);
        lblPreview.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.ACCENT_CYAN, 2, true),
                new EmptyBorder(2, 2, 2, 2)
        ));

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 4, 6));
        btnPanel.setOpaque(false);

        JButton btnSubir = new JButton("📁 Seleccionar Foto de PC");
        SicaTheme.styleButton(btnSubir, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MAIN);
        btnSubir.setFont(SicaTheme.FONT_SMALL);

        JButton btnReset = new JButton("🔄 Avatar Predeterminado");
        SicaTheme.styleButton(btnReset, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);
        btnReset.setFont(SicaTheme.FONT_SMALL);

        btnSubir.addActionListener(e -> seleccionarFotoDePC());
        btnReset.addActionListener(e -> restablecerAvatarPredeterminado());

        btnPanel.add(btnSubir);
        btnPanel.add(btnReset);

        add(lblPreview, BorderLayout.WEST);
        add(btnPanel, BorderLayout.CENTER);

        if (initialFotoUrl != null && !initialFotoUrl.trim().isEmpty()) {
            setFotoUrl(initialFotoUrl);
        } else {
            restablecerAvatarPredeterminado();
        }
    }

    public void restablecerAvatarPredeterminado() {
        this.fotoUrl = null;
        renderDefaultTextPreview();
    }

    private void seleccionarFotoDePC() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("📷 Seleccionar Foto de Perfil");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG, JPEG, WEBP)", "jpg", "jpeg", "png", "webp"));

        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage originalImage = ImageIO.read(selectedFile);
                if (originalImage != null) {
                    BufferedImage resized = resizeImage(originalImage, 200, 200);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resized, "png", baos);
                    String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                    this.fotoUrl = "data:image/png;base64," + base64;
                    renderPreview(resized);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen seleccionada: " + ex.getMessage(), "Error de Archivo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setFotoUrl(String url) {
        if (url == null || !url.startsWith("data:image")) {
            restablecerAvatarPredeterminado();
            return;
        }

        this.fotoUrl = url;
        BufferedImage img = ImageUtils.fetchImage(url);
        if (img != null) {
            renderPreview(img);
        } else {
            restablecerAvatarPredeterminado();
        }
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    private void renderPreview(BufferedImage img) {
        Image scaled = img.getScaledInstance(76, 76, Image.SCALE_SMOOTH);
        lblPreview.setIcon(new ImageIcon(scaled));
        lblPreview.setText("");
    }

    private void renderDefaultTextPreview() {
        lblPreview.setIcon(ImageUtils.createVectorAvatarIcon(76, 76));
        lblPreview.setText("");
    }


    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return outputImage;
    }
}
