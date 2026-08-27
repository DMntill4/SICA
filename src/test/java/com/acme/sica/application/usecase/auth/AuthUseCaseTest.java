package com.acme.sica.application.usecase.auth;

import com.acme.sica.application.dto.LoginRequestDTO;
import com.acme.sica.application.dto.LoginResponseDTO;
import com.acme.sica.application.port.out.JwtPort;
import com.acme.sica.application.port.out.PasswordEncoderPort;
import com.acme.sica.application.port.out.UsuarioRepository;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.domain.model.Usuario;
import com.acme.sica.infrastructure.security.JwtUtil;
import com.acme.sica.infrastructure.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthUseCaseTest {

    private UsuarioRepository usuarioRepository;
    private JwtPort jwtPort;
    private PasswordEncoderPort passwordEncoderPort;
    private AuditService auditService;
    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        jwtPort = new JwtUtil();
        passwordEncoderPort = new PasswordHasher();
        auditService = mock(AuditService.class);
        authUseCase = new AuthUseCase(usuarioRepository, jwtPort, passwordEncoderPort, auditService);
    }

    @Test
    @DisplayName("Debe autenticar exitosamente a admin con admin123")
    void testLoginExitosoAdmin() {
        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoderPort.hashPassword("admin123"));
        admin.setNombreCompleto("Administrador General");
        admin.setRolId(1L);
        admin.setRolNombre("ADMIN");
        admin.setBloqueado(false);
        admin.setIntentosFallidos(0);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        LoginResponseDTO response = authUseCase.login(new LoginRequestDTO("admin", "admin123"), "127.0.0.1");

        assertNotNull(response);
        assertNotNull(response.token());
        assertEquals("admin", response.username());
        assertEquals(1L, response.userId());
        assertEquals("ADMIN", response.roleName());
    }

    @Test
    @DisplayName("Debe fallar autenticacion cuando la contrasenia es incorrecta")
    void testLoginFallido() {
        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoderPort.hashPassword("admin123"));
        admin.setBloqueado(false);
        admin.setIntentosFallidos(0);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThrows(SecurityException.class, () -> {
            authUseCase.login(new LoginRequestDTO("admin", "wrongpass"), "127.0.0.1");
        });

        assertEquals(1, admin.getIntentosFallidos());
    }
}
