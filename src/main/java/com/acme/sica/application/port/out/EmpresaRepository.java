package com.acme.sica.application.port.out;

import com.acme.sica.domain.model.Empresa;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {
    Optional<Empresa> findById(Long id);
    Optional<Empresa> findByNit(String nit);
    Empresa save(Empresa empresa);
    void update(Empresa empresa);
    void deleteById(Long id);
    List<Empresa> findAll();
}
