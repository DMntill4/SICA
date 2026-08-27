package com.acme.sica.application.port.out;

import com.acme.sica.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UsuarioRepository {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findById(Long id);
    Set<String> findPermissionsByRoleId(Long roleId);
    Usuario save(Usuario usuario);
    void update(Usuario usuario);
    void deleteById(Long id);
    List<Usuario> findAll();
    
    void revokeToken(String tokenJti, LocalDateTime expiraEn);
    boolean isTokenRevoked(String tokenJti);
}
