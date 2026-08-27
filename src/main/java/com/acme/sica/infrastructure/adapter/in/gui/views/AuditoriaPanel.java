package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.domain.model.Permiso;
import com.acme.sica.domain.model.Rol;
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

        JPanel userActionPanel = new JPanel(new GridLayout(2, 3, 4, 4));
        JButton btnCrearUsuario = new JButton("👤 Crear Usuario");
        btnCrearUsuario.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCrearUsuario.setBackground(new Color(16, 185, 129));
        btnCrearUsuario.setForeground(Color.WHITE);
        btnCrearUsuario.addActionListener(e -> openCrearUsuarioDialog());

        JButton btnEditarUsuario = new JButton("✏️ Editar Usuario");
        btnEditarUsuario.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnEditarUsuario.setBackground(new Color(14, 165, 233));
        btnEditarUsuario.setForeground(Color.WHITE);
        btnEditarUsuario.addActionListener(e -> executeEditarUsuario());

        JButton btnToggleBloqueo = new JButton("🔒 Bloquear/Desbloq");
        btnToggleBloqueo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnToggleBloqueo.setBackground(new Color(239, 68, 68));
        btnToggleBloqueo.setForeground(Color.WHITE);
        btnToggleBloqueo.addActionListener(e -> executeToggleBloqueoUsuario());

        JButton btnGestionarRoles = new JButton("🛡️ Roles & Permisos");
        btnGestionarRoles.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGestionarRoles.setBackground(new Color(99, 102, 241));
        btnGestionarRoles.setForeground(Color.WHITE);
        btnGestionarRoles.addActionListener(e -> openRolesDialog());

        JButton btnEmpresas = new JButton("🏢 Empresas");
        btnEmpresas.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnEmpresas.setBackground(new Color(245, 158, 11));
        btnEmpresas.setForeground(Color.WHITE);
        btnEmpresas.addActionListener(e -> openEmpresasDialog());

        JButton btnEliminarUsuario = new JButton("🗑️ Eliminar");
        btnEliminarUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnEliminarUsuario.addActionListener(e -> executeEliminarUsuario());

        userActionPanel.add(btnCrearUsuario);
        userActionPanel.add(btnEditarUsuario);
        userActionPanel.add(btnToggleBloqueo);
        userActionPanel.add(btnGestionarRoles);
        userActionPanel.add(btnEmpresas);
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

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnRefresh = new JButton("🔄 Actualizar Auditoría");
        btnRefresh.addActionListener(e -> loadData());

        JButton btnExportarCSV = new JButton("📥 Exportar Reporte CSV");
        btnExportarCSV.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnExportarCSV.setBackground(new Color(16, 185, 129));
        btnExportarCSV.setForeground(Color.WHITE);
        btnExportarCSV.addActionListener(e -> executeExportarCSV());

        actionPanel.add(btnExportarCSV);
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
                    Object listObj = res.get("auditoria") != null ? res.get("auditoria") : res.get("registros");
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

        JComboBox<String> comboRol = new JComboBox<>();
        try {
            List<Rol> roles = apiClient.listarRoles();
            for (Rol r : roles) {
                comboRol.addItem(r.getId() + " - " + r.getNombre() + " (" + (r.getDescripcion() != null ? r.getDescripcion() : "") + ")");
            }
        } catch (Exception e) {
            comboRol.addItem("1 - ADMIN");
            comboRol.addItem("2 - GUARDIA");
            comboRol.addItem("3 - FUNCIONARIO");
        }

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

    private void openRolesDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "🛡️ Gestión de Roles y Permisos RBAC", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(750, 500);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"ID", "Nombre de Rol", "Descripción", "Permisos Asignados"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);

        Runnable reloadRoles = () -> {
            SwingWorker<List<Rol>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Rol> doInBackground() throws Exception {
                    return apiClient.listarRoles();
                }

                @Override
                protected void done() {
                    try {
                        List<Rol> list = get();
                        model.setRowCount(0);
                        for (Rol r : list) {
                            model.addRow(new Object[]{
                                    r.getId(),
                                    r.getNombre(),
                                    r.getDescripcion(),
                                    String.join(", ", r.getPermisoCodigos())
                            });
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(dialog, "Error al cargar roles: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        };

        reloadRoles.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));

        JButton btnCrear = new JButton("➕ Crear Nuevo Rol");
        btnCrear.setBackground(new Color(16, 185, 129));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.addActionListener(e -> openCrearRolDialog(dialog, reloadRoles));

        JButton btnModificar = new JButton("✏️ Editar Permisos");
        btnModificar.setBackground(new Color(99, 102, 241));
        btnModificar.setForeground(Color.WHITE);
        btnModificar.addActionListener(e -> openEditarPermisosDialog(dialog, table, model, reloadRoles));

        JButton btnEliminar = new JButton("🗑️ Eliminar Rol");
        btnEliminar.setBackground(new Color(239, 68, 68));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.addActionListener(e -> executeEliminarRol(dialog, table, model, reloadRoles));

        btnPanel.add(btnCrear);
        btnPanel.add(btnModificar);
        btnPanel.add(btnEliminar);

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openCrearRolDialog(JDialog parent, Runnable onSuccess) {
        JTextField txtNombre = new JTextField(15);
        JTextField txtDesc = new JTextField(20);

        JPanel permPanel = new JPanel(new GridLayout(0, 2, 4, 4));
        List<JCheckBox> checkBoxes = new java.util.ArrayList<>();

        try {
            List<Permiso> permisos = apiClient.listarPermisos();
            for (Permiso p : permisos) {
                JCheckBox chk = new JCheckBox(p.getNombre() + " (" + p.getDescripcion() + ")");
                chk.putClientProperty("permisoId", p.getId());
                checkBoxes.add(chk);
                permPanel.add(chk);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Error cargando lista de permisos: " + e.getMessage());
            return;
        }

        JPanel form = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new GridLayout(2, 2, 4, 4));
        top.add(new JLabel("Nombre del Rol (ej. Recepcionista):")); top.add(txtNombre);
        top.add(new JLabel("Descripción:")); top.add(txtDesc);

        form.add(top, BorderLayout.NORTH);
        form.add(new JScrollPane(permPanel), BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(parent, form, "➕ Crear Nuevo Rol en SICA", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nom = txtNombre.getText().trim();
            String desc = txtDesc.getText().trim();

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "El nombre del rol es obligatorio", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<Long> selIds = new java.util.ArrayList<>();
            for (JCheckBox chk : checkBoxes) {
                if (chk.isSelected()) {
                    selIds.add((Long) chk.getClientProperty("permisoId"));
                }
            }

            try {
                apiClient.crearRol(nom, desc, selIds);
                JOptionPane.showMessageDialog(parent, "✅ Rol '" + nom + "' creado exitosamente con " + selIds.size() + " permisos.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                onSuccess.run();
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEditarPermisosDialog(JDialog parent, JTable table, DefaultTableModel model, Runnable onSuccess) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(parent, "Selecciona un rol de la tabla para editar sus permisos", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long rolId = Long.valueOf(model.getValueAt(row, 0).toString());
        String rolNombre = (String) model.getValueAt(row, 1);

        JPanel permPanel = new JPanel(new GridLayout(0, 2, 4, 4));
        List<JCheckBox> checkBoxes = new java.util.ArrayList<>();

        try {
            List<Rol> roles = apiClient.listarRoles();
            Rol rolActual = roles.stream().filter(r -> r.getId().equals(rolId)).findFirst().orElse(null);
            List<Long> actualIds = rolActual != null ? rolActual.getPermisoIds() : List.of();

            List<Permiso> permisos = apiClient.listarPermisos();
            for (Permiso p : permisos) {
                JCheckBox chk = new JCheckBox(p.getNombre() + " (" + p.getDescripcion() + ")");
                chk.putClientProperty("permisoId", p.getId());
                if (actualIds.contains(p.getId())) {
                    chk.setSelected(true);
                }
                checkBoxes.add(chk);
                permPanel.add(chk);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Error cargando datos: " + e.getMessage());
            return;
        }

        int option = JOptionPane.showConfirmDialog(parent, new JScrollPane(permPanel), "✏️ Modificar Permisos de Rol: " + rolNombre, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            List<Long> selIds = new java.util.ArrayList<>();
            for (JCheckBox chk : checkBoxes) {
                if (chk.isSelected()) {
                    selIds.add((Long) chk.getClientProperty("permisoId"));
                }
            }

            try {
                apiClient.actualizarPermisosRol(rolId, selIds);
                JOptionPane.showMessageDialog(parent, "✅ Permisos actualizados correctamente para el rol '" + rolNombre + "'.", "Permisos Actualizados", JOptionPane.INFORMATION_MESSAGE);
                onSuccess.run();
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeEliminarRol(JDialog parent, JTable table, DefaultTableModel model, Runnable onSuccess) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(parent, "Selecciona un rol de la tabla para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long rolId = Long.valueOf(model.getValueAt(row, 0).toString());
        String rolNombre = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(parent,
                "¿Estás seguro de eliminar el rol '" + rolNombre + "' (ID #" + rolId + ")?",
                "Confirmar Eliminación de Rol", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                apiClient.eliminarRol(rolId);
                JOptionPane.showMessageDialog(parent, "✅ Rol '" + rolNombre + "' eliminado correctamente.", "Rol Eliminado", JOptionPane.INFORMATION_MESSAGE);
                onSuccess.run();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), "Error al Eliminar Rol", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeEditarUsuario() {


        int row = tblUsuarios.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la lista para editar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long userId = Long.valueOf(tableModelUsuarios.getValueAt(row, 0).toString());
        String username = (String) tableModelUsuarios.getValueAt(row, 1);
        String nombreActual = (String) tableModelUsuarios.getValueAt(row, 2);
        String emailActual = (String) tableModelUsuarios.getValueAt(row, 3);

        JTextField txtNombre = new JTextField(nombreActual, 15);
        JTextField txtEmail = new JTextField(emailActual, 15);

        JComboBox<String> comboRol = new JComboBox<>();
        try {
            List<Rol> roles = apiClient.listarRoles();
            for (Rol r : roles) {
                comboRol.addItem(r.getId() + " - " + r.getNombre() + " (" + (r.getDescripcion() != null ? r.getDescripcion() : "") + ")");
            }
        } catch (Exception e) {
            comboRol.addItem("1 - ADMIN");
            comboRol.addItem("2 - GUARDIA");
            comboRol.addItem("3 - FUNCIONARIO");
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 6, 6));
        panel.add(new JLabel("Username:")); panel.add(new JLabel("<b>" + username + "</b>"));
        panel.add(new JLabel("Nombre Completo:")); panel.add(txtNombre);
        panel.add(new JLabel("Email:")); panel.add(txtEmail);
        panel.add(new JLabel("Rol del Sistema:")); panel.add(comboRol);

        int option = JOptionPane.showConfirmDialog(this, panel, "✏️ Editar Usuario: " + username, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String nom = txtNombre.getText().trim();
            String email = txtEmail.getText().trim();
            String selRol = (String) comboRol.getSelectedItem();
            Long rolId = Long.parseLong(selRol.split(" - ")[0]);

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre completo no puede estar vacío", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                apiClient.actualizarUsuario(userId, nom, email, rolId);
                JOptionPane.showMessageDialog(this, "✅ Usuario '" + username + "' actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEmpresasDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "🏢 Gestión de Empresas (Zona Acme)", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(650, 420);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"ID", "NIT", "Nombre de Empresa", "Ubicación Oficina", "Estado"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);

        Runnable reloadEmpresas = () -> {
            SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Map<String, Object>> doInBackground() throws Exception {
                    return apiClient.listarEmpresas();
                }

                @Override
                protected void done() {
                    try {
                        List<Map<String, Object>> list = get();
                        model.setRowCount(0);
                        for (Map<String, Object> emp : list) {
                            model.addRow(new Object[]{
                                    emp.get("id"),
                                    emp.get("nit"),
                                    emp.get("nombre"),
                                    emp.get("ubicacionOficina"),
                                    Boolean.TRUE.equals(emp.get("activa")) ? "🟢 ACTIVA" : "🔴 INACTIVA"
                            });
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(dialog, "Error cargando empresas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        };

        reloadEmpresas.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton btnCrear = new JButton("➕ Crear Empresa");
        btnCrear.setBackground(new Color(16, 185, 129));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.addActionListener(e -> {
            JTextField txtNit = new JTextField(15);
            JTextField txtNombre = new JTextField(15);
            JTextField txtOficina = new JTextField(15);

            JPanel form = new JPanel(new GridLayout(3, 2, 4, 4));
            form.add(new JLabel("NIT Empresa:")); form.add(txtNit);
            form.add(new JLabel("Nombre Empresa:")); form.add(txtNombre);
            form.add(new JLabel("Ubicación Oficina:")); form.add(txtOficina);

            int opt = JOptionPane.showConfirmDialog(dialog, form, "➕ Registrar Nueva Empresa (USR-05)", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String nit = txtNit.getText().trim();
                String nom = txtNombre.getText().trim();
                String ofic = txtOficina.getText().trim();

                if (nit.isEmpty() || nom.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "NIT y Nombre son obligatorios", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    apiClient.crearEmpresa(nit, nom, ofic);
                    JOptionPane.showMessageDialog(dialog, "✅ Empresa '" + nom + "' registrada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    reloadEmpresas.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnEliminar = new JButton("🗑️ Eliminar Empresa");
        btnEliminar.setBackground(new Color(239, 68, 68));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Selecciona una empresa de la lista", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Long id = Long.valueOf(model.getValueAt(row, 0).toString());
            String nom = (String) model.getValueAt(row, 2);

            int conf = JOptionPane.showConfirmDialog(dialog, "¿Estás seguro de eliminar la empresa '" + nom + "'?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    apiClient.eliminarEmpresa(id);
                    JOptionPane.showMessageDialog(dialog, "Empresa eliminada correctamente");
                    reloadEmpresas.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            }
        });

        btnPanel.add(btnCrear);
        btnPanel.add(btnEliminar);

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void executeExportarCSV() {
        if (tableModelAuditoria.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros en la tabla para exportar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("📥 Guardar Reporte de Auditoría (CSV)");
        fileChooser.setSelectedFile(new java.io.File("reporte_auditoria_sica.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(fileToSave, java.nio.charset.StandardCharsets.UTF_8))) {
                pw.println("ID,Usuario,Accion,Detalle,IP_Origen,Fecha_Hora");
                for (int i = 0; i < tableModelAuditoria.getRowCount(); i++) {
                    String id = String.valueOf(tableModelAuditoria.getValueAt(i, 0));
                    String usr = String.valueOf(tableModelAuditoria.getValueAt(i, 1));
                    String acc = String.valueOf(tableModelAuditoria.getValueAt(i, 2));
                    String det = String.valueOf(tableModelAuditoria.getValueAt(i, 3)).replace(",", ";");
                    String ip = String.valueOf(tableModelAuditoria.getValueAt(i, 4));
                    String fecha = String.valueOf(tableModelAuditoria.getValueAt(i, 5));

                    pw.printf("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", id, usr, acc, det, ip, fecha);
                }
                JOptionPane.showMessageDialog(this, "✅ Reporte exportado exitosamente en:\n" + fileToSave.getAbsolutePath(), "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exportando CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}




