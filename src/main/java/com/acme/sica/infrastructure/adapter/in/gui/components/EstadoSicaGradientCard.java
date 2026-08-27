package com.acme.sica.infrastructure.adapter.in.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EstadoSicaGradientCard extends JPanel {

    private final JLabel lblPersonaNombre;
    private final JLabel lblPersonaDocInfo;
    private final JLabel lblFotoUrlInfo;
    private final JLabel lblVisitaTarget;
    private final JLabel lblEstadoBanner;

    public EstadoSicaGradientCard() {
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel contentPanel = new JPanel(new GridLayout(5, 1, 4, 4));
        contentPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("ESTADO SICA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(148, 163, 184));

        lblPersonaNombre = new JLabel("Persona: (Selecciona una persona)");
        lblPersonaNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPersonaNombre.setForeground(Color.WHITE);

        lblPersonaDocInfo = new JLabel("Doc: - | Tipo: CC");
        lblPersonaDocInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPersonaDocInfo.setForeground(new Color(203, 213, 225));

        lblFotoUrlInfo = new JLabel("[i] Foto: Registrada en Sistema");
        lblFotoUrlInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFotoUrlInfo.setForeground(new Color(148, 163, 184));

        lblVisitaTarget = new JLabel("Visita a: Ninguna visita activa");
        lblVisitaTarget.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVisitaTarget.setForeground(new Color(203, 213, 225));

        contentPanel.add(lblTitle);
        contentPanel.add(lblPersonaNombre);
        contentPanel.add(lblPersonaDocInfo);
        contentPanel.add(lblFotoUrlInfo);
        contentPanel.add(lblVisitaTarget);

        lblEstadoBanner = new JLabel(" SELECCIONE PERSONA DE LA LISTA ", SwingConstants.CENTER);
        lblEstadoBanner.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoBanner.setOpaque(true);
        lblEstadoBanner.setBackground(new Color(71, 85, 105));
        lblEstadoBanner.setForeground(Color.WHITE);
        lblEstadoBanner.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        add(contentPanel, BorderLayout.CENTER);
        add(lblEstadoBanner, BorderLayout.SOUTH);
    }

    public void updateState(String nombre, String doc, String visitaDestino, String estadoAcceso) {
        lblPersonaNombre.setText("Persona: " + (nombre != null ? nombre : "-"));
        lblPersonaDocInfo.setText("Doc: " + (doc != null ? doc : "-") + " | Tipo: CC");
        lblVisitaTarget.setText("Visita a: " + (visitaDestino != null && !visitaDestino.isEmpty() ? visitaDestino : "Ninguna visita activa"));

        if ("HABILITADO".equalsIgnoreCase(estadoAcceso) || "ACTIVO".equalsIgnoreCase(estadoAcceso)) {
            lblEstadoBanner.setText("ACCESO AUTORIZADO - HABILITADO");
            lblEstadoBanner.setBackground(new Color(16, 185, 129));
            lblEstadoBanner.setForeground(Color.WHITE);
        } else if ("RESTRINGIDO".equalsIgnoreCase(estadoAcceso) || "BLOQUEADO".equalsIgnoreCase(estadoAcceso)) {
            lblEstadoBanner.setText("ACCESO RESTRINGIDO - BLOQUEADO");
            lblEstadoBanner.setBackground(new Color(225, 29, 72));
            lblEstadoBanner.setForeground(Color.WHITE);
        } else {
            lblEstadoBanner.setText("SELECCIONE PERSONA DE LA LISTA");
            lblEstadoBanner.setBackground(new Color(71, 85, 105));
            lblEstadoBanner.setForeground(Color.WHITE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Degradado visual idéntico al diseño Figma
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(15, 52, 67),
                getWidth(), getHeight(), new Color(36, 59, 85)
        );
        g2d.setPaint(gp);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        // Borde redondeado fino
        g2d.setColor(new Color(56, 189, 248, 80));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2d.dispose();
        super.paintComponent(g);
    }
}
