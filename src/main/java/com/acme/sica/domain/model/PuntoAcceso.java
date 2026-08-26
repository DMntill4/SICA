package com.acme.sica.domain.model;

public class PuntoAcceso {
    private Long id;
    private String nombre;
    private String ubicacion;
    private boolean activo;

    public PuntoAcceso() {}

    public PuntoAcceso(Long id, String nombre, String ubicacion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
