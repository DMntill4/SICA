package com.acme.sica.application.usecase.personas;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.application.dto.PersonaDTO;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.application.AuthenticatedUserContext;

import java.util.List;

public class GestionarPersonaUseCase {

    private final PersonaRepository personaRepository;
    private final AuditService auditService;

    public GestionarPersonaUseCase(PersonaRepository personaRepository, AuditService auditService) {
        this.personaRepository = personaRepository;
        this.auditService = auditService;
    }

    public Persona registrarPersona(PersonaDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        if (personaRepository.findByDocIdentidad(dto.docIdentidad()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una persona registrada con el documento: " + dto.docIdentidad());
        }

        Persona persona = new Persona();
        persona.setDocIdentidad(dto.docIdentidad());
        persona.setTipoDocumento(dto.tipoDocumento());
        persona.setNombre(dto.nombre());
        persona.setApellido(dto.apellido());
        persona.setEmail(dto.email());
        persona.setTelefono(dto.telefono());
        persona.setEmpresaId(dto.empresaId());
        if (dto.fotoUrl() != null && !dto.fotoUrl().isEmpty()) {
            persona.setFotoUrl(dto.fotoUrl());
        }
        persona.setEstadoAcceso(EstadoAcceso.HABILITADO);

        Persona creada = personaRepository.save(persona);
        auditService.log(actor.userId(), actor.username(), "CREAR_PERSONA", "Persona registrada: " + creada.getDocIdentidad(), ipOrigen);
        return creada;
    }

    public Persona actualizarPersona(Long id, PersonaDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));

        persona.setTipoDocumento(dto.tipoDocumento());
        persona.setNombre(dto.nombre());
        persona.setApellido(dto.apellido());
        persona.setEmail(dto.email());
        persona.setTelefono(dto.telefono());
        persona.setEmpresaId(dto.empresaId());
        if (dto.fotoUrl() != null && !dto.fotoUrl().isEmpty()) {
            persona.setFotoUrl(dto.fotoUrl());
        }


        personaRepository.update(persona);
        auditService.log(actor.userId(), actor.username(), "ACTUALIZAR_PERSONA", "Persona actualizada: " + persona.getDocIdentidad(), ipOrigen);
        return persona;
    }

    public Persona buscarPorDocumento(String docIdentidad) {
        return personaRepository.findByDocIdentidad(docIdentidad)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con documento: " + docIdentidad));
    }

    public Persona buscarPorId(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));
    }

    public void eliminarPersona(Long id, AuthenticatedUserContext actor, String ipOrigen) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));
        personaRepository.deleteById(id);
        auditService.log(actor.userId(), actor.username(), "ELIMINAR_PERSONA", "Persona eliminada ID: " + id + " doc: " + persona.getDocIdentidad(), ipOrigen);
    }

    public Persona rehabilitarAcceso(Long id, AuthenticatedUserContext actor, String ipOrigen) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));
        persona.setEstadoAcceso(EstadoAcceso.HABILITADO);
        personaRepository.update(persona);
        auditService.log(actor.userId(), actor.username(), "REHABILITAR_ACCESO", "Acceso rehabilitado (HABILITADO) para persona doc: " + persona.getDocIdentidad(), ipOrigen);
        return persona;
    }

    public List<Persona> listarTodas() {
        return personaRepository.findAll();
    }
}
