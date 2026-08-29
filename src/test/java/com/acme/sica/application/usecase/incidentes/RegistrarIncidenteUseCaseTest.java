package com.acme.sica.application.usecase.incidentes;

import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.dto.IncidenteDTO;
import com.acme.sica.application.port.out.IncidenteRepository;
import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.enums.NivelGravedad;
import com.acme.sica.domain.model.Incidente;
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
public class RegistrarIncidenteUseCaseTest {

    @Mock
    private IncidenteRepository incidenteRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RegistrarIncidenteUseCase useCase;

    private AuthenticatedUserContext actor;
    private Persona persona;

    @BeforeEach
    void setUp() {
        actor = new AuthenticatedUserContext(1L, "guardia", 1L, "Guardia", Set.of(), "token");
        
        persona = new Persona();
        persona.setId(10L);
        persona.setDocIdentidad("12345");
        persona.setEstadoAcceso(EstadoAcceso.HABILITADO);
    }

    @Test
    void testRegistrarIncidenteCambiaEstado() {
        when(personaRepository.findById(10L)).thenReturn(Optional.of(persona));
        when(incidenteRepository.save(any(Incidente.class))).thenAnswer(i -> {
            Incidente inc = i.getArgument(0);
            inc.setId(100L);
            return inc;
        });

        IncidenteDTO dto = new IncidenteDTO(10L, "Pelea", "Pelea en lobby", NivelGravedad.ALTO);
        Incidente result = useCase.registrarIncidente(dto, actor, "127.0.0.1");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Pelea", result.getTitulo());

        // Verificar cambio de estado a RESTRINGIDO (INC-01, INC-02, AUD-06)
        verify(personaRepository, times(1)).updateEstadoAcceso(10L, EstadoAcceso.RESTRINGIDO);
        verify(auditService, times(1)).log(eq(1L), eq("guardia"), eq("REGISTRO_INCIDENTE"), anyString(), eq("127.0.0.1"));
    }
}
