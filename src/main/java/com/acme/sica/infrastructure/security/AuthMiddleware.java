package com.acme.sica.infrastructure.security;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.port.out.UsuarioRepository;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.acme.sica.infrastructure.adapter.in.http.router.Route;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Set;

public class AuthMiddleware {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final PermissionChecker permissionChecker;

    public AuthMiddleware(JwtUtil jwtUtil, UsuarioRepository usuarioRepository, PermissionChecker permissionChecker) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.permissionChecker = permissionChecker;
    }

    public boolean intercept(HttpExchange exchange, Route route) throws IOException {
        // REGLA DE SEGURIDAD: Si la ruta HTTP es publica (no requiere auth), permitir paso directo
        if (!route.requiresAuth()) {
            return true;
        }

        // REGLA DE SEGURIDAD: Extraer encabezado Authorization: Bearer <token>
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpUtils.sendErrorResponse(exchange, 401, "Acceso denegado: Token Bearer JWT no proporcionado");
            return false;
        }

        String token = authHeader.substring(7).trim();
        try {
            // INTENCION: Verificar firma JWT con clave secreta HMAC256
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            String jti = jwt.getId();

            // REGLA DE SEGURIDAD: Comprobar si el token jti se encuentra en la tabla negra token_revocado (logout)
            if (usuarioRepository.isTokenRevoked(jti)) {
                HttpUtils.sendErrorResponse(exchange, 401, "Acceso denegado: El token ha sido revocado (Sesion cerrada)");
                return false;
            }

            Long userId = jwt.getClaim("userId").asLong();
            String username = jwt.getSubject();
            Long roleId = jwt.getClaim("roleId").asLong();
            String roleName = jwt.getClaim("roleName").asString();

            Set<String> userPermissions = permissionChecker.getPermissions(roleId);

            // REGLA DE SEGURIDAD RBAC: Verificar si el rol del usuario posee el permiso especifico requerido por la ruta
            if (route.requiredPermission() != null && !permissionChecker.hasPermission(roleId, route.requiredPermission())) {
                HttpUtils.sendErrorResponse(exchange, 403, "Acceso prohibido: El rol '" + roleName + "' no posee el permiso '" + route.requiredPermission() + "'");
                return false;
            }

            AuthenticatedUserContext userContext = new AuthenticatedUserContext(userId, username, roleId, roleName, userPermissions, jti);
            exchange.setAttribute("userContext", userContext);

            return true;
        } catch (Exception e) {
            HttpUtils.sendErrorResponse(exchange, 401, "Acceso denegado: Token JWT invalido o expirado - " + e.getMessage());
            return false;
        }
    }
}
