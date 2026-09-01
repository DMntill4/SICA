package com.acme.sica.infrastructure.adapter.in.gui.components;

import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Tarjeta de Estado en Tiempo Real (Sin Emojis).
 */
public class EstadoSicaGradientCard extends JPanel {

    private final JLabel lblPersonaNombre;
    private final JLabel lblPersonaDocInfo;
    private final JLabel lblFotoAvatar;
    private final JLabel lblVisitaTarget;
    private final JLabel lblEstadoBanner;

    public EstadoSicaGradientCard() {
        setLayout(new BorderLayout(8, 8));
        setBackground(SicaTheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel contentPanel = new JPanel(new GridLayout(4, 1, 4, 4));
        contentPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("ESTADO EN TIEMPO REAL");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(SicaTheme.ACCENT_NAVY);

        lblPersonaNombre = new JLabel("Persona: (Selecciona una persona)");
        lblPersonaNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPersonaNombre.setForeground(SicaTheme.TEXT_MAIN);

        lblPersonaDocInfo = new JLabel("Doc: - | Tipo: CC");
        lblPersonaDocInfo.setFont(SicaTheme.FONT_BODY);
        lblPersonaDocInfo.setForeground(SicaTheme.TEXT_MUTED);

        lblVisitaTarget = new JLabel("Visita a: Ninguna visita activa");
        lblVisitaTarget.setFont(SicaTheme.FONT_BODY);
        lblVisitaTarget.setForeground(SicaTheme.TEXT_MUTED);

        contentPanel.add(lblTitle);
        contentPanel.add(lblPersonaNombre);
        contentPanel.add(lblPersonaDocInfo);
        contentPanel.add(lblVisitaTarget);

        lblFotoAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblFotoAvatar.setPreferredSize(new Dimension(72, 72));
        lblFotoAvatar.setMinimumSize(new Dimension(72, 72));
        lblFotoAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        lblFotoAvatar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.ACCENT_CYAN, 2, true),
                new EmptyBorder(2, 2, 2, 2)
        ));

        JPanel mainCenter = new JPanel(new BorderLayout(10, 0));
        mainCenter.setOpaque(false);
        mainCenter.add(contentPanel, BorderLayout.CENTER);
        mainCenter.add(lblFotoAvatar, BorderLayout.EAST);

        lblEstadoBanner = new JLabel(" SELECCIONE PERSONA EN PORTERÍA ", SwingConstants.CENTER);
        lblEstadoBanner.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoBanner.setOpaque(true);
        lblEstadoBanner.setBackground(SicaTheme.STATUS_INFO_BG);
        lblEstadoBanner.setForeground(SicaTheme.STATUS_INFO_TEXT);
        lblEstadoBanner.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        add(mainCenter, BorderLayout.CENTER);
        add(lblEstadoBanner, BorderLayout.SOUTH);
    }

    public void updateState(String nombre, String doc, String visitaDestino, String estadoAcceso) {
        updateState(nombre, doc, visitaDestino, estadoAcceso, null);
    }

    public void updateState(String nombre, String doc, String visitaDestino, String estadoAcceso, String fotoUrl) {
        lblPersonaNombre.setText("Persona: " + (nombre != null ? nombre : "-"));
        lblPersonaDocInfo.setText("Doc: " + (doc != null ? doc : "-") + " | Tipo: CC");
        lblVisitaTarget.setText("Visita a: " + (visitaDestino != null && !visitaDestino.isEmpty() ? visitaDestino : "Ninguna visita activa"));

        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    java.awt.image.BufferedImage img = ImageUtils.fetchImage(fotoUrl);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(68, 68, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        lblFotoAvatar.setIcon(icon != null ? icon : ImageUtils.createVectorAvatarIcon(68, 68));
                        lblFotoAvatar.setText("");
                    } catch (Exception ignored) {
                        lblFotoAvatar.setIcon(ImageUtils.createVectorAvatarIcon(68, 68));
                        lblFotoAvatar.setText("");
                    }
                }
            };
            worker.execute();
        } else {
            lblFotoAvatar.setIcon(ImageUtils.createVectorAvatarIcon(68, 68));
            lblFotoAvatar.setText("");
        }


        if ("HABILITADO".equalsIgnoreCase(estadoAcceso) || "ACTIVO".equalsIgnoreCase(estadoAcceso)) {
            lblEstadoBanner.setText("ACCESO AUTORIZADO - HABILITADO");
            lblEstadoBanner.setBackground(SicaTheme.STATUS_GRANTED_BG);
            lblEstadoBanner.setForeground(SicaTheme.STATUS_GRANTED_TEXT);
        } else if ("RESTRINGIDO".equalsIgnoreCase(estadoAcceso) || "BLOQUEADO".equalsIgnoreCase(estadoAcceso)) {
            lblEstadoBanner.setText("ACCESO RESTRINGIDO - BLOQUEADO");
            lblEstadoBanner.setBackground(SicaTheme.STATUS_DENIED_BG);
            lblEstadoBanner.setForeground(SicaTheme.STATUS_DENIED_TEXT);
        } else {
            lblEstadoBanner.setText("SELECCIONE PERSONA EN PORTERÍA");
            lblEstadoBanner.setBackground(SicaTheme.STATUS_INFO_BG);
            lblEstadoBanner.setForeground(SicaTheme.STATUS_INFO_TEXT);
        }
    }

}
