package com.acme.sica.infrastructure.security;

import com.acme.sica.application.port.out.UsuarioRepository;

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
    @DisplayName("Debe retornar true si el rol posee el permiso requerido en BD")
    void testHasPermission_Success() {
        Long roleId = 2L; // GUARDIA
        when(usuarioRepository.findPermissionsByRoleId(roleId)).thenReturn(Set.of("crear_persona", "checkin_visita"));

        boolean result = permissionChecker.hasPermission(roleId, "crear_persona");

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
        Long roleId = 3L; // FUNCIONARIO
        when(usuarioRepository.findPermissionsByRoleId(roleId)).thenReturn(Set.of("preregistrar_visita"));

        permissionChecker.hasPermission(roleId, "preregistrar_visita");
        permissionChecker.hasPermission(roleId, "preregistrar_visita");

        verify(usuarioRepository, times(1)).findPermissionsByRoleId(roleId);
    }

    @Test
    @DisplayName("ROL ADMIN (roleId 1) posee autorizacion automatica para todos los permisos")
    void testAdminTieneTodosLosPermisos() {
        Long roleId = 1L; // ADMIN

        assertTrue(permissionChecker.hasPermission(roleId, "crear_usuario"));
        assertTrue(permissionChecker.hasPermission(roleId, "gestionar_roles"));
        assertTrue(permissionChecker.hasPermission(roleId, "cualquier_permiso_futuro"));
    }
}

