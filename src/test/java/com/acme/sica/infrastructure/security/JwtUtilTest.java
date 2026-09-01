package com.acme.sica.infrastructure.security;

import com.acme.sica.domain.model.Usuario;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerarYVerificarTokenJWTCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setUsername("admin");
        usuario.setRolId(1L);
        usuario.setRolNombre("ADMINISTRADOR");

        String token = jwtUtil.generateToken(usuario);

        assertNotNull(token, "El token generado no debe ser nulo");
        assertFalse(token.trim().isEmpty(), "El token generado no debe estar vacio");

        DecodedJWT jwt = jwtUtil.verifyToken(token);

        assertEquals("admin", jwt.getSubject(), "El subject del JWT debe ser el username");
        assertEquals(10L, jwt.getClaim("userId").asLong(), "El claim 'userId' debe coincidir");
        assertEquals(1L, jwt.getClaim("roleId").asLong(), "El claim 'roleId' debe coincidir");
        assertEquals("ADMINISTRADOR", jwt.getClaim("roleName").asString(), "El claim 'roleName' debe coincidir");
        assertNotNull(jwt.getId(), "El JWT debe contener un identificador único jti");
    }

    @Test
    void testFirmaJWTInvalidaLanzaExcepcion() {
        String tokenAlterado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiJ9.firma_falsa";

        assertThrows(Exception.class, () -> jwtUtil.verifyToken(tokenAlterado),
                "Un token JWT alterado o con firma invalida debe lanzar excepcion");
    }
}
