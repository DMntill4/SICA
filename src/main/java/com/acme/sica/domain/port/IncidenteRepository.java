package com.acme.sica.domain.port;

import com.acme.sica.domain.model.Incidente;
import java.util.List;

public interface IncidenteRepository {
    Incidente save(Incidente incidente);
    List<Incidente> findAll();
    List<Incidente> findByPersonaId(Long personaId);
}
