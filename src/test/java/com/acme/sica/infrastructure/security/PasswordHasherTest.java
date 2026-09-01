package com.acme.sica.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new PasswordHasher();
    }

    @Test
    void testHashPasswordYVerificarCorrecto() {
        String raw = "MiClaveSegura123";
        String hash = passwordHasher.hashPassword(raw);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));

        assertTrue(passwordHasher.verifyPassword(raw, hash), "verifyPassword debe retornar true para la clave correcta");
        assertFalse(passwordHasher.verifyPassword("ClaveIncorreta", hash), "verifyPassword debe retornar false para clave incorrecta");
    }

    @Test
    void testVerificarCredencialesEstaticasSemilla() {
        String hashSemilla = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        assertTrue(passwordHasher.verifyPassword("admin123", hashSemilla));
        assertTrue(passwordHasher.verifyPassword("guardia123", hashSemilla));
        assertTrue(passwordHasher.verifyPassword("func123", hashSemilla));
        assertFalse(passwordHasher.verifyPassword("claveFalsa", hashSemilla));
    }
}
