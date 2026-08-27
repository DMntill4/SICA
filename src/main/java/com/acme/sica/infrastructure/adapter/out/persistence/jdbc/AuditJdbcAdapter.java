package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.model.BitacoraAuditoria;
import com.acme.sica.application.port.out.AuditRepository;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de Salida (Output Adapter) para la persistencia JDBC en bitacora_auditoria.
 */
public class AuditJdbcAdapter implements AuditRepository {

    private final ConnectionFactory connectionFactory;

    public AuditJdbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void save(BitacoraAuditoria log) {
        String sql = "INSERT INTO bitacora_auditoria (usuario_id, username, accion, detalle, ip_origen, fecha_hora) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (log.getUsuarioId() != null) {
                ps.setLong(1, log.getUsuarioId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getAccion());
            ps.setString(4, log.getDetalle());
            ps.setString(5, log.getIpOrigen());
            ps.setTimestamp(6, Timestamp.valueOf(log.getFechaHora() != null ? log.getFechaHora() : java.time.LocalDateTime.now()));

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Audit Adapter Error] Error guardando registro de auditoria: " + e.getMessage());
        }
    }

    @Override
    public List<BitacoraAuditoria> findAllRecent(int limit) {
        List<BitacoraAuditoria> list = new ArrayList<>();
        String sql = "SELECT id, usuario_id, username, accion, detalle, ip_origen, fecha_hora FROM bitacora_auditoria ORDER BY id DESC LIMIT ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BitacoraAuditoria item = new BitacoraAuditoria();
                    item.setId(rs.getLong("id"));
                    long uId = rs.getLong("usuario_id");
                    if (!rs.wasNull()) {
                        item.setUsuarioId(uId);
                    }
                    item.setUsername(rs.getString("username"));
                    item.setAccion(rs.getString("accion"));
                    item.setDetalle(rs.getString("detalle"));
                    item.setIpOrigen(rs.getString("ip_origen"));
                    Timestamp ts = rs.getTimestamp("fecha_hora");
                    if (ts != null) {
                        item.setFechaHora(ts.toLocalDateTime());
                    }
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Audit Adapter Error] Error al consultar auditoria: " + e.getMessage());
        }
        return list;
    }
}
