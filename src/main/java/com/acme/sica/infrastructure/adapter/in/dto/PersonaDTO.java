package com.acme.sica.infrastructure.adapter.in.dto;

public record PersonaDTO(
    String docIdentidad,
    String tipoDocumento,
    String nombre,
    String apellido,
    String email,
    String telefono,
    Long empresaId
) {}
