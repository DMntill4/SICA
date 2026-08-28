package com.acme.sica.infrastructure.adapter.out.persistence;

import com.acme.sica.application.port.out.SolicitudPaseRepository;
import com.acme.sica.domain.model.SolicitudPase;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSolicitudPaseRepository implements SolicitudPaseRepository {

    private final ConnectionFactory connectionFactory;

    public JdbcSolicitudPaseRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public SolicitudPase guardar(SolicitudPase s) {
        String sql = """
            INSERT INTO solicitud_pase 
            (nombre_completo, doc_identidad, email, telefono, empresa_destino, funcionario_destino_id, motivo, fecha_hora_solicitada, vector_biometrico, foto_url, estado, creado_en)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, s.getNombreCompleto());
            pst.setString(2, s.getDocIdentidad());
            pst.setString(3, s.getEmail());
            pst.setString(4, s.getTelefono());
            pst.setString(5, s.getEmpresaDestino());
            if (s.getFuncionarioDestinoId() != null) {
                pst.setLong(6, s.getFuncionarioDestinoId());
            } else {
                pst.setNull(6, Types.BIGINT);
            }
            pst.setString(7, s.getMotivo());
            pst.setTimestamp(8, Timestamp.valueOf(s.getFechaHoraSolicitada() != null ? s.getFechaHoraSolicitada() : LocalDateTime.now()));
            pst.setString(9, s.getVectorBiometrico());
            pst.setString(10, s.getFotoUrl());
            pst.setString(11, s.getEstado() != null ? s.getEstado().name() : SolicitudPase.EstadoSolicitud.PENDIENTE_APROBACION.name());
            pst.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getLong(1));
                }
            }
            return s;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar solicitud de pase: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<SolicitudPase> buscarPorId(Long id) {
        String sql = """
            SELECT sp.*, u.nombre_completo as func_nombre 
            FROM solicitud_pase sp 
            LEFT JOIN usuario u ON sp.funcionario_destino_id = u.id 
            WHERE sp.id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setLong(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSolicitudPase(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar solicitud de pase por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SolicitudPase> listarTodas() {
        String sql = """
            SELECT sp.*, u.nombre_completo as func_nombre 
            FROM solicitud_pase sp 
            LEFT JOIN usuario u ON sp.funcionario_destino_id = u.id 
            ORDER BY sp.id DESC
        """;
        return ejecutarConsultaListar(sql);
    }

    @Override
    public List<SolicitudPase> listarPendientes() {
        String sql = """
            SELECT sp.*, u.nombre_completo as func_nombre 
            FROM solicitud_pase sp 
            LEFT JOIN usuario u ON sp.funcionario_destino_id = u.id 
            WHERE sp.estado = 'PENDIENTE_APROBACION'
            ORDER BY sp.id DESC
        """;
        return ejecutarConsultaListar(sql);
    }

    @Override
    public void actualizarEstado(Long id, SolicitudPase.EstadoSolicitud estado) {
        String sql = "UPDATE solicitud_pase SET estado = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, estado.name());
            pst.setLong(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado de solicitud de pase: " + e.getMessage(), e);
        }
    }

    private List<SolicitudPase> ejecutarConsultaListar(String sql) {
        List<SolicitudPase> lista = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToSolicitudPase(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar solicitudes de pase: " + e.getMessage(), e);
        }
        return lista;
    }

    private SolicitudPase mapResultSetToSolicitudPase(ResultSet rs) throws SQLException {
        SolicitudPase s = new SolicitudPase();
        s.setId(rs.getLong("id"));
        s.setNombreCompleto(rs.getString("nombre_completo"));
        s.setDocIdentidad(rs.getString("doc_identidad"));
        s.setEmail(rs.getString("email"));
        s.setTelefono(rs.getString("telefono"));
        s.setEmpresaDestino(rs.getString("empresa_destino"));

        long funcId = rs.getLong("funcionario_destino_id");
        if (!rs.wasNull()) {
            s.setFuncionarioDestinoId(funcId);
        }
        try {
            s.setFuncionarioNombreCompleto(rs.getString("func_nombre"));
        } catch (SQLException ignored) {}

        s.setMotivo(rs.getString("motivo"));

        Timestamp tsSol = rs.getTimestamp("fecha_hora_solicitada");
        if (tsSol != null) s.setFechaHoraSolicitada(tsSol.toLocalDateTime());

        s.setVectorBiometrico(rs.getString("vector_biometrico"));
        s.setFotoUrl(rs.getString("foto_url"));

        String est = rs.getString("estado");
        if (est != null) s.setEstado(SolicitudPase.EstadoSolicitud.valueOf(est));

        Timestamp tsCre = rs.getTimestamp("creado_en");
        if (tsCre != null) s.setCreadoEn(tsCre.toLocalDateTime());

        return s;
    }
}
