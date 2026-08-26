package com.acme.sica.usecase.visitas;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoCierreVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.domain.port.PersonaRepository;
import com.acme.sica.domain.port.VisitaRepository;
import com.acme.sica.infrastructure.adapter.in.dto.CheckInDTO;
import com.acme.sica.infrastructure.adapter.in.dto.PaseTemporalDTO;
import com.acme.sica.infrastructure.adapter.in.dto.PreregistroVisitaDTO;
import com.acme.sica.infrastructure.adapter.in.dto.VisitaNoAnunciadaDTO;
import com.acme.sica.infrastructure.audit.AuditService;
import com.acme.sica.infrastructure.security.AuthenticatedUserContext;
import com.acme.sica.usecase.visitas.strategy.AccessValidationStrategy;
import com.acme.sica.usecase.visitas.strategy.PreRegisteredValidationStrategy;
import com.acme.sica.usecase.visitas.strategy.RestrictedPersonValidationStrategy;
import com.acme.sica.usecase.visitas.strategy.UnannouncedValidationStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GestionarVisitaUseCase {

    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;
    private final AuditService auditService;

    public GestionarVisitaUseCase(VisitaRepository visitaRepository, PersonaRepository personaRepository, AuditService auditService) {
        this.visitaRepository = visitaRepository;
        this.personaRepository = personaRepository;
        this.auditService = auditService;
    }

    public Visita preregistrarVisita(PreregistroVisitaDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        Visita visita = VisitaFactory.createVisita(TipoVisita.PRE_REGISTRADA, dto.personaId(), actor.userId(), dto.motivo(), dto.fechaHoraProgramada());
        Visita guardada = visitaRepository.save(visita);
        
        Persona persona = personaRepository.findById(dto.personaId()).orElse(null);
        String doc = persona != null ? persona.getDocIdentidad() : String.valueOf(dto.personaId());
        
        auditService.log(actor.userId(), actor.username(), "PREREGISTRO_VISITA",
                "Visita pre-registrada ID " + guardada.getId() + " para persona " + doc, ipOrigen);
        return guardada;
    }

    public Visita registrarNoAnunciada(VisitaNoAnunciadaDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        Visita visita = VisitaFactory.createVisita(TipoVisita.NO_ANUNCIADA, dto.personaId(), dto.funcionarioId(), dto.motivo(), null);
        visita.setGuardiaIngresoId(actor.userId());
        if (dto.puntoAccesoIngresoId() != null) {
            visita.setPuntoAccesoIngresoId(dto.puntoAccesoIngresoId());
        }

        Visita guardada = visitaRepository.save(visita);
        auditService.log(actor.userId(), actor.username(), "REGISTRO_NO_ANUNCIADA",
                "Visita no anunciada ID " + guardada.getId() + " registrada (PENDIENTE_APROBACION)", ipOrigen);
        return guardada;
    }

    public Visita registrarPaseTemporal(PaseTemporalDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        Visita visita = VisitaFactory.createVisita(TipoVisita.PASE_TEMPORAL, dto.personaId(), dto.funcionarioId(), dto.motivo(), null);
        visita.setGuardiaIngresoId(actor.userId());
        if (dto.puntoAccesoIngresoId() != null) {
            visita.setPuntoAccesoIngresoId(dto.puntoAccesoIngresoId());
        }

        Visita guardada = visitaRepository.save(visita);
        auditService.log(actor.userId(), actor.username(), "REGISTRO_PASE_TEMPORAL",
                "Pase temporal ID " + guardada.getId() + " registrado por carnet olvidado (PENDIENTE_APROBACION_OLVIDO)", ipOrigen);
        return guardada;
    }

    public Visita aprobarVisita(Long visitaId, AuthenticatedUserContext actor, String ipOrigen) {
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada con ID: " + visitaId));

        if (visita.getEstadoVisita() != EstadoVisita.PENDIENTE_APROBACION &&
            visita.getEstadoVisita() != EstadoVisita.PENDIENTE_APROBACION_OLVIDO) {
            throw new IllegalStateException("La visita no requiere aprobacion o ya fue procesada. Estado: " + visita.getEstadoVisita());
        }

        visita.setEstadoVisita(EstadoVisita.APROBADO);
        visitaRepository.update(visita);
        auditService.log(actor.userId(), actor.username(), "APROBAR_VISITA", "Visita ID " + visitaId + " aprobada por funcionario", ipOrigen);
        return visita;
    }

    public Visita rechazarVisita(Long visitaId, AuthenticatedUserContext actor, String ipOrigen) {
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada con ID: " + visitaId));

        visita.setEstadoVisita(EstadoVisita.RECHAZADO);
        visitaRepository.update(visita);
        auditService.log(actor.userId(), actor.username(), "RECHAZAR_VISITA", "Visita ID " + visitaId + " rechazada por funcionario", ipOrigen);
        return visita;
    }

    public Visita checkIn(Long visitaId, CheckInDTO dto, AuthenticatedUserContext guardiaContext, String ipOrigen) {
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada con ID: " + visitaId));

        Persona persona = personaRepository.findById(visita.getPersonaId())
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada asociada a la visita"));

        // 1. Validar restriccion por incidente
        new RestrictedPersonValidationStrategy().validate(persona, visita);

        // 2. Validar estrategia segun tipo de visita
        AccessValidationStrategy strategy = visita.getTipoVisita() == TipoVisita.PRE_REGISTRADA
                ? new PreRegisteredValidationStrategy()
                : new UnannouncedValidationStrategy();
        strategy.validate(persona, visita);

        // 3. REGLA 3.3.d: Regularizacion de Salida Olvidada (CERRADA_POR_SISTEMA)
        Optional<Visita> visitaActivaPrevia = visitaRepository.findLatestActiveVisitByPersonaId(persona.getId());
        if (visitaActivaPrevia.isPresent()) {
            Visita previa = visitaActivaPrevia.get();
            previa.setEstadoVisita(EstadoVisita.FINALIZADO);
            previa.setTipoCierre(TipoCierreVisita.CERRADA_POR_SISTEMA);
            previa.setFechaHoraSalida(LocalDateTime.now());
            visitaRepository.update(previa);

            auditService.logSalidaOlvidada(persona.getId(), persona.getDocIdentidad(), previa.getId(), ipOrigen);
        }

        // 4. Marcar ingreso
        visita.setEstadoVisita(EstadoVisita.DENTRO);
        visita.setFechaHoraIngreso(LocalDateTime.now());
        visita.setGuardiaIngresoId(guardiaContext.userId());
        if (dto != null && dto.puntoAccesoId() != null) {
            visita.setPuntoAccesoIngresoId(dto.puntoAccesoId());
        } else if (visita.getPuntoAccesoIngresoId() == null) {
            visita.setPuntoAccesoIngresoId(1L); // Default recepcion
        }

        visitaRepository.update(visita);

        auditService.log(guardiaContext.userId(), guardiaContext.username(), "CHECK_IN",
                "Check-in registrado para persona con doc: " + persona.getDocIdentidad() +
                        " en PuntoAcceso #" + visita.getPuntoAccesoIngresoId(), ipOrigen);

        return visita;
    }

    public Visita checkOut(Long visitaId, AuthenticatedUserContext guardiaContext, String ipOrigen) {
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada con ID: " + visitaId));

        if (visita.getEstadoVisita() != EstadoVisita.DENTRO) {
            throw new IllegalStateException("La visita no se encuentra en estado DENTRO. Estado actual: " + visita.getEstadoVisita());
        }

        visita.setEstadoVisita(EstadoVisita.FINALIZADO);
        visita.setTipoCierre(TipoCierreVisita.NORMAL);
        visita.setFechaHoraSalida(LocalDateTime.now());
        visita.setGuardiaSalidaId(guardiaContext.userId());

        visitaRepository.update(visita);

        Persona persona = personaRepository.findById(visita.getPersonaId()).orElse(null);
        String doc = persona != null ? persona.getDocIdentidad() : String.valueOf(visita.getPersonaId());

        auditService.log(guardiaContext.userId(), guardiaContext.username(), "CHECK_OUT",
                "Check-out normal registrado para persona doc: " + doc, ipOrigen);

        return visita;
    }

    public List<Visita> findAll() {
        return visitaRepository.findAll();
    }

    public void limpiarVisitas(AuthenticatedUserContext actor, String ipOrigen) {
        visitaRepository.deleteAll();
        auditService.log(actor.userId(), actor.username(), "LIMPIAR_VISITAS", "El administrador limpio todo el historial de visitas", ipOrigen);
    }

    public Visita findById(Long id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada con ID: " + id));
    }
}
