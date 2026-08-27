package com.acme.sica.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Rol {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<Long> permisoIds = new ArrayList<>();
    private List<String> permisoCodigos = new ArrayList<>();

    public Rol() {}

    public Rol(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<Long> getPermisoIds() { return permisoIds; }
    public void setPermisoIds(List<Long> permisoIds) { this.permisoIds = permisoIds; }

    public List<String> getPermisoCodigos() { return permisoCodigos; }
    public void setPermisoCodigos(List<String> permisoCodigos) { this.permisoCodigos = permisoCodigos; }
}
