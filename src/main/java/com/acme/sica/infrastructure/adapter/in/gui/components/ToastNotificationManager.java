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

    public static void showToast(Component owner, String message, ToastType type) {
        SwingUtilities.invokeLater(() -> {
            Window window = null;
            if (owner != null) {
                if (owner instanceof Window w) {
                    window = w;
                } else {
                    window = SwingUtilities.getWindowAncestor(owner);
                }
            }

            if (window == null) {
                Frame[] frames = Frame.getFrames();
                for (Frame f : frames) {
                    if (f.isVisible() && f.isShowing()) {
                        window = f;
                        break;
                    }
                }
            }

            JDialog toast = window != null ? new JDialog(window) : new JDialog();
            toast.setUndecorated(true);
            toast.setAlwaysOnTop(true);
            toast.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(getBorderColor(type), 1, true),
                    new EmptyBorder(12, 18, 12, 18)
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

            int x, y;
            if (window != null && window.isShowing()) {
                Point loc = window.getLocationOnScreen();
                x = loc.x + window.getWidth() - toast.getWidth() - 30;
                y = loc.y + window.getHeight() - toast.getHeight() - 45;
            } else {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                x = screenSize.width - toast.getWidth() - 30;
                y = screenSize.height - toast.getHeight() - 60;
            }

            toast.setLocation(x, y);
            toast.setVisible(true);

            Timer timer = new Timer(3800, e -> {
                toast.setVisible(false);
                toast.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    public static void showToast(String message, ToastType type) {
        showToast((Component) null, message, type);
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
            case SUCCESS -> new Color(15, 118, 110); // Emerald Deep
            case WARNING -> new Color(180, 83, 9);   // Amber Deep
            case ERROR -> new Color(190, 18, 60);    // Rose Deep
            case INFO -> new Color(3, 105, 161);    // Cyan Deep
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
