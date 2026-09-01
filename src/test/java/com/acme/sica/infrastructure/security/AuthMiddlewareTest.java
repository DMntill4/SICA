package com.acme.sica.infrastructure.security;

import com.acme.sica.application.port.out.UsuarioRepository;
import com.acme.sica.infrastructure.adapter.in.http.router.Route;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthMiddlewareTest {

    private JwtUtil jwtUtil;
    private UsuarioRepository usuarioRepository;
    private PermissionChecker permissionChecker;
    private AuthMiddleware authMiddleware;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        usuarioRepository = mock(UsuarioRepository.class);
        permissionChecker = mock(PermissionChecker.class);
        authMiddleware = new AuthMiddleware(jwtUtil, usuarioRepository, permissionChecker);
    }

    @Test
    void testPermitirAccesoCuandoUsuarioTienePermisoRequerido() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = new Headers();
        headers.set("Authorization", "Bearer token_valido");
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/visitas/1/check-in"));

        DecodedJWT jwt = mock(DecodedJWT.class);
        when(jwtUtil.verifyToken("token_valido")).thenReturn(jwt);
        when(jwt.getId()).thenReturn("jti_123");
        when(jwt.getSubject()).thenReturn("guardia1");

        com.auth0.jwt.interfaces.Claim roleIdClaim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(roleIdClaim.asLong()).thenReturn(2L);
        when(jwt.getClaim("roleId")).thenReturn(roleIdClaim);

        com.auth0.jwt.interfaces.Claim userIdClaim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(userIdClaim.asLong()).thenReturn(2L);
        when(jwt.getClaim("userId")).thenReturn(userIdClaim);

        com.auth0.jwt.interfaces.Claim roleNameClaim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(roleNameClaim.asString()).thenReturn("GUARDIA");
        when(jwt.getClaim("roleName")).thenReturn(roleNameClaim);

        when(usuarioRepository.isTokenRevoked("jti_123")).thenReturn(false);
        when(permissionChecker.getPermissions(2L)).thenReturn(Set.of("checkin_visita"));
        when(permissionChecker.hasPermission(2L, "checkin_visita")).thenReturn(true);

        Route route = new Route("POST", "/api/visitas/1/check-in", null, true, "checkin_visita");

        boolean permitido = authMiddleware.intercept(exchange, route);

        assertTrue(permitido, "El middleware debe PERMITIR el acceso cuando el usuario TIENE el permiso requerido");
    }

    @Test
    void testDenegarAccesoCuandoUsuarioNoTienePermisoRequerido() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = new Headers();
        headers.set("Authorization", "Bearer token_valido");
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/usuarios/1"));

        DecodedJWT jwt = mock(DecodedJWT.class);
        when(jwtUtil.verifyToken("token_valido")).thenReturn(jwt);
        when(jwt.getId()).thenReturn("jti_123");
        when(jwt.getSubject()).thenReturn("guardia1");

        com.auth0.jwt.interfaces.Claim roleIdClaim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(roleIdClaim.asLong()).thenReturn(2L);
        when(jwt.getClaim("roleId")).thenReturn(roleIdClaim);

        com.auth0.jwt.interfaces.Claim userIdClaim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(userIdClaim.asLong()).thenReturn(2L);
        when(jwt.getClaim("userId")).thenReturn(userIdClaim);

        com.auth0.jwt.interfaces.Claim roleNameClaim = mock(com.auth0.jwt.interfaces.Claim.class);
        when(roleNameClaim.asString()).thenReturn("GUARDIA");
        when(jwt.getClaim("roleName")).thenReturn(roleNameClaim);

        when(usuarioRepository.isTokenRevoked("jti_123")).thenReturn(false);
        when(permissionChecker.getPermissions(2L)).thenReturn(Set.of("checkin_visita"));
        when(permissionChecker.hasPermission(2L, "eliminar_usuario")).thenReturn(false);

        Route route = new Route("DELETE", "/api/usuarios/1", null, true, "eliminar_usuario");

        boolean permitido = authMiddleware.intercept(exchange, route);

        assertFalse(permitido, "El middleware debe DENEGAR el acceso cuando el usuario NO tiene el permiso");
    }
}
