package com.acme.sica.application.port.out;

import com.acme.sica.domain.model.Usuario;

public interface JwtPort {
    String generateToken(Usuario usuario);
}
