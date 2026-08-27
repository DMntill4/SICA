package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonaJdbcAdapter implements PersonaRepository {

    private final ConnectionFactory connectionFactory;

    public PersonaJdbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Persona> findByDocIdentidad(String docIdentidad) {
        String sql = """
            SELECT p.id, p.doc_identidad, p.tipo_documento, p.nombre, p.apellido, p.email, p.telefono,
                   p.empresa_id, e.nombre AS empresa_nombre, p.estado_acceso, p.creado_en
            FROM persona p
            LEFT JOIN empresa e ON p.empresa_id = e.id
            WHERE p.doc_identidad = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docIdentidad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonaAdapter Error] error en findByDocIdentidad: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Persona> findById(Long id) {
        String sql = """
            SELECT p.id, p.doc_identidad, p.tipo_documento, p.nombre, p.apellido, p.email, p.telefono,
                   p.empresa_id, e.nombre AS empresa_nombre, p.estado_acceso, p.creado_en
            FROM persona p
            LEFT JOIN empresa e ON p.empresa_id = e.id
            WHERE p.id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonaAdapter Error] error en findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Persona save(Persona persona) {
        String sql = "INSERT INTO persona (doc_identidad, tipo_documento, nombre, apellido, email, telefono, empresa_id, estado_acceso) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, persona.getDocIdentidad());
            ps.setString(2, persona.getTipoDocumento());
            ps.setString(3, persona.getNombre());
            ps.setString(4, persona.getApellido());
            ps.setString(5, persona.getEmail());
            ps.setString(6, persona.getTelefono());
            if (persona.getEmpresaId() != null) {
                ps.setLong(7, persona.getEmpresaId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            ps.setString(8, persona.getEstadoAcceso().name());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    persona.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar persona en BD: " + e.getMessage(), e);
        }
        return persona;
    }

    @Override
    public void update(Persona persona) {
        String sql = "UPDATE persona SET tipo_documento = ?, nombre = ?, apellido = ?, email = ?, telefono = ?, empresa_id = ?, estado_acceso = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, persona.getTipoDocumento());
            ps.setString(2, persona.getNombre());
            ps.setString(3, persona.getApellido());
            ps.setString(4, persona.getEmail());
            ps.setString(5, persona.getTelefono());
            if (persona.getEmpresaId() != null) {
                ps.setLong(6, persona.getEmpresaId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setString(7, persona.getEstadoAcceso().name());
            ps.setLong(8, persona.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar persona en BD: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEstadoAcceso(Long personaId, EstadoAcceso nuevoEstado) {
        String sql = "UPDATE persona SET estado_acceso = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setLong(2, personaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado de acceso en BD: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM persona WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar persona en BD: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Persona> findAll() {
        List<Persona> list = new ArrayList<>();
        String sql = """
            SELECT p.id, p.doc_identidad, p.tipo_documento, p.nombre, p.apellido, p.email, p.telefono,
                   p.empresa_id, e.nombre AS empresa_nombre, p.estado_acceso, p.creado_en
            FROM persona p
            LEFT JOIN empresa e ON p.empresa_id = e.id
            ORDER BY p.id ASC
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPersona(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PersonaAdapter Error] error en findAll: " + e.getMessage());
        }
        return list;
    }

    private Persona mapResultSetToPersona(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setId(rs.getLong("id"));
        p.setDocIdentidad(rs.getString("doc_identidad"));
        p.setTipoDocumento(rs.getString("tipo_documento"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        p.setEmail(rs.getString("email"));
        p.setTelefono(rs.getString("telefono"));
        
        long eId = rs.getLong("empresa_id");
        if (!rs.wasNull()) {
            p.setEmpresaId(eId);
            p.setEmpresaNombre(rs.getString("empresa_nombre"));
        }
        
        p.setEstadoAcceso(EstadoAcceso.valueOf(rs.getString("estado_acceso")));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) {
            p.setCreadoEn(ts.toLocalDateTime());
        }
        return p;
    }
}
