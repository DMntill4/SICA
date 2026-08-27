package com.acme.sica.application.usecase.empresas;

import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.domain.model.Empresa;
import com.acme.sica.application.port.out.EmpresaRepository;
import com.acme.sica.application.AuthenticatedUserContext;

import java.util.List;

public class GestionarEmpresaUseCase {
    private final EmpresaRepository empresaRepository;
    private final AuditService auditService;

    public GestionarEmpresaUseCase(EmpresaRepository empresaRepository, AuditService auditService) {
        this.empresaRepository = empresaRepository;
        this.auditService = auditService;
    }

    public List<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    public Empresa findById(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + id));
    }

    public Empresa crearEmpresa(Empresa empresa, AuthenticatedUserContext actor, String ipOrigen) {
        if (empresaRepository.findByNit(empresa.getNit()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una empresa con ese NIT");
        }
        Empresa saved = empresaRepository.save(empresa);
        auditService.log(actor.userId(), actor.username(), "CREAR_EMPRESA", "Se registro empresa NIT: " + saved.getNit(), ipOrigen);
        return saved;
    }

    public Empresa actualizarEmpresa(Long id, Empresa datosUpdate, AuthenticatedUserContext actor, String ipOrigen) {
        Empresa empresa = findById(id);
        empresa.setNit(datosUpdate.getNit());
        empresa.setNombre(datosUpdate.getNombre());
        empresa.setUbicacionOficina(datosUpdate.getUbicacionOficina());
        empresa.setActiva(datosUpdate.isActiva());

        empresaRepository.update(empresa);
        auditService.log(actor.userId(), actor.username(), "MODIFICAR_EMPRESA", "Se actualizo empresa ID: " + id, ipOrigen);
        return empresa;
    }

    public void eliminarEmpresa(Long id, AuthenticatedUserContext actor, String ipOrigen) {
        Empresa empresa = findById(id);
        empresaRepository.deleteById(id);
        auditService.log(actor.userId(), actor.username(), "ELIMINAR_EMPRESA", "Se elimino empresa ID: " + id + " (NIT: " + empresa.getNit() + ")", ipOrigen);
    }
}
