package com.acme.sica.application.port.out;

import com.acme.sica.domain.model.Visita;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitaRepository {
    Visita save(Visita visita);
    void update(Visita visita);
    Optional<Visita> findById(Long id);
    Optional<Visita> findLatestActiveVisitByPersonaId(Long personaId);
    List<Visita> findCurrentlyInside();
    List<Visita> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<Visita> findAll();
    void deleteAll();
}
