package com.acme.sica.infrastructure.adapter.out.jdbc;

import com.acme.sica.domain.enums.NivelGravedad;
import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.port.IncidenteRepository;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidenteJdbcAdapter implements IncidenteRepository {

    private final ConnectionFactory connectionFactory;

    public IncidenteJdbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Incidente save(Incidente incidente) {
        String sql = "INSERT INTO incidente (persona_id, reportado_por_usuario_id, titulo, descripcion, nivel_gravedad, fecha_hora) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, incidente.getPersonaId());
            ps.setLong(2, incidente.getReportadoPorUsuarioId());
            ps.setString(3, incidente.getTitulo());
            ps.setString(4, incidente.getDescripcion());
            ps.setString(5, incidente.getNivelGravedad().name());
            ps.setTimestamp(6, Timestamp.valueOf(incidente.getFechaHora() != null ? incidente.getFechaHora() : java.time.LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    incidente.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar incidente en BD: " + e.getMessage(), e);
        }
        return incidente;
    }

    @Override
    public List<Incidente> findAll() {
        List<Incidente> list = new ArrayList<>();
        String sql = """
            SELECT i.id, i.persona_id, p.doc_identidad AS persona_doc, CONCAT(p.nombre, ' ', p.apellido) AS persona_nombre,
                   i.reportado_por_usuario_id AS usuario_reporta_id, u.username AS usuario_reporta_name,
                   i.titulo, i.descripcion, i.nivel_gravedad, i.fecha_hora
            FROM incidente i
            JOIN persona p ON i.persona_id = p.id
            JOIN usuario u ON i.reportado_por_usuario_id = u.id
            ORDER BY i.id DESC
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToIncidente(rs));
            }
        } catch (SQLException e) {
            System.err.println("[IncidenteAdapter Error] error en findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Incidente> findByPersonaId(Long personaId) {
        List<Incidente> list = new ArrayList<>();
        String sql = """
            SELECT i.id, i.persona_id, p.doc_identidad AS persona_doc, CONCAT(p.nombre, ' ', p.apellido) AS persona_nombre,
                   i.reportado_por_usuario_id AS usuario_reporta_id, u.username AS usuario_reporta_name,
                   i.titulo, i.descripcion, i.nivel_gravedad, i.fecha_hora
            FROM incidente i
            JOIN persona p ON i.persona_id = p.id
            JOIN usuario u ON i.reportado_por_usuario_id = u.id
            WHERE i.persona_id = ?
            ORDER BY i.id DESC
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToIncidente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[IncidenteAdapter Error] error en findByPersonaId: " + e.getMessage());
        }
        return list;
    }

    private Incidente mapResultSetToIncidente(ResultSet rs) throws SQLException {
        Incidente i = new Incidente();
        i.setId(rs.getLong("id"));
        i.setPersonaId(rs.getLong("persona_id"));
        i.setPersonaDocIdentidad(rs.getString("persona_doc"));
        i.setPersonaNombreCompleto(rs.getString("persona_nombre"));
        i.setReportadoPorUsuarioId(rs.getLong("usuario_reporta_id"));
        i.setReportadoPorUsername(rs.getString("usuario_reporta_name"));
        i.setTitulo(rs.getString("titulo"));
        i.setDescripcion(rs.getString("descripcion"));
        i.setNivelGravedad(NivelGravedad.valueOf(rs.getString("nivel_gravedad")));
        Timestamp ts = rs.getTimestamp("fecha_hora");
        if (ts != null) {
            i.setFechaHora(ts.toLocalDateTime());
        }
        return i;
    }
}
