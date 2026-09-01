package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;
import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;
import com.acme.sica.infrastructure.adapter.in.gui.components.CriticalConfirmationDialog;
import com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;


/**
 * Panel de Control de Accesos en Vivo en Portería (Sin Emojis).
 */
public class GuardiaPanel extends JPanel {

    private final SicaApiClient apiClient;

    private JTable tblPersonas;
    private DefaultTableModel tableModelPersonas;

    private JTable tblPersonaIncidentes;
    private DefaultTableModel tableModelPersonaIncidentes;

    private JTextField txtSearchDoc;
    private JButton btnSearch;
    private JLabel lblPersonaNombre;
    private JLabel lblPersonaDoc;
    private JLabel lblVisitaTarget;
    private JLabel lblVisitaEstado;
    private JLabel lblFotoConsulta;


    private JTable tblVisitas;
    private DefaultTableModel tableModelVisitas;

    private JButton btnCheckIn;
    private JButton btnCheckOut;

    private JButton btnAutoSeed;
    private JButton btnNoAnunciada;
    private JButton btnPaseTemporal;
    private JButton btnRefresh;

    private JLabel lblHeroAccesosActivos;
    private JLabel lblKpiTotalPersonas;
    private JLabel lblKpiTotalIncidentes;

    private com.acme.sica.infrastructure.adapter.in.gui.components.EstadoSicaGradientCard gradientCardRef;

    private List<Persona> currentPersonas;
    private List<Visita> currentVisitas;

    public GuardiaPanel(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
        loadAllData();

        Timer timer = new Timer(3000, e -> loadVisitas());
        timer.start();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 14, 12, 14));
        setBackground(SicaTheme.BG_MAIN);

        // SECCIÓN SUPERIOR: MÉTRICA HERO PROTAGONISTA & KPIS
        JPanel topHeaderGrid = new JPanel(new BorderLayout(12, 0));
        topHeaderGrid.setOpaque(false);

        JPanel heroCard = new JPanel(new BorderLayout(12, 0));
        heroCard.setBackground(SicaTheme.ACCENT_CYAN_LIGHT);
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.ACCENT_CYAN, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel heroLeft = new JPanel(new GridLayout(2, 1, 2, 2));
        heroLeft.setOpaque(false);

        JLabel lblHeroTitle = new JLabel("● ACCESOS ACTIVOS EN INSTALACIONES (EN VIVO)");
        lblHeroTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeroTitle.setForeground(SicaTheme.STATUS_GRANTED_TEXT);

        lblHeroAccesosActivos = new JLabel("0");
        lblHeroAccesosActivos.setFont(SicaTheme.FONT_HERO);
        lblHeroAccesosActivos.setForeground(SicaTheme.ACCENT_NAVY);

        heroLeft.add(lblHeroTitle);
        heroLeft.add(lblHeroAccesosActivos);

        heroCard.add(heroLeft, BorderLayout.CENTER);

        JPanel secondaryKpis = new JPanel(new GridLayout(1, 2, 8, 0));
        secondaryKpis.setOpaque(false);

        JPanel kpi1 = createCompactKpiCard("PERSONAS EN BD", lblKpiTotalPersonas = new JLabel("0"));
        JPanel kpi2 = createCompactKpiCard("INCIDENTES REGISTRADOS", lblKpiTotalIncidentes = new JLabel("0"));

        secondaryKpis.add(kpi1);
        secondaryKpis.add(kpi2);

        topHeaderGrid.add(heroCard, BorderLayout.CENTER);
        topHeaderGrid.add(secondaryKpis, BorderLayout.EAST);

        add(topHeaderGrid, BorderLayout.NORTH);

        // CONTAINER SPLIT CENTRAL
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(380);
        mainSplit.setResizeWeight(0.35);

        // PANEL IZQUIERDO: Personas & Incidentes
        String[] colsPersonas = { "ID", "DOCUMENTO", "NOMBRE", "ESTADO" };
        tableModelPersonas = new DefaultTableModel(colsPersonas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPersonas = new JTable(tableModelPersonas);
        SicaTheme.styleTable(tblPersonas);
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
        JPanel cardPersonas = SicaTheme.createHeaderCard("Personas Registradas", scrollPersonas);

        String[] colsIncidentes = {"ID", "Gravedad", "Título Incidente", "Fecha"};
        tableModelPersonaIncidentes = new DefaultTableModel(colsIncidentes, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPersonaIncidentes = new JTable(tableModelPersonaIncidentes);
        SicaTheme.styleTable(tblPersonaIncidentes);
        JScrollPane scrollIncidentes = new JScrollPane(tblPersonaIncidentes);
        scrollIncidentes.setPreferredSize(new Dimension(340, 130));
        JPanel cardIncidentes = SicaTheme.createHeaderCard("Historial de Incidentes", scrollIncidentes);

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setDividerLocation(240);
        leftSplit.setTopComponent(cardPersonas);
        leftSplit.setBottomComponent(cardIncidentes);

        mainSplit.setLeftComponent(leftSplit);

        // PANEL DERECHO: Consulta + Estado Real-Time + Visitas
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setOpaque(false);

        JPanel topRight = new JPanel(new GridLayout(1, 2, 8, 8));
        topRight.setOpaque(false);

        JPanel searchForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchForm.setOpaque(false);
        txtSearchDoc = new JTextField("98765432", 12);
        txtSearchDoc.setFont(SicaTheme.FONT_BODY);
        txtSearchDoc.setBackground(SicaTheme.CARD_BG);
        txtSearchDoc.setForeground(SicaTheme.TEXT_MAIN);
        txtSearchDoc.setCaretColor(SicaTheme.ACCENT_CYAN);
        txtSearchDoc.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        btnSearch = new JButton("Buscar");
        SicaTheme.styleButton(btnSearch, SicaTheme.ACCENT_CYAN, Color.WHITE);
        btnSearch.addActionListener(e -> searchPersona());

        searchForm.add(txtSearchDoc);
        searchForm.add(btnSearch);

        JPanel detailsGrid = new JPanel(new GridLayout(4, 1, 3, 3));
        detailsGrid.setOpaque(false);

        lblPersonaNombre = new JLabel("Persona: (Selecciona una persona)");
        lblPersonaNombre.setFont(SicaTheme.FONT_BOLD);
        lblPersonaNombre.setForeground(SicaTheme.TEXT_MAIN);

        lblPersonaDoc = new JLabel("Doc: -");
        lblPersonaDoc.setFont(SicaTheme.FONT_BODY);
        lblPersonaDoc.setForeground(SicaTheme.TEXT_MUTED);

        lblVisitaTarget = new JLabel("Visita a: Ninguna");
        lblVisitaTarget.setFont(SicaTheme.FONT_BODY);
        lblVisitaTarget.setForeground(SicaTheme.TEXT_MUTED);

        lblVisitaEstado = new JLabel("Estado Visita: -");
        lblVisitaEstado.setFont(SicaTheme.FONT_BOLD);
        lblVisitaEstado.setForeground(SicaTheme.ACCENT_CYAN);

        detailsGrid.add(lblPersonaNombre);
        detailsGrid.add(lblPersonaDoc);
        detailsGrid.add(lblVisitaTarget);
        detailsGrid.add(lblVisitaEstado);




        lblFotoConsulta = new JLabel();
        lblFotoConsulta.setPreferredSize(new Dimension(64, 64));
        lblFotoConsulta.setMinimumSize(new Dimension(64, 64));
        lblFotoConsulta.setHorizontalAlignment(SwingConstants.CENTER);
        lblFotoConsulta.setIcon(com.acme.sica.infrastructure.adapter.in.gui.components.ImageUtils.createVectorAvatarIcon(60, 60));
        lblFotoConsulta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.ACCENT_CYAN, 1, true),
                new EmptyBorder(2, 2, 2, 2)
        ));

        JPanel detailsAndPhoto = new JPanel(new BorderLayout(8, 0));
        detailsAndPhoto.setOpaque(false);
        detailsAndPhoto.add(detailsGrid, BorderLayout.CENTER);
        detailsAndPhoto.add(lblFotoConsulta, BorderLayout.EAST);

        JPanel searchContent = new JPanel(new BorderLayout(4, 4));
        searchContent.setOpaque(false);
        searchContent.add(searchForm, BorderLayout.NORTH);
        searchContent.add(detailsAndPhoto, BorderLayout.CENTER);

        JPanel cardSearch = SicaTheme.createHeaderCard("Consulta de Visitante", searchContent);


        com.acme.sica.infrastructure.adapter.in.gui.components.EstadoSicaGradientCard gradientCard = new com.acme.sica.infrastructure.adapter.in.gui.components.EstadoSicaGradientCard();
        this.gradientCardRef = gradientCard;

        topRight.add(cardSearch);
        topRight.add(gradientCard);

        rightPanel.add(topRight, BorderLayout.NORTH);

        String[] colsVisitas = { "ID", "Persona / Visitante", "Documento", "Tipo Visita", "Estado", "Motivo / Detalle" };
        tableModelVisitas = new DefaultTableModel(colsVisitas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblVisitas = new JTable(tableModelVisitas);
        SicaTheme.styleTable(tblVisitas);
        tblVisitas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblVisitas.getColumnModel().getColumn(4).setCellRenderer(new StatusPillCellRenderer());
        JScrollPane scrollVisitas = new JScrollPane(tblVisitas);

        JPanel visitsCard = SicaTheme.createHeaderCard("Registro de Visitas y Control de Accesos Físicos", scrollVisitas);

        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        actionPanel.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row1.setOpaque(false);
        btnAutoSeed = new JButton("+ Visita Rápida");
        SicaTheme.styleButton(btnAutoSeed, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MAIN);

        btnNoAnunciada = new JButton("+ No Anunciado");
        SicaTheme.styleButton(btnNoAnunciada, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MAIN);

        btnPaseTemporal = new JButton("+ Pase Temporal");
        SicaTheme.styleButton(btnPaseTemporal, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MAIN);

        btnRefresh = new JButton("Actualizar Tabla");
        SicaTheme.styleButton(btnRefresh, SicaTheme.CARD_BG_ALT, SicaTheme.ACCENT_CYAN);

        row1.add(btnAutoSeed);
        row1.add(btnNoAnunciada);
        row1.add(btnPaseTemporal);
        row1.add(btnRefresh);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row2.setOpaque(false);
        JButton btnCrearPersona = new JButton("+ Registrar Persona");
        SicaTheme.styleButton(btnCrearPersona, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MAIN);
        btnCrearPersona.addActionListener(e -> openCrearPersonaDialog());

        JButton btnEditarPersona = new JButton("✏️ Editar Persona");
        SicaTheme.styleButton(btnEditarPersona, SicaTheme.CARD_BG_ALT, SicaTheme.ACCENT_CYAN);
        btnEditarPersona.addActionListener(e -> openEditarPersonaDialog());

        JButton btnEliminarPersona = new JButton("Eliminar Persona");
        SicaTheme.styleButton(btnEliminarPersona, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);
        btnEliminarPersona.addActionListener(e -> executeEliminarPersona());

        JButton btnLimpiarVisitas = new JButton("Limpiar Historial");
        SicaTheme.styleButton(btnLimpiarVisitas, SicaTheme.CARD_BG_ALT, SicaTheme.STATUS_DENIED_TEXT);
        btnLimpiarVisitas.addActionListener(e -> executeLimpiarVisitas());

        row2.add(btnCrearPersona);
        row2.add(btnEditarPersona);
        row2.add(btnEliminarPersona);
        row2.add(btnLimpiarVisitas);


        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        row3.setOpaque(false);
        btnCheckIn = new JButton("CHECK-IN (ENTRADA)");
        SicaTheme.styleButton(btnCheckIn, SicaTheme.STATUS_GRANTED_TEXT, Color.WHITE);
        btnCheckIn.setPreferredSize(new Dimension(220, 34));

        btnCheckOut = new JButton("CHECK-OUT (SALIDA)");
        SicaTheme.styleButton(btnCheckOut, SicaTheme.STATUS_DENIED_TEXT, Color.WHITE);
        btnCheckOut.setPreferredSize(new Dimension(220, 34));

        row3.add(btnCheckIn);
        row3.add(btnCheckOut);

        actionPanel.add(row1);
        actionPanel.add(row2);
        actionPanel.add(row3);

        JPanel centerRight = new JPanel(new BorderLayout(0, 6));
        centerRight.setOpaque(false);
        centerRight.add(visitsCard, BorderLayout.CENTER);
        centerRight.add(actionPanel, BorderLayout.SOUTH);

        rightPanel.add(centerRight, BorderLayout.CENTER);
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);

        btnCheckIn.addActionListener(e -> executeCheckIn());
        btnCheckOut.addActionListener(e -> executeCheckOut());
        btnAutoSeed.addActionListener(e -> executeVisitaRapidaPrueba());
        btnNoAnunciada.addActionListener(e -> openVisitaNoAnunciadaDialog());
        btnPaseTemporal.addActionListener(e -> openPaseTemporalDialog());
        btnRefresh.addActionListener(e -> loadAllData());
    }

    private JPanel createCompactKpiCard(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setBackground(SicaTheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(SicaTheme.FONT_SMALL);
        t.setForeground(SicaTheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(SicaTheme.ACCENT_NAVY);

        p.add(t, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    public void loadAllData() {
        loadPersonas();
        loadVisitas();
    }

    public void loadPersonas() {
        SwingWorker<List<Persona>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Persona> doInBackground() throws Exception {
                return apiClient.listarPersonas();
            }

            @Override
            protected void done() {
                try {
                    currentPersonas = get();
                    tableModelPersonas.setRowCount(0);
                    if (lblKpiTotalPersonas != null) {
                        lblKpiTotalPersonas.setText(String.valueOf(currentPersonas.size()));
                    }
                    for (Persona p : currentPersonas) {
                        tableModelPersonas.addRow(new Object[] {
                                p.getId(),
                                p.getDocIdentidad(),
                                p.getNombreCompleto(),
                                p.getEstadoAcceso() != null ? p.getEstadoAcceso().name() : "HABILITADO"
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    public void loadVisitas() {
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
                    int activosCount = 0;
                    for (Visita v : currentVisitas) {
                        if ("DENTRO".equalsIgnoreCase(v.getEstadoVisita() != null ? v.getEstadoVisita().name() : "")) {
                            activosCount++;
                        }
                        tableModelVisitas.addRow(new Object[] {
                                v.getId(),
                                v.getNombrePersona(),
                                v.getDocPersona(),
                                v.getTipoVisita() != null ? v.getTipoVisita().name() : "ESTANDAR",
                                v.getEstadoVisita() != null ? v.getEstadoVisita().name() : "PRE_REGISTRADA",
                                v.getMotivo() != null ? v.getMotivo() : "-"
                        });
                    }
                    if (lblHeroAccesosActivos != null) {
                        lblHeroAccesosActivos.setText(String.valueOf(activosCount));
                    }
                } catch (Exception ignored) {}
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
                    lblPersonaNombre.setText("Persona: " + p.getNombreCompleto());
                    lblPersonaDoc.setText("Doc: " + p.getDocIdentidad() + " | Email: " + p.getEmail());
                    lblVisitaEstado.setText("Estado: " + p.getEstadoAcceso());

                    if (gradientCardRef != null) {
                        gradientCardRef.updateState(
                                p.getNombreCompleto(),
                                p.getDocIdentidad(),
                                "",
                                p.getEstadoAcceso() != null ? p.getEstadoAcceso().name() : "HABILITADO",
                                p.getFotoUrl()
                        );
                    }

                    if (lblFotoConsulta != null) {
                        java.awt.image.BufferedImage img = com.acme.sica.infrastructure.adapter.in.gui.components.ImageUtils.fetchImage(p.getFotoUrl());
                        if (img != null) {
                            lblFotoConsulta.setIcon(new ImageIcon(img.getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
                        } else {
                            lblFotoConsulta.setIcon(com.acme.sica.infrastructure.adapter.in.gui.components.ImageUtils.createVectorAvatarIcon(60, 60));
                        }
                    }

                    loadIncidentesDePersona(p.getId());


                } catch (Exception e) {
                    lblPersonaNombre.setText("Persona: No encontrada");
                    lblPersonaDoc.setText("Doc: " + doc);
                    lblVisitaEstado.setText("Estado: -");
                }
            }
        };
        worker.execute();
    }

    private void loadIncidentesDePersona(Long personaId) {
        SwingWorker<List<Incidente>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Incidente> doInBackground() throws Exception {
                return apiClient.listarIncidentesPorPersona(personaId);
            }

            @Override
            protected void done() {
                try {
                    List<Incidente> incs = get();
                    tableModelPersonaIncidentes.setRowCount(0);
                    if (lblKpiTotalIncidentes != null) {
                        lblKpiTotalIncidentes.setText(String.valueOf(incs.size()));
                    }
                    for (Incidente inc : incs) {
                        tableModelPersonaIncidentes.addRow(new Object[]{
                                inc.getId(),
                                inc.getNivelGravedad() != null ? inc.getNivelGravedad().name() : "ALTO",
                                inc.getTitulo(),
                                inc.getFechaHora()
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void executeCheckIn() {
        int selectedRow = tblVisitas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una visita de la tabla de accesos para realizar Check-In.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long visitaId = (Long) tableModelVisitas.getValueAt(selectedRow, 0);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiClient.checkInVisita(visitaId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(GuardiaPanel.this,
                            "[+] CHECK-IN REALIZADO EXITOSAMENTE",
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);
                    loadVisitas();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void executeCheckOut() {
        int selectedRow = tblVisitas.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una visita activa para realizar Check-Out.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long visitaId = (Long) tableModelVisitas.getValueAt(selectedRow, 0);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiClient.checkOutVisita(visitaId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(GuardiaPanel.this,
                            "[+] CHECK-OUT REALIZADO EXITOSAMENTE",
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);
                    loadVisitas();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(GuardiaPanel.this, "Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void executeVisitaRapidaPrueba() {
        JTextField txtDoc = new JTextField();
        JTextField txtNom = new JTextField();
        JTextField txtMotivo = new JTextField("Entrega Rápida / Mensajería Express");

        JPanel panel = new JPanel(new java.awt.GridLayout(3, 2, 6, 6));
        panel.add(new JLabel("Documento (*):")); panel.add(txtDoc);
        panel.add(new JLabel("Nombre:")); panel.add(txtNom);
        panel.add(new JLabel("Motivo Visita:")); panel.add(txtMotivo);
        SicaTheme.applyDarkThemeRecursively(panel);

        int res = JOptionPane.showConfirmDialog(this, panel, "⚡ Registro de Visita Rápida / Express (Portería)", JOptionPane.OK_CANCEL_OPTION);

        if (res == JOptionPane.OK_OPTION) {
            String doc = txtDoc.getText().trim();
            String nom = txtNom.getText().trim();
            String mot = txtMotivo.getText().trim();

            if (doc.isEmpty()) doc = "EXPRESS-" + (System.currentTimeMillis() % 100000);
            if (nom.isEmpty()) nom = "Visitante Express";
            if (mot.isEmpty()) mot = "Visita Rápida / Entrega Express";

            final String finalDoc = doc;
            final String finalNom = nom;
            final String finalMot = mot;

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        apiClient.crearPersona(finalNom, "Express", finalDoc, "express@sica.local");
                    } catch (Exception ignored) {}
                    apiClient.registrarVisitaNoAnunciada(finalDoc, "[EXPRESS] " + finalMot);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "✅ Visita Rápida / Express registrada exitosamente.\n\nDocumento: " + finalDoc + "\nVisitante: " + finalNom, "Visita Rápida Creada", JOptionPane.INFORMATION_MESSAGE);
                        loadAllData();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(GuardiaPanel.this, "Error al registrar Visita Rápida: " + e.getMessage(), "Error Visita Rápida", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }


    private void openCrearPersonaDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame f ? f : null, "👤 Registrar Nueva Persona / Trabajador", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);

        AvatarPickerPanel avatarPicker = new AvatarPickerPanel();

        JTextField txtDoc = new JTextField(15);
        JTextField txtNom = new JTextField(15);
        JTextField txtApe = new JTextField(15);
        JTextField txtMail = new JTextField(15);

        JComboBox<String> comboEmpresas = new JComboBox<>();
        comboEmpresas.addItem("0 - Sin Empresa (Independiente)");
        try {
            List<Map<String, Object>> empresas = apiClient.listarEmpresas();
            for (Map<String, Object> e : empresas) {
                comboEmpresas.addItem(e.get("id") + " - " + e.get("nombre"));
            }
        } catch (Exception ignored) {}

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setOpaque(false);
        formPanel.add(new JLabel("Documento (*):")); formPanel.add(txtDoc);
        formPanel.add(new JLabel("Nombre (*):")); formPanel.add(txtNom);
        formPanel.add(new JLabel("Apellido (*):")); formPanel.add(txtApe);
        formPanel.add(new JLabel("Empresa Asociada:")); formPanel.add(comboEmpresas);
        formPanel.add(new JLabel("Email:")); formPanel.add(txtMail);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 12));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        centerPanel.setOpaque(false);

        JPanel avatarContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarContainer.setOpaque(false);
        avatarContainer.add(avatarPicker);

        centerPanel.add(avatarContainer, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);

        JLabel lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setForeground(new Color(239, 68, 68));
        lblError.setFont(SicaTheme.FONT_BOLD);
        centerPanel.add(lblError, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Aceptar");

        SicaTheme.styleButton(btnAceptar, SicaTheme.ACCENT_CYAN, Color.WHITE);
        SicaTheme.styleButton(btnCancelar, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnAceptar.addActionListener(e -> {
            String doc = txtDoc.getText().trim();
            String nom = txtNom.getText().trim();
            String ape = txtApe.getText().trim();
            String mail = txtMail.getText().trim();
            String fotoUrl = avatarPicker.getFotoUrl();

            txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

            boolean hasError = false;
            if (doc.isEmpty()) {
                txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (nom.isEmpty()) {
                txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (ape.isEmpty()) {
                txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }

            if (hasError) {
                lblError.setText("⚠️ Debe rellenar Documento, Nombre y Apellido para continuar");
                return;
            }

            String selEmp = (String) comboEmpresas.getSelectedItem();
            Long empId = null;
            if (selEmp != null && !selEmp.startsWith("0")) {
                try { empId = Long.parseLong(selEmp.split(" - ")[0]); } catch (Exception ignored) {}
            }

            final Long finalEmpId = empId;
            btnAceptar.setEnabled(false);
            lblError.setForeground(SicaTheme.ACCENT_CYAN);
            lblError.setText("Guardando persona...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.crearPersona(doc, "CC", nom, ape, mail, "", finalEmpId, fotoUrl);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dialog.dispose();
                        loadPersonas();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(
                                GuardiaPanel.this,
                                "[+] Persona Registrada: " + nom + " " + ape,
                                com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS
                        );
                    } catch (Exception ex) {
                        btnAceptar.setEnabled(true);
                        lblError.setForeground(new Color(239, 68, 68));
                        lblError.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnAceptar);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        SicaTheme.applyDarkThemeRecursively(dialog);
        dialog.setVisible(true);
    }

    private void openEditarPersonaDialog() {
        int row = tblPersonas.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una persona de la tabla para editar.", "Editar Persona", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String docSelect = (String) tableModelPersonas.getValueAt(row, 1);
        Long personaId = (Long) tableModelPersonas.getValueAt(row, 0);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "✏️ Editar Persona Registrada", true);
        dialog.setSize(480, 530);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JTextField txtDoc = new JTextField(15);
        txtDoc.setText(docSelect);
        txtDoc.setEditable(false);
        JTextField txtNom = new JTextField(15);
        JTextField txtApe = new JTextField(15);
        JTextField txtMail = new JTextField(15);

        JComboBox<String> comboEmpresas = new JComboBox<>();
        comboEmpresas.addItem("0 - Ninguna / Visitante Externo");
        try {
            List<Map<String, Object>> emps = apiClient.listarEmpresas();
            for (Map<String, Object> e : emps) {
                comboEmpresas.addItem(e.get("id") + " - " + e.get("nombre"));
            }
        } catch (Exception ignored) {}

        formPanel.add(new JLabel("Documento (Doc):")); formPanel.add(txtDoc);
        formPanel.add(new JLabel("Nombre: *")); formPanel.add(txtNom);
        formPanel.add(new JLabel("Apellido: *")); formPanel.add(txtApe);
        formPanel.add(new JLabel("Email:")); formPanel.add(txtMail);
        formPanel.add(new JLabel("Empresa / Filial:")); formPanel.add(comboEmpresas);

        com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel avatarPicker =
                new com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel();

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(avatarPicker, BorderLayout.CENTER);

        JLabel lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(SicaTheme.FONT_SMALL);
        lblError.setForeground(new Color(239, 68, 68));
        centerPanel.add(lblError, BorderLayout.SOUTH);

        SwingWorker<Persona, Void> loader = new SwingWorker<>() {
            @Override
            protected Persona doInBackground() throws Exception {
                return apiClient.buscarPersonaPorDoc(docSelect);
            }

            @Override
            protected void done() {
                try {
                    Persona p = get();
                    if (p != null) {
                        txtNom.setText(p.getNombre() != null ? p.getNombre() : "");
                        txtApe.setText(p.getApellido() != null ? p.getApellido() : "");
                        txtMail.setText(p.getEmail() != null ? p.getEmail() : "");
                        if (p.getFotoUrl() != null && !p.getFotoUrl().isEmpty()) {
                            avatarPicker.setFotoUrl(p.getFotoUrl());
                        }
                    }
                } catch (Exception ignored) {}
            }
        };
        loader.execute();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Guardar Cambios");

        SicaTheme.styleButton(btnAceptar, SicaTheme.ACCENT_CYAN, Color.WHITE);
        SicaTheme.styleButton(btnCancelar, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnAceptar.addActionListener(e -> {
            String nom = txtNom.getText().trim();
            String ape = txtApe.getText().trim();
            String mail = txtMail.getText().trim();
            String fotoUrl = avatarPicker.getFotoUrl();

            txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

            boolean hasError = false;
            if (nom.isEmpty()) {
                txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (ape.isEmpty()) {
                txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }

            if (hasError) {
                lblError.setText("⚠️ Debe rellenar Nombre y Apellido para continuar");
                return;
            }

            String selEmp = (String) comboEmpresas.getSelectedItem();
            Long empId = null;
            if (selEmp != null && !selEmp.startsWith("0")) {
                try { empId = Long.parseLong(selEmp.split(" - ")[0]); } catch (Exception ignored) {}
            }

            final Long finalEmpId = empId;
            btnAceptar.setEnabled(false);
            lblError.setForeground(SicaTheme.ACCENT_CYAN);
            lblError.setText("Actualizando datos...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.actualizarPersona(personaId, docSelect, "CC", nom, ape, mail, "", finalEmpId, fotoUrl);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dialog.dispose();
                        loadPersonas();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(
                                GuardiaPanel.this,
                                "[✏️] Persona Actualizada: " + nom + " " + ape,
                                com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS
                        );
                    } catch (Exception ex) {
                        btnAceptar.setEnabled(true);
                        lblError.setForeground(new Color(239, 68, 68));
                        lblError.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnAceptar);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        SicaTheme.applyDarkThemeRecursively(dialog);
        dialog.setVisible(true);
    }





    private void openVisitaNoAnunciadaDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame f ? f : null, "🚪 Registrar Visitante No Anunciado (WALKIN-01)", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(520, 570);
        dialog.setLocationRelativeTo(this);

        String initialDoc = txtSearchDoc != null ? txtSearchDoc.getText().trim() : "";
        JTextField txtDoc = new JTextField(initialDoc, 15);
        JTextField txtNom = new JTextField(15);
        JTextField txtApe = new JTextField(15);
        JTextField txtMail = new JTextField(15);
        JTextField txtMotivo = new JTextField("Reunión de Negocios No Anunciada", 15);

        JComboBox<String> comboFuncionarios = new JComboBox<>();
        try {
            List<Map<String, Object>> users = apiClient.listarUsuarios();
            for (Map<String, Object> u : users) {
                comboFuncionarios.addItem(u.get("id") + " - " + u.get("nombreCompleto") + " (" + u.get("rolNombre") + ")");
            }
        } catch (Exception ignored) {
            comboFuncionarios.addItem("3 - Funcionario Principal (func1)");
        }

        JPanel formGrid = new JPanel(new GridLayout(6, 2, 8, 8));
        formGrid.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        formGrid.setOpaque(false);

        formGrid.add(new JLabel("Documento (Doc) (*):")); formGrid.add(txtDoc);
        formGrid.add(new JLabel("Nombre del Invitado (*):")); formGrid.add(txtNom);
        formGrid.add(new JLabel("Apellido del Invitado (*):")); formGrid.add(txtApe);
        formGrid.add(new JLabel("Email Invitado:")); formGrid.add(txtMail);
        formGrid.add(new JLabel("A quién visita (Anfitrión) (*):")); formGrid.add(comboFuncionarios);
        formGrid.add(new JLabel("Motivo de Visita (*):")); formGrid.add(txtMotivo);

        com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel avatarPicker =
                new com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel();

        txtDoc.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String d = txtDoc.getText().trim();
                if (!d.isEmpty()) {
                    SwingWorker<Persona, Void> loader = new SwingWorker<>() {
                        @Override
                        protected Persona doInBackground() throws Exception {
                            return apiClient.buscarPersonaPorDoc(d);
                        }

                        @Override
                        protected void done() {
                            try {
                                Persona p = get();
                                if (p != null) {
                                    txtNom.setText(p.getNombre() != null ? p.getNombre() : "");
                                    txtApe.setText(p.getApellido() != null ? p.getApellido() : "");
                                    txtMail.setText(p.getEmail() != null ? p.getEmail() : "");
                                    if (p.getFotoUrl() != null && !p.getFotoUrl().isEmpty()) {
                                        avatarPicker.setFotoUrl(p.getFotoUrl());
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    };
                    loader.execute();
                }
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        centerPanel.add(formGrid, BorderLayout.NORTH);
        centerPanel.add(avatarPicker, BorderLayout.CENTER);

        JLabel lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(SicaTheme.FONT_SMALL);
        lblError.setForeground(new Color(239, 68, 68));
        centerPanel.add(lblError, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Crear Visita No Anunciada");

        SicaTheme.styleButton(btnAceptar, SicaTheme.ACCENT_CYAN, Color.WHITE);
        SicaTheme.styleButton(btnCancelar, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnAceptar.addActionListener(e -> {
            String doc = txtDoc.getText().trim();
            String nom = txtNom.getText().trim();
            String ape = txtApe.getText().trim();
            String mail = txtMail.getText().trim();
            String mot = txtMotivo.getText().trim();
            String fotoUrl = avatarPicker.getFotoUrl();

            txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtMotivo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

            boolean hasError = false;
            if (doc.isEmpty()) {
                txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (nom.isEmpty()) {
                txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (ape.isEmpty()) {
                txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (mot.isEmpty()) {
                txtMotivo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }

            if (hasError) {
                lblError.setText("⚠️ Debe rellenar Documento, Nombre, Apellido y Motivo para continuar");
                return;
            }

            String selFunc = (String) comboFuncionarios.getSelectedItem();
            Long funcId = 3L;
            if (selFunc != null) {
                try { funcId = Long.parseLong(selFunc.split(" - ")[0]); } catch (Exception ignored) {}
            }

            final Long finalFuncId = funcId;
            btnAceptar.setEnabled(false);
            lblError.setForeground(SicaTheme.ACCENT_CYAN);
            lblError.setText("Registrando visita no anunciada...");

            SwingWorker<Visita, Void> worker = new SwingWorker<>() {
                @Override
                protected Visita doInBackground() throws Exception {
                    return apiClient.registrarVisitaNoAnunciada(doc, nom, ape, mail, finalFuncId, mot, fotoUrl);
                }

                @Override
                protected void done() {
                    try {
                        Visita v = get();
                        dialog.dispose();
                        loadVisitas();
                        loadPersonas();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(
                                GuardiaPanel.this,
                                "[🚪] Visita No Anunciada Creada (#" + v.getId() + " - Pendiente de Aprobación)",
                                com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS
                        );
                    } catch (Exception ex) {
                        btnAceptar.setEnabled(true);
                        lblError.setForeground(new Color(239, 68, 68));
                        lblError.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnAceptar);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        SicaTheme.applyDarkThemeRecursively(dialog);
        dialog.setVisible(true);
    }


    private void openPaseTemporalDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame f ? f : null, "🪪 Pase Temporal por Olvido de Carnet (FORGET-01)", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(520, 570);
        dialog.setLocationRelativeTo(this);

        String initialDoc = txtSearchDoc != null ? txtSearchDoc.getText().trim() : "";
        JTextField txtDoc = new JTextField(initialDoc, 15);
        JTextField txtNom = new JTextField(15);
        JTextField txtApe = new JTextField(15);
        JTextField txtMail = new JTextField(15);
        JTextField txtMotivo = new JTextField("Pase Temporal por Olvido de Carnet Físico", 15);

        JComboBox<String> comboFuncionarios = new JComboBox<>();
        try {
            List<Map<String, Object>> users = apiClient.listarUsuarios();
            for (Map<String, Object> u : users) {
                comboFuncionarios.addItem(u.get("id") + " - " + u.get("nombreCompleto") + " (" + u.get("rolNombre") + ")");
            }
        } catch (Exception ignored) {
            comboFuncionarios.addItem("3 - Funcionario Principal (func1)");
        }

        JPanel formGrid = new JPanel(new GridLayout(6, 2, 8, 8));
        formGrid.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        formGrid.setOpaque(false);

        formGrid.add(new JLabel("Documento Trabajador (*):")); formGrid.add(txtDoc);
        formGrid.add(new JLabel("Nombre (*):")); formGrid.add(txtNom);
        formGrid.add(new JLabel("Apellido (*):")); formGrid.add(txtApe);
        formGrid.add(new JLabel("Email:")); formGrid.add(txtMail);
        formGrid.add(new JLabel("A quién notifica (Jefe/Anfitrión) (*):")); formGrid.add(comboFuncionarios);
        formGrid.add(new JLabel("Motivo de Ingreso (*):")); formGrid.add(txtMotivo);

        com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel avatarPicker =
                new com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel();

        txtDoc.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String d = txtDoc.getText().trim();
                if (!d.isEmpty()) {
                    SwingWorker<Persona, Void> loader = new SwingWorker<>() {
                        @Override
                        protected Persona doInBackground() throws Exception {
                            return apiClient.buscarPersonaPorDoc(d);
                        }

                        @Override
                        protected void done() {
                            try {
                                Persona p = get();
                                if (p != null) {
                                    txtNom.setText(p.getNombre() != null ? p.getNombre() : "");
                                    txtApe.setText(p.getApellido() != null ? p.getApellido() : "");
                                    txtMail.setText(p.getEmail() != null ? p.getEmail() : "");
                                    if (p.getFotoUrl() != null && !p.getFotoUrl().isEmpty()) {
                                        avatarPicker.setFotoUrl(p.getFotoUrl());
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    };
                    loader.execute();
                }
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        centerPanel.add(formGrid, BorderLayout.NORTH);
        centerPanel.add(avatarPicker, BorderLayout.CENTER);

        JLabel lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(SicaTheme.FONT_SMALL);
        lblError.setForeground(new Color(239, 68, 68));
        centerPanel.add(lblError, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Emitir Pase por Olvido");

        SicaTheme.styleButton(btnAceptar, SicaTheme.ACCENT_CYAN, Color.WHITE);
        SicaTheme.styleButton(btnCancelar, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnAceptar.addActionListener(e -> {
            String doc = txtDoc.getText().trim();
            String nom = txtNom.getText().trim();
            String ape = txtApe.getText().trim();
            String mail = txtMail.getText().trim();
            String mot = txtMotivo.getText().trim();
            String fotoUrl = avatarPicker.getFotoUrl();

            txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtMotivo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

            boolean hasError = false;
            if (doc.isEmpty()) {
                txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (nom.isEmpty()) {
                txtNom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (ape.isEmpty()) {
                txtApe.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (mot.isEmpty()) {
                txtMotivo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }

            if (hasError) {
                lblError.setText("⚠️ Debe rellenar Documento, Nombre, Apellido y Motivo para continuar");
                return;
            }

            String selFunc = (String) comboFuncionarios.getSelectedItem();
            Long funcId = 3L;
            if (selFunc != null) {
                try { funcId = Long.parseLong(selFunc.split(" - ")[0]); } catch (Exception ignored) {}
            }

            final Long finalFuncId = funcId;
            btnAceptar.setEnabled(false);
            lblError.setForeground(SicaTheme.ACCENT_CYAN);
            lblError.setText("Emitiendo pase por olvido...");

            SwingWorker<Visita, Void> worker = new SwingWorker<>() {
                @Override
                protected Visita doInBackground() throws Exception {
                    return apiClient.emitirPaseTemporal(doc, nom, ape, mail, finalFuncId, mot, fotoUrl);
                }

                @Override
                protected void done() {
                    try {
                        Visita v = get();
                        dialog.dispose();
                        loadVisitas();
                        loadPersonas();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(
                                GuardiaPanel.this,
                                "[🪪] Pase Temporal Creado (#" + v.getId() + " - Pendiente Aprobación por Olvido)",
                                com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS
                        );
                    } catch (Exception ex) {
                        btnAceptar.setEnabled(true);
                        lblError.setForeground(new Color(239, 68, 68));
                        lblError.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnAceptar);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        SicaTheme.applyDarkThemeRecursively(dialog);
        dialog.setVisible(true);
    }


    private void executeEliminarPersona() {
        int row = tblPersonas.getSelectedRow();
        if (row == -1) return;
        Long id = (Long) tableModelPersonas.getValueAt(row, 0);

        boolean conf = CriticalConfirmationDialog.showConfirm(SwingUtilities.getWindowAncestor(this),
                "ELIMINAR PERSONA",
                "¿Estás seguro de eliminar a la persona seleccionada del registro?",
                "ELIMINAR");
        if (conf) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.eliminarPersona(id);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadPersonas();
                    } catch (Exception ignored) {}
                }
            };
            worker.execute();
        }
    }

    private void executeLimpiarVisitas() {
        boolean confirm = CriticalConfirmationDialog.showConfirm(SwingUtilities.getWindowAncestor(this),
                "LIMPIAR HISTORIAL",
                "¿Estás seguro de vaciar todo el historial de visitas registradas?",
                "VACIAR REGISTROS");
        if (confirm) {
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
                        loadVisitas();
                    } catch (Exception ignored) {}
                }
            };
            worker.execute();
        }
    }

    private static class StatusPillCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));

            String str = value != null ? value.toString() : "";
            if ("DENTRO".equalsIgnoreCase(str)) {
                label.setText("ACCESO CONCEDIDO");
                label.setBackground(SicaTheme.STATUS_GRANTED_BG);
                label.setForeground(SicaTheme.STATUS_GRANTED_TEXT);
            } else if ("FINALIZADO".equalsIgnoreCase(str) || "CERRADO_POR_SISTEMA".equalsIgnoreCase(str)) {
                label.setText("CERRADO");
                label.setBackground(SicaTheme.CARD_BG_ALT);
                label.setForeground(SicaTheme.TEXT_MUTED);
            } else if ("RESTRINGIDO".equalsIgnoreCase(str) || "BLOQUEADO".equalsIgnoreCase(str)) {
                label.setText("ACCESO DENEGADO");
                label.setBackground(SicaTheme.STATUS_DENIED_BG);
                label.setForeground(SicaTheme.STATUS_DENIED_TEXT);
            } else if ("HABILITADO".equalsIgnoreCase(str) || "APROBADO".equalsIgnoreCase(str) || "ACTIVO".equalsIgnoreCase(str)) {
                label.setText(str);
                label.setBackground(SicaTheme.STATUS_GRANTED_BG);
                label.setForeground(SicaTheme.STATUS_GRANTED_TEXT);
            } else if ("PENDIENTE_APROBACION".equalsIgnoreCase(str) || "PENDIENTE".equalsIgnoreCase(str) || "PRE_REGISTRADA".equalsIgnoreCase(str)) {
                label.setText("PENDIENTE");
                label.setBackground(SicaTheme.STATUS_WARNING_BG);
                label.setForeground(SicaTheme.STATUS_WARNING_TEXT);
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
