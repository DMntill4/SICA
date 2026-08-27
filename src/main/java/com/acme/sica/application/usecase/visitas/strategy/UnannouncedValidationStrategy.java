package com.acme.sica.application.usecase.visitas.strategy;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;

public class UnannouncedValidationStrategy implements AccessValidationStrategy {
    @Override
    public void validate(Persona persona, Visita visita) {
        if (visita.getEstadoVisita() != EstadoVisita.APROBADO) {
            throw new IllegalStateException("La visita no anunciada o pase temporal requiere aprobacion previa del funcionario. Estado actual: " + visita.getEstadoVisita());
        }
        
        // REGLA: El Pase Temporal y la Visita No Anunciada solo son válidos para el día en que se aprueban/crean
        if (visita.getCreadoEn() != null) {
            java.time.LocalDate fechaCreacion = visita.getCreadoEn().toLocalDate();
            java.time.LocalDate hoy = java.time.LocalDate.now();
            
            if (!fechaCreacion.equals(hoy)) {
                throw new IllegalStateException("El pase o autorización ha expirado. Solo es válido para el día " + fechaCreacion + ". Hoy es " + hoy + ".");
            }
        }
    }
}
