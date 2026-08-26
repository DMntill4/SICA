package com.acme.sica.infrastructure.adapter.in.dto;

public record PaseTemporalDTO(
    Long personaId,
    Long funcionarioId,
    Long puntoAccesoIngresoId,
    String motivo
) {}
