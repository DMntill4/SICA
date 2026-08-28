package com.acme.sica.domain.model;

import com.acme.sica.domain.enums.EstadoAcceso;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Persona {

    private Long id;
    private String docIdentidad;
    private String tipoDocumento;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private Long empresaId;
    private String empresaNombre;
    private EstadoAcceso estadoAcceso;
    private String vectorBiometrico;
    private String fotoUrl;
    private LocalDateTime creadoEn;

    public Persona() {
        this.estadoAcceso = EstadoAcceso.HABILITADO;
    }

    public String getVectorBiometrico() { return vectorBiometrico; }
    public void setVectorBiometrico(String vectorBiometrico) { this.vectorBiometrico = vectorBiometrico; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocIdentidad() {
        return docIdentidad;
    }

    public void setDocIdentidad(String docIdentidad) {
        this.docIdentidad = docIdentidad;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    @JsonIgnore
    public String getNombreCompleto() {
        if (nombre == null) return "";
        if (apellido == null || apellido.trim().isEmpty()) return nombre;
        return nombre + " " + apellido;
    }


    public String getNombre() {
        return nombre;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public EstadoAcceso getEstadoAcceso() {
        return estadoAcceso;
    }

    public void setEstadoAcceso(EstadoAcceso estadoAcceso) {
        this.estadoAcceso = estadoAcceso;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
