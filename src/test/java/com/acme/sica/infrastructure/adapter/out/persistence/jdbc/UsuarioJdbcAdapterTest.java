package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.model.Usuario;
import com.acme.sica.infrastructure.db.SchemaInitializer;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;
import com.acme.sica.infrastructure.db.connection.H2ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioJdbcAdapterTest {

    private UsuarioJdbcAdapter adapter;

    @BeforeEach
    void setUp() {
        ConnectionFactory connectionFactory = new H2ConnectionFactory();
        SchemaInitializer initializer = new SchemaInitializer(connectionFactory);
        initializer.initialize();

        adapter = new UsuarioJdbcAdapter(connectionFactory);
    }

    @Test
    void testBuscarUsuarioPorUsernameSemilla() {
        Optional<Usuario> adminOpt = adapter.findByUsername("admin");

        assertTrue(adminOpt.isPresent(), "El usuario 'admin' de datos semilla debe existir en la BD");
        Usuario admin = adminOpt.get();

        assertEquals("admin", admin.getUsername());
        assertNotNull(admin.getPasswordHash());
        assertFalse(admin.isBloqueado());
    }

    @Test
    void testActualizarIntentosFallidosYBloqueo() {
        Optional<Usuario> guardiaOpt = adapter.findByUsername("guardia1");
        assertTrue(guardiaOpt.isPresent());

        Usuario guardia = guardiaOpt.get();
        guardia.setIntentosFallidos(3);
        guardia.setBloqueado(true);

        adapter.update(guardia);

        Usuario modificado = adapter.findByUsername("guardia1").orElseThrow();
        assertEquals(3, modificado.getIntentosFallidos());
        assertTrue(modificado.isBloqueado());
    }

    @Test
    void testRevocarYVerificarTokenRevocado() {
        String tokenJti = "jti_unique_test_" + System.currentTimeMillis();
        assertFalse(adapter.isTokenRevoked(tokenJti), "Un token no revocado debe retornar false");

        adapter.revokeToken(tokenJti, LocalDateTime.now().plusHours(8));

        assertTrue(adapter.isTokenRevoked(tokenJti), "El adapter debe registrar el token revocado en SQL");
    }

    @Test
    void testConsultarPermisosPorRolId() {
        Set<String> permisosAdmin = adapter.findPermissionsByRoleId(1L);

        assertNotNull(permisosAdmin);
        assertFalse(permisosAdmin.isEmpty(), "El rol ADMIN debe tener permisos registrados en BD");
    }
}
