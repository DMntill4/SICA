package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;
import com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme;
import com.acme.sica.infrastructure.adapter.in.gui.components.AvatarPickerPanel;


import javax.swing.*;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FuncionarioPanel extends JPanel {


    private final SicaApiClient apiClient;

    private JComboBox<String> comboPersonas;
    private JTextField txtMotivo;
    private JButton btnPreregistrar;
    private JButton btnCrearPersona;

    private JTable tblPendientes;
    private DefaultTableModel tableModel;
    private JTable tblPasesWeb;
    private DefaultTableModel tableModelPasesWeb;
    private JTable tblMisPreregistros;
    private DefaultTableModel tableModelPreregistros;

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
        setBackground(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.BG_DARK);

        // PANEL CONTENEDOR SUPERIOR: Banner + Formulario Pre-Registro
        JPanel topContainer = new JPanel(new BorderLayout(6, 6));
        topContainer.setOpaque(false);

        // BANNER GUÍA
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.CARD_BG_ALT);
        bannerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel lblHelp = new JLabel("[i] MÓDULO DE FUNCIONARIO: Pre-registra invitados para que su ingreso sea directo o aprueba/rechaza solicitudes de personas no anunciadas en tiempo real.");
        lblHelp.setFont(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.FONT_BOLD);
        lblHelp.setForeground(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.TEXT_MAIN);
        bannerPanel.add(lblHelp, BorderLayout.CENTER);
        topContainer.add(bannerPanel, BorderLayout.NORTH);

        // FORMULARIO PRE-REGISTRO ORGANIZADO EN 2 FILAS INDEPENDIENTES
        JPanel formPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        formPanel.setBackground(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.CARD_BG);
        formPanel.setBorder(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.createCardBorder("Pre-Registrar Invitado Aprobado"));

        // Fila 1: Campos de Selección
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.setOpaque(false);
        comboPersonas = new JComboBox<>();
        comboPersonas.setPreferredSize(new Dimension(280, 28));

        txtMotivo = new JTextField("Reunión de Negocios y Consultoría", 22);
        txtMotivo.setFont(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.FONT_BODY);

        JLabel lblInv = new JLabel("Seleccionar Invitado:");
        lblInv.setForeground(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.TEXT_MUTED);
        JLabel lblMot = new JLabel("Motivo:");
        lblMot.setForeground(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.TEXT_MUTED);

        row1.add(lblInv);
        row1.add(comboPersonas);
        row1.add(lblMot);
        row1.add(txtMotivo);

        // Fila 2: Botones de Acción
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.setOpaque(false);
        btnPreregistrar = new JButton("Pre-Registrar Visita");
        SicaTheme.styleButton(btnPreregistrar, SicaTheme.ACCENT_CYAN, Color.WHITE);

        btnCrearPersona = new JButton("Registrar Nueva Persona");
        SicaTheme.styleButton(btnCrearPersona, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MAIN);

        btnPreregistrar.addActionListener(e -> executePreregistro());
        btnCrearPersona.addActionListener(e -> openCrearPersonaDialog());


        row2.add(btnPreregistrar);
        row2.add(btnCrearPersona);

        formPanel.add(row1);
        formPanel.add(row2);

        topContainer.add(formPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- PANEL CENTRAL: Solicitudes Pendientes (Internas + Portal Web IA) ---
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.CARD_BG);
        centerPanel.setBorder(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.createCardBorder("Solicitudes Pendientes de Aprobación"));

        JTabbedPane tabbedPendientes = new JTabbedPane();
        tabbedPendientes.setFont(com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.FONT_BOLD);

        String[] columns = {"ID Visita", "Persona / Visitante", "Documento", "Tipo Visita", "Estado", "Motivo / Detalle"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPendientes = new JTable(tableModel);
        com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.styleTable(tblPendientes);
        JScrollPane scrollPane = new JScrollPane(tblPendientes);
        tabbedPendientes.addTab("[📋] Pendientes de Mi Aprobación", scrollPane);

        // Tabla 2: Mis Visitas Pre-Registradas (Enviadas a Portería)
        String[] colsPrereg = {"ID Visita", "Persona / Visitante", "Documento", "Tipo Visita", "Estado Visita", "Motivo"};
        tableModelPreregistros = new DefaultTableModel(colsPrereg, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblMisPreregistros = new JTable(tableModelPreregistros);
        com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.styleTable(tblMisPreregistros);
        JScrollPane scrollPrereg = new JScrollPane(tblMisPreregistros);
        tabbedPendientes.addTab("[📅] Mis Visitas Pre-Registradas", scrollPrereg);

        // Tabla 3: Pases Web Biométricos
        String[] colsWeb = {"ID Pase", "Nombre Completo", "Documento", "Empresa Destino", "Biometría IA", "Motivo"};
        tableModelPasesWeb = new DefaultTableModel(colsWeb, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPasesWeb = new JTable(tableModelPasesWeb);
        com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.styleTable(tblPasesWeb);
        JScrollPane scrollWeb = new JScrollPane(tblPasesWeb);
        tabbedPendientes.addTab("[🌐] Solicitudes Portal Web (Biometría IA)", scrollWeb);


        centerPanel.add(tabbedPendientes, BorderLayout.CENTER);

        // BOTONERA APROBACIONES
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        actionPanel.setOpaque(false);
        btnRefresh = new JButton("[R] Actualizar Tabla");
        com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.styleButton(btnRefresh, com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.CARD_BG_ALT, com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.ACCENT_CYAN);

        btnAprobar = new JButton("[+] APROBAR VISITA / PASE WEB");
        com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.styleButton(btnAprobar, com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.ACCENT_EMERALD, Color.WHITE);
        btnAprobar.setPreferredSize(new Dimension(240, 32));

        btnRechazar = new JButton("[x] RECHAZAR SOLICITUD");
        com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.styleButton(btnRechazar, com.acme.sica.infrastructure.adapter.in.gui.theme.SicaTheme.ACCENT_ROSE, Color.WHITE);
        btnRechazar.setPreferredSize(new Dimension(200, 32));

        btnRefresh.addActionListener(e -> loadAll());
        btnAprobar.addActionListener(e -> executeAprobarGenerico());
        btnRechazar.addActionListener(e -> executeRechazarGenerico());

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
                    if (tableModelPreregistros != null) tableModelPreregistros.setRowCount(0);

                    for (Visita v : allVisitas) {
                        if (v.getEstadoVisita().name().contains("PENDIENTE")) {
                            tableModel.addRow(new Object[]{
                                    v.getId(),
                                    v.getPersonaNombreCompleto(),
                                    v.getPersonaDocIdentidad(),
                                    v.getTipoVisita(),
                                    v.getEstadoVisita(),
                                    v.getMotivo()
                            });
                        } else if (tableModelPreregistros != null) {
                            tableModelPreregistros.addRow(new Object[]{
                                    v.getId(),
                                    v.getPersonaNombreCompleto(),
                                    v.getPersonaDocIdentidad(),
                                    v.getTipoVisita(),
                                    v.getEstadoVisita(),
                                    v.getMotivo()
                            });
                        }
                    }

                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();

        // Cargar Solicitudes de Pases Web (Biometría IA)
        SwingWorker<List<Map<String, Object>>, Void> webWorker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return apiClient.listarSolicitudesPasePendientes();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> pases = get();
                    tableModelPasesWeb.setRowCount(0);
                    for (Map<String, Object> p : pases) {
                        String bioStatus = p.get("vectorBiometrico") != null ? "[✓] REGISTRADA (128-d Vector)" : "[x] SIN ROSTRO";
                        tableModelPasesWeb.addRow(new Object[]{
                                p.get("id"),
                                p.get("nombreCompleto"),
                                p.get("docIdentidad"),
                                p.get("empresaDestino"),
                                bioStatus,
                                p.get("motivo")
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        };
        webWorker.execute();
    }

    private void executeAprobarGenerico() {
        if (tblPasesWeb.getSelectedRow() != -1) {
            int row = tblPasesWeb.getSelectedRow();
            Long paseId = Long.valueOf(tableModelPasesWeb.getValueAt(row, 0).toString());

            SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
                @Override
                protected Map<String, Object> doInBackground() throws Exception {
                    return apiClient.aprobarSolicitudPase(paseId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(FuncionarioPanel.this,
                                "[+] Pase Web Biométrico Aprobado para Portería",
                                com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);
                        JOptionPane.showMessageDialog(FuncionarioPanel.this,
                                "Pase Web Biométrico Aprobado Exitosamente.\nLa persona y visita han sido registradas en portería para Check-In.",
                                "Pase Aprobado", JOptionPane.INFORMATION_MESSAGE);
                        loadPendientes();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(FuncionarioPanel.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
            return;
        }

        executeAprobar();
    }

    private void executeRechazarGenerico() {
        if (tblPasesWeb.getSelectedRow() != -1) {
            int row = tblPasesWeb.getSelectedRow();
            Long paseId = Long.valueOf(tableModelPasesWeb.getValueAt(row, 0).toString());

            SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
                @Override
                protected Map<String, Object> doInBackground() throws Exception {
                    return apiClient.rechazarSolicitudPase(paseId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(FuncionarioPanel.this,
                                "[x] Solicitud de Pase Web Rechazada",
                                com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.ERROR);
                        loadPendientes();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(FuncionarioPanel.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
            return;
        }

        executeRechazar();
    }


    private void executePreregistro() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame f ? f : null, "📅 Pre-Registrar Visita (Ingreso Autorizado)", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);

        AvatarPickerPanel avatarPicker = new AvatarPickerPanel();

        JComboBox<String> comboInvitados = new JComboBox<>();
        if (personasCache != null) {
            for (Persona p : personasCache) {
                comboInvitados.addItem(p.getId() + " - " + p.getNombre() + " " + p.getApellido() + " (" + p.getDocIdentidad() + ")");
            }
        }

        Runnable updateAvatarFromSelection = () -> {
            String sel = (String) comboInvitados.getSelectedItem();
            if (sel != null && personasCache != null) {
                try {
                    Long selectedId = Long.parseLong(sel.split(" - ")[0]);
                    for (Persona p : personasCache) {
                        if (p.getId().equals(selectedId)) {
                            if (p.getFotoUrl() != null && !p.getFotoUrl().trim().isEmpty()) {
                                avatarPicker.setFotoUrl(p.getFotoUrl());
                            } else {
                                avatarPicker.restablecerAvatarPredeterminado();
                            }
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
        };

        comboInvitados.addActionListener(e -> updateAvatarFromSelection.run());
        updateAvatarFromSelection.run();

        String fechaDefecto = LocalDateTime.now().plusHours(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        JTextField txtFechaHora = new JTextField(fechaDefecto, 16);

        JTextField txtMotivoVisita = new JTextField("Reunión de Negocios y Consultoría", 16);

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        formGrid.setOpaque(false);
        formGrid.add(new JLabel("Seleccionar Invitado (*):")); formGrid.add(comboInvitados);
        formGrid.add(new JLabel("Fecha / Hora Visita (*):")); formGrid.add(txtFechaHora);
        formGrid.add(new JLabel("Motivo de Visita (*):")); formGrid.add(txtMotivoVisita);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 12));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        centerPanel.setOpaque(false);

        JPanel avatarContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarContainer.setOpaque(false);
        avatarContainer.add(avatarPicker);

        centerPanel.add(avatarContainer, BorderLayout.NORTH);
        centerPanel.add(formGrid, BorderLayout.CENTER);

        JLabel lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setForeground(new Color(239, 68, 68));
        lblError.setFont(SicaTheme.FONT_BOLD);
        centerPanel.add(lblError, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAceptar = new JButton("Aceptar y Guardar");

        SicaTheme.styleButton(btnAceptar, SicaTheme.ACCENT_CYAN, Color.WHITE);
        SicaTheme.styleButton(btnCancelar, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnAceptar.addActionListener(e -> {
            if (comboInvitados.getSelectedItem() == null) {
                lblError.setText("⚠️ Debe seleccionar un invitado válido");
                return;
            }
            String mot = txtMotivoVisita.getText().trim();
            String fecStr = txtFechaHora.getText().trim();

            if (mot.isEmpty() || fecStr.isEmpty()) {
                lblError.setText("⚠️ Complete la fecha/hora y motivo de la visita");
                return;
            }

            LocalDateTime fecha;
            try {
                fecha = LocalDateTime.parse(fecStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception ex) {
                lblError.setText("⚠️ Formato de fecha inválido. Ejemplo: 2026-08-29 14:00");
                return;
            }

            String sel = (String) comboInvitados.getSelectedItem();
            Long personaId = Long.parseLong(sel.split(" - ")[0]);

            btnAceptar.setEnabled(false);
            lblError.setForeground(SicaTheme.ACCENT_CYAN);
            lblError.setText("Enviando pre-registro a portería...");

            SwingWorker<Visita, Void> worker = new SwingWorker<>() {
                @Override
                protected Visita doInBackground() throws Exception {
                    return apiClient.preregistrarVisita(personaId, mot, fecha);
                }

                @Override
                protected void done() {
                    try {
                        Visita v = get();
                        dialog.dispose();
                        loadAll();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(
                                FuncionarioPanel.this,
                                "[+] Visita Pre-Registrada Exitosamente (ID #" + v.getId() + ")",
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
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(FuncionarioPanel.this,
                            "[+] Visita Aprobada (ID #" + v.getId() + ")",
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.SUCCESS);
                    JOptionPane.showMessageDialog(FuncionarioPanel.this, "Visita ID #" + v.getId() + " APROBADA.\nEl guardia ya tiene la autorización de ingreso.", "Aprobada", JOptionPane.INFORMATION_MESSAGE);
                    loadPendientes();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(FuncionarioPanel.this,
                            "[x] Error al Aprobar: " + cause.getMessage(),
                            com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.ToastType.ERROR);
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
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame f ? f : null, "👤 Registrar Nuevo Trabajador / Persona", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        AvatarPickerPanel avatarPicker = new AvatarPickerPanel();

        JTextField txtDoc = new JTextField(15);
        JComboBox<String> comboTipoDoc = new JComboBox<>(new String[]{"CC", "CE", "PASAPORTE", "TI"});
        JTextField txtNombre = new JTextField(15);
        JTextField txtApellido = new JTextField(15);
        JTextField txtEmail = new JTextField(15);
        JTextField txtTel = new JTextField(15);

        JComboBox<String> comboEmpresas = new JComboBox<>();
        comboEmpresas.addItem("0 - Sin Empresa (Independiente / Visitante)");
        try {
            List<Map<String, Object>> empresas = apiClient.listarEmpresas();
            for (Map<String, Object> e : empresas) {
                comboEmpresas.addItem(e.get("id") + " - " + e.get("nombre"));
            }
        } catch (Exception ignored) {}

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        formPanel.setOpaque(false);
        formPanel.add(new JLabel("Documento (*):")); formPanel.add(txtDoc);
        formPanel.add(new JLabel("Tipo Documento:")); formPanel.add(comboTipoDoc);
        formPanel.add(new JLabel("Nombre (*):")); formPanel.add(txtNombre);
        formPanel.add(new JLabel("Apellido (*):")); formPanel.add(txtApellido);
        formPanel.add(new JLabel("Empresa Asociada:")); formPanel.add(comboEmpresas);
        formPanel.add(new JLabel("Email:")); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Teléfono:")); formPanel.add(txtTel);

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
        JButton btnAceptar = new JButton("Aceptar y Guardar");

        SicaTheme.styleButton(btnAceptar, SicaTheme.ACCENT_CYAN, Color.WHITE);
        SicaTheme.styleButton(btnCancelar, SicaTheme.CARD_BG_ALT, SicaTheme.TEXT_MUTED);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnAceptar.addActionListener(e -> {
            String doc = txtDoc.getText().trim();
            String tipo = (String) comboTipoDoc.getSelectedItem();
            String nom = txtNombre.getText().trim();
            String ape = txtApellido.getText().trim();
            String email = txtEmail.getText().trim();
            String tel = txtTel.getText().trim();
            String fotoUrl = avatarPicker.getFotoUrl();

            txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtNombre.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            txtApellido.setBorder(BorderFactory.createCompoundBorder(new LineBorder(SicaTheme.BORDER_SUBTLE, 1, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

            boolean hasError = false;
            if (doc.isEmpty()) {
                txtDoc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (nom.isEmpty()) {
                txtNombre.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                hasError = true;
            }
            if (ape.isEmpty()) {
                txtApellido.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(239, 68, 68), 2, true), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
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
            lblError.setText("Guardando persona con fotografía...");

            SwingWorker<Persona, Void> worker = new SwingWorker<>() {
                @Override
                protected Persona doInBackground() throws Exception {
                    return apiClient.crearPersona(doc, tipo, nom, ape, email, tel, finalEmpId, fotoUrl);
                }

                @Override
                protected void done() {
                    try {
                        Persona p = get();
                        dialog.dispose();
                        loadAll();
                        com.acme.sica.infrastructure.adapter.in.gui.components.ToastNotificationManager.showToast(
                                FuncionarioPanel.this,
                                "[+] Persona Registrada Exitosamente: " + p.getNombre() + " " + p.getApellido(),
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



}
