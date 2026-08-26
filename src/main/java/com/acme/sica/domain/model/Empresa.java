package com.acme.sica.domain.model;

public class Empresa {
    private Long id;
    private String nit;
    private String nombre;
    private String ubicacionOficina;
    private boolean activa;

    public Empresa() {}

    public Empresa(Long id, String nit, String nombre, String ubicacionOficina, boolean activa) {
        this.id = id;
        this.nit = nit;
        this.nombre = nombre;
        this.ubicacionOficina = ubicacionOficina;
        this.activa = activa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacionOficina() {
        return ubicacionOficina;
    }

    public void setUbicacionOficina(String ubicacionOficina) {
        this.ubicacionOficina = ubicacionOficina;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
