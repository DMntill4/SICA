package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.infrastructure.adapter.in.gui.client.SessionContext;
import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainDashboardFrame extends JFrame {

    private final SicaApiClient apiClient;

    private GuardiaPanel guardiaPanel;
    private FuncionarioPanel funcionarioPanel;
    private IncidentesPanel incidentesPanel;
    private AuditoriaPanel auditoriaPanel;
    private ReportesPanel reportesPanel;

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
        JPanel headerContainer = new JPanel(new BorderLayout());

        // Banner de Alerta de Emergencia (Oculto por defecto)
        JPanel lockdownBanner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        lockdownBanner.setBackground(new Color(225, 29, 72));
        JLabel lblLockdownAlert = new JLabel("[!] ATENCIÓN: SISTEMA EN MODO DE EMERGENCIA - TODOS LOS ACCESOS BLOQUEADOS POR DIRECCIÓN DE SEGURIDAD");
        lblLockdownAlert.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLockdownAlert.setForeground(Color.WHITE);
        lockdownBanner.add(lblLockdownAlert);
        lockdownBanner.setVisible(false);
        headerContainer.add(lockdownBanner, BorderLayout.NORTH);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Dark slate
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("SICA - Complejo Empresarial Zona Acme");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(56, 189, 248));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        JLabel userLabel = new JLabel("[+] " + session.getNombreCompleto() + " (" + session.getRoleName() + ")");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(Color.WHITE);

        // Botón exclusivo de Admin para Modo de Emergencia / Lockdown
        if (session.isAdmin()) {
            JButton btnLockdown = new JButton("[!] MODO EMERGENCIA");
            btnLockdown.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnLockdown.setBackground(new Color(225, 29, 72));
            btnLockdown.setForeground(Color.WHITE);
            btnLockdown.addActionListener(e -> {
                boolean active = com.acme.sica.infrastructure.adapter.in.gui.components.LockdownManager.getInstance().isLockdownActive();
                if (!active) {
                    int conf = JOptionPane.showConfirmDialog(this,
                            "[!] ¿Estás seguro de activar el BLOQUEO TOTAL DE EMERGENCIA?\nSe inhabilitarán todos los accesos en portería.",
                            "Confirmar Modo de Emergencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (conf == JOptionPane.YES_OPTION) {
                        com.acme.sica.infrastructure.adapter.in.gui.components.LockdownManager.getInstance().setLockdownActive(true);
                        lockdownBanner.setVisible(true);
                        btnLockdown.setText("[🔑] DESACTIVAR EMERGENCIA");
                        btnLockdown.setBackground(new Color(16, 185, 129));
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(this,
                                "[!] MODO DE EMERGENCIA ACTIVADO - Accesos Bloqueados",
                                com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.ERROR);
                    }
                } else {
                    com.acme.sica.infrastructure.adapter.in.gui.components.LockdownManager.getInstance().setLockdownActive(false);
                    lockdownBanner.setVisible(false);
                    btnLockdown.setText("[!] MODO EMERGENCIA");
                    btnLockdown.setBackground(new Color(225, 29, 72));
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(this,
                            "[+] Operación Normal Restaurada",
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);
                }
            });
            userPanel.add(btnLockdown);
        }

        JButton btnLogout = new JButton("[x] Cerrar Sesión");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> executeLogout());

        userPanel.add(userLabel);
        userPanel.add(btnLogout);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        headerContainer.add(headerPanel, BorderLayout.CENTER);

        mainPanel.add(headerContainer, BorderLayout.NORTH);

        // --- CONTENEDOR CON PESTAÑAS (JTabbedPane) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Inicializar paneles
        guardiaPanel = new GuardiaPanel(apiClient);
        funcionarioPanel = new FuncionarioPanel(apiClient);
        incidentesPanel = new IncidentesPanel(apiClient);
        auditoriaPanel = new AuditoriaPanel(apiClient);
        reportesPanel = new ReportesPanel(apiClient);

        // Filtrar según el rol y/o permisos asignados al usuario (Soporte RBAC Dinámico)
        boolean showGuardiaTab = session.isAdmin() || session.isGuardia() || session.hasPermission("checkin_visita") || session.hasPermission("checkout_visita") || session.hasPermission("crear_persona");
        boolean showFuncionarioTab = session.isAdmin() || session.isFuncionario() || session.hasPermission("preregistrar_visita") || session.hasPermission("aprobar_visita");
        boolean showIncidentesTab = session.isAdmin() || session.isGuardia() || session.hasPermission("registrar_incidente");
        boolean showReportesTab = session.isAdmin() || session.hasPermission("generar_reporte");
        boolean showAuditoriaTab = session.isAdmin() || session.hasPermission("consultar_auditoria") || session.hasPermission("gestionar_roles") || session.hasPermission("crear_usuario");

        if (showGuardiaTab) {
            tabbedPane.addTab("[◆] Control de Accesos (Portería)", guardiaPanel);
        }

        if (showFuncionarioTab) {
            tabbedPane.addTab("[📋] Pre-Registro y Aprobaciones", funcionarioPanel);
        }

        if (showIncidentesTab) {
            tabbedPane.addTab("[!] Gestión de Incidentes", incidentesPanel);
        }

        if (showReportesTab) {
            tabbedPane.addTab("[📊] Reportes & Estadísticas", reportesPanel);
        }

        if (showAuditoriaTab) {
            tabbedPane.addTab("[📜] Bitácora & Auditoría", auditoriaPanel);
        }

        if (tabbedPane.getTabCount() == 0) {
            tabbedPane.addTab("[◆] Control de Accesos", guardiaPanel);
            tabbedPane.addTab("[📋] Pre-Registro", funcionarioPanel);
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
