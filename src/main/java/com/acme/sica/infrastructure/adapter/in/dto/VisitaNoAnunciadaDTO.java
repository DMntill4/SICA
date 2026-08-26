package com.acme.sica.infrastructure.adapter.in.dto;

public record VisitaNoAnunciadaDTO(
    Long personaId,
    Long funcionarioId,
    Long puntoAccesoIngresoId,
    String motivo
) {}
