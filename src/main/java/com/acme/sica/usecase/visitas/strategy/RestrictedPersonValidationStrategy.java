package com.acme.sica.usecase.visitas.strategy;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;

public class RestrictedPersonValidationStrategy implements AccessValidationStrategy {
    @Override
    public void validate(Persona persona, Visita visita) {
        if (persona.getEstadoAcceso() == EstadoAcceso.RESTRINGIDO) {
            throw new SecurityException("ACCESO DENEGADO: La persona con documento " + persona.getDocIdentidad() + " tiene el acceso RESTRINGIDO por un incidente.");
        }
    }
}
