package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.application.dto.LoginResponseDTO;
import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;
import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Pantalla de Inicio de Sesión Institucional (Sin Emojis).
 */
public class LoginFrame extends JFrame {

    private final SicaApiClient apiClient;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    public LoginFrame(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
    }

    private void initUI() {
        setTitle("SICA - Sistema Integrado de Control de Accesos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 0, 0));
        mainContainer.setBackground(SicaTheme.BG_MAIN);
        setContentPane(mainContainer);

        // COLUMNA IZQUIERDA: Contexto Institucional
        JPanel leftBrandPanel = new JPanel(new BorderLayout(0, 16));
        leftBrandPanel.setBackground(SicaTheme.HEADER_BG);
        leftBrandPanel.setBorder(new EmptyBorder(40, 36, 40, 36));

        JPanel brandHeader = new JPanel(new GridLayout(3, 1, 6, 6));
        brandHeader.setOpaque(false);

        JLabel lblBadge = new JLabel("INFRAESTRUCTURA DE SEGURIDAD");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(SicaTheme.ACCENT_CYAN);

        JLabel lblBrandTitle = new JLabel("SICA ZONA ACME");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBrandTitle.setForeground(Color.WHITE);

        try {
            java.net.URL logoUrl = getClass().getResource("/img/enter.png");
            if (logoUrl != null) {
                ImageIcon originalIcon = new ImageIcon(logoUrl);
                this.setIconImage(originalIcon.getImage());
                Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                lblBrandTitle.setIcon(new ImageIcon(scaledImage));
                lblBrandTitle.setIconTextGap(12);
            }
        } catch (Exception e) {
            System.err.println("Error loading default logo: " + e.getMessage());
        }

        JLabel lblSub = new JLabel("Plataforma Integrada de Control de Accesos y Registro Biométrico.");
        lblSub.setFont(SicaTheme.FONT_BODY);
        lblSub.setForeground(new Color(200, 220, 240));

        brandHeader.add(lblBadge);
        brandHeader.add(lblBrandTitle);
        brandHeader.add(lblSub);

        leftBrandPanel.add(brandHeader, BorderLayout.NORTH);

        JPanel statusBox = new JPanel(new GridLayout(3, 1, 10, 10));
        statusBox.setOpaque(false);

        statusBox.add(createStatusPill("[ONLINE] SERVIDOR REST", "Puerto 8080 en ejecucion activa"));
        statusBox.add(createStatusPill("[BIOMETRÍA] CONTROL ACTIVO", "Reconocimiento Facial de 5 segundos"));
        statusBox.add(createStatusPill("[SEGURIDAD] AUDITORÍA", "Registro inmutable de accesos"));

        leftBrandPanel.add(statusBox, BorderLayout.CENTER);

        JLabel lblFooterBrand = new JLabel("SICA Enterprise v6.0 • Acme Security Group", SwingConstants.LEFT);
        lblFooterBrand.setFont(SicaTheme.FONT_SMALL);
        lblFooterBrand.setForeground(SicaTheme.TEXT_DISABLED);
        leftBrandPanel.add(lblFooterBrand, BorderLayout.SOUTH);

        mainContainer.add(leftBrandPanel);

        // COLUMNA DERECHA: Formulario
        JPanel rightFormPanel = new JPanel(new BorderLayout());
        rightFormPanel.setBackground(SicaTheme.BG_MAIN);
        rightFormPanel.setBorder(new EmptyBorder(40, 36, 40, 36));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(SicaTheme.CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblFormTitle = new JLabel("Iniciar Sesión");
        lblFormTitle.setFont(SicaTheme.FONT_TITLE);
        lblFormTitle.setForeground(SicaTheme.TEXT_MAIN);

        JLabel lblFormSub = new JLabel("Ingrese sus credenciales de operador autorizadas.");
        lblFormSub.setFont(SicaTheme.FONT_BODY);
        lblFormSub.setForeground(SicaTheme.TEXT_MUTED);

        JLabel lblUsername = new JLabel("Nombre de Usuario:");
        lblUsername.setFont(SicaTheme.FONT_BOLD);
        lblUsername.setForeground(SicaTheme.TEXT_MAIN);

        txtUsername = new JTextField("admin", 18);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setBackground(SicaTheme.CARD_BG_ALT);
        txtUsername.setForeground(SicaTheme.TEXT_MAIN);
        txtUsername.setCaretColor(SicaTheme.ACCENT_CYAN);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(SicaTheme.FONT_BOLD);
        lblPassword.setForeground(SicaTheme.TEXT_MAIN);

        txtPassword = new JPasswordField("admin123", 18);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setBackground(SicaTheme.CARD_BG_ALT);
        txtPassword.setForeground(SicaTheme.TEXT_MAIN);
        txtPassword.setCaretColor(SicaTheme.ACCENT_CYAN);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        btnLogin = new JButton("AUTENTICAR E INGRESAR");
        SicaTheme.styleButton(btnLogin, SicaTheme.ACCENT_CYAN, Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.addActionListener(e -> executeLogin());

        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setForeground(SicaTheme.STATUS_DENIED_TEXT);
        lblError.setFont(SicaTheme.FONT_BOLD);

        gbc.gridy = 0; formCard.add(lblFormTitle, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 14, 0); formCard.add(lblFormSub, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(4, 0, 4, 0); formCard.add(lblUsername, gbc);
        gbc.gridy = 3; formCard.add(txtUsername, gbc);
        gbc.gridy = 4; formCard.add(lblPassword, gbc);
        gbc.gridy = 5; formCard.add(txtPassword, gbc);
        gbc.gridy = 6; gbc.insets = new Insets(16, 0, 4, 0); formCard.add(btnLogin, gbc);
        gbc.gridy = 7; gbc.insets = new Insets(4, 0, 0, 0); formCard.add(lblError, gbc);

        rightFormPanel.add(formCard, BorderLayout.CENTER);

        JPanel footerHintPanel = new JPanel(new GridLayout(1, 3, 4, 4));
        footerHintPanel.setOpaque(false);
        footerHintPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnHintAdmin = new JButton("Admin");
        JButton btnHintGuardia = new JButton("Guardia");
        JButton btnHintFunc = new JButton("Funcionario");

        styleHintButton(btnHintAdmin, "admin", "admin123");
        styleHintButton(btnHintGuardia, "guardia1", "guardia123");
        styleHintButton(btnHintFunc, "func1", "func123");

        footerHintPanel.add(btnHintAdmin);
        footerHintPanel.add(btnHintGuardia);
        footerHintPanel.add(btnHintFunc);

        rightFormPanel.add(footerHintPanel, BorderLayout.SOUTH);

        mainContainer.add(rightFormPanel);
    }

    private JPanel createStatusPill(String title, String desc) {
        JPanel p = new JPanel(new GridLayout(2, 1, 2, 2));
        p.setBackground(new Color(21, 62, 90));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(40, 95, 135), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(SicaTheme.ACCENT_CYAN);

        JLabel d = new JLabel(desc);
        d.setFont(SicaTheme.FONT_SMALL);
        d.setForeground(Color.WHITE);

        p.add(t);
        p.add(d);
        return p;
    }

    private void styleHintButton(JButton btn, String u, String p) {
        btn.setFont(SicaTheme.FONT_SMALL);
        btn.setForeground(SicaTheme.TEXT_MUTED);
        btn.setBackground(SicaTheme.CARD_BG_ALT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(SicaTheme.BORDER_SUBTLE, 1, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            txtUsername.setText(u);
            txtPassword.setText(p);
        });
    }

    private void executeLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Ingrese su usuario y contraseña");
            return;
        }

        btnLogin.setEnabled(false);
        lblError.setText("Autenticando...");

        SwingWorker<LoginResponseDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResponseDTO doInBackground() throws Exception {
                return apiClient.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    LoginResponseDTO response = get();
                    lblError.setText("");

                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(LoginFrame.this,
                            "[+] Sesión Iniciada Correctamente. Bienvenido " + response.nombreCompleto(),
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);

                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        MainDashboardFrame dashboard = new MainDashboardFrame(apiClient);
                        dashboard.setVisible(true);
                    });

                } catch (Exception e) {
                    btnLogin.setEnabled(true);
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblError.setText("Error: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}
