package com.acme.sica.domain.model;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoCierreVisita;
import com.acme.sica.domain.enums.TipoVisita;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VisitaDomainTest {

    @Test
    void testEstadoInicialYAsignacionCampos() {
        Visita v = new Visita();
        v.setId(10L);
        v.setPersonaId(5L);
        v.setFuncionarioId(2L);
        v.setTipoVisita(TipoVisita.PRE_REGISTRADA);
        v.setEstadoVisita(EstadoVisita.APROBADO);
        v.setMotivo("Reunion Comercial");

        assertEquals(10L, v.getId());
        assertEquals(5L, v.getPersonaId());
        assertEquals(2L, v.getFuncionarioId());
        assertEquals(TipoVisita.PRE_REGISTRADA, v.getTipoVisita());
        assertEquals(EstadoVisita.APROBADO, v.getEstadoVisita());
        assertEquals("Reunion Comercial", v.getMotivo());
    }

    @Test
    void testMarcarIngresoYSalida() {
        Visita v = new Visita();
        v.setEstadoVisita(EstadoVisita.APROBADO);

        LocalDateTime now = LocalDateTime.now();
        v.setFechaHoraIngreso(now);
        v.setEstadoVisita(EstadoVisita.DENTRO);
        v.setGuardiaIngresoId(1L);
        v.setPuntoAccesoIngresoId(2L);

        assertEquals(EstadoVisita.DENTRO, v.getEstadoVisita());
        assertEquals(now, v.getFechaHoraIngreso());
        assertEquals(1L, v.getGuardiaIngresoId());
        assertEquals(2L, v.getPuntoAccesoIngresoId());

        v.setFechaHoraSalida(now.plusHours(1));
        v.setEstadoVisita(EstadoVisita.FINALIZADO);
        v.setTipoCierre(TipoCierreVisita.NORMAL);
        v.setGuardiaSalidaId(1L);

        assertEquals(EstadoVisita.FINALIZADO, v.getEstadoVisita());
        assertEquals(TipoCierreVisita.NORMAL, v.getTipoCierre());
    }
}
