package com.acme.sica.shared.security;

import com.acme.sica.domain.port.UsuarioRepository;
import com.acme.sica.infrastructure.security.PermissionChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionCheckerTest {

    private UsuarioRepository usuarioRepository;
    private PermissionChecker permissionChecker;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        permissionChecker = new PermissionChecker(usuarioRepository);
    }

    @Test
    @DisplayName("Debe retornar true si el rol posee el permiso requerido")
    void testHasPermission_Success() {
        Long roleId = 1L; // ADMIN
        when(usuarioRepository.findPermissionsByRoleId(roleId)).thenReturn(Set.of("crear_usuario", "consultar_auditoria"));

        boolean result = permissionChecker.hasPermission(roleId, "crear_usuario");

        assertTrue(result);
        verify(usuarioRepository, times(1)).findPermissionsByRoleId(roleId);
    }

    @Test
    @DisplayName("Debe retornar false si el rol NO posee el permiso requerido")
    void testHasPermission_Denied() {
        Long roleId = 2L; // GUARDIA
        when(usuarioRepository.findPermissionsByRoleId(roleId)).thenReturn(Set.of("checkin_visita", "checkout_visita"));

        boolean result = permissionChecker.hasPermission(roleId, "crear_usuario");

        assertFalse(result);
    }

    @Test
    @DisplayName("Debe usar el cache en subsecuentes llamadas sin volver a consultar el repositorio")
    void testHasPermission_CacheHit() {
        Long roleId = 1L;
        when(usuarioRepository.findPermissionsByRoleId(roleId)).thenReturn(Set.of("crear_usuario"));

        permissionChecker.hasPermission(roleId, "crear_usuario");
        permissionChecker.hasPermission(roleId, "crear_usuario");

        verify(usuarioRepository, times(1)).findPermissionsByRoleId(roleId);
    }
}
