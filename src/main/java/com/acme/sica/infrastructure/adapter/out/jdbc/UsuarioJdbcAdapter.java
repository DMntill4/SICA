package com.acme.sica.infrastructure.adapter.out.jdbc;

import com.acme.sica.domain.model.Usuario;
import com.acme.sica.domain.port.UsuarioRepository;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Adaptador de Salida JDBC para la gestion de usuarios, roles, permisos y revocacion de tokens.
 */
public class UsuarioJdbcAdapter implements UsuarioRepository {

    private final ConnectionFactory connectionFactory;

    public UsuarioJdbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        String sql = """
            SELECT u.id, u.username, u.password_hash, u.nombre_completo, u.email, u.rol_id, r.nombre AS rol_nombre,
                   u.empresa_id, e.nombre AS empresa_nombre, u.intentos_fallidos, u.bloqueado, u.creado_en
            FROM usuario u
            JOIN rol r ON u.rol_id = r.id
            LEFT JOIN empresa e ON u.empresa_id = e.id
            WHERE u.username = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioAdapter Error] error en findByUsername: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        String sql = """
            SELECT u.id, u.username, u.password_hash, u.nombre_completo, u.email, u.rol_id, r.nombre AS rol_nombre,
                   u.empresa_id, e.nombre AS empresa_nombre, u.intentos_fallidos, u.bloqueado, u.creado_en
            FROM usuario u
            JOIN rol r ON u.rol_id = r.id
            LEFT JOIN empresa e ON u.empresa_id = e.id
            WHERE u.id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioAdapter Error] error en findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Set<String> findPermissionsByRoleId(Long roleId) {
        Set<String> permisos = new HashSet<>();
        String sql = """
            SELECT p.nombre
            FROM permiso p
            JOIN rol_permiso rp ON p.id = rp.permiso_id
            WHERE rp.rol_id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioAdapter Error] error en findPermissionsByRoleId: " + e.getMessage());
        }
        return permisos;
    }

    @Override
    public Usuario save(Usuario usuario) {
        String sql = "INSERT INTO usuario (username, password_hash, nombre_completo, email, rol_id, empresa_id, intentos_fallidos, bloqueado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getNombreCompleto());
            ps.setString(4, usuario.getEmail());
            ps.setLong(5, usuario.getRolId());
            if (usuario.getEmpresaId() != null) {
                ps.setLong(6, usuario.getEmpresaId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setInt(7, usuario.getIntentosFallidos());
            ps.setBoolean(8, usuario.isBloqueado());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario en BD: " + e.getMessage(), e);
        }
        return usuario;
    }

    @Override
    public void update(Usuario usuario) {
        String sql = "UPDATE usuario SET password_hash = ?, nombre_completo = ?, email = ?, rol_id = ?, empresa_id = ?, intentos_fallidos = ?, bloqueado = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, usuario.getPasswordHash());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, usuario.getEmail());
            ps.setLong(4, usuario.getRolId());
            if (usuario.getEmpresaId() != null) {
                ps.setLong(5, usuario.getEmpresaId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setInt(6, usuario.getIntentosFallidos());
            ps.setBoolean(7, usuario.isBloqueado());
            ps.setLong(8, usuario.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario en BD: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario en BD: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> findAll() {
        List<Usuario> list = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username, u.password_hash, u.nombre_completo, u.email, u.rol_id, r.nombre AS rol_nombre,
                   u.empresa_id, e.nombre AS empresa_nombre, u.intentos_fallidos, u.bloqueado, u.creado_en
            FROM usuario u
            JOIN rol r ON u.rol_id = r.id
            LEFT JOIN empresa e ON u.empresa_id = e.id
            ORDER BY u.id ASC
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioAdapter Error] error en findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void revokeToken(String tokenJti, LocalDateTime expiraEn) {
        String sql = "INSERT INTO token_revocado (token_jti, expira_en) VALUES (?, ?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenJti);
            ps.setTimestamp(2, Timestamp.valueOf(expiraEn));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UsuarioAdapter Error] error al revocar token: " + e.getMessage());
        }
    }

    @Override
    public boolean isTokenRevoked(String tokenJti) {
        String sql = "SELECT COUNT(*) FROM token_revocado WHERE token_jti = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenJti);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioAdapter Error] error en isTokenRevoked: " + e.getMessage());
        }
        return false;
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setEmail(rs.getString("email"));
        u.setRolId(rs.getLong("rol_id"));
        u.setRolNombre(rs.getString("rol_nombre"));
        
        long eId = rs.getLong("empresa_id");
        if (!rs.wasNull()) {
            u.setEmpresaId(eId);
            u.setEmpresaNombre(rs.getString("empresa_nombre"));
        }
        
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        u.setBloqueado(rs.getBoolean("bloqueado"));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) {
            u.setCreadoEn(ts.toLocalDateTime());
        }
        return u;
    }
}
