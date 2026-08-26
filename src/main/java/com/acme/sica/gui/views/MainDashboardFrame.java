package com.acme.sica.gui.views;

import com.acme.sica.gui.client.SessionContext;
import com.acme.sica.gui.client.SicaApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainDashboardFrame extends JFrame {

    private final SicaApiClient apiClient;

    private GuardiaPanel guardiaPanel;
    private FuncionarioPanel funcionarioPanel;
    private IncidentesPanel incidentesPanel;
    private AuditoriaPanel auditoriaPanel;

    public MainDashboardFrame(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
    }

    private void initUI() {
        SessionContext session = SessionContext.getInstance();

        setTitle("SICA - Dashboard Principal [" + session.getRoleName() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // --- ENCABEZADO DE NAVEGACION Y USUARIO ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Dark slate
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("SICA - Complejo Empresarial Zona Acme");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(56, 189, 248));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("👤 " + session.getNombreCompleto() + " (" + session.getRoleName() + ")");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("🚪 Cerrar Sesión");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> executeLogout());

        userPanel.add(userLabel);
        userPanel.add(btnLogout);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- CONTENEDOR CON PESTAÑAS (JTabbedPane) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Inicializar paneles
        guardiaPanel = new GuardiaPanel(apiClient);
        funcionarioPanel = new FuncionarioPanel(apiClient);
        incidentesPanel = new IncidentesPanel(apiClient);
        auditoriaPanel = new AuditoriaPanel(apiClient);

        // Filtrar según el rol del usuario
        if (session.isAdmin() || session.isGuardia()) {
            tabbedPane.addTab("🛡️ Control de Accesos (Guardia)", guardiaPanel);
        }

        if (session.isAdmin() || session.isFuncionario()) {
            tabbedPane.addTab("📋 Pre-Registro y Aprobaciones", funcionarioPanel);
        }

        if (session.isAdmin() || session.isGuardia()) {
            tabbedPane.addTab("🚨 Gestión de Incidentes", incidentesPanel);
        }

        if (session.isAdmin()) {
            tabbedPane.addTab("📊 Bitácora & Auditoría", auditoriaPanel);
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void executeLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión activa?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            apiClient.logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame(apiClient);
                login.setVisible(true);
            });
        }
    }
}
