package com.acme.sica.application.usecase.roles;

import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.dto.RolDTO;
import com.acme.sica.application.port.out.RolRepository;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.domain.model.Permiso;
import com.acme.sica.domain.model.Rol;

import java.util.List;

public class GestionarRolUseCase {

    private final RolRepository rolRepository;
    private final AuditService auditService;

    public GestionarRolUseCase(RolRepository rolRepository, AuditService auditService) {
        this.rolRepository = rolRepository;
        this.auditService = auditService;
    }

    public Rol crearRol(RolDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }

        if (rolRepository.findByNombre(dto.nombre().trim()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre '" + dto.nombre() + "'");
        }

        Rol nuevoRol = new Rol(null, dto.nombre().trim(), dto.descripcion());
        Rol guardado = rolRepository.save(nuevoRol, dto.permisoIds());

        auditService.log(
            actor != null ? actor.userId() : null,
            actor != null ? actor.username() : "SISTEMA",
            "CREAR_ROL",
            "Rol creado: " + guardado.getNombre() + " (ID #" + guardado.getId() + ") con " + (dto.permisoIds() != null ? dto.permisoIds().size() : 0) + " permisos",
            ipOrigen
        );
        return guardado;
    }

    public void actualizarPermisosRol(Long rolId, List<Long> permisoIds, AuthenticatedUserContext actor, String ipOrigen) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + rolId));

        rolRepository.updatePermisos(rolId, permisoIds);

        auditService.log(
            actor != null ? actor.userId() : null,
            actor != null ? actor.username() : "SISTEMA",
            "ACTUALIZAR_PERMISOS_ROL",
            "Permisos actualizados para rol '" + rol.getNombre() + "' (ID #" + rolId + "): " + (permisoIds != null ? permisoIds.size() : 0) + " permisos asignados",
            ipOrigen
        );
    }

    public void eliminarRol(Long rolId, AuthenticatedUserContext actor, String ipOrigen) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + rolId));

        // Regla RBAC-07: Impedir la eliminación si hay usuarios asignados a este rol
        int usuariosAsociados = rolRepository.countUsuariosByRolId(rolId);
        if (usuariosAsociados > 0) {
            throw new IllegalStateException("No se puede eliminar el rol '" + rol.getNombre() + "' porque tiene " + usuariosAsociados + " usuario(s) asignado(s). Reasigne los usuarios antes de eliminar.");
        }

        if (rolId <= 3) {
            throw new IllegalArgumentException("Los roles base del sistema (ADMIN, GUARDIA, FUNCIONARIO) no pueden ser eliminados");
        }

        rolRepository.deleteById(rolId);
        auditService.log(
            actor != null ? actor.userId() : null,
            actor != null ? actor.username() : "SISTEMA",
            "ELIMINAR_ROL",
            "Rol eliminado ID: " + rolId + " (" + rol.getNombre() + ")",
            ipOrigen
        );
    }

    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    public List<Permiso> listarPermisos() {
        return rolRepository.findAllPermisos();
    }
}
