package com.acme.sica.infrastructure.adapter.in.dto;

public record LoginResponseDTO(
    String token,
    String tokenType,
    Long userId,
    String username,
    String nombreCompleto,
    Long roleId,
    String roleName,
    Long empresaId
) {}
