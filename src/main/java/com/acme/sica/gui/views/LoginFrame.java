package com.acme.sica.gui.views;

import com.acme.sica.gui.client.SessionContext;
import com.acme.sica.gui.client.SicaApiClient;
import com.acme.sica.infrastructure.adapter.in.dto.LoginResponseDTO;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        setTitle("SICA - Control de Acceso (Inicio de Sesión)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(contentPane);

        // Encabezado
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel titleLabel = new JLabel("SICA - Zona Acme", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(56, 189, 248)); // Blue accent

        JLabel subtitleLabel = new JLabel("Sistema Integrado de Control de Acceso", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Formulario Central
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Campo Username
        JLabel lblUsername = new JLabel("Nombre de Usuario:");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtUsername = new JTextField("admin", 20);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Campo Password
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtPassword = new JPasswordField("admin123", 20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Botón Login
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(14, 165, 233));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> executeLogin());

        // Mensaje de Error
        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setForeground(new Color(239, 68, 68)); // Red error
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));

        gbc.gridy = 0; formPanel.add(lblUsername, gbc);
        gbc.gridy = 1; formPanel.add(txtUsername, gbc);
        gbc.gridy = 2; formPanel.add(lblPassword, gbc);
        gbc.gridy = 3; formPanel.add(txtPassword, gbc);
        gbc.gridy = 4; gbc.insets = new Insets(20, 0, 5, 0); formPanel.add(btnLogin, gbc);
        gbc.gridy = 5; gbc.insets = new Insets(5, 0, 0, 0); formPanel.add(lblError, gbc);

        contentPane.add(formPanel, BorderLayout.CENTER);

        // Preset sugerencias en footer
        JPanel footerPanel = new JPanel(new GridLayout(3, 1));
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel hintAdmin = new JLabel("• Admin: admin / admin123", SwingConstants.CENTER);
        JLabel hintGuardia = new JLabel("• Guardia: guardia1 / guardia123", SwingConstants.CENTER);
        JLabel hintFunc = new JLabel("• Funcionario: func1 / func123", SwingConstants.CENTER);
        hintAdmin.setFont(new Font("Segoe UI", Font.ITALIC, 11)); hintAdmin.setForeground(Color.GRAY);
        hintGuardia.setFont(new Font("Segoe UI", Font.ITALIC, 11)); hintGuardia.setForeground(Color.GRAY);
        hintFunc.setFont(new Font("Segoe UI", Font.ITALIC, 11)); hintFunc.setForeground(Color.GRAY);

        footerPanel.add(hintAdmin);
        footerPanel.add(hintGuardia);
        footerPanel.add(hintFunc);
        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }

    private void executeLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Ingrese usuario y contraseña");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Autenticando...");
        lblError.setText("");

        // Ejecutar petición asíncrona con SwingWorker
        SwingWorker<LoginResponseDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResponseDTO doInBackground() throws Exception {
                return apiClient.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    LoginResponseDTO response = get();
                    dispose(); // Cerrar ventana Login
                    
                    // Abrir Dashboard Principal
                    SwingUtilities.invokeLater(() -> {
                        MainDashboardFrame dashboard = new MainDashboardFrame(apiClient);
                        dashboard.setVisible(true);
                    });
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblError.setText(cause.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Iniciar Sesión");
                }
            }
        };
        worker.execute();
    }
}
