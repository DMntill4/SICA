package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.model.Empresa;
import com.acme.sica.application.port.out.EmpresaRepository;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpresaJdbcAdapter implements EmpresaRepository {
    private final ConnectionFactory connectionFactory;

    public EmpresaJdbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Empresa> findById(Long id) {
        String sql = "SELECT * FROM empresa WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching empresa by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Empresa> findByNit(String nit) {
        String sql = "SELECT * FROM empresa WHERE nit = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching empresa by nit", e);
        }
        return Optional.empty();
    }

    @Override
    public Empresa save(Empresa empresa) {
        String sql = "INSERT INTO empresa (nit, nombre, ubicacion_oficina, activa) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, empresa.getNit());
            stmt.setString(2, empresa.getNombre());
            stmt.setString(3, empresa.getUbicacionOficina());
            stmt.setBoolean(4, empresa.isActiva());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    empresa.setId(rs.getLong(1));
                }
            }
            return empresa;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving empresa", e);
        }
    }

    @Override
    public void update(Empresa empresa) {
        String sql = "UPDATE empresa SET nit = ?, nombre = ?, ubicacion_oficina = ?, activa = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empresa.getNit());
            stmt.setString(2, empresa.getNombre());
            stmt.setString(3, empresa.getUbicacionOficina());
            stmt.setBoolean(4, empresa.isActiva());
            stmt.setLong(5, empresa.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating empresa", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM empresa WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting empresa", e);
        }
    }

    @Override
    public List<Empresa> findAll() {
        String sql = "SELECT * FROM empresa";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                empresas.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all empresas", e);
        }
        return empresas;
    }

    private Empresa mapRow(ResultSet rs) throws SQLException {
        return new Empresa(
                rs.getLong("id"),
                rs.getString("nit"),
                rs.getString("nombre"),
                rs.getString("ubicacion_oficina"),
                rs.getBoolean("activa")
        );
    }
}
