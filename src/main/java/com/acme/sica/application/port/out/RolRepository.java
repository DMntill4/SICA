package com.acme.sica.application.port.out;

import com.acme.sica.domain.model.Permiso;
import com.acme.sica.domain.model.Rol;

import java.util.List;
import java.util.Optional;

public interface RolRepository {
    List<Rol> findAll();
    Optional<Rol> findById(Long id);
    Optional<Rol> findByNombre(String nombre);
    Rol save(Rol rol, List<Long> permisoIds);
    void updatePermisos(Long rolId, List<Long> permisoIds);
    void deleteById(Long id);
    int countUsuariosByRolId(Long rolId);
    List<Permiso> findAllPermisos();
}
