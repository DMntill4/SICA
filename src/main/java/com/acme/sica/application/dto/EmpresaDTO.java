package com.acme.sica.application.dto;

public class EmpresaDTO {
    private String nit;
    private String nombre;
    private String ubicacionOficina;
    private boolean activa;

    // Getters y setters
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacionOficina() { return ubicacionOficina; }
    public void setUbicacionOficina(String ubicacionOficina) { this.ubicacionOficina = ubicacionOficina; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
