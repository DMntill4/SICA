package com.acme.sica.usecase.auth;

import com.acme.sica.domain.model.Usuario;
import com.acme.sica.domain.port.UsuarioRepository;
import com.acme.sica.infrastructure.adapter.in.dto.LoginRequestDTO;
import com.acme.sica.infrastructure.adapter.in.dto.LoginResponseDTO;
import com.acme.sica.infrastructure.audit.AuditService;
import com.acme.sica.infrastructure.security.AuthenticatedUserContext;
import com.acme.sica.infrastructure.security.JwtUtil;
import com.acme.sica.infrastructure.security.PasswordHasher;

import java.time.LocalDateTime;

public class AuthUseCase {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    public AuthUseCase(UsuarioRepository usuarioRepository, JwtUtil jwtUtil, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
    }

    public LoginResponseDTO login(LoginRequestDTO request, String ipOrigen) {
        if (request == null || request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("Debe ingresar usuario y contrasenia");
        }

        Usuario usuario = usuarioRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> {
                    auditService.log(null, request.username(), "LOGIN_FAILED", "Intento de inicio de sesion con usuario inexistente", ipOrigen);
                    return new SecurityException("Credenciales invalidas");
                });

        if (usuario.isBloqueado()) {
            auditService.log(usuario.getId(), usuario.getUsername(), "LOGIN_BLOCKED", "Intento de login en cuenta bloqueada", ipOrigen);
            throw new SecurityException("La cuenta se encuentra bloqueada por multiples intentos fallidos");
        }

        if (!PasswordHasher.verifyPassword(request.password(), usuario.getPasswordHash())) {
            int nuevosIntentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(nuevosIntentos);
            if (nuevosIntentos >= 3) {
                usuario.setBloqueado(true);
                auditService.log(usuario.getId(), usuario.getUsername(), "ACCOUNT_LOCKED", "Cuenta bloqueada tras 3 intentos fallidos de contrasenia", ipOrigen);
            } else {
                auditService.log(usuario.getId(), usuario.getUsername(), "LOGIN_FAILED", "Contrasenia incorrecta. Intento " + nuevosIntentos + "/3", ipOrigen);
            }
            usuarioRepository.update(usuario);
            throw new SecurityException("Credenciales invalidas");
        }

        usuario.setIntentosFallidos(0);
        usuarioRepository.update(usuario);

        String token = jwtUtil.generateToken(usuario);
        auditService.log(usuario.getId(), usuario.getUsername(), "LOGIN_SUCCESS", "Inicio de sesion exitoso", ipOrigen);

        return new LoginResponseDTO(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRolId(),
                usuario.getRolNombre(),
                usuario.getEmpresaId()
        );
    }

    public void logout(AuthenticatedUserContext userContext, String tokenJti, String ipOrigen) {
        if (userContext != null && tokenJti != null) {
            LocalDateTime expiraEn = LocalDateTime.now().plusHours(8);
            usuarioRepository.revokeToken(tokenJti, expiraEn);
            auditService.log(userContext.userId(), userContext.username(), "LOGOUT", "Cierre de sesion exitoso y token revocado", ipOrigen);
        }
    }
}
