package com.acme.sica.application.usecase.auth;

import com.acme.sica.domain.model.Usuario;
import com.acme.sica.application.port.out.UsuarioRepository;
import com.acme.sica.application.dto.LoginRequestDTO;
import com.acme.sica.application.dto.LoginResponseDTO;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.port.out.JwtPort;
import com.acme.sica.application.port.out.PasswordEncoderPort;

import java.time.LocalDateTime;

public class AuthUseCase {

    private final UsuarioRepository usuarioRepository;
    private final JwtPort jwtPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final AuditService auditService;

    public AuthUseCase(UsuarioRepository usuarioRepository, JwtPort jwtPort, PasswordEncoderPort passwordEncoderPort, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtPort = jwtPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.auditService = auditService;
    }

    public LoginResponseDTO login(LoginRequestDTO request, String ipOrigen) {
        // REGLA DE NEGOCIO: Validar que la peticion contenga campos requeridos no nulos
        if (request == null || request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("Debe ingresar usuario y contrasenia");
        }

        // REGLA DE NEGOCIO: Buscar usuario por username. Si no existe, registrar auditoria y lanzar excepcion
        Usuario usuario = usuarioRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> {
                    auditService.log(null, request.username(), "LOGIN_FAILED", "Intento de inicio de sesion con usuario inexistente", ipOrigen);
                    return new SecurityException("Credenciales invalidas");
                });

        // REGLA DE NEGOCIO: Verificar si la cuenta esta previamente bloqueada por intentos fallidos
        if (usuario.isBloqueado()) {
            auditService.log(usuario.getId(), usuario.getUsername(), "LOGIN_BLOCKED", "Intento de inicio de sesion en cuenta bloqueada", ipOrigen);
            throw new SecurityException("La cuenta se encuentra bloqueada por exceder los intentos fallidos");
        }

        // REGLA DE NEGOCIO: Verificar hash de contraseña con BCrypt (!verifyPassword indica contraseña incorrecta)
        if (!passwordEncoderPort.verifyPassword(request.password(), usuario.getPasswordHash())) {
            // INTENCION: Incrementar en 1 el contador de intentos fallidos
            int nuevosIntentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(nuevosIntentos);

            // REGLA DE NEGOCIO: Si acumula 3 o mas intentos y no es usuario admin, bloquear cuenta
            boolean esAdmin = "admin".equalsIgnoreCase(usuario.getUsername());
            if (nuevosIntentos >= 3 && !esAdmin) {
                usuario.setBloqueado(true);
                usuarioRepository.update(usuario);
                auditService.log(usuario.getId(), usuario.getUsername(), "ACCOUNT_LOCKED", "Cuenta bloqueada tras 3 intentos fallidos", ipOrigen);
                throw new SecurityException("La cuenta ha sido bloqueada por exceder los intentos fallidos");
            } else {
                usuarioRepository.update(usuario);
                auditService.log(usuario.getId(), usuario.getUsername(), "LOGIN_FAILED", "Contrasenia incorrecta. Intento #" + nuevosIntentos, ipOrigen);
                throw new SecurityException("Credenciales invalidas");
            }
        }

        // REGLA DE NEGOCIO: Si la contraseña fue correcta, reiniciar contador de intentos a 0
        usuario.setIntentosFallidos(0);
        usuarioRepository.update(usuario);

        // INTENCION: Generar token JWT firmado con 8 horas de vigencia y consultar permisos por rol
        String token = jwtPort.generateToken(usuario);
        auditService.log(usuario.getId(), usuario.getUsername(), "LOGIN_SUCCESS", "Inicio de sesion exitoso", ipOrigen);

        java.util.Set<String> permisos = usuarioRepository.findPermissionsByRoleId(usuario.getRolId());

        return new LoginResponseDTO(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRolId(),
                usuario.getRolNombre(),
                usuario.getEmpresaId(),
                permisos
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
