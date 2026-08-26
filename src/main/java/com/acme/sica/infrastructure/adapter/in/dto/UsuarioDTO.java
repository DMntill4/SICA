package com.acme.sica.infrastructure.adapter.in.dto;

public record UsuarioDTO(
    String username,
    String password,
    String nombreCompleto,
    String email,
    Long rolId,
    Long empresaId,
    Boolean bloqueado
) {}
