package com.acme.sica.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDomainTest {

    @Test
    void testReglaDeBloqueoPorIntentosFallidos() {
        Usuario u = new Usuario();
        u.setUsername("guardia1");
        u.setIntentosFallidos(0);
        u.setBloqueado(false);

        assertFalse(u.isBloqueado());

        u.setIntentosFallidos(3);
        u.setBloqueado(true);

        assertTrue(u.isBloqueado());
        assertEquals(3, u.getIntentosFallidos());
    }

    @Test
    void testResetIntentosFallidos() {
        Usuario u = new Usuario();
        u.setIntentosFallidos(2);

        u.setIntentosFallidos(0);
        assertEquals(0, u.getIntentosFallidos());
    }
}
