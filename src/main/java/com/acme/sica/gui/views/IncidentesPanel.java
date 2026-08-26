package com.acme.sica.gui.views;

import com.acme.sica.domain.enums.NivelGravedad;
import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.gui.client.SicaApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class IncidentesPanel extends JPanel {

    private final SicaApiClient apiClient;

    private JComboBox<String> comboPersonas;
    private JTextField txtTitulo;
    private JTextArea txtDescripcion;
    private JComboBox<NivelGravedad> comboGravedad;
    private JButton btnRegistrar;

    private JTable tblIncidentes;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;

    private List<Persona> personasCache;

    public IncidentesPanel(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
        loadAll();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // BANNER GUÍA
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(153, 27, 27)); // Deep Red
        bannerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lblHelp = new JLabel("🚨 MÓDULO DE SEGURIDAD: Al registrar un incidente con gravedad CRÍTICO o ALTO, la persona cambia automáticamente a RESTRINGIDO y su ingreso queda bloqueado en portería con Alerta Roja.");
        lblHelp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHelp.setForeground(Color.WHITE);
        bannerPanel.add(lblHelp, BorderLayout.CENTER);
        add(bannerPanel, BorderLayout.NORTH);

        // --- PANEL SUPERIOR: Formulario de Incidente ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("🚨 Registrar Incidente de Seguridad (Restricción de Acceso)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        comboPersonas = new JComboBox<>();
        comboPersonas.setPreferredSize(new Dimension(280, 26));

        txtTitulo = new JTextField("Intento de ingreso no autorizado a zona de servidores", 25);
        txtTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtDescripcion = new JTextArea("Sorprendido intentando abrir puertas de área restringida sin acreditación.", 2, 25);
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);

        comboGravedad = new JComboBox<>(NivelGravedad.values());
        comboGravedad.setSelectedItem(NivelGravedad.CRITICO);
        comboGravedad.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnRegistrar = new JButton("🚨 Registrar Incidente & Bloquear Acceso");
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegistrar.setBackground(new Color(220, 38, 38));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.addActionListener(e -> executeRegistrarIncidente());

        JButton btnRehabilitar = new JButton("✅ Rehabilitar Acceso (Quitar Restricción)");
        btnRehabilitar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRehabilitar.setBackground(new Color(16, 185, 129));
        btnRehabilitar.setForeground(Color.WHITE);
        btnRehabilitar.addActionListener(e -> executeRehabilitarAcceso());

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 4));
        btnPanel.add(btnRegistrar);
        btnPanel.add(btnRehabilitar);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Persona Involucrada:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(comboPersonas, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Nivel de Gravedad:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(comboGravedad, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Título Incidente:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(txtTitulo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(scrollDesc, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(8, 8, 4, 8);
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // --- PANEL CENTRAL: Historial de Incidentes ---
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(new TitledBorder("📋 Historial de Incidentes de Seguridad Registrados"));

        String[] columns = {"ID", "Persona Afectada", "Documento", "Título Incidente", "Gravedad", "Reportado Por", "Fecha / Hora"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblIncidentes = new JTable(tableModel);
        tblIncidentes.setRowHeight(26);
        tblIncidentes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollTable = new JScrollPane(tblIncidentes);

        centerPanel.add(scrollTable, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefresh = new JButton("🔄 Actualizar Incidentes");
        btnRefresh.addActionListener(e -> loadIncidentes());
        actionPanel.add(btnRefresh);

        centerPanel.add(actionPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadAll() {
        loadPersonas();
        loadIncidentes();
    }

    private void loadPersonas() {
        SwingWorker<List<Persona>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Persona> doInBackground() throws Exception {
                return apiClient.listarPersonas();
            }

            @Override
            protected void done() {
                try {
                    personasCache = get();
                    comboPersonas.removeAllItems();
                    for (Persona p : personasCache) {
                        comboPersonas.addItem(p.getId() + " - " + p.getNombre() + " " + p.getApellido() + " (" + p.getDocIdentidad() + ")");
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    public void loadIncidentes() {
        SwingWorker<List<Incidente>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Incidente> doInBackground() throws Exception {
                return apiClient.listarIncidentes();
            }

            @Override
            protected void done() {
                try {
                    List<Incidente> list = get();
                    tableModel.setRowCount(0);
                    for (Incidente i : list) {
                        tableModel.addRow(new Object[]{
                                i.getId(),
                                i.getPersonaNombreCompleto(),
                                i.getPersonaDocIdentidad(),
                                i.getTitulo(),
                                i.getNivelGravedad(),
                                i.getReportadoPorUsername(),
                                i.getFechaHora()
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void executeRegistrarIncidente() {
        if (comboPersonas.getSelectedItem() == null) return;
        try {
            String sel = (String) comboPersonas.getSelectedItem();
            Long personaId = Long.parseLong(sel.split(" - ")[0]);
            String titulo = txtTitulo.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            NivelGravedad gravedad = (NivelGravedad) comboGravedad.getSelectedItem();

            if (titulo.isEmpty() || descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete el título y la descripción", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SwingWorker<Incidente, Void> worker = new SwingWorker<>() {
                @Override
                protected Incidente doInBackground() throws Exception {
                    return apiClient.registrarIncidente(personaId, titulo, descripcion, gravedad);
                }

                @Override
                protected void done() {
                    try {
                        Incidente inc = get();
                        JOptionPane.showMessageDialog(IncidentesPanel.this,
                                "🚨 Incidente #" + inc.getId() + " REGISTRADO.\n" +
                                        "El estado de acceso de la persona cambió automáticamente a RESTRINGIDO.\n" +
                                        "El guardia verá Alerta Roja en portería.",
                                "Acceso Bloqueado", JOptionPane.WARNING_MESSAGE);
                        loadAll();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(IncidentesPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Selección inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeRehabilitarAcceso() {
        if (comboPersonas.getSelectedItem() == null) return;
        try {
            String sel = (String) comboPersonas.getSelectedItem();
            Long personaId = Long.parseLong(sel.split(" - ")[0]);

            SwingWorker<Persona, Void> worker = new SwingWorker<>() {
                @Override
                protected Persona doInBackground() throws Exception {
                    return apiClient.rehabilitarPersona(personaId);
                }

                @Override
                protected void done() {
                    try {
                        Persona p = get();
                        JOptionPane.showMessageDialog(IncidentesPanel.this,
                                "✅ Acceso REHABILITADO para " + p.getNombre() + " " + p.getApellido() + " (Doc: " + p.getDocIdentidad() + ").\n" +
                                        "La persona vuelve a estar HABILITADA (Verde) para ingresar al complejo.",
                                "Restricción Levantada", JOptionPane.INFORMATION_MESSAGE);
                        loadAll();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(IncidentesPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Selección inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
