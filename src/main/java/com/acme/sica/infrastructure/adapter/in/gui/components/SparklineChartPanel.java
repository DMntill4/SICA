package com.acme.sica.infrastructure.adapter.in.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SparklineChartPanel extends JPanel {

    public SparklineChartPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("📈 Tendencia de Incidentes (Histórico)"),
                new EmptyBorder(4, 6, 4, 6)
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - 20;
        int height = getHeight() - 40;
        int startX = 10;
        int startY = 25;

        // Dibujar fondo oscuro semi-transparente
        g2d.setColor(new Color(15, 23, 42, 220));
        g2d.fillRoundRect(startX, startY, width, height, 10, 10);

        // Dibujar rejilla tenue de fondo
        g2d.setColor(new Color(51, 65, 85, 100));
        for (int i = 1; i <= 3; i++) {
            int y = startY + (height * i / 4);
            g2d.drawLine(startX + 10, y, startX + width - 10, y);
        }

        // Puntos de prueba para la gráfica de línea azul/roja
        int[] pointsY = {
                startY + height - 15,
                startY + height - 25,
                startY + height - 10,
                startY + height - 35,
                startY + height - 20,
                startY + height - 45,
                startY + height - 30,
                startY + height - 55,
                startY + height - 40
        };

        int stepX = (width - 30) / (pointsY.length - 1);

        // Línea 1 (Azul Neón)
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.setColor(new Color(56, 189, 248));
        for (int i = 0; i < pointsY.length - 1; i++) {
            int x1 = startX + 15 + (i * stepX);
            int y1 = pointsY[i];
            int x2 = startX + 15 + ((i + 1) * stepX);
            int y2 = pointsY[i + 1];
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Línea 2 (Roja de Advertencia)
        g2d.setColor(new Color(244, 63, 94));
        for (int i = 0; i < pointsY.length - 1; i++) {
            int x1 = startX + 15 + (i * stepX);
            int y1 = pointsY[i] + 8;
            int x2 = startX + 15 + ((i + 1) * stepX);
            int y2 = pointsY[i + 1] + 5;
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Puntos destacados
        for (int i = 0; i < pointsY.length; i++) {
            int x = startX + 15 + (i * stepX);
            int y = pointsY[i];
            g2d.setColor(new Color(56, 189, 248));
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }

        g2d.dispose();
    }
}
