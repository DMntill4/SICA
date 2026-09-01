package com.acme.sica.application.usecase.personas;

import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.dto.PersonaDTO;
import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.model.Persona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GestionarPersonaUseCaseTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private GestionarPersonaUseCase useCase;

    private AuthenticatedUserContext actor;
    private PersonaDTO dto;

    @BeforeEach
    void setUp() {
        actor = new AuthenticatedUserContext(1L, "admin", 1L, "Administrador", Set.of(), "token");
        dto = new PersonaDTO("12345678", "CC", "Juan", "Perez", "juan@test.com", "555-1234", 1L, null);
    }

    @Test
    void testCrearPersonaExitosa() {
        when(personaRepository.findByDocIdentidad("12345678")).thenReturn(Optional.empty());
        Persona personaGuardada = new Persona();
        personaGuardada.setId(1L);
        personaGuardada.setDocIdentidad("12345678");
        personaGuardada.setEstadoAcceso(EstadoAcceso.HABILITADO);
        when(personaRepository.save(any(Persona.class))).thenReturn(personaGuardada);

        Persona result = useCase.registrarPersona(dto, actor, "127.0.0.1");

        assertNotNull(result);
        assertEquals("12345678", result.getDocIdentidad());
        assertEquals(EstadoAcceso.HABILITADO, result.getEstadoAcceso());
        verify(personaRepository, times(1)).save(any(Persona.class));
        verify(auditService, times(1)).log(eq(1L), eq("admin"), eq("CREAR_PERSONA"), anyString(), eq("127.0.0.1"));
    }

    @Test
    void testCrearPersonaDuplicada() {
        when(personaRepository.findByDocIdentidad("12345678")).thenReturn(Optional.of(new Persona()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.registrarPersona(dto, actor, "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("Ya existe una persona registrada"));
        verify(personaRepository, never()).save(any(Persona.class));
    }
}
