package com.acme.sica.application.dto;

public record PersonaDTO(
    String docIdentidad,
    String tipoDocumento,
    String nombre,
    String apellido,
    String email,
    String telefono,
    Long empresaId,
    String fotoUrl
) {}

