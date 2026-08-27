package com.acme.sica.application.dto;

import java.util.Set;

public record LoginResponseDTO(
    String token,
    String tokenType,
    Long userId,
    String username,
    String nombreCompleto,
    Long roleId,
    String roleName,
    Long empresaId,
    Set<String> permissions
) {}

