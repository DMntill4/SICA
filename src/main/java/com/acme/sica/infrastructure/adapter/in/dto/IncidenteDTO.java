package com.acme.sica.infrastructure.adapter.in.dto;

import com.acme.sica.domain.enums.NivelGravedad;

public record IncidenteDTO(
    Long personaId,
    String titulo,
    String descripcion,
    NivelGravedad nivelGravedad
) {}
