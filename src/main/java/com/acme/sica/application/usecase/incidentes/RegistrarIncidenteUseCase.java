package com.acme.sica.application.usecase.incidentes;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.application.port.out.IncidenteRepository;
import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.application.dto.IncidenteDTO;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.application.AuthenticatedUserContext;

import java.time.LocalDateTime;
import java.util.List;

public class RegistrarIncidenteUseCase {

    private final IncidenteRepository incidenteRepository;
    private final PersonaRepository personaRepository;
    private final AuditService auditService;

    public RegistrarIncidenteUseCase(IncidenteRepository incidenteRepository, PersonaRepository personaRepository, AuditService auditService) {
        this.incidenteRepository = incidenteRepository;
        this.personaRepository = personaRepository;
        this.auditService = auditService;
    }

    public Incidente registrarIncidente(IncidenteDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        Persona persona = personaRepository.findById(dto.personaId())
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + dto.personaId()));

        Incidente incidente = new Incidente();
        incidente.setPersonaId(persona.getId());
        incidente.setReportadoPorUsuarioId(actor.userId());
        incidente.setTitulo(dto.titulo());
        incidente.setDescripcion(dto.descripcion());
        incidente.setNivelGravedad(dto.nivelGravedad());
        incidente.setFechaHora(LocalDateTime.now());

        Incidente guardado = incidenteRepository.save(incidente);

        // REGLA DE NEGOCIO CRITICA: Al registrar un incidente (gravedad ALTO o CRITICO), cambiar inmediatamente estadoAcceso de la persona a RESTRINGIDO
        personaRepository.updateEstadoAcceso(persona.getId(), EstadoAcceso.RESTRINGIDO);

        auditService.log(actor.userId(), actor.username(), "REGISTRO_INCIDENTE",
                "Incidente '" + guardado.getTitulo() + "' registrado para persona doc: " + persona.getDocIdentidad() +
                        ". Estado cambiado a RESTRINGIDO.", ipOrigen);

        return guardado;
    }

    public List<Incidente> listarTodos() {
        return incidenteRepository.findAll();
    }
}
