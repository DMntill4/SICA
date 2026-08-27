package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.application.port.out.RolRepository;
import com.acme.sica.domain.model.Permiso;
import com.acme.sica.domain.model.Rol;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolJdbcAdapter implements RolRepository {

    private final ConnectionFactory connectionFactory;

    public RolJdbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Rol> findAll() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM rol ORDER BY id ASC";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Rol r = mapResultSetToRol(rs);
                r.setPermisoIds(findPermisoIdsByRolId(conn, r.getId()));
                r.setPermisoCodigos(findPermisoCodigosByRolId(conn, r.getId()));
                roles.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar roles: " + e.getMessage(), e);
        }
        return roles;
    }

    @Override
    public Optional<Rol> findById(Long id) {
        String sql = "SELECT id, nombre, descripcion FROM rol WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol r = mapResultSetToRol(rs);
                    r.setPermisoIds(findPermisoIdsByRolId(conn, r.getId()));
                    r.setPermisoCodigos(findPermisoCodigosByRolId(conn, r.getId()));
                    return Optional.of(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Rol> findByNombre(String nombre) {
        String sql = "SELECT id, nombre, descripcion FROM rol WHERE nombre = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol r = mapResultSetToRol(rs);
                    r.setPermisoIds(findPermisoIdsByRolId(conn, r.getId()));
                    r.setPermisoCodigos(findPermisoCodigosByRolId(conn, r.getId()));
                    return Optional.of(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol por nombre: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Rol save(Rol rol, List<Long> permisoIds) {
        String sql = "INSERT INTO rol (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, rol.getNombre().trim());
            ps.setString(2, rol.getDescripcion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    rol.setId(rs.getLong(1));
                }
            }

            if (permisoIds != null && !permisoIds.isEmpty()) {
                insertRolPermisos(conn, rol.getId(), permisoIds);
            }
            rol.setPermisoIds(findPermisoIdsByRolId(conn, rol.getId()));
            rol.setPermisoCodigos(findPermisoCodigosByRolId(conn, rol.getId()));
            return rol;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar rol en BD: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePermisos(Long rolId, List<Long> permisoIds) {
        try (Connection conn = connectionFactory.getConnection()) {
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM rol_permiso WHERE rol_id = ?")) {
                psDel.setLong(1, rolId);
                psDel.executeUpdate();
            }

            if (permisoIds != null && !permisoIds.isEmpty()) {
                insertRolPermisos(conn, rolId, permisoIds);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar permisos de rol: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM rol WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar rol de BD: " + e.getMessage(), e);
        }
    }

    @Override
    public int countUsuariosByRolId(Long rolId) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE rol_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar usuarios asociados a rol: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<Permiso> findAllPermisos() {
        List<Permiso> list = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM permiso ORDER BY id ASC";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Permiso(rs.getLong("id"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar permisos: " + e.getMessage(), e);
        }
        return list;
    }

    private void insertRolPermisos(Connection conn, Long rolId, List<Long> permisoIds) throws SQLException {
        String sql = "INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long pId : permisoIds) {
                ps.setLong(1, rolId);
                ps.setLong(2, pId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Long> findPermisoIdsByRolId(Connection conn, Long rolId) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT permiso_id FROM rol_permiso WHERE rol_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("permiso_id"));
                }
            }
        }
        return ids;
    }

    private List<String> findPermisoCodigosByRolId(Connection conn, Long rolId) throws SQLException {
        List<String> codigos = new ArrayList<>();
        String sql = "SELECT p.nombre FROM permiso p JOIN rol_permiso rp ON p.id = rp.permiso_id WHERE rp.rol_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    codigos.add(rs.getString("nombre"));
                }
            }
        }
        return codigos;
    }

    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        return new Rol(
            rs.getLong("id"),
            rs.getString("nombre"),
            rs.getString("descripcion")
        );
    }
}
