package com.acme.sica.application.dto;

import java.util.List;

public record RolDTO(
    Long id,
    String nombre,
    String descripcion,
    List<Long> permisoIds
) {}
