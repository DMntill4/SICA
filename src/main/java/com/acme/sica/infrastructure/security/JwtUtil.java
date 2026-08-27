package com.acme.sica.infrastructure.security;

import com.acme.sica.domain.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;
import java.util.UUID;
import com.acme.sica.application.port.out.JwtPort;

public class JwtUtil implements JwtPort {

    private static final String SECRET = "SICA_SUPER_SECRET_KEY_ZONA_ACME_2026_VERY_SECURE";
    private static final String ISSUER = "SICA_ZONA_ACME";
    private static final long EXPIRATION_TIME_MS = 8 * 60 * 60 * 1000; // 8 Horas

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    private final JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();

    public String generateToken(Usuario usuario) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + EXPIRATION_TIME_MS);

        var builder = JWT.create()
                .withIssuer(ISSUER)
                .withJWTId(jti)
                .withSubject(usuario.getUsername())
                .withClaim("userId", usuario.getId())
                .withClaim("roleId", usuario.getRolId())
                .withClaim("roleName", usuario.getRolNombre())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt);

        if (usuario.getEmpresaId() != null) {
            builder.withClaim("empresaId", usuario.getEmpresaId());
        }

        return builder.sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        return verifier.verify(token);
    }
}
