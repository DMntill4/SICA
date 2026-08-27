package com.acme.sica.infrastructure.adapter.in.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ToastNotificationManager {

    public enum ToastType {
        SUCCESS,
        WARNING,
        ERROR,
        INFO
    }

    public static void showToast(JFrame parentFrame, String message, ToastType type) {
        if (parentFrame == null) return;

        JDialog toast = new JDialog(parentFrame);
        toast.setUndecorated(true);
        toast.setAlwaysOnTop(true);
        toast.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(getBorderColor(type), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
        panel.setBackground(getBackgroundColor(type));

        JLabel lblSymbol = new JLabel(getSymbol(type));
        lblSymbol.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSymbol.setForeground(getTextColor(type));

        JLabel lblText = new JLabel(message);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblText.setForeground(getTextColor(type));

        panel.add(lblSymbol, BorderLayout.WEST);
        panel.add(lblText, BorderLayout.CENTER);

        toast.add(panel);
        toast.pack();

        // Posicionar en la esquina inferior derecha del Frame principal
        Point loc = parentFrame.getLocationOnScreen();
        int x = loc.x + parentFrame.getWidth() - toast.getWidth() - 25;
        int y = loc.y + parentFrame.getHeight() - toast.getHeight() - 40;
        toast.setLocation(x, y);

        toast.setVisible(true);

        // Desvanecimiento / Cierre automático tras 3.5 segundos
        Timer timer = new Timer(3500, e -> {
            toast.setVisible(false);
            toast.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static String getSymbol(ToastType type) {
        return switch (type) {
            case SUCCESS -> "[+]";
            case WARNING -> "[!]";
            case ERROR -> "[x]";
            case INFO -> "[i]";
        };
    }

    private static Color getBackgroundColor(ToastType type) {
        return switch (type) {
            case SUCCESS -> new Color(16, 185, 129); // Dark emerald
            case WARNING -> new Color(245, 158, 11); // Dark amber
            case ERROR -> new Color(225, 29, 72);   // Dark rose
            case INFO -> new Color(14, 165, 233);   // Dark cyan
        };
    }

    private static Color getBorderColor(ToastType type) {
        return switch (type) {
            case SUCCESS -> new Color(52, 211, 153);
            case WARNING -> new Color(252, 211, 77);
            case ERROR -> new Color(251, 113, 133);
            case INFO -> new Color(56, 189, 248);
        };
    }

    private static Color getTextColor(ToastType type) {
        return Color.WHITE;
    }
}
