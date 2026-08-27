package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class FuncionarioPanel extends JPanel {

    private final SicaApiClient apiClient;

    private JComboBox<String> comboPersonas;
    private JTextField txtMotivo;
    private JButton btnPreregistrar;
    private JButton btnCrearPersona;

    private JTable tblPendientes;
    private DefaultTableModel tableModel;
    private JButton btnAprobar;
    private JButton btnRechazar;
    private JButton btnRefresh;

    private List<Persona> personasCache;
    private List<Visita> allVisitas;

    public FuncionarioPanel(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
        loadAll();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // PANEL CONTENEDOR SUPERIOR: Banner + Formulario Pre-Registro
        JPanel topContainer = new JPanel(new BorderLayout(6, 6));

        // BANNER GUÍA
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(30, 41, 59));
        bannerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lblHelp = new JLabel("💡 MÓDULO DE FUNCIONARIO: Pre-registra invitados para que su ingreso sea directo o aprueba/rechaza solicitudes de personas no anunciadas en tiempo real.");
        lblHelp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHelp.setForeground(new Color(226, 232, 240));
        bannerPanel.add(lblHelp, BorderLayout.CENTER);
        topContainer.add(bannerPanel, BorderLayout.NORTH);

        // FORMULARIO PRE-REGISTRO ORGANIZADO EN 2 FILAS INDEPENDIENTES (CERO RECORTES)
        JPanel formPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        formPanel.setBorder(new TitledBorder("✨ Pre-Registrar Invitado Aprobado"));

        // Fila 1: Campos de Selección
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        comboPersonas = new JComboBox<>();
        comboPersonas.setPreferredSize(new Dimension(280, 26));

        txtMotivo = new JTextField("Reunión de Negocios y Consultoría", 22);
        txtMotivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        row1.add(new JLabel("Seleccionar Invitado:"));
        row1.add(comboPersonas);
        row1.add(new JLabel("Motivo:"));
        row1.add(txtMotivo);

        // Fila 2: Botones de Acción
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        btnPreregistrar = new JButton("✨ Pre-Registrar Visita");
        btnPreregistrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPreregistrar.setBackground(new Color(14, 165, 233));
        btnPreregistrar.setForeground(Color.WHITE);

        btnCrearPersona = new JButton("👤 Registrar Nueva Persona");
        btnCrearPersona.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCrearPersona.setBackground(new Color(139, 92, 246));
        btnCrearPersona.setForeground(Color.WHITE);

        btnPreregistrar.addActionListener(e -> executePreregistro());
        btnCrearPersona.addActionListener(e -> openCrearPersonaDialog());

        row2.add(btnPreregistrar);
        row2.add(btnCrearPersona);

        formPanel.add(row1);
        formPanel.add(row2);

        topContainer.add(formPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- PANEL CENTRAL: Solicitudes Pendientes ---
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(new TitledBorder("📋 Solicitudes Pendientes de Aprobación (En Tiempo Real)"));

        String[] columns = {"ID Visita", "Persona / Visitante", "Documento", "Tipo Visita", "Estado", "Motivo / Detalle"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblPendientes = new JTable(tableModel);
        tblPendientes.setRowHeight(26);
        tblPendientes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblPendientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tblPendientes);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // BOTONERA APROBACIONES
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        btnRefresh = new JButton("🔄 Actualizar Tabla");

        btnAprobar = new JButton("✅ APROBAR VISITA");
        btnAprobar.setBackground(new Color(34, 197, 94));
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAprobar.setPreferredSize(new Dimension(190, 30));

        btnRechazar = new JButton("❌ RECHAZAR VISITA");
        btnRechazar.setBackground(new Color(239, 68, 68));
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRechazar.setPreferredSize(new Dimension(190, 30));

        btnRefresh.addActionListener(e -> loadAll());
        btnAprobar.addActionListener(e -> executeAprobar());
        btnRechazar.addActionListener(e -> executeRechazar());

        actionPanel.add(btnRefresh);
        actionPanel.add(btnAprobar);
        actionPanel.add(btnRechazar);

        centerPanel.add(actionPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadAll() {
        loadPersonas();
        loadPendientes();
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

    public void loadPendientes() {
        SwingWorker<List<Visita>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Visita> doInBackground() throws Exception {
                return apiClient.listarVisitas();
            }

            @Override
            protected void done() {
                try {
                    allVisitas = get();
                    tableModel.setRowCount(0);
                    allVisitas.stream()
                            .filter(v -> v.getEstadoVisita().name().contains("PENDIENTE"))
                            .forEach(v -> tableModel.addRow(new Object[]{
                                    v.getId(),
                                    v.getPersonaNombreCompleto(),
                                    v.getPersonaDocIdentidad(),
                                    v.getTipoVisita(),
                                    v.getEstadoVisita(),
                                    v.getMotivo()
                            }));
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void executePreregistro() {
        if (comboPersonas.getSelectedItem() == null) return;
        try {
            String sel = (String) comboPersonas.getSelectedItem();
            Long pId = Long.parseLong(sel.split(" - ")[0]);
            String motivo = txtMotivo.getText().trim();
            if (motivo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el motivo de la visita", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SwingWorker<Visita, Void> worker = new SwingWorker<>() {
                @Override
                protected Visita doInBackground() throws Exception {
                    return apiClient.preregistrarVisita(pId, motivo, LocalDateTime.now().plusHours(2));
                }

                @Override
                protected void done() {
                    try {
                        Visita v = get();
                        JOptionPane.showMessageDialog(FuncionarioPanel.this,
                                "✨ Visita pre-registrada exitosamente (ID #" + v.getId() + ").\nEl guardia ya puede realizar el Check-In en portería.",
                                "Pre-Registro Éxitoso", JOptionPane.INFORMATION_MESSAGE);
                        loadPendientes();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(FuncionarioPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Selección inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeAprobar() {
        int row = tblPendientes.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud pendiente de la tabla para aprobar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long visitaId = (Long) tableModel.getValueAt(row, 0);

        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() throws Exception {
                return apiClient.aprobarVisita(visitaId);
            }

            @Override
            protected void done() {
                try {
                    Visita v = get();
                    JOptionPane.showMessageDialog(FuncionarioPanel.this, "✅ Visita ID #" + v.getId() + " APROBADA.\nEl guardia ya tiene la autorización de ingreso.", "Aprobada", JOptionPane.INFORMATION_MESSAGE);
                    loadPendientes();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(FuncionarioPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void executeRechazar() {
        int row = tblPendientes.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud pendiente de la tabla para rechazar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long visitaId = (Long) tableModel.getValueAt(row, 0);

        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() throws Exception {
                return apiClient.rechazarVisita(visitaId);
            }

            @Override
            protected void done() {
                try {
                    Visita v = get();
                    JOptionPane.showMessageDialog(FuncionarioPanel.this, "❌ Visita ID #" + v.getId() + " RECHAZADA.\nEl ingreso ha sido denegado al visitante.", "Rechazada", JOptionPane.INFORMATION_MESSAGE);
                    loadPendientes();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(FuncionarioPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openCrearPersonaDialog() {
        JTextField txtDoc = new JTextField(12);
        JComboBox<String> comboTipoDoc = new JComboBox<>(new String[]{"CC", "CE", "PASAPORTE", "TI"});
        JTextField txtNombre = new JTextField(15);
        JTextField txtApellido = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JTextField txtTel = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(6, 2, 6, 6));
        panel.add(new JLabel("Documento:")); panel.add(txtDoc);
        panel.add(new JLabel("Tipo Documento:")); panel.add(comboTipoDoc);
        panel.add(new JLabel("Nombre:")); panel.add(txtNombre);
        panel.add(new JLabel("Apellido:")); panel.add(txtApellido);
        panel.add(new JLabel("Email:")); panel.add(txtEmail);
        panel.add(new JLabel("Teléfono:")); panel.add(txtTel);

        int option = JOptionPane.showConfirmDialog(this, panel, "👤 Registrar Nuevo Invitado / Persona", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String doc = txtDoc.getText().trim();
            String tipo = (String) comboTipoDoc.getSelectedItem();
            String nom = txtNombre.getText().trim();
            String ape = txtApellido.getText().trim();
            String email = txtEmail.getText().trim();
            String tel = txtTel.getText().trim();

            if (doc.isEmpty() || nom.isEmpty() || ape.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Documento, Nombre y Apellido son obligatorios", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SwingWorker<Persona, Void> worker = new SwingWorker<>() {
                @Override
                protected Persona doInBackground() throws Exception {
                    return apiClient.crearPersona(doc, tipo, nom, ape, email, tel);
                }

                @Override
                protected void done() {
                    try {
                        Persona p = get();
                        JOptionPane.showMessageDialog(FuncionarioPanel.this,
                                "✅ Persona registrada exitosamente:\n" + p.getNombre() + " " + p.getApellido() + " (Doc: " + p.getDocIdentidad() + ")",
                                "Registro Éxitoso", JOptionPane.INFORMATION_MESSAGE);
                        loadPersonas();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(FuncionarioPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
}
