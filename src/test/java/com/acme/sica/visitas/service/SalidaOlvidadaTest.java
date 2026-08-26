package com.acme.sica.visitas.service;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoCierreVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.domain.port.AuditRepository;
import com.acme.sica.domain.port.PersonaRepository;
import com.acme.sica.domain.port.VisitaRepository;
import com.acme.sica.infrastructure.adapter.in.dto.CheckInDTO;
import com.acme.sica.infrastructure.audit.AuditService;
import com.acme.sica.infrastructure.security.AuthenticatedUserContext;
import com.acme.sica.usecase.visitas.GestionarVisitaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalidaOlvidadaTest {

    private VisitaRepository visitaRepository;
    private PersonaRepository personaRepository;
    private AuditService auditService;
    private GestionarVisitaUseCase visitaUseCase;

    @BeforeEach
    void setUp() {
        visitaRepository = mock(VisitaRepository.class);
        personaRepository = mock(PersonaRepository.class);
        AuditRepository auditRepository = mock(AuditRepository.class);
        auditService = spy(new AuditService(auditRepository));
        visitaUseCase = new GestionarVisitaUseCase(visitaRepository, personaRepository, auditService);
    }

    @Test
    @DisplayName("Debe regularizar automaticamente una salida olvidada (CERRADA_POR_SISTEMA) al hacer un nuevo check-in")
    void testCheckIn_SalidaOlvidadaAutoClose() {
        Long personaId = 100L;
        Long visitaPreviaId = 50L;
        Long nuevaVisitaId = 51L;

        Persona persona = new Persona();
        persona.setId(personaId);
        persona.setDocIdentidad("12345678");
        persona.setEstadoAcceso(EstadoAcceso.HABILITADO);

        Visita visitaPrevia = new Visita();
        visitaPrevia.setId(visitaPreviaId);
        visitaPrevia.setPersonaId(personaId);
        visitaPrevia.setEstadoVisita(EstadoVisita.DENTRO);

        Visita nuevaVisita = new Visita();
        nuevaVisita.setId(nuevaVisitaId);
        nuevaVisita.setPersonaId(personaId);
        nuevaVisita.setTipoVisita(TipoVisita.PRE_REGISTRADA);
        nuevaVisita.setEstadoVisita(EstadoVisita.APROBADO);

        when(visitaRepository.findById(nuevaVisitaId)).thenReturn(Optional.of(nuevaVisita));
        when(personaRepository.findById(personaId)).thenReturn(Optional.of(persona));
        when(visitaRepository.findLatestActiveVisitByPersonaId(personaId)).thenReturn(Optional.of(visitaPrevia));

        AuthenticatedUserContext guardia = new AuthenticatedUserContext(2L, "guardia1", 2L, "GUARDIA", null, "jti-123");

        Visita result = visitaUseCase.checkIn(nuevaVisitaId, new CheckInDTO(1L), guardia, "127.0.0.1");

        // 1. Verificar que la visita previa se cerro automaticamente como CERRADA_POR_SISTEMA
        assertEquals(EstadoVisita.FINALIZADO, visitaPrevia.getEstadoVisita());
        assertEquals(TipoCierreVisita.CERRADA_POR_SISTEMA, visitaPrevia.getTipoCierre());
        verify(visitaRepository).update(visitaPrevia);
        verify(auditService).logSalidaOlvidada(eq(personaId), eq("12345678"), eq(visitaPreviaId), anyString());

        // 2. Verificar que la nueva visita quedo en DENTRO
        assertEquals(EstadoVisita.DENTRO, result.getEstadoVisita());
        verify(visitaRepository).update(nuevaVisita);
    }
}
