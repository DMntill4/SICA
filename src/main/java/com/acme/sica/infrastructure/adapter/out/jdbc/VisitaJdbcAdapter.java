package com.acme.sica.infrastructure.adapter.out.jdbc;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoCierreVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.domain.port.VisitaRepository;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisitaJdbcAdapter implements VisitaRepository {

    private final ConnectionFactory connectionFactory;

    public VisitaJdbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Visita save(Visita visita) {
        String sql = """
            INSERT INTO visita (persona_id, funcionario_id, punto_acceso_ingreso_id, punto_acceso_salida_id,
                                guardia_ingreso_id, guardia_salida_id, tipo_visita, estado_visita, tipo_cierre,
                                motivo, fecha_hora_programada, fecha_hora_ingreso, fecha_hora_salida)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setVisitaParameters(ps, visita);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    visita.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar visita en BD: " + e.getMessage(), e);
        }
        return visita;
    }

    @Override
    public void update(Visita visita) {
        String sql = """
            UPDATE visita SET persona_id = ?, funcionario_id = ?, punto_acceso_ingreso_id = ?, punto_acceso_salida_id = ?,
                              guardia_ingreso_id = ?, guardia_salida_id = ?, tipo_visita = ?, estado_visita = ?,
                              tipo_cierre = ?, motivo = ?, fecha_hora_programada = ?, fecha_hora_ingreso = ?, fecha_hora_salida = ?
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setVisitaParameters(ps, visita);
            ps.setLong(14, visita.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar visita en BD: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Visita> findById(Long id) {
        String sql = getSelectBaseSql() + " WHERE v.id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVisita(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VisitaAdapter Error] error en findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Visita> findLatestActiveVisitByPersonaId(Long personaId) {
        String sql = getSelectBaseSql() + " WHERE v.persona_id = ? AND v.estado_visita = 'DENTRO' ORDER BY v.id DESC LIMIT 1";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVisita(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VisitaAdapter Error] error en findLatestActiveVisitByPersonaId: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Visita> findCurrentlyInside() {
        List<Visita> list = new ArrayList<>();
        String sql = getSelectBaseSql() + " WHERE v.estado_visita = 'DENTRO' ORDER BY v.fecha_hora_ingreso DESC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToVisita(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VisitaAdapter Error] error en findCurrentlyInside: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Visita> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Visita> list = new ArrayList<>();
        String sql = getSelectBaseSql() + " WHERE v.creado_en BETWEEN ? AND ? ORDER BY v.creado_en DESC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVisita(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VisitaAdapter Error] error en findByDateRange: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Visita> findAll() {
        List<Visita> list = new ArrayList<>();
        String sql = getSelectBaseSql() + " ORDER BY v.id DESC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToVisita(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VisitaAdapter Error] error en findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM visita";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al limpiar visitas de la BD: " + e.getMessage(), e);
        }
    }

    private String getSelectBaseSql() {
        return """
            SELECT v.id, v.persona_id, p.nombre AS persona_nombre, p.apellido AS persona_apellido, p.doc_identidad AS persona_doc,
                   v.funcionario_id, uf.nombre_completo AS funcionario_nombre,
                   v.punto_acceso_ingreso_id, v.punto_acceso_salida_id,
                   v.guardia_ingreso_id, v.guardia_salida_id,
                   v.tipo_visita, v.estado_visita, v.tipo_cierre, v.motivo,
                   v.fecha_hora_programada, v.fecha_hora_ingreso, v.fecha_hora_salida, v.creado_en
            FROM visita v
            JOIN persona p ON v.persona_id = p.id
            LEFT JOIN usuario uf ON v.funcionario_id = uf.id
        """;
    }

    private void setVisitaParameters(PreparedStatement ps, Visita v) throws SQLException {
        ps.setLong(1, v.getPersonaId());
        setNullableLong(ps, 2, v.getFuncionarioId());
        setNullableLong(ps, 3, v.getPuntoAccesoIngresoId());
        setNullableLong(ps, 4, v.getPuntoAccesoSalidaId());
        setNullableLong(ps, 5, v.getGuardiaIngresoId());
        setNullableLong(ps, 6, v.getGuardiaSalidaId());
        ps.setString(7, v.getTipoVisita().name());
        ps.setString(8, v.getEstadoVisita().name());
        ps.setString(9, v.getTipoCierre() != null ? v.getTipoCierre().name() : null);
        ps.setString(10, v.getMotivo());
        setNullableTimestamp(ps, 11, v.getFechaHoraProgramada());
        setNullableTimestamp(ps, 12, v.getFechaHoraIngreso());
        setNullableTimestamp(ps, 13, v.getFechaHoraSalida());
    }

    private void setNullableLong(PreparedStatement ps, int paramIndex, Long value) throws SQLException {
        if (value != null) {
            ps.setLong(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.BIGINT);
        }
    }

    private void setNullableTimestamp(PreparedStatement ps, int paramIndex, LocalDateTime value) throws SQLException {
        if (value != null) {
            ps.setTimestamp(paramIndex, Timestamp.valueOf(value));
        } else {
            ps.setNull(paramIndex, Types.TIMESTAMP);
        }
    }

    private Visita mapResultSetToVisita(ResultSet rs) throws SQLException {
        Visita v = new Visita();
        v.setId(rs.getLong("id"));
        v.setPersonaId(rs.getLong("persona_id"));
        v.setPersonaNombreCompleto(rs.getString("persona_nombre") + " " + rs.getString("persona_apellido"));
        v.setPersonaDocIdentidad(rs.getString("persona_doc"));

        long fId = rs.getLong("funcionario_id");
        if (!rs.wasNull()) {
            v.setFuncionarioId(fId);
            v.setFuncionarioNombreCompleto(rs.getString("funcionario_nombre"));
        }

        long pai = rs.getLong("punto_acceso_ingreso_id");
        if (!rs.wasNull()) v.setPuntoAccesoIngresoId(pai);

        long pas = rs.getLong("punto_acceso_salida_id");
        if (!rs.wasNull()) v.setPuntoAccesoSalidaId(pas);

        long gi = rs.getLong("guardia_ingreso_id");
        if (!rs.wasNull()) v.setGuardiaIngresoId(gi);

        long gs = rs.getLong("guardia_salida_id");
        if (!rs.wasNull()) v.setGuardiaSalidaId(gs);

        v.setTipoVisita(TipoVisita.valueOf(rs.getString("tipo_visita")));
        v.setEstadoVisita(EstadoVisita.valueOf(rs.getString("estado_visita")));
        
        String tipoCierre = rs.getString("tipo_cierre");
        if (tipoCierre != null) {
            v.setTipoCierre(TipoCierreVisita.valueOf(tipoCierre));
        }

        v.setMotivo(rs.getString("motivo"));

        Timestamp tp = rs.getTimestamp("fecha_hora_programada");
        if (tp != null) v.setFechaHoraProgramada(tp.toLocalDateTime());

        Timestamp ti = rs.getTimestamp("fecha_hora_ingreso");
        if (ti != null) v.setFechaHoraIngreso(ti.toLocalDateTime());

        Timestamp ts = rs.getTimestamp("fecha_hora_salida");
        if (ts != null) v.setFechaHoraSalida(ts.toLocalDateTime());

        Timestamp tc = rs.getTimestamp("creado_en");
        if (tc != null) v.setCreadoEn(tc.toLocalDateTime());

        return v;
    }
}
