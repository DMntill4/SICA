package com.acme.sica.application.dto;

public record PaseTemporalDTO(
    Long personaId,
    Long funcionarioId,
    Long puntoAccesoIngresoId,
    String motivo
) {}
