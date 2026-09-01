package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.db.SchemaInitializer;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;
import com.acme.sica.infrastructure.db.connection.H2ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VisitaJdbcAdapterTest {

    private VisitaJdbcAdapter adapter;
    private PersonaJdbcAdapter personaAdapter;

    @BeforeEach
    void setUp() throws Exception {
        ConnectionFactory connectionFactory = new H2ConnectionFactory();
        SchemaInitializer initializer = new SchemaInitializer(connectionFactory);
        initializer.initialize();

        adapter = new VisitaJdbcAdapter(connectionFactory);
        personaAdapter = new PersonaJdbcAdapter(connectionFactory);
        adapter.deleteAll();
    }

    private Persona crearPersonaDummy(String doc) {
        Persona p = new Persona();
        p.setDocIdentidad(doc);
        p.setTipoDocumento("CC");
        p.setNombre("Juan");
        p.setApellido("Prueba");
        p.setEmail("juan@example.com");
        p.setEstadoAcceso(EstadoAcceso.HABILITADO);
        return personaAdapter.save(p);
    }

    @Test
    void testGuardarConsultarYActualizarVisita() {
        Persona p = crearPersonaDummy("11112222");

        Visita v = new Visita();
        v.setPersonaId(p.getId());
        v.setFuncionarioId(3L);
        v.setTipoVisita(TipoVisita.PRE_REGISTRADA);
        v.setEstadoVisita(EstadoVisita.APROBADO);
        v.setMotivo("Reunion de prueba JDBC");
        v.setFechaHoraProgramada(LocalDateTime.now());

        Visita guardada = adapter.save(v);
        assertNotNull(guardada.getId(), "El adapter debe asignar ID autonumerico en INSERT SQL");

        Optional<Visita> halladaOpt = adapter.findById(guardada.getId());
        assertTrue(halladaOpt.isPresent(), "findById debe retornar la visita guardada en BD");
        Visita hallada = halladaOpt.get();

        assertEquals(p.getId(), hallada.getPersonaId());
        assertEquals("Reunion de prueba JDBC", hallada.getMotivo());
        assertEquals(EstadoVisita.APROBADO, hallada.getEstadoVisita());

        hallada.setEstadoVisita(EstadoVisita.DENTRO);
        hallada.setFechaHoraIngreso(LocalDateTime.now());
        adapter.update(hallada);

        Optional<Visita> actualizadaOpt = adapter.findById(guardada.getId());
        assertTrue(actualizadaOpt.isPresent());
        assertEquals(EstadoVisita.DENTRO, actualizadaOpt.get().getEstadoVisita());
    }

    @Test
    void testBuscarUltimaVisitaActivaPorPersonaId() {
        Persona p = crearPersonaDummy("33334444");

        Visita v = new Visita();
        v.setPersonaId(p.getId());
        v.setFuncionarioId(3L);
        v.setTipoVisita(TipoVisita.PRE_REGISTRADA);
        v.setEstadoVisita(EstadoVisita.DENTRO);
        v.setFechaHoraIngreso(LocalDateTime.now());

        adapter.save(v);

        Optional<Visita> activaOpt = adapter.findLatestActiveVisitByPersonaId(p.getId());
        assertTrue(activaOpt.isPresent(), "findLatestActiveVisitByPersonaId debe encontrar visitas en estado DENTRO");
        assertEquals(EstadoVisita.DENTRO, activaOpt.get().getEstadoVisita());
    }
}
