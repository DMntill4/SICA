package com.acme.sica.infrastructure.adapter.in.dto;

import java.time.LocalDateTime;

public record PreregistroVisitaDTO(
    Long personaId,
    String motivo,
    LocalDateTime fechaHoraProgramada
) {}
