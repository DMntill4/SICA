package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.enums.NivelGravedad;
import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.infrastructure.db.SchemaInitializer;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;
import com.acme.sica.infrastructure.db.connection.H2ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncidenteJdbcAdapterTest {

    private IncidenteJdbcAdapter adapter;
    private PersonaJdbcAdapter personaAdapter;

    @BeforeEach
    void setUp() {
        ConnectionFactory connectionFactory = new H2ConnectionFactory();
        SchemaInitializer initializer = new SchemaInitializer(connectionFactory);
        initializer.initialize();

        adapter = new IncidenteJdbcAdapter(connectionFactory);
        personaAdapter = new PersonaJdbcAdapter(connectionFactory);
    }

    @Test
    void testGuardarYListarIncidentes() {
        Persona p = new Persona();
        p.setDocIdentidad("99887766");
        p.setTipoDocumento("CC");
        p.setNombre("Prueba");
        p.setApellido("Incidente");
        p.setEstadoAcceso(EstadoAcceso.HABILITADO);
        Persona guardadaP = personaAdapter.save(p);

        Incidente inc = new Incidente();
        inc.setPersonaId(guardadaP.getId());
        inc.setReportadoPorUsuarioId(2L);
        inc.setTitulo("Intento de acceso forzado");
        inc.setDescripcion("Persona intento ingresar sin autorizacion a zona restringida");
        inc.setNivelGravedad(NivelGravedad.ALTO);
        inc.setFechaHora(LocalDateTime.now());

        Incidente guardado = adapter.save(inc);
        assertNotNull(guardado.getId(), "El adapter debe asignar ID autonumerico en INSERT SQL");

        List<Incidente> todos = adapter.findAll();
        assertFalse(todos.isEmpty(), "findAll debe retornar la lista de incidentes en BD");
    }
}
