package com.acme.sica.domain.port;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.model.Persona;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository {
    Optional<Persona> findByDocIdentidad(String docIdentidad);
    Optional<Persona> findById(Long id);
    Persona save(Persona persona);
    void update(Persona persona);
    void updateEstadoAcceso(Long personaId, EstadoAcceso nuevoEstado);
    void deleteById(Long id);
    List<Persona> findAll();
}
