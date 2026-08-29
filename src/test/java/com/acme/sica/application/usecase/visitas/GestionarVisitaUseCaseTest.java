package com.acme.sica.application.usecase.visitas;

import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.dto.CheckInDTO;
import com.acme.sica.application.dto.PaseTemporalDTO;
import com.acme.sica.application.dto.PreregistroVisitaDTO;
import com.acme.sica.application.dto.VisitaNoAnunciadaDTO;
import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.application.port.out.VisitaRepository;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GestionarVisitaUseCaseTest {

    @Mock
    private VisitaRepository visitaRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private GestionarVisitaUseCase useCase;

    private AuthenticatedUserContext actorFuncionario;
    private AuthenticatedUserContext actorGuardia;
    private Persona persona;

    @BeforeEach
    void setUp() {
        actorFuncionario = new AuthenticatedUserContext(1L, "func1", 3L, "Funcionario", Set.of(), "token");
        actorGuardia = new AuthenticatedUserContext(2L, "guardia1", 2L, "Guardia", Set.of(), "token");
        
        persona = new Persona();
        persona.setId(10L);
        persona.setEstadoAcceso(EstadoAcceso.HABILITADO);
    }

    @Test
    void testPreregistrarVisita() {
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(visitaRepository.save(any(Visita.class))).thenAnswer(i -> {
            Visita v = i.getArgument(0);
            v.setId(100L);
            return v;
        });

        PreregistroVisitaDTO dto = new PreregistroVisitaDTO(10L, "Reunión", LocalDateTime.now().plusDays(1));
        Visita result = useCase.preregistrarVisita(dto, actorFuncionario, "127.0.0.1");

        assertNotNull(result);
        assertEquals(TipoVisita.PRE_REGISTRADA, result.getTipoVisita());
        assertEquals(EstadoVisita.APROBADO, result.getEstadoVisita());
        verify(auditService).log(eq(1L), eq("func1"), eq("PREREGISTRO_VISITA"), anyString(), eq("127.0.0.1"));
    }

    @Test
    void testCheckInVisitaAprobada() {
        Visita visita = new Visita();
        visita.setId(100L);
        visita.setPersonaId(10L);
        visita.setEstadoVisita(EstadoVisita.APROBADO);
        
        when(visitaRepository.findById(100L)).thenReturn(Optional.of(visita));
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(visitaRepository.findLatestActiveVisitByPersonaId(10L)).thenReturn(Optional.empty());

        CheckInDTO dto = new CheckInDTO(1L);
        Visita result = useCase.checkIn(100L, dto, actorGuardia, "127.0.0.1");

        assertEquals(EstadoVisita.DENTRO, result.getEstadoVisita());
        assertNotNull(result.getFechaHoraIngreso());
        verify(visitaRepository).update(visita);
        verify(auditService).log(eq(2L), eq("guardia1"), eq("CHECK_IN"), anyString(), eq("127.0.0.1"));
    }

    @Test
    void testRegistrarVisitaNoAnunciada() {
        lenient().when(visitaRepository.save(any(Visita.class))).thenAnswer(i -> {
            Visita v = i.getArgument(0);
            v.setId(101L);
            return v;
        });

        VisitaNoAnunciadaDTO dto = new VisitaNoAnunciadaDTO(10L, 1L, 1L, "Entrevista");
        Visita result = useCase.registrarNoAnunciada(dto, actorGuardia, "127.0.0.1");

        assertEquals(TipoVisita.NO_ANUNCIADA, result.getTipoVisita());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION, result.getEstadoVisita());
    }

    @Test
    void testAprobarVisitaNoAnunciada() {
        Visita visita = new Visita();
        visita.setId(101L);
        visita.setFuncionarioId(1L);
        visita.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION);

        when(visitaRepository.findById(101L)).thenReturn(Optional.of(visita));

        Visita result = useCase.aprobarVisita(101L, actorFuncionario, "127.0.0.1");

        assertEquals(EstadoVisita.APROBADO, result.getEstadoVisita());
        verify(visitaRepository).update(visita);
    }

    @Test
    void testRegistrarPaseTemporalOlvido() {
        lenient().when(visitaRepository.save(any(Visita.class))).thenAnswer(i -> {
            Visita v = i.getArgument(0);
            v.setId(102L);
            return v;
        });

        PaseTemporalDTO dto = new PaseTemporalDTO(10L, 1L, 1L, "Olvidó carnet");
        Visita result = useCase.registrarPaseTemporal(dto, actorGuardia, "127.0.0.1");

        assertEquals(TipoVisita.PASE_TEMPORAL, result.getTipoVisita());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION_OLVIDO, result.getEstadoVisita());
    }

    @Test
    void testCheckInImpideIngresoSinAprobacion() {
        Visita visita = new Visita();
        visita.setId(101L);
        visita.setPersonaId(10L);
        visita.setTipoVisita(TipoVisita.PRE_REGISTRADA);
        visita.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION);
        
        when(visitaRepository.findById(101L)).thenReturn(Optional.of(visita));
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));

        CheckInDTO dto = new CheckInDTO(1L);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            useCase.checkIn(101L, dto, actorGuardia, "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("APROBADO"));
        verify(auditService, never()).log(anyLong(), anyString(), eq("CHECK_IN"), anyString(), anyString());
    }

    @Test
    void testPersonaBloqueadaNoPuedeIngresar() {
        Visita visita = new Visita();
        visita.setId(101L);
        visita.setPersonaId(10L);
        visita.setEstadoVisita(EstadoVisita.APROBADO);
        
        persona.setEstadoAcceso(EstadoAcceso.RESTRINGIDO);
        
        when(visitaRepository.findById(101L)).thenReturn(Optional.of(visita));
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));

        CheckInDTO dto = new CheckInDTO(1L);
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            useCase.checkIn(101L, dto, actorGuardia, "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("RESTRINGIDO"));
        verify(auditService, never()).log(anyLong(), anyString(), eq("CHECK_IN"), anyString(), anyString());
    }
}
