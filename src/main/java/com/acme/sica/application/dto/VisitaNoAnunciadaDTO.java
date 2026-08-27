package com.acme.sica.application.dto;

public record VisitaNoAnunciadaDTO(
    Long personaId,
    Long funcionarioId,
    Long puntoAccesoIngresoId,
    String motivo
) {}
