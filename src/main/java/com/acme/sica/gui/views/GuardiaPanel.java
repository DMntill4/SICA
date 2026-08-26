package com.acme.sica.gui.views;

import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.gui.client.SicaApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class GuardiaPanel extends JPanel {

    private final SicaApiClient apiClient;

    private JTextField txtSearchDoc;
    private JButton btnSearch;
    private JLabel lblPersonaNombre;
    private JLabel lblPersonaDoc;
    private JLabel lblPersonaEstado;

    private JTable tblPersonas;
    private DefaultTableModel tableModelPersonas;
    private List<Persona> listaPersonasCache;

    private JTable tblVisitas;
    private DefaultTableModel tableModelVisitas;
    private JButton btnCheckIn;
    private JButton btnCheckOut;
    private JButton btnRefresh;
    private JButton btnNoAnunciada;
    private JButton btnPaseTemporal;
    private JButton btnAutoSeed;

    private List<Visita> currentVisitas;

    public GuardiaPanel(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
        loadAllData();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // --- BANNER DE GUÍA ---
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(30, 41, 59));
        bannerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lblHelp = new JLabel("💡 MÓDULO DE GUARDIA: Selecciona una persona de la lista izquierda o ingresa su documento para consultar su estado (VERDE = Habilitado / ROJO = Restringido). Haz Check-In para autorizar el ingreso físico.");
        lblHelp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHelp.setForeground(new Color(226, 232, 240));
        bannerPanel.add(lblHelp, BorderLayout.CENTER);
        add(bannerPanel, BorderLayout.NORTH);

        // --- CONTAINER SPLIT: Izquierda (Personas BD) | Derecha (Tarjeta + Visitas) ---
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(360);
        mainSplit.setResizeWeight(0.35);

        // ==================== PANEL IZQUIERDO: Personas en BD ====================
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("👥 Personas Registradas en Base de Datos"));

        String[] colsPersonas = {"ID", "Documento", "Nombre Completo", "Estado"};
        tableModelPersonas = new DefaultTableModel(colsPersonas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPersonas = new JTable(tableModelPersonas);
        tblPersonas.setRowHeight(24);
        tblPersonas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblPersonas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPersonas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPersonas.getSelectedRow() != -1) {
                int row = tblPersonas.getSelectedRow();
                String doc = (String) tableModelPersonas.getValueAt(row, 1);
                txtSearchDoc.setText(doc);
                searchPersona();
            }
        });

        JScrollPane scrollPersonas = new JScrollPane(tblPersonas);
        leftPanel.add(scrollPersonas, BorderLayout.CENTER);

        JLabel lblClickHint = new JLabel(" 👆 Haz clic en una persona para cargar su estado ", SwingConstants.CENTER);
        lblClickHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblClickHint.setForeground(Color.GRAY);
        leftPanel.add(lblClickHint, BorderLayout.SOUTH);

        mainSplit.setLeftComponent(leftPanel);

        // ==================== PANEL DERECHO: Estado + Visitas ====================
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // Subpanel Superior Derecho: Buscador + Tarjeta Estado
        JPanel topRight = new JPanel(new BorderLayout(8, 8));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchBar.setBorder(new TitledBorder("🔍 Búsqueda Directa por Documento"));
        txtSearchDoc = new JTextField("1010101010", 12);
        txtSearchDoc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnSearch = new JButton("Consultar Estado");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchPersona());

        searchBar.add(new JLabel("Documento:"));
        searchBar.add(txtSearchDoc);
        searchBar.add(btnSearch);

        // Tarjeta Visual de Estado
        JPanel cardPersona = new JPanel(new GridLayout(3, 1, 4, 4));
        cardPersona.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Tarjeta de Estado SICA"),
                new EmptyBorder(4, 10, 4, 10)
        ));
        lblPersonaNombre = new JLabel("Persona: (Selecciona una persona de la lista)");
        lblPersonaNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lblPersonaDoc = new JLabel("Documento: -");
        lblPersonaDoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblPersonaEstado = new JLabel(" ESTADO SICA: SELECCIONE PERSONA ", SwingConstants.CENTER);
        lblPersonaEstado.setOpaque(true);
        lblPersonaEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPersonaEstado.setBackground(new Color(100, 116, 139));
        lblPersonaEstado.setForeground(Color.WHITE);

        cardPersona.add(lblPersonaNombre);
        cardPersona.add(lblPersonaDoc);
        cardPersona.add(lblPersonaEstado);

        topRight.add(searchBar, BorderLayout.WEST);
        topRight.add(cardPersona, BorderLayout.CENTER);

        rightPanel.add(topRight, BorderLayout.NORTH);

        // Subpanel Central Derecho: Tabla de Visitas y Control de Accesos
        JPanel visitsPanel = new JPanel(new BorderLayout(5, 5));
        visitsPanel.setBorder(new TitledBorder("🚪 Registro de Visitas y Control de Accesos Físicos"));

        String[] colsVisitas = {"ID", "Persona / Visitante", "Documento", "Tipo Visita", "Estado", "Motivo / Detalle"};
        tableModelVisitas = new DefaultTableModel(colsVisitas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblVisitas = new JTable(tableModelVisitas);
        tblVisitas.setRowHeight(26);
        tblVisitas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblVisitas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollVisitas = new JScrollPane(tblVisitas);

        visitsPanel.add(scrollVisitas, BorderLayout.CENTER);

        // Botonera de Acciones (Organizada en 3 Filas independientes para CERO recortes)
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 4, 4));

        // Fila 1: Novedades de Registro y Pruebas
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        btnAutoSeed = new JButton("⚡ Visita de Prueba Rápida");
        btnAutoSeed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAutoSeed.setBackground(new Color(139, 92, 246));
        btnAutoSeed.setForeground(Color.WHITE);

        btnNoAnunciada = new JButton("➕ Visitante No Anunciado");
        btnPaseTemporal = new JButton("🪪 Pase Temporal");
        btnRefresh = new JButton("🔄 Actualizar Tabla");

        row1.add(btnAutoSeed);
        row1.add(btnNoAnunciada);
        row1.add(btnPaseTemporal);
        row1.add(btnRefresh);

        // Fila 2: Gestión de Personas y Limpieza de Historial
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton btnCrearPersona = new JButton("👤 Registrar Nueva Persona");
        btnCrearPersona.setBackground(new Color(14, 165, 233));
        btnCrearPersona.setForeground(Color.WHITE);
        btnCrearPersona.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCrearPersona.addActionListener(e -> openCrearPersonaDialog());

        JButton btnEliminarPersona = new JButton("🗑️ Eliminar Persona");
        btnEliminarPersona.addActionListener(e -> executeEliminarPersona());

        JButton btnLimpiarVisitas = new JButton("🧹 Limpiar Historial de Visitas (Admin)");
        btnLimpiarVisitas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiarVisitas.setBackground(new Color(225, 29, 72));
        btnLimpiarVisitas.setForeground(Color.WHITE);
        btnLimpiarVisitas.addActionListener(e -> executeLimpiarVisitas());

        row2.add(btnCrearPersona);
        row2.add(btnEliminarPersona);
        row2.add(btnLimpiarVisitas);

        // Fila 3: Acciones Principales de Check-In y Check-Out destacados
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        btnCheckIn = new JButton("➡️ REALIZAR CHECK-IN (ENTRADA)");
        btnCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckIn.setBackground(new Color(16, 185, 129));
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setPreferredSize(new Dimension(240, 30));

        btnCheckOut = new JButton("⬅️ REALIZAR CHECK-OUT (SALIDA)");
        btnCheckOut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckOut.setBackground(new Color(239, 68, 68));
        btnCheckOut.setForeground(Color.WHITE);
        btnCheckOut.setPreferredSize(new Dimension(240, 30));

        row3.add(btnCheckIn);
        row3.add(btnCheckOut);

        actionPanel.add(row1);
        actionPanel.add(row2);
        actionPanel.add(row3);

        btnAutoSeed.addActionListener(e -> quickSeedVisita());
        btnRefresh.addActionListener(e -> loadAllData());
        btnCheckIn.addActionListener(e -> executeCheckIn());
        btnCheckOut.addActionListener(e -> executeCheckOut());
        btnNoAnunciada.addActionListener(e -> openNoAnunciadaDialog());
        btnPaseTemporal.addActionListener(e -> openPaseTemporalDialog());

        visitsPanel.add(actionPanel, BorderLayout.SOUTH);
        rightPanel.add(visitsPanel, BorderLayout.CENTER);

        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    public void loadAllData() {
        loadPersonas();
        loadVisitas();
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
                    listaPersonasCache = get();
                    tableModelPersonas.setRowCount(0);
                    for (Persona p : listaPersonasCache) {
                        tableModelPersonas.addRow(new Object[]{
                                p.getId(),
                                p.getDocIdentidad(),
                                p.getNombre() + " " + p.getApellido(),
                                p.getEstadoAcceso()
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void loadVisitas() {
        SwingWorker<List<Visita>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Visita> doInBackground() throws Exception {
                return apiClient.listarVisitas();
            }

            @Override
            protected void done() {
                try {
                    currentVisitas = get();
                    tableModelVisitas.setRowCount(0);
                    for (Visita v : currentVisitas) {
                        tableModelVisitas.addRow(new Object[]{
                                v.getId(),
                                v.getPersonaNombreCompleto(),
                                v.getPersonaDocIdentidad(),
                                v.getTipoVisita(),
                                v.getEstadoVisita(),
                                v.getMotivo()
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void searchPersona() {
        String doc = txtSearchDoc.getText().trim();
        if (doc.isEmpty()) return;

        SwingWorker<Persona, Void> worker = new SwingWorker<>() {
            @Override
            protected Persona doInBackground() throws Exception {
                return apiClient.buscarPersonaPorDoc(doc);
            }

            @Override
            protected void done() {
                try {
                    Persona p = get();
                    lblPersonaNombre.setText("Persona: " + p.getNombre() + " " + p.getApellido());
                    lblPersonaDoc.setText("Doc: " + p.getDocIdentidad() + " | Tipo: " + p.getTipoDocumento());

                    if ("RESTRINGIDO".equals(p.getEstadoAcceso().name())) {
                        lblPersonaEstado.setText(" 🚨 ALERTA: ACCESO DENEGADO (PERSONA RESTRINGIDA) ");
                        lblPersonaEstado.setBackground(new Color(220, 38, 38));
                    } else {
                        lblPersonaEstado.setText(" ✅ ACCESO AUTORIZADO - HABILITADO EN SICA ");
                        lblPersonaEstado.setBackground(new Color(34, 197, 94));
                    }
                } catch (Exception e) {
                    lblPersonaNombre.setText("Persona: No encontrada en BD");
                    lblPersonaDoc.setText("Documento: " + doc);
                    lblPersonaEstado.setText(" ⚠️ PERSONA NO REGISTRADA ");
                    lblPersonaEstado.setBackground(new Color(234, 179, 8));
                }
            }
        };
        worker.execute();
    }

    private void quickSeedVisita() {
        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() throws Exception {
                return apiClient.preregistrarVisita(1L, "Visita de Prueba Rápida QA", LocalDateTime.now().plusHours(1));
            }

            @Override
            protected void done() {
                try {
                    Visita v = get();
                    JOptionPane.showMessageDialog(GuardiaPanel.this,
                            "✨ Se ha creado una visita de prueba rápida para Juan Pérez (ID #" + v.getId() + " - APROBADO).\nAhora puedes hacerle Check-In directo.",
                            "Visita de Prueba Creada", JOptionPane.INFORMATION_MESSAGE);
                    loadVisitas();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void executeCheckIn() {
        int selectedRow = tblVisitas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona una visita de la tabla para realizar el Check-In", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long visitaId = (Long) tableModelVisitas.getValueAt(selectedRow, 0);

        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() throws Exception {
                return apiClient.checkIn(visitaId, 1L);
            }

            @Override
            protected void done() {
                try {
                    Visita updated = get();
                    JOptionPane.showMessageDialog(GuardiaPanel.this,
                            "✅ Check-In Exitoso para " + updated.getPersonaNombreCompleto() + "\nEstado actual: " + updated.getEstadoVisita(),
                            "Ingreso Registrado", JOptionPane.INFORMATION_MESSAGE);
                    loadVisitas();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(GuardiaPanel.this,
                            "❌ Error realizando Check-In:\n" + cause.getMessage(),
                            "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void executeCheckOut() {
        int selectedRow = tblVisitas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una visita activa para registrar su Check-Out", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long visitaId = (Long) tableModelVisitas.getValueAt(selectedRow, 0);

        SwingWorker<Visita, Void> worker = new SwingWorker<>() {
            @Override
            protected Visita doInBackground() throws Exception {
                return apiClient.checkOut(visitaId);
            }

            @Override
            protected void done() {
                try {
                    Visita updated = get();
                    JOptionPane.showMessageDialog(GuardiaPanel.this,
                            "👋 Check-Out exitoso para " + updated.getPersonaNombreCompleto() + "\nVisita finalizada.",
                            "Salida Registrada", JOptionPane.INFORMATION_MESSAGE);
                    loadVisitas();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openNoAnunciadaDialog() {
        if (listaPersonasCache == null || listaPersonasCache.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cargando lista de personas...", "Espera", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> comboPersonas = new JComboBox<>();
        for (Persona p : listaPersonasCache) {
            comboPersonas.addItem(p.getId() + " - " + p.getNombre() + " " + p.getApellido() + " (" + p.getDocIdentidad() + ")");
        }
        JTextField txtFuncionarioId = new JTextField("3", 5);
        JTextField txtMotivo = new JTextField("Reunión imprevista", 20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Seleccionar Visitante:")); panel.add(comboPersonas);
        panel.add(new JLabel("ID Funcionario Anfitrión:")); panel.add(txtFuncionarioId);
        panel.add(new JLabel("Motivo de Visita:")); panel.add(txtMotivo);

        int option = JOptionPane.showConfirmDialog(this, panel, "Registrar Visitante No Anunciado", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String sel = (String) comboPersonas.getSelectedItem();
                Long pId = Long.parseLong(sel.split(" - ")[0]);
                Long fId = Long.parseLong(txtFuncionarioId.getText().trim());
                String mot = txtMotivo.getText().trim();

                SwingWorker<Visita, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Visita doInBackground() throws Exception {
                        return apiClient.registrarNoAnunciada(pId, fId, mot);
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            JOptionPane.showMessageDialog(GuardiaPanel.this, "Visita No Anunciada registrada (Estado: PENDIENTE_APROBACION).\nNotificado a la pantalla del funcionario.", "Registrado", JOptionPane.INFORMATION_MESSAGE);
                            loadVisitas();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openPaseTemporalDialog() {
        if (listaPersonasCache == null || listaPersonasCache.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cargando lista de personas...", "Espera", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> comboPersonas = new JComboBox<>();
        for (Persona p : listaPersonasCache) {
            comboPersonas.addItem(p.getId() + " - " + p.getNombre() + " " + p.getApellido() + " (" + p.getDocIdentidad() + ")");
        }
        JTextField txtFuncionarioId = new JTextField("3", 5);
        JTextField txtMotivo = new JTextField("Olvido de carnet físico", 20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Empleado / Persona:")); panel.add(comboPersonas);
        panel.add(new JLabel("ID Jefe / Anfitrión:")); panel.add(txtFuncionarioId);
        panel.add(new JLabel("Observación:")); panel.add(txtMotivo);

        int option = JOptionPane.showConfirmDialog(this, panel, "Registrar Pase Temporal (Carnet Olvidado)", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String sel = (String) comboPersonas.getSelectedItem();
                Long pId = Long.parseLong(sel.split(" - ")[0]);
                Long fId = Long.parseLong(txtFuncionarioId.getText().trim());
                String mot = txtMotivo.getText().trim();

                SwingWorker<Visita, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Visita doInBackground() throws Exception {
                        return apiClient.registrarPaseTemporal(pId, fId, mot);
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            JOptionPane.showMessageDialog(GuardiaPanel.this, "Pase Temporal registrado (Estado: PENDIENTE_APROBACION_OLVIDO).\nEsperando aprobación del funcionario.", "Registrado", JOptionPane.INFORMATION_MESSAGE);
                            loadVisitas();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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

        int option = JOptionPane.showConfirmDialog(this, panel, "👤 Registrar Nueva Persona en SICA", JOptionPane.OK_CANCEL_OPTION);
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
                        JOptionPane.showMessageDialog(GuardiaPanel.this,
                                "✅ Persona registrada exitosamente en SICA:\n" + p.getNombre() + " " + p.getApellido() + " (Doc: " + p.getDocIdentidad() + ")",
                                "Registro Éxitoso", JOptionPane.INFORMATION_MESSAGE);
                        loadPersonas();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void executeEliminarPersona() {
        int selectedRow = tblPersonas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una persona de la tabla izquierda para eliminar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long personaId = (Long) tableModelPersonas.getValueAt(selectedRow, 0);
        String nombre = (String) tableModelPersonas.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar a " + nombre + " (ID #" + personaId + ") de la base de datos?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.eliminarPersona(personaId);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Persona eliminada correctamente", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                        loadPersonas();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void executeLimpiarVisitas() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de vaciar y limpiar TODO el historial de visitas registradas en el sistema?",
                "Limpiar Historial de Visitas", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.limpiarVisitas();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "✨ Historial de visitas limpiado correctamente.", "Limpieza Éxitosa", JOptionPane.INFORMATION_MESSAGE);
                        loadVisitas();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
}
