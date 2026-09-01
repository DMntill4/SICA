package com.acme.sica.infrastructure.adapter.out.persistence;

import com.acme.sica.domain.model.SolicitudPase;
import com.acme.sica.infrastructure.db.SchemaInitializer;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;
import com.acme.sica.infrastructure.db.connection.H2ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JdbcSolicitudPaseRepositoryTest {

    private JdbcSolicitudPaseRepository repository;

    @BeforeEach
    void setUp() {
        ConnectionFactory connectionFactory = new H2ConnectionFactory();
        SchemaInitializer initializer = new SchemaInitializer(connectionFactory);
        initializer.initialize();

        repository = new JdbcSolicitudPaseRepository(connectionFactory);
    }

    @Test
    void testGuardarConsultarYActualizarSolicitudPasePortalWeb() {
        SolicitudPase sol = new SolicitudPase();
        sol.setNombreCompleto("Carlos Gomez");
        sol.setDocIdentidad("87654321");
        sol.setEmail("carlos@example.com");
        sol.setTelefono("555-9999");
        sol.setEmpresaDestino("Acme Corp");
        sol.setFuncionarioDestinoId(3L);
        sol.setMotivo("Entrevista Tecnica");
        sol.setFechaHoraSolicitada(LocalDateTime.now().plusDays(1));
        sol.setEstado(SolicitudPase.EstadoSolicitud.PENDIENTE_APROBACION);

        SolicitudPase guardada = repository.guardar(sol);
        assertNotNull(guardada.getId(), "El repositorio web debe asignar ID autonumerico en SQL");

        SolicitudPase hallada = repository.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals("Carlos Gomez", hallada.getNombreCompleto());
        assertEquals("87654321", hallada.getDocIdentidad());
        assertEquals(SolicitudPase.EstadoSolicitud.PENDIENTE_APROBACION, hallada.getEstado());

        repository.actualizarEstado(guardada.getId(), SolicitudPase.EstadoSolicitud.APROBADO);

        SolicitudPase actualizada = repository.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(SolicitudPase.EstadoSolicitud.APROBADO, actualizada.getEstado());
    }
}
