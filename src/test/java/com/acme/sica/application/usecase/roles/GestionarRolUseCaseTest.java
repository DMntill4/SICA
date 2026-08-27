package com.acme.sica.application.usecase.roles;

import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.dto.RolDTO;
import com.acme.sica.application.port.out.RolRepository;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.domain.model.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestionarRolUseCaseTest {

    private RolRepository rolRepository;
    private AuditService auditService;
    private GestionarRolUseCase gestionarRolUseCase;
    private AuthenticatedUserContext adminActor;

    @BeforeEach
    void setUp() {
        rolRepository = mock(RolRepository.class);
        auditService = mock(AuditService.class);
        gestionarRolUseCase = new GestionarRolUseCase(rolRepository, auditService);
        adminActor = new AuthenticatedUserContext(1L, "admin", 1L, "ADMIN", java.util.Set.of("gestionar_roles"), "jti-123");

    }

    @Test
    @DisplayName("RBAC-01 & RBAC-02: Debe crear un nuevo rol como Recepcionista y asociarle permisos")
    void testCrearNuevoRol() {
        RolDTO dto = new RolDTO(null, "Recepcionista", "Atención en entrada principal", List.of(4L, 6L, 11L));
        Rol mockGuardado = new Rol(4L, "Recepcionista", "Atención en entrada principal");
        mockGuardado.setPermisoIds(List.of(4L, 6L, 11L));

        when(rolRepository.findByNombre("Recepcionista")).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class), eq(List.of(4L, 6L, 11L)))).thenReturn(mockGuardado);

        Rol resultado = gestionarRolUseCase.crearRol(dto, adminActor, "127.0.0.1");

        assertNotNull(resultado);
        assertEquals("Recepcionista", resultado.getNombre());
        assertEquals(3, resultado.getPermisoIds().size());
        verify(auditService).log(eq(1L), eq("admin"), eq("CREAR_ROL"), contains("Recepcionista"), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("RBAC-06: Debe actualizar/revocar los permisos de un rol existente de forma inmediata")
    void testActualizarPermisosRol() {
        Rol mockRol = new Rol(4L, "Recepcionista", "Atención entrada");
        when(rolRepository.findById(4L)).thenReturn(Optional.of(mockRol));

        gestionarRolUseCase.actualizarPermisosRol(4L, List.of(4L), adminActor, "127.0.0.1");

        verify(rolRepository).updatePermisos(4L, List.of(4L));
        verify(auditService).log(eq(1L), eq("admin"), eq("ACTUALIZAR_PERMISOS_ROL"), contains("Recepcionista"), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("RBAC-07: Debe impedir la eliminacion de un rol que posee usuarios asignados")
    void testEliminarRolConUsuariosBloqueado() {
        Rol mockRol = new Rol(4L, "Recepcionista", "Atención entrada");
        when(rolRepository.findById(4L)).thenReturn(Optional.of(mockRol));
        when(rolRepository.countUsuariosByRolId(4L)).thenReturn(2); // Tiene 2 usuarios

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            gestionarRolUseCase.eliminarRol(4L, adminActor, "127.0.0.1");
        });

        assertTrue(ex.getMessage().contains("tiene 2 usuario(s) asignado(s)"));
        verify(rolRepository, never()).deleteById(4L);
    }
}
