package com.acme.sica.infrastructure.adapter.in.gui.views;

import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportesPanel extends JPanel {

    private final SicaApiClient apiClient;

    private JComboBox<String> comboTipoReporte;
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JTextField txtFiltroTexto;
    private JButton btnGenerar;
    private JButton btnExportarCSV;

    private JLabel lblTotalRegistros;
    private JLabel lblVisitasDentro;
    private JLabel lblIncidentesCriticos;

    private JTable tblReporte;
    private DefaultTableModel tableModel;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportesPanel(SicaApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
        executeGenerarReporte();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // --- TOP: PANEL DE FILTROS ---
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(new TitledBorder("📊 Filtros de Reportes e Históricos (Stream API - REP-01 a REP-08)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        comboTipoReporte = new JComboBox<>(new String[]{
                "1. 🟢 Aforo Actual (Personas Dentro) [REP-01]",
                "2. 👤 Histórico de Accesos por Persona [REP-02]",
                "3. 🏢 Histórico de Accesos por Empresa [REP-03]",
                "4. 🚨 Reporte de Incidentes de Seguridad [REP-06]"
        });
        comboTipoReporte.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtFechaInicio = new JTextField(LocalDateTime.now().minusDays(30).format(formatter), 12);
        txtFechaFin = new JTextField(LocalDateTime.now().plusDays(1).format(formatter), 12);
        txtFiltroTexto = new JTextField("", 15);

        btnGenerar = new JButton("🔍 Generar Reporte");
        btnGenerar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerar.setBackground(new Color(14, 165, 233));
        btnGenerar.setForeground(Color.WHITE);
        btnGenerar.addActionListener(e -> executeGenerarReporte());

        btnExportarCSV = new JButton("📥 Exportar CSV / Excel [REP-07]");
        btnExportarCSV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportarCSV.setBackground(new Color(16, 185, 129));
        btnExportarCSV.setForeground(Color.WHITE);
        btnExportarCSV.addActionListener(e -> executeExportarCSV());

        gbc.gridx = 0; gbc.gridy = 0; filterPanel.add(new JLabel("Tipo de Reporte:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; filterPanel.add(comboTipoReporte, gbc);

        gbc.gridx = 2; gbc.gridy = 0; filterPanel.add(new JLabel("Filtro Persona / Empresa:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; filterPanel.add(txtFiltroTexto, gbc);

        gbc.gridx = 0; gbc.gridy = 1; filterPanel.add(new JLabel("Fecha Inicio (yyyy-MM-dd HH:mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; filterPanel.add(txtFechaInicio, gbc);

        gbc.gridx = 2; gbc.gridy = 1; filterPanel.add(new JLabel("Fecha Fin (yyyy-MM-dd HH:mm):"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; filterPanel.add(txtFechaFin, gbc);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBar.add(btnGenerar);
        btnBar.add(btnExportarCSV);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; filterPanel.add(btnBar, gbc);

        add(filterPanel, BorderLayout.NORTH);

        // --- CENTER: TABLA DE RESULTADOS ---
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblReporte = new JTable(tableModel);
        tblReporte.setRowHeight(26);
        tblReporte.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        add(new JScrollPane(tblReporte), BorderLayout.CENTER);

        // --- SOUTH: BARRA DE MÉTRICAS KPI ---
        JPanel kpiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        kpiPanel.setBackground(new Color(15, 23, 42));

        lblTotalRegistros = new JLabel("📊 Total Registros: 0");
        lblTotalRegistros.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalRegistros.setForeground(Color.WHITE);

        lblVisitasDentro = new JLabel("🟢 Personas Dentro: 0");
        lblVisitasDentro.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblVisitasDentro.setForeground(new Color(56, 189, 248));

        lblIncidentesCriticos = new JLabel("🚨 Incidentes Registrados: 0");
        lblIncidentesCriticos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIncidentesCriticos.setForeground(new Color(248, 113, 113));

        kpiPanel.add(lblTotalRegistros);
        kpiPanel.add(lblVisitasDentro);
        kpiPanel.add(lblIncidentesCriticos);

        add(kpiPanel, BorderLayout.SOUTH);
    }

    private void executeGenerarReporte() {
        int indexTipo = comboTipoReporte.getSelectedIndex();
        String filtroText = txtFiltroTexto.getText().trim().toLowerCase();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (indexTipo == 0) {
                    // REP-01: Aforo Actual (Personas Dentro)
                    Map<String, Object> data = apiClient.getPersonasDentro();
                    Object personasObj = data.get("personas");
                    ObjectMapper mapper = com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils.objectMapper;
                    List<Visita> dentro = new ArrayList<>();
                    if (personasObj instanceof List) {
                        for (Object item : (List<?>) personasObj) {
                            dentro.add(mapper.convertValue(item, Visita.class));
                        }
                    }


                    List<Visita> filtradas = dentro.stream()
                            .filter(v -> filtroText.isEmpty() ||
                                    (v.getPersonaNombreCompleto() != null && v.getPersonaNombreCompleto().toLowerCase().contains(filtroText)) ||
                                    (v.getPersonaDocIdentidad() != null && v.getPersonaDocIdentidad().toLowerCase().contains(filtroText)))
                            .collect(Collectors.toList());

                    SwingUtilities.invokeLater(() -> renderTablaVisitas(filtradas, "Aforo Actual (Personas Dentro)"));

                } else if (indexTipo == 1 || indexTipo == 2) {
                    // REP-02 & REP-03: Histórico por Persona / Empresa
                    List<Visita> todas = apiClient.listarVisitas();

                    List<Visita> filtradas = todas.stream()
                            .filter(v -> filtroText.isEmpty() ||
                                    (v.getPersonaNombreCompleto() != null && v.getPersonaNombreCompleto().toLowerCase().contains(filtroText)) ||
                                    (v.getPersonaDocIdentidad() != null && v.getPersonaDocIdentidad().toLowerCase().contains(filtroText)) ||
                                    (v.getMotivo() != null && v.getMotivo().toLowerCase().contains(filtroText)))
                            .sorted((v1, v2) -> v2.getId().compareTo(v1.getId()))
                            .collect(Collectors.toList());

                    SwingUtilities.invokeLater(() -> renderTablaVisitas(filtradas, indexTipo == 1 ? "Histórico por Persona" : "Histórico por Empresa"));

                } else if (indexTipo == 3) {
                    // REP-06: Reporte de Incidentes de Seguridad
                    List<Incidente> incidentes = apiClient.listarIncidentes();

                    List<Incidente> filtrados = incidentes.stream()
                            .filter(i -> filtroText.isEmpty() ||
                                    (i.getTitulo() != null && i.getTitulo().toLowerCase().contains(filtroText)) ||
                                    (i.getDescripcion() != null && i.getDescripcion().toLowerCase().contains(filtroText)))
                            .sorted((i1, i2) -> i2.getId().compareTo(i1.getId()))
                            .collect(Collectors.toList());

                    SwingUtilities.invokeLater(() -> renderTablaIncidentes(filtrados));
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReportesPanel.this, "Error al generar reporte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void renderTablaVisitas(List<Visita> lista, String tituloModulo) {
        tableModel.setColumnIdentifiers(new String[]{
                "ID Visita", "Persona / Visitante", "Documento", "Motivo / Detalle", "Tipo Visita", "Estado", "Ingreso", "Salida"
        });
        tableModel.setRowCount(0);

        long dentroCount = 0;
        for (Visita v : lista) {
            if ("DENTRO".equalsIgnoreCase(String.valueOf(v.getEstadoVisita()))) {
                dentroCount++;
            }
            tableModel.addRow(new Object[]{
                    v.getId(),
                    v.getPersonaNombreCompleto(),
                    v.getPersonaDocIdentidad(),
                    v.getMotivo() != null ? v.getMotivo() : "N/A",
                    v.getTipoVisita(),
                    v.getEstadoVisita(),
                    v.getFechaHoraIngreso() != null ? v.getFechaHoraIngreso().toString().replace("T", " ") : "-",
                    v.getFechaHoraSalida() != null ? v.getFechaHoraSalida().toString().replace("T", " ") : "-"
            });
        }


        lblTotalRegistros.setText("📊 Total Registros (" + tituloModulo + "): " + lista.size());
        lblVisitasDentro.setText("🟢 Personas Dentro: " + dentroCount);

        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ℹ️ No se encontraron registros para los filtros seleccionados (REP-04)", "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void renderTablaIncidentes(List<Incidente> lista) {
        tableModel.setColumnIdentifiers(new String[]{
                "ID Incidente", "Persona ID", "Título de Incidente", "Descripción de Gravedad", "Nivel Gravedad", "Fecha / Hora"
        });
        tableModel.setRowCount(0);

        for (Incidente i : lista) {
            tableModel.addRow(new Object[]{
                    i.getId(),
                    i.getReportadoPorUsuarioId() != null ? i.getReportadoPorUsuarioId() : "-",
                    i.getTitulo(),
                    i.getDescripcion(),
                    i.getNivelGravedad(),
                    i.getFechaHora() != null ? i.getFechaHora().toString().replace("T", " ") : "-"
            });
        }

        lblTotalRegistros.setText("📊 Total Incidentes: " + lista.size());
        lblIncidentesCriticos.setText("🚨 Incidentes Totales: " + lista.size());

        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ℹ️ No se encontraron incidentes registrados para el filtro (REP-04)", "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void executeExportarCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos cargados en el reporte para exportar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("📥 Guardar Reporte SICA (CSV / Excel) [REP-07]");
        fileChooser.setSelectedFile(new File("reporte_sica_export.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(fileToSave, StandardCharsets.UTF_8))) {
                StringBuilder header = new StringBuilder();
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    header.append(tableModel.getColumnName(c)).append(c == tableModel.getColumnCount() - 1 ? "" : ",");
                }
                pw.println(header);

                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    StringBuilder row = new StringBuilder();
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        String val = String.valueOf(tableModel.getValueAt(r, c)).replace(",", ";");
                        row.append("\"").append(val).append("\"").append(c == tableModel.getColumnCount() - 1 ? "" : ",");
                    }
                    pw.println(row);
                }
                JOptionPane.showMessageDialog(this, "✅ Reporte exportado exitosamente en:\n" + fileToSave.getAbsolutePath(), "Éxito Exportación [REP-07]", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al exportar archivo CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
