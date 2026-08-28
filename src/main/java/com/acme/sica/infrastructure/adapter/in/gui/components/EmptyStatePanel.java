package com.acme.sica.infrastructure.adapter.in.gui.components;

import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Componente reutilizable para visualizar estados vacíos (empty states)
 * sin emojis (compatibilidad total Swing Windows).
 */
public class EmptyStatePanel extends JPanel {

    public EmptyStatePanel(String badgeText, String title, String description, String actionButtonText, ActionListener actionListener) {
        setLayout(new GridBagLayout());
        setBackground(SicaTheme.CARD_BG);
        setBorder(new EmptyBorder(32, 24, 32, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblBadge = new JLabel(badgeText != null ? badgeText : "[SICA]", SwingConstants.CENTER);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBadge.setOpaque(true);
        lblBadge.setBackground(SicaTheme.ACCENT_CYAN_LIGHT);
        lblBadge.setForeground(SicaTheme.ACCENT_NAVY);
        lblBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        add(lblBadge, gbc);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(SicaTheme.TEXT_MAIN);
        add(lblTitle, gbc);

        JLabel lblDesc = new JLabel("<html><center>" + description + "</center></html>", SwingConstants.CENTER);
        lblDesc.setFont(SicaTheme.FONT_BODY);
        lblDesc.setForeground(SicaTheme.TEXT_MUTED);
        lblDesc.setPreferredSize(new Dimension(280, 40));
        add(lblDesc, gbc);

        if (actionButtonText != null && actionListener != null) {
            gbc.insets = new Insets(12, 0, 0, 0);
            JButton btnAction = new JButton(actionButtonText);
            SicaTheme.styleButton(btnAction, SicaTheme.ACCENT_CYAN, Color.WHITE);
            btnAction.addActionListener(actionListener);
            add(btnAction, gbc);
        }
    }
}
