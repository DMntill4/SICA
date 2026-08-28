package com.acme.sica.infrastructure.adapter.in.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;

public class SparklineChartPanel extends JPanel {

    private final JLabel lblTitle;

    public SparklineChartPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        lblTitle = new JLabel("📈 Tendencia de Incidentes (Histórico)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(148, 163, 184));
        lblTitle.setBorder(new EmptyBorder(0, 4, 6, 0));
        add(lblTitle, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int width = getWidth() - 16;
        int height = getHeight() - 32;
        int startX = 8;
        int startY = 28;

        // Fondo oscuro redondeado con gradiente sutil
        GradientPaint bgGradient = new GradientPaint(
                0, startY, new Color(15, 23, 42, 240),
                0, startY + height, new Color(9, 13, 22, 240)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(startX, startY, width, height, 16, 16);

        // Borde tenue menos evidente
        g2d.setColor(new Color(56, 189, 248, 40));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(startX, startY, width, height, 16, 16);

        // Rejilla horizontal tenue de fondo
        g2d.setColor(new Color(51, 65, 85, 60));
        for (int i = 1; i <= 3; i++) {
            int y = startY + (height * i / 4);
            g2d.drawLine(startX + 12, y, startX + width - 12, y);
        }

        // Puntos de datos para la tendencia
        int[] pointsY = {
                startY + height - 15,
                startY + height - 28,
                startY + height - 12,
                startY + height - 38,
                startY + height - 22,
                startY + height - 48,
                startY + height - 32,
                startY + height - 58,
                startY + height - 42
        };

        int stepX = (width - 32) / (pointsY.length - 1);

        // Relleno de Área Neón bajo la curva principal (Azul Cyan)
        Path2D.Double areaPath = new Path2D.Double();
        areaPath.moveTo(startX + 16, startY + height - 5);
        for (int i = 0; i < pointsY.length; i++) {
            int x = startX + 16 + (i * stepX);
            int y = pointsY[i];
            areaPath.lineTo(x, y);
        }
        areaPath.lineTo(startX + 16 + ((pointsY.length - 1) * stepX), startY + height - 5);
        areaPath.closePath();

        GradientPaint areaGradient = new GradientPaint(
                0, startY, new Color(56, 189, 248, 45),
                0, startY + height, new Color(56, 189, 248, 0)
        );
        g2d.setPaint(areaGradient);
        g2d.fill(areaPath);

        // Curva 1: Azul Cyan Neón
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.setColor(new Color(56, 189, 248));
        for (int i = 0; i < pointsY.length - 1; i++) {
            int x1 = startX + 16 + (i * stepX);
            int y1 = pointsY[i];
            int x2 = startX + 16 + ((i + 1) * stepX);
            int y2 = pointsY[i + 1];
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Curva 2: Rosa Advertencia
        g2d.setColor(new Color(244, 63, 94));
        g2d.setStroke(new BasicStroke(1.8f));
        for (int i = 0; i < pointsY.length - 1; i++) {
            int x1 = startX + 16 + (i * stepX);
            int y1 = pointsY[i] + 8;
            int x2 = startX + 16 + ((i + 1) * stepX);
            int y2 = pointsY[i + 1] + 6;
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Puntos brillaradores en la línea principal
        for (int i = 0; i < pointsY.length; i++) {
            int x = startX + 16 + (i * stepX);
            int y = pointsY[i];
            g2d.setColor(new Color(56, 189, 248));
            g2d.fillOval(x - 4, y - 4, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - 2, y - 2, 4, 4);
        }

        g2d.dispose();
    }
}
