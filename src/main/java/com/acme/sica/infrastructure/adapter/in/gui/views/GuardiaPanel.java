package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.domain.model.Incidente;
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
import java.util.Map;

public class GuardiaPanel extends JPanel {

    private final SicaApiClient apiClient;

    private JTextField txtSearchDoc;
    private JButton btnSearch;
    private JLabel lblPersonaNombre;
    private JLabel lblPersonaDoc;
    private JLabel lblPersonaEstado;
    private JLabel lblPersonaFoto;
    private JLabel lblVisitaTarget;
    private JLabel lblVisitaEstado;
    private com.acme.sica.infrastructure.adapter.in.gui.components.EstadoSicaGradientCard gradientCardRef;


    private JTable tblPersonas;
    private DefaultTableModel tableModelPersonas;
    private List<Persona> listaPersonasCache;

    private JTable tblPersonaIncidentes;
    private DefaultTableModel tableModelPersonaIncidentes;

    private List<Map<String, Object>> listaUsuariosCache;

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

        // Timer para actualización en tiempo real (cada 3 segundos)
        Timer timer = new Timer(3000, e -> loadVisitas());
        timer.start();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // --- BANNER DE GUÍA ---
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(30, 41, 59));
        bannerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lblHelp = new JLabel(
                "[i] MÓDULO DE PORTERÍA (Avanzado): Selecciona una persona de la lista izquierda o ingresa su documento para consultar su estado en tiempo real.");
        lblHelp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHelp.setForeground(new Color(226, 232, 240));
        bannerPanel.add(lblHelp, BorderLayout.CENTER);
        add(bannerPanel, BorderLayout.NORTH);

        // --- CONTAINER SPLIT: Izquierda (Personas BD + Incidentes + Gráfica) | Derecha (Consulta + Estado + Visitas)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(380);
        mainSplit.setResizeWeight(0.35);

        // ==================== PANEL IZQUIERDO: Personas en BD ====================
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("LISTA DE PERSONAS"));

        String[] colsPersonas = { "ID", "DOCUMENTO", "NOMBRE COM", "ESTADO" };
        tableModelPersonas = new DefaultTableModel(colsPersonas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPersonas = new JTable(tableModelPersonas);
        tblPersonas.setRowHeight(26);
        tblPersonas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblPersonas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPersonas.getColumnModel().getColumn(3).setCellRenderer(new StatusPillCellRenderer());
        tblPersonas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPersonas.getSelectedRow() != -1) {
                int row = tblPersonas.getSelectedRow();
                String doc = (String) tableModelPersonas.getValueAt(row, 1);
                txtSearchDoc.setText(doc);
                searchPersona();
            }
        });

        JScrollPane scrollPersonas = new JScrollPane(tblPersonas);

        JPanel bottomIncidentContainer = new JPanel(new BorderLayout(4, 4));
        bottomIncidentContainer.setBorder(new TitledBorder("Incidentes de la Persona Seleccionada"));
        String[] colsIncidentes = {"ID", "Gravedad", "Título resumido", "Fecha"};
        tableModelPersonaIncidentes = new DefaultTableModel(colsIncidentes, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPersonaIncidentes = new JTable(tableModelPersonaIncidentes);
        tblPersonaIncidentes.setRowHeight(22);
        tblPersonaIncidentes.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JScrollPane scrollIncidentes = new JScrollPane(tblPersonaIncidentes);
        scrollIncidentes.setPreferredSize(new Dimension(340, 110));
        bottomIncidentContainer.add(scrollIncidentes, BorderLayout.CENTER);

        // Gráfica Sparkline de tendencia abajo
        com.acme.sica.infrastructure.adapter.in.gui.components.SparklineChartPanel sparklinePanel = new com.acme.sica.infrastructure.adapter.in.gui.components.SparklineChartPanel();
        sparklinePanel.setPreferredSize(new Dimension(340, 95));
        bottomIncidentContainer.add(sparklinePanel, BorderLayout.SOUTH);

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setDividerLocation(230);
        leftSplit.setTopComponent(scrollPersonas);
        leftSplit.setBottomComponent(bottomIncidentContainer);

        leftPanel.add(leftSplit, BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);

        // ==================== PANEL DERECHO: Consulta + Estado Degradado + Visitas ====================
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));

        // Subpanel Superior Derecho: Consulta + Tarjeta Degradada SICA
        JPanel topRight = new JPanel(new GridLayout(1, 2, 8, 8));

        JPanel searchBoxPanel = new JPanel(new BorderLayout(6, 6));
        searchBoxPanel.setBorder(new TitledBorder("CONSULTA DE PERSONA"));

        JPanel searchForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        txtSearchDoc = new JTextField("1010101010", 14);
        txtSearchDoc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnSearch = new JButton("[Q] CONSULTAR");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchPersona());

        searchForm.add(txtSearchDoc);
        searchForm.add(btnSearch);

        JPanel detailsGrid = new JPanel(new GridLayout(4, 1, 4, 4));
        lblPersonaNombre = new JLabel("Persona: (Selecciona una persona)");
        lblPersonaNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblPersonaDoc = new JLabel("Doc: -");
        lblPersonaDoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblVisitaTarget = new JLabel("Visita a: Ninguna");
        lblVisitaTarget.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblVisitaEstado = new JLabel("Estado Visita: -");
        lblVisitaEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));

        detailsGrid.add(lblPersonaNombre);
        detailsGrid.add(lblPersonaDoc);
        detailsGrid.add(lblVisitaTarget);
        detailsGrid.add(lblVisitaEstado);

        searchBoxPanel.add(searchForm, BorderLayout.NORTH);
        searchBoxPanel.add(detailsGrid, BorderLayout.CENTER);

        // Tarjeta Degradada ESTADO SICA
        com.acme.sica.infrastructure.adapter.in.gui.components.EstadoSicaGradientCard gradientCard = new com.acme.sica.infrastructure.adapter.in.gui.components.EstadoSicaGradientCard();
        this.gradientCardRef = gradientCard;

        topRight.add(searchBoxPanel);
        topRight.add(gradientCard);

        rightPanel.add(topRight, BorderLayout.NORTH);

        // Subpanel Central Derecho: Tabla de Visitas y Control de Accesos
        JPanel visitsPanel = new JPanel(new BorderLayout(5, 5));

        visitsPanel.setBorder(new TitledBorder("🚪 Registro de Visitas y Control de Accesos Físicos"));

        String[] colsVisitas = { "ID", "Persona / Visitante", "Documento", "Tipo Visita", "Estado",
                "Motivo / Detalle" };
        tableModelVisitas = new DefaultTableModel(colsVisitas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblVisitas = new JTable(tableModelVisitas);
        tblVisitas.setRowHeight(26);
        tblVisitas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblVisitas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblVisitas.getColumnModel().getColumn(4).setCellRenderer(new StatusPillCellRenderer());
        JScrollPane scrollVisitas = new JScrollPane(tblVisitas);

        visitsPanel.add(scrollVisitas, BorderLayout.CENTER);

        // Botonera de Acciones (Organizada en 3 Filas independientes para CERO recortes)
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 4, 4));

        // Fila 1: Novedades de Registro y Pruebas
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        btnAutoSeed = new JButton("[▶] Visita Rápida de Prueba");
        btnAutoSeed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAutoSeed.setBackground(new Color(139, 92, 246));
        btnAutoSeed.setForeground(Color.WHITE);

        btnNoAnunciada = new JButton("[+] Visitante No Anunciado");
        btnPaseTemporal = new JButton("[+] Pase Temporal");
        btnRefresh = new JButton("[R] Actualizar Tabla");

        row1.add(btnAutoSeed);
        row1.add(btnNoAnunciada);
        row1.add(btnPaseTemporal);
        row1.add(btnRefresh);

        // Fila 2: Gestión de Personas y Limpieza de Historial
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton btnCrearPersona = new JButton("[+] Registrar Persona");
        btnCrearPersona.setBackground(new Color(14, 165, 233));
        btnCrearPersona.setForeground(Color.WHITE);
        btnCrearPersona.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCrearPersona.addActionListener(e -> openCrearPersonaDialog());

        JButton btnEliminarPersona = new JButton("[x] Eliminar Persona");
        btnEliminarPersona.addActionListener(e -> executeEliminarPersona());

        JButton btnLimpiarVisitas = new JButton("[!] Limpiar Historial (Admin)");
        btnLimpiarVisitas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiarVisitas.setBackground(new Color(225, 29, 72));
        btnLimpiarVisitas.setForeground(Color.WHITE);
        btnLimpiarVisitas.addActionListener(e -> executeLimpiarVisitas());

        row2.add(btnCrearPersona);
        row2.add(btnEliminarPersona);
        row2.add(btnLimpiarVisitas);

        // Fila 3: Acciones Principales de Check-In y Check-Out destacados
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        btnCheckIn = new JButton("[▶] CHECK-IN (ENTRADA)");
        btnCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckIn.setBackground(new Color(16, 185, 129));
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setPreferredSize(new Dimension(240, 30));

        btnCheckOut = new JButton("[■] CHECK-OUT (SALIDA)");
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
        loadUsuarios();
    }

    private void loadUsuarios() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return apiClient.listarUsuarios();
            }

            @Override
            protected void done() {
                try {
                    listaUsuariosCache = get();
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
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
                        tableModelPersonas.addRow(new Object[] {
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
                    int selectedRow = tblVisitas.getSelectedRow();
                    Long selectedId = null;
                    if (selectedRow != -1) {
                        selectedId = (Long) tableModelVisitas.getValueAt(selectedRow, 0);
                    }

                    tableModelVisitas.setRowCount(0);
                    for (Visita v : currentVisitas) {
                        tableModelVisitas.addRow(new Object[] {
                                v.getId(),
                                v.getPersonaNombreCompleto(),
                                v.getPersonaDocIdentidad(),
                                v.getTipoVisita(),
                                v.getEstadoVisita(),
                                v.getMotivo()
                        });
                    }

                    if (selectedId != null) {
                        for (int i = 0; i < tableModelVisitas.getRowCount(); i++) {
                            if (tableModelVisitas.getValueAt(i, 0).equals(selectedId)) {
                                tblVisitas.setRowSelectionInterval(i, i);
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void searchPersona() {
        String doc = txtSearchDoc.getText().trim();
        if (doc.isEmpty())
            return;

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
                    
                    // TODO: The backend doesn't support photo URL yet. We simulate it for now.
                    lblPersonaFoto.setText("📷 Foto: [URL https://sica.local/fotos/" + p.getDocIdentidad() + ".jpg]");

                    Visita activeVisita = currentVisitas != null ? currentVisitas.stream()
                            .filter(v -> v.getPersonaDocIdentidad() != null && v.getPersonaDocIdentidad().equals(p.getDocIdentidad()))
                            .filter(v -> "PRE_REGISTRADA".equals(v.getEstadoVisita().name()) || "APROBADA".equals(v.getEstadoVisita().name()) || "EN_CURSO".equals(v.getEstadoVisita().name()))
                            .findFirst().orElse(null) : null;

                    if (activeVisita != null) {
                        String target = activeVisita.getFuncionarioNombreCompleto() != null ? activeVisita.getFuncionarioNombreCompleto() : "N/A";
                        lblVisitaTarget.setText("🏢 Visita a: " + target);
                        lblVisitaEstado.setText("🎫 Estado Visita: " + activeVisita.getEstadoVisita().name());
                    } else {
                        lblVisitaTarget.setText("🏢 Visita a: Ninguna visita activa");
                        lblVisitaEstado.setText("🎫 Estado Visita: -");
                    }

                    if (gradientCardRef != null) {
                        String target = activeVisita != null ? activeVisita.getFuncionarioNombreCompleto() : "Ninguna visita activa";
                        gradientCardRef.updateState(p.getNombre() + " " + p.getApellido(), p.getDocIdentidad(), target, p.getEstadoAcceso().name());
                    }

                    if ("RESTRINGIDO".equals(p.getEstadoAcceso().name())) {
                        lblPersonaEstado.setText(" [!] ALERTA: ACCESO DENEGADO (PERSONA RESTRINGIDA) ");
                        lblPersonaEstado.setBackground(new Color(220, 38, 38));
                    } else {
                        lblPersonaEstado.setText(" [+] ACCESO AUTORIZADO - HABILITADO EN SICA ");
                        lblPersonaEstado.setBackground(new Color(34, 197, 94));
                    }

                    
                    tableModelPersonaIncidentes.setRowCount(0);
                    SwingWorker<List<Incidente>, Void> incWorker = new SwingWorker<>() {
                        @Override protected List<Incidente> doInBackground() throws Exception {
                            return apiClient.listarIncidentes().stream()
                                    .filter(inc -> inc.getPersonaId().equals(p.getId()))
                                    .toList();
                        }
                        @Override protected void done() {
                            try {
                                List<Incidente> incs = get();
                                for (Incidente i : incs) {
                                    tableModelPersonaIncidentes.addRow(new Object[]{ i.getId(), i.getNivelGravedad(), i.getTitulo(), i.getFechaHora() });
                                }
                            } catch(Exception ignored) {}
                        }
                    };
                    incWorker.execute();
                    
                } catch (Exception e) {
                    lblPersonaNombre.setText("Persona: No encontrada en BD");
                    lblPersonaDoc.setText("Documento: " + doc);
                    lblPersonaFoto.setText("📷 Foto: [No Disponible]");
                    lblVisitaTarget.setText("🏢 Visita a: -");
                    lblVisitaEstado.setText("🎫 Estado Visita: -");
                    lblPersonaEstado.setText(" ⚠️ PERSONA NO REGISTRADA ");
                    lblPersonaEstado.setBackground(new Color(234, 179, 8));
                    if (tableModelPersonaIncidentes != null) {
                        tableModelPersonaIncidentes.setRowCount(0);
                    }
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
                            "✨ Se ha creado una visita de prueba rápida para Juan Pérez (ID #" + v.getId()
                                    + " - APROBADO).\nAhora puedes hacerle Check-In directo.",
                            "Visita de Prueba Creada", JOptionPane.INFORMATION_MESSAGE);
                    loadVisitas();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + e.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void executeCheckIn() {
        int selectedRow = tblVisitas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona una visita de la tabla para realizar el Check-In",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
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
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(GuardiaPanel.this,
                            "[+] Check-In Exitoso para " + updated.getPersonaNombreCompleto(),
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);
                    JOptionPane.showMessageDialog(GuardiaPanel.this,
                            "Check-In Exitoso para " + updated.getPersonaNombreCompleto() + "\nEstado actual: "
                                    + updated.getEstadoVisita(),
                            "Ingreso Registrado", JOptionPane.INFORMATION_MESSAGE);
                    loadVisitas();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(GuardiaPanel.this,
                            "[x] Acceso Denegado: " + cause.getMessage(),
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.ERROR);
                    JOptionPane.showMessageDialog(GuardiaPanel.this,
                            "Error realizando Check-In:\n" + cause.getMessage(),
                            "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void executeCheckOut() {
        int selectedRow = tblVisitas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una visita activa para registrar su Check-Out", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
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
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(GuardiaPanel.this,
                            "[+] Check-Out Exitoso para " + updated.getPersonaNombreCompleto(),
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);
                    JOptionPane.showMessageDialog(GuardiaPanel.this,
                            "Check-Out exitoso para " + updated.getPersonaNombreCompleto() + "\nVisita finalizada.",
                            "Salida Registrada", JOptionPane.INFORMATION_MESSAGE);
                    loadVisitas();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(GuardiaPanel.this,
                            "[x] Error en Check-Out: " + cause.getMessage(),
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.ERROR);
                    JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }


    private void openNoAnunciadaDialog() {
        if (listaPersonasCache == null || listaPersonasCache.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cargando lista de personas...", "Espera",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> comboPersonas = new JComboBox<>();
        for (Persona p : listaPersonasCache) {
            comboPersonas.addItem(
                    p.getId() + " - " + p.getNombre() + " " + p.getApellido() + " (" + p.getDocIdentidad() + ")");
        }
        JComboBox<String> comboFuncionarios = new JComboBox<>();
        if (listaUsuariosCache != null && !listaUsuariosCache.isEmpty()) {
            for (Map<String, Object> u : listaUsuariosCache) {
                comboFuncionarios
                        .addItem(u.get("id") + " - " + u.get("nombreCompleto") + " (" + u.get("username") + ")");
            }
        } else {
            comboFuncionarios.addItem("1 - Administrador General (admin)");
            comboFuncionarios.addItem("2 - Carlos Guardia (guardia1)");
            comboFuncionarios.addItem("3 - Ana Funcionario (func1)");
        }
        JTextField txtMotivo = new JTextField("Reunión imprevista", 20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Seleccionar Visitante:"));
        panel.add(comboPersonas);
        panel.add(new JLabel("Funcionario Anfitrión:"));
        panel.add(comboFuncionarios);
        panel.add(new JLabel("Motivo de Visita:"));
        panel.add(txtMotivo);

        int option = JOptionPane.showConfirmDialog(this, panel, "Registrar Visitante No Anunciado",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String sel = (String) comboPersonas.getSelectedItem();
                Long pId = Long.parseLong(sel.split(" - ")[0]);
                String selFunc = (String) comboFuncionarios.getSelectedItem();
                Long fId = Long.parseLong(selFunc.split(" - ")[0]);
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
                            JOptionPane.showMessageDialog(GuardiaPanel.this,
                                    "Visita No Anunciada registrada (Estado: PENDIENTE_APROBACION).\nNotificado a la pantalla del funcionario.",
                                    "Registrado", JOptionPane.INFORMATION_MESSAGE);
                            loadVisitas();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + e.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openPaseTemporalDialog() {
        if (listaPersonasCache == null || listaPersonasCache.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cargando lista de personas...", "Espera",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> comboPersonas = new JComboBox<>();
        for (Persona p : listaPersonasCache) {
            comboPersonas.addItem(
                    p.getId() + " - " + p.getNombre() + " " + p.getApellido() + " (" + p.getDocIdentidad() + ")");
        }
        JComboBox<String> comboFuncionarios = new JComboBox<>();
        if (listaUsuariosCache != null && !listaUsuariosCache.isEmpty()) {
            for (Map<String, Object> u : listaUsuariosCache) {
                comboFuncionarios
                        .addItem(u.get("id") + " - " + u.get("nombreCompleto") + " (" + u.get("username") + ")");
            }
        } else {
            comboFuncionarios.addItem("1 - Administrador General (admin)");
            comboFuncionarios.addItem("2 - Carlos Guardia (guardia1)");
            comboFuncionarios.addItem("3 - Ana Funcionario (func1)");
        }
        JTextField txtMotivo = new JTextField("Olvido de carnet físico", 20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Empleado / Persona:"));
        panel.add(comboPersonas);
        panel.add(new JLabel("Jefe / Anfitrión:"));
        panel.add(comboFuncionarios);
        panel.add(new JLabel("Observación:"));
        panel.add(txtMotivo);

        int option = JOptionPane.showConfirmDialog(this, panel, "Registrar Pase Temporal (Carnet Olvidado)",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String sel = (String) comboPersonas.getSelectedItem();
                Long pId = Long.parseLong(sel.split(" - ")[0]);
                String selFunc = (String) comboFuncionarios.getSelectedItem();
                Long fId = Long.parseLong(selFunc.split(" - ")[0]);
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
                            JOptionPane.showMessageDialog(GuardiaPanel.this,
                                    "Pase Temporal registrado (Estado: PENDIENTE_APROBACION_OLVIDO).\nEsperando aprobación del funcionario.",
                                    "Registrado", JOptionPane.INFORMATION_MESSAGE);
                            loadVisitas();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + e.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openCrearPersonaDialog() {
        JTextField txtDoc = new JTextField(12);
        JComboBox<String> comboTipoDoc = new JComboBox<>(new String[] { "CC", "CE", "PASAPORTE", "TI" });
        JTextField txtNombre = new JTextField(15);
        JTextField txtApellido = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JTextField txtTel = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(6, 2, 6, 6));
        panel.add(new JLabel("Documento:"));
        panel.add(txtDoc);
        panel.add(new JLabel("Tipo Documento:"));
        panel.add(comboTipoDoc);
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Apellido:"));
        panel.add(txtApellido);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("Teléfono:"));
        panel.add(txtTel);

        int option = JOptionPane.showConfirmDialog(this, panel, "👤 Registrar Nueva Persona en SICA",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String doc = txtDoc.getText().trim();
            String tipo = (String) comboTipoDoc.getSelectedItem();
            String nom = txtNombre.getText().trim();
            String ape = txtApellido.getText().trim();
            String email = txtEmail.getText().trim();
            String tel = txtTel.getText().trim();

            if (doc.isEmpty() || nom.isEmpty() || ape.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Documento, Nombre y Apellido son obligatorios", "Aviso",
                        JOptionPane.WARNING_MESSAGE);
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
                                "✅ Persona registrada exitosamente en SICA:\n" + p.getNombre() + " " + p.getApellido()
                                        + " (Doc: " + p.getDocIdentidad() + ")",
                                "Registro Éxitoso", JOptionPane.INFORMATION_MESSAGE);
                        loadPersonas();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void executeEliminarPersona() {
        int selectedRow = tblPersonas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una persona de la tabla izquierda para eliminar", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
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
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Persona eliminada correctamente", "Eliminado",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadPersonas();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showMessageDialog(GuardiaPanel.this,
                                "✨ Historial de visitas limpiado correctamente.", "Limpieza Éxitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadVisitas();
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private static class StatusPillCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));

            String str = value != null ? value.toString() : "";
            if ("DENTRO".equalsIgnoreCase(str)) {
                label.setText("[●] DENTRO");
                label.setBackground(new Color(16, 185, 129));
                label.setForeground(Color.WHITE);
            } else if ("FINALIZADO".equalsIgnoreCase(str) || "CERRADO_POR_SISTEMA".equalsIgnoreCase(str)) {
                label.setText("[○] CERRADO");
                label.setBackground(new Color(71, 85, 105));
                label.setForeground(Color.WHITE);
            } else if ("RESTRINGIDO".equalsIgnoreCase(str) || "BLOQUEADO".equalsIgnoreCase(str)) {
                label.setText("[!] RESTRINGIDO");
                label.setBackground(new Color(225, 29, 72));
                label.setForeground(Color.WHITE);
            } else if ("HABILITADO".equalsIgnoreCase(str) || "APROBADO".equalsIgnoreCase(str) || "ACTIVO".equalsIgnoreCase(str)) {
                label.setText("[+] " + str);
                label.setBackground(new Color(14, 165, 233));
                label.setForeground(Color.WHITE);
            } else if ("PENDIENTE_APROBACION".equalsIgnoreCase(str) || "PENDIENTE".equalsIgnoreCase(str)) {
                label.setText("[?] PENDIENTE");
                label.setBackground(new Color(245, 158, 11));
                label.setForeground(Color.WHITE);
            } else {
                label.setText(str);
                if (!isSelected) {
                    label.setBackground(table.getBackground());
                    label.setForeground(table.getForeground());
                }
            }
            label.setOpaque(true);
            return label;
        }
    }
}

