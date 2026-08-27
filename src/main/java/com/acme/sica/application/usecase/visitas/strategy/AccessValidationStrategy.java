package com.acme.sica.application.usecase.visitas.strategy;

import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Visita;

public interface AccessValidationStrategy {
    void validate(Persona persona, Visita visita);
}
