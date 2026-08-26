package com.acme.sica.visitas.factory;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.usecase.visitas.VisitaFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VisitaFactoryTest {

    @Test
    @DisplayName("Debe crear una visita PRE_REGISTRADA con estado APROBADO")
    void testCreateVisita_PreRegistrada() {
        Visita visita = VisitaFactory.createVisita(TipoVisita.PRE_REGISTRADA, 10L, 1L, "Reunion de Negocios", LocalDateTime.now());

        assertNotNull(visita);
        assertEquals(TipoVisita.PRE_REGISTRADA, visita.getTipoVisita());
        assertEquals(EstadoVisita.APROBADO, visita.getEstadoVisita());
        assertEquals(10L, visita.getPersonaId());
    }

    @Test
    @DisplayName("Debe crear una visita NO_ANUNCIADA con estado PENDIENTE_APROBACION")
    void testCreateVisita_NoAnunciada() {
        Visita visita = VisitaFactory.createVisita(TipoVisita.NO_ANUNCIADA, 11L, 2L, "Entrevista de Trabajo", null);

        assertNotNull(visita);
        assertEquals(TipoVisita.NO_ANUNCIADA, visita.getTipoVisita());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION, visita.getEstadoVisita());
    }

    @Test
    @DisplayName("Debe crear una visita PASE_TEMPORAL con estado PENDIENTE_APROBACION_OLVIDO")
    void testCreateVisita_PaseTemporal() {
        Visita visita = VisitaFactory.createVisita(TipoVisita.PASE_TEMPORAL, 12L, 3L, "Carnet Olvidado", null);

        assertNotNull(visita);
        assertEquals(TipoVisita.PASE_TEMPORAL, visita.getTipoVisita());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION_OLVIDO, visita.getEstadoVisita());
    }
}
