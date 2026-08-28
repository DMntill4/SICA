package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;
import com.acme.sica.infrastructure.adapter.in.gui.client.SessionContext;
import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dashboard Principal con Navegación Operativa (Sin Emojis).
 */
public class MainDashboardFrame extends JFrame {

    private final SicaApiClient apiClient;

    private GuardiaPanel guardiaPanel;
    private FuncionarioPanel funcionarioPanel;
    private IncidentesPanel incidentesPanel;
    private ReportesPanel reportesPanel;
    private AuditoriaPanel auditoriaPanel;

    private JTabbedPane tabbedPane;

    public MainDashboardFrame(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
    }

    private void initUI() {
        setTitle("SICA - Plataforma de Control de Accesos (Zona Acme)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(SicaTheme.BG_MAIN);
        setContentPane(mainPanel);

        // ENCABEZADO PRINCIPAL (NAVY DE SEGURIDAD)
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(SicaTheme.HEADER_BG);

        // Banner de Alerta de Emergencia (Modo Lockdown)
        JPanel lockdownBanner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        lockdownBanner.setBackground(SicaTheme.STATUS_DENIED_TEXT);
        JLabel lblLockdownAlert = new JLabel("! ATENCIÓN: SISTEMA EN MODO DE EMERGENCIA - TODOS LOS ACCESOS BLOQUEADOS POR DIRECCIÓN DE SEGURIDAD");
        lblLockdownAlert.setFont(SicaTheme.FONT_BOLD);
        lblLockdownAlert.setForeground(Color.WHITE);
        lockdownBanner.add(lblLockdownAlert);
        lockdownBanner.setVisible(false);
        headerContainer.add(lockdownBanner, BorderLayout.NORTH);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SicaTheme.HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Isotipo e Identificación Institucional
        JLabel titleLabel = new JLabel("SICA ZONA ACME");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);

        SessionContext session = SessionContext.getInstance();
        JLabel userLabel = new JLabel("OPERADOR: " + session.getNombreCompleto() + " (" + session.getRoleName() + ")");
        userLabel.setFont(SicaTheme.FONT_BOLD);
        userLabel.setForeground(SicaTheme.ACCENT_CYAN_LIGHT);

        if (session.isAdmin()) {
            JButton btnLockdown = new JButton("EMERGENCIA");
            SicaTheme.styleButton(btnLockdown, SicaTheme.STATUS_DENIED_TEXT, Color.WHITE);
            btnLockdown.addActionListener(e -> {
                boolean active = com.acme.sica.infrastructure.adapter.in.gui.components.LockdownManager.getInstance().isLockdownActive();
                if (!active) {
                    boolean conf = com.acme.sica.infrastructure.adapter.in.gui.components.CriticalConfirmationDialog.showConfirm(this,
                            "MODO DE EMERGENCIA",
                            "¿Estás seguro de activar el BLOQUEO TOTAL DE EMERGENCIA?\nSe inhabilitarán todos los accesos en portería.",
                            "ACTIVAR BLOQUEO");
                    if (conf) {
                        com.acme.sica.infrastructure.adapter.in.gui.components.LockdownManager.getInstance().setLockdownActive(true);
                        lockdownBanner.setVisible(true);
                        btnLockdown.setText("DESACTIVAR EMERGENCIA");
                        btnLockdown.setBackground(SicaTheme.STATUS_GRANTED_TEXT);
                    }
                } else {
                    com.acme.sica.infrastructure.adapter.in.gui.components.LockdownManager.getInstance().setLockdownActive(false);
                    lockdownBanner.setVisible(false);
                    btnLockdown.setText("EMERGENCIA");
                    btnLockdown.setBackground(SicaTheme.STATUS_DENIED_TEXT);
                }
            });
            userPanel.add(btnLockdown);
        }

        JButton btnLogout = new JButton("Cerrar Sesión");
        SicaTheme.styleButton(btnLogout, new Color(40, 95, 135), Color.WHITE);
        btnLogout.addActionListener(e -> executeLogout());

        userPanel.add(userLabel);
        userPanel.add(btnLogout);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        headerContainer.add(headerPanel, BorderLayout.CENTER);

        mainPanel.add(headerContainer, BorderLayout.NORTH);

        // PANEL CENTRAL DE NAVEGACIÓN Y MÓDULOS
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(SicaTheme.BG_MAIN);

        // Inicializar Vistas
        guardiaPanel = new GuardiaPanel(apiClient);
        funcionarioPanel = new FuncionarioPanel(apiClient);
        incidentesPanel = new IncidentesPanel(apiClient);
        reportesPanel = new ReportesPanel(apiClient);
        auditoriaPanel = new AuditoriaPanel(apiClient);

        // Pestañas por Grupos Operativos sin Emojis
        if (session.isAdmin() || session.isGuardia() || session.hasPermission("consultar_visitas")) {
            tabbedPane.addTab("[MONITOREO] Accesos en Vivo", guardiaPanel);
        }

        if (session.isAdmin() || session.isFuncionario() || session.hasPermission("preregistrar_visita")) {
            tabbedPane.addTab("[GESTIÓN] Pre-Registros", funcionarioPanel);
        }

        if (session.isAdmin() || session.isGuardia() || session.hasPermission("registrar_incidente")) {
            tabbedPane.addTab("[GESTIÓN] Incidentes", incidentesPanel);
        }

        if (session.isAdmin() || session.hasPermission("generar_reporte")) {
            tabbedPane.addTab("[ANÁLISIS] Reportes", reportesPanel);
        }

        if (session.isAdmin() || session.hasPermission("consultar_auditoria")) {
            tabbedPane.addTab("[SISTEMA] Auditoría & Usuarios", auditoriaPanel);
        }

        if (tabbedPane.getTabCount() == 0) {
            tabbedPane.addTab("[MONITOREO]", guardiaPanel);
            tabbedPane.addTab("[GESTIÓN]", funcionarioPanel);
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void executeLogout() {
        int conf = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión actual?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            apiClient.logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame(apiClient);
                login.setVisible(true);
            });
        }
    }
}
