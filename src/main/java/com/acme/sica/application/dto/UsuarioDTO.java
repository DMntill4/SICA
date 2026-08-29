package com.acme.sica.application.dto;

public record UsuarioDTO(
    String username,
    String password,
    String nombreCompleto,
    String email,
    Long rolId,
    Long empresaId,
    Boolean bloqueado,
    String fotoUrl
) {}

