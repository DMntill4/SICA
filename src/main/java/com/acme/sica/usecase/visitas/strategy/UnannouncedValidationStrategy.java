package com.acme.sica.usecase.visitas.strategy;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;

public class UnannouncedValidationStrategy implements AccessValidationStrategy {
    @Override
    public void validate(Persona persona, Visita visita) {
        if (visita.getEstadoVisita() != EstadoVisita.APROBADO) {
            throw new IllegalStateException("La visita no anunciada o pase temporal requiere aprobacion previa del funcionario. Estado actual: " + visita.getEstadoVisita());
        }
    }
}
