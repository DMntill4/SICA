package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AuditoriaPanel extends JPanel {

    private final SicaApiClient apiClient;

    private JLabel lblTotalDentro;

    private JTable tblUsuarios;
    private DefaultTableModel tableModelUsuarios;

    private JTable tblAuditoria;
    private DefaultTableModel tableModelAuditoria;

    private JButton btnRefresh;

    public AuditoriaPanel(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // BANNER GUÍA
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        topPanel.setBorder(new TitledBorder("📊 Módulo de Auditoría, Usuarios y Métricas del Sistema"));

        lblTotalDentro = new JLabel(" 🏢 Ocupación Actual: Consultando... ", SwingConstants.CENTER);
        lblTotalDentro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalDentro.setOpaque(true);
        lblTotalDentro.setBackground(new Color(14, 165, 233));
        lblTotalDentro.setForeground(Color.WHITE);
        lblTotalDentro.setBorder(new EmptyBorder(6, 12, 6, 12));

        topPanel.add(lblTotalDentro);
        add(topPanel, BorderLayout.NORTH);

        // --- SPLIT CONTAINER: Usuarios Sistema (Izquierda) | Bitácora Audit (Derecha) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.35);

        // IZQUIERDA: Usuarios del Sistema
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("👤 Usuarios del Sistema (Cuentas Activas)"));

        String[] colsUsers = {"ID", "Username", "Nombre Completo", "Rol", "Estado"};
        tableModelUsuarios = new DefaultTableModel(colsUsers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblUsuarios = new JTable(tableModelUsuarios);
        tblUsuarios.setRowHeight(24);
        tblUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollUsers = new JScrollPane(tblUsuarios);
        leftPanel.add(scrollUsers, BorderLayout.CENTER);

        JPanel userActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        JButton btnCrearUsuario = new JButton("👤 Crear Usuario");
        btnCrearUsuario.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCrearUsuario.setBackground(new Color(16, 185, 129));
        btnCrearUsuario.setForeground(Color.WHITE);
        btnCrearUsuario.addActionListener(e -> openCrearUsuarioDialog());

        JButton btnToggleBloqueo = new JButton("🔒 Bloquear/Desbloquear");
        btnToggleBloqueo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnToggleBloqueo.setBackground(new Color(239, 68, 68));
        btnToggleBloqueo.setForeground(Color.WHITE);
        btnToggleBloqueo.addActionListener(e -> executeToggleBloqueoUsuario());

        JButton btnEliminarUsuario = new JButton("🗑️ Eliminar");
        btnEliminarUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnEliminarUsuario.addActionListener(e -> executeEliminarUsuario());

        userActionPanel.add(btnCrearUsuario);
        userActionPanel.add(btnToggleBloqueo);
        userActionPanel.add(btnEliminarUsuario);
        leftPanel.add(userActionPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);


        // DERECHA: Bitácora de Auditoría
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(new TitledBorder("📜 Bitácora Inmutable de Auditoría (Trazabilidad SICA)"));

        String[] colsAudit = {"ID", "Usuario", "Acción Realizada", "Detalle Técnico", "IP Origen", "Fecha / Hora"};
        tableModelAuditoria = new DefaultTableModel(colsAudit, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblAuditoria = new JTable(tableModelAuditoria);
        tblAuditoria.setRowHeight(24);
        tblAuditoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollAudit = new JScrollPane(tblAuditoria);

        rightPanel.add(scrollAudit, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefresh = new JButton("🔄 Actualizar Auditoría");
        btnRefresh.addActionListener(e -> loadData());
        actionPanel.add(btnRefresh);

        rightPanel.add(actionPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    public void loadData() {
        // Cargar Ocupación
        SwingWorker<Map<String, Object>, Void> workerOccupancy = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return apiClient.getPersonasDentro();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> map = get();
                    Object total = map.get("total_dentro");
                    lblTotalDentro.setText(" 🏢 Ocupación Actual del Complejo: " + (total != null ? total : 0) + " Personas Dentro ");
                } catch (Exception e) {
                    lblTotalDentro.setText(" Ocupación Actual: 0 Personas ");
                }
            }
        };
        workerOccupancy.execute();

        // Cargar Usuarios Sistema
        SwingWorker<List<Map<String, Object>>, Void> workerUsers = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return apiClient.listarUsuarios();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> users = get();
                    tableModelUsuarios.setRowCount(0);
                    for (Map<String, Object> u : users) {
                        Boolean bloq = (Boolean) u.get("bloqueado");
                        tableModelUsuarios.addRow(new Object[]{
                                u.get("id"),
                                u.get("username"),
                                u.get("nombreCompleto"),
                                u.get("rolNombre"),
                                (bloq != null && bloq) ? "🔴 BLOQUEADO" : "🟢 ACTIVO"
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        workerUsers.execute();

        // Cargar Bitácora
        SwingWorker<Map<String, Object>, Void> workerAudit = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return apiClient.getAuditoria(100);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> res = get();
                    tableModelAuditoria.setRowCount(0);
                    Object listObj = res.get("registros");
                    if (listObj instanceof List) {
                        List<?> list = (List<?>) listObj;
                        ObjectMapper mapper = new ObjectMapper();
                        for (Object item : list) {
                            Map<String, Object> reg = mapper.convertValue(item, new TypeReference<Map<String, Object>>() {});
                            tableModelAuditoria.addRow(new Object[]{
                                    reg.get("id"),
                                    reg.get("username"),
                                    reg.get("accion"),
                                    reg.get("detalle"),
                                    reg.get("ipOrigen"),
                                    reg.get("fechaHora")
                            });
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        };
        workerAudit.execute();
    }

    private void openCrearUsuarioDialog() {
        JTextField txtUsername = new JTextField(15);
        JPasswordField txtPass = new JPasswordField(15);
        JTextField txtNombre = new JTextField(15);
        JTextField txtEmail = new JTextField(15);

        JComboBox<String> comboRol = new JComboBox<>(new String[]{
                "1 - ADMIN (Administrador Total)",
                "2 - GUARDIA (Control Portería)",
                "3 - FUNCIONARIO (Anfitrión)"
        });

        JPanel panel = new JPanel(new GridLayout(5, 2, 6, 6));
        panel.add(new JLabel("Username:")); panel.add(txtUsername);
        panel.add(new JLabel("Contraseña:")); panel.add(txtPass);
        panel.add(new JLabel("Nombre Completo:")); panel.add(txtNombre);
        panel.add(new JLabel("Email:")); panel.add(txtEmail);
        panel.add(new JLabel("Rol del Sistema:")); panel.add(comboRol);

        int option = JOptionPane.showConfirmDialog(this, panel, "👤 Crear Nuevo Usuario en SICA", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String nom = txtNombre.getText().trim();
            String email = txtEmail.getText().trim();
            String selRol = (String) comboRol.getSelectedItem();
            Long rolId = Long.parseLong(selRol.split(" - ")[0]);

            if (user.isEmpty() || pass.isEmpty() || nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, Contraseña y Nombre Completo son obligatorios", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
                @Override
                protected Map<String, Object> doInBackground() throws Exception {
                    return apiClient.crearUsuario(user, pass, nom, email, rolId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AuditoriaPanel.this, "✅ Usuario '" + user + "' creado exitosamente en SICA.", "Usuario Creado", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(AuditoriaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void executeEliminarUsuario() {
        int row = tblUsuarios.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la lista izquierda para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long userId = Long.valueOf(tableModelUsuarios.getValueAt(row, 0).toString());
        String username = (String) tableModelUsuarios.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar la cuenta de usuario '" + username + "' (ID #" + userId + ")?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.eliminarUsuario(userId);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AuditoriaPanel.this, "Usuario eliminado correctamente", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(AuditoriaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void executeToggleBloqueoUsuario() {
        int row = tblUsuarios.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la lista de la izquierda para cambiar su estado de bloqueo", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long userId = Long.valueOf(tableModelUsuarios.getValueAt(row, 0).toString());
        String username = (String) tableModelUsuarios.getValueAt(row, 1);
        String estadoActual = (String) tableModelUsuarios.getValueAt(row, 4);


        boolean esBloqueadoActualmente = estadoActual.contains("BLOQUEADO");
        String accionMsg = esBloqueadoActualmente ? "DESBLOQUEAR" : "BLOQUEAR";

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de " + accionMsg + " el acceso al usuario '" + username + "' (ID #" + userId + ")?",
                "Confirmar Cambio de Estado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.toggleBloqueoUsuario(userId);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AuditoriaPanel.this, "✅ Estado de bloqueo de '" + username + "' actualizado correctamente.", "Estado Actualizado", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(AuditoriaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
}

