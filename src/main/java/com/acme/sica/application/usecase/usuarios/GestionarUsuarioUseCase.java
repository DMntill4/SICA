package com.acme.sica.application.usecase.usuarios;

import com.acme.sica.domain.model.Usuario;
import com.acme.sica.application.port.out.UsuarioRepository;
import com.acme.sica.application.dto.UsuarioDTO;
import com.acme.sica.application.usecase.audit.AuditService;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.port.out.PasswordEncoderPort;

import java.util.List;

public class GestionarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final AuditService auditService;

    public GestionarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordEncoderPort passwordEncoderPort, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.auditService = auditService;
    }

    public Usuario crearUsuario(UsuarioDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        if (usuarioRepository.findByUsername(dto.username()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario '" + dto.username() + "' ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.username().trim());
        usuario.setPasswordHash(passwordEncoderPort.hashPassword(dto.password()));
        usuario.setNombreCompleto(dto.nombreCompleto());
        usuario.setEmail(dto.email());
        usuario.setRolId(dto.rolId());
        usuario.setEmpresaId(dto.empresaId());
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado(false);

        Usuario creado = usuarioRepository.save(usuario);
        auditService.log(actor.userId(), actor.username(), "CREAR_USUARIO", "Usuario creado: " + creado.getUsername(), ipOrigen);
        return creado;
    }

    public Usuario actualizarUsuario(Long id, UsuarioDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (dto.password() != null && !dto.password().trim().isEmpty()) {
            usuario.setPasswordHash(passwordEncoderPort.hashPassword(dto.password()));
        }
        usuario.setNombreCompleto(dto.nombreCompleto());
        usuario.setEmail(dto.email());
        usuario.setRolId(dto.rolId());
        usuario.setEmpresaId(dto.empresaId());
        if (dto.bloqueado() != null) {
            usuario.setBloqueado(dto.bloqueado());
            if (!dto.bloqueado()) {
                usuario.setIntentosFallidos(0);
            }
        }

        usuarioRepository.update(usuario);
        auditService.log(actor.userId(), actor.username(), "ACTUALIZAR_USUARIO", "Usuario actualizado: " + usuario.getUsername(), ipOrigen);
        return usuario;
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    public void eliminarUsuario(Long id, AuthenticatedUserContext actor, String ipOrigen) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        usuarioRepository.deleteById(id);
        auditService.log(actor.userId(), actor.username(), "ELIMINAR_USUARIO", "Usuario eliminado ID: " + id + " username: " + usuario.getUsername(), ipOrigen);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario toggleBloqueoUsuario(Long id, AuthenticatedUserContext actor, String ipOrigen) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        boolean nuevoEstado = !usuario.isBloqueado();
        usuario.setBloqueado(nuevoEstado);
        if (!nuevoEstado) {
            usuario.setIntentosFallidos(0);
        }
        usuarioRepository.update(usuario);
        String accion = nuevoEstado ? "BLOQUEAR_USUARIO" : "DESBLOQUEAR_USUARIO";
        auditService.log(actor.userId(), actor.username(), accion, "Estado de bloqueo del usuario '" + usuario.getUsername() + "' cambiado a: " + (nuevoEstado ? "BLOQUEADO" : "ACTIVO"), ipOrigen);
        return usuario;
    }
}

