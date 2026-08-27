package com.acme.sica.application.dto;

import java.time.LocalDateTime;

public record PreregistroVisitaDTO(
    Long personaId,
    String motivo,
    LocalDateTime fechaHoraProgramada
) {}
