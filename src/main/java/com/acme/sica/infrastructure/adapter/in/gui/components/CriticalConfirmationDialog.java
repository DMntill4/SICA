package com.acme.sica.infrastructure.adapter.in.gui.components;

import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


/**
 * Modal personalizado para confirmaciones críticas de seguridad
 * (revocación de accesos, eliminación de personas/usuarios o limpieza de registros).
 */
public class CriticalConfirmationDialog extends JDialog {

    private boolean confirmed = false;

    public CriticalConfirmationDialog(Window owner, String title, String warningMessage, String confirmButtonText) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setResizable(false);
        initUI(title, warningMessage, confirmButtonText);
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI(String title, String warningMessage, String confirmButtonText) {
        JPanel contentPane = new JPanel(new BorderLayout(0, 16));
        contentPane.setBackground(SicaTheme.CARD_BG);
        contentPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, SicaTheme.STATUS_DENIED_TEXT),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Encabezado Alerta
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);

        JLabel lblBadge = new JLabel(" ALERTA DE SEGURIDAD ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setOpaque(true);
        lblBadge.setBackground(SicaTheme.STATUS_DENIED_BG);
        lblBadge.setForeground(SicaTheme.STATUS_DENIED_TEXT);
        lblBadge.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(SicaTheme.TEXT_MAIN);

        headerPanel.add(lblBadge);
        headerPanel.add(lblTitle);

        // Mensaje de Advertencia
        JLabel lblMessage = new JLabel("<html><body style='width: 320px;'>" + warningMessage + "</body></html>");
        lblMessage.setFont(SicaTheme.FONT_BODY);
        lblMessage.setForeground(SicaTheme.TEXT_MUTED);

        // Botonera
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnCancel = new JButton("Cancelar");
        SicaTheme.styleButton(btnCancel, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MAIN);
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JButton btnConfirm = new JButton(confirmButtonText != null ? confirmButtonText : "Confirmar Acción");
        SicaTheme.styleButton(btnConfirm, SicaTheme.STATUS_DENIED_TEXT, Color.WHITE);
        btnConfirm.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);

        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(lblMessage, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static boolean showConfirm(Window owner, String title, String warningMessage, String confirmButtonText) {
        CriticalConfirmationDialog dialog = new CriticalConfirmationDialog(owner, title, warningMessage, confirmButtonText);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }
}
