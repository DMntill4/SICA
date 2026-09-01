package com.acme.sica.domain.model;

import com.acme.sica.domain.enums.EstadoAcceso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonaDomainTest {

    @Test
    void testEstadoAccesoHabilitadoYRestringido() {
        Persona p = new Persona();
        p.setEstadoAcceso(EstadoAcceso.HABILITADO);

        assertEquals(EstadoAcceso.HABILITADO, p.getEstadoAcceso());

        p.setEstadoAcceso(EstadoAcceso.RESTRINGIDO);

        assertEquals(EstadoAcceso.RESTRINGIDO, p.getEstadoAcceso());
    }

    @Test
    void testVectorBiometricoYFotoUrl() {
        Persona p = new Persona();
        p.setVectorBiometrico("[0.12, 0.45, -0.89]");
        p.setFotoUrl("http://localhost/fotos/persona1.jpg");

        assertEquals("[0.12, 0.45, -0.89]", p.getVectorBiometrico());
        assertEquals("http://localhost/fotos/persona1.jpg", p.getFotoUrl());
    }
}
