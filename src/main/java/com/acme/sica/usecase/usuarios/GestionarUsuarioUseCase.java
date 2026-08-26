package com.acme.sica.usecase.usuarios;

import com.acme.sica.domain.model.Usuario;
import com.acme.sica.domain.port.UsuarioRepository;
import com.acme.sica.infrastructure.adapter.in.dto.UsuarioDTO;
import com.acme.sica.infrastructure.audit.AuditService;
import com.acme.sica.infrastructure.security.AuthenticatedUserContext;
import com.acme.sica.infrastructure.security.PasswordHasher;

import java.util.List;

public class GestionarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public GestionarUsuarioUseCase(UsuarioRepository usuarioRepository, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    public Usuario crearUsuario(UsuarioDTO dto, AuthenticatedUserContext actor, String ipOrigen) {
        if (usuarioRepository.findByUsername(dto.username()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario '" + dto.username() + "' ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.username().trim());
        usuario.setPasswordHash(PasswordHasher.hashPassword(dto.password()));
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
            usuario.setPasswordHash(PasswordHasher.hashPassword(dto.password()));
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
}
