package com.acme.sica.domain.model;

import com.acme.sica.domain.enums.NivelGravedad;
import java.time.LocalDateTime;

public class Incidente {
    private Long id;
    private Long personaId;
    private String personaNombreCompleto;
    private String personaDocIdentidad;
    
    private Long reportadoPorUsuarioId;
    private String reportadoPorUsername;
    
    private String titulo;
    private String descripcion;
    private NivelGravedad nivelGravedad;
    private LocalDateTime fechaHora;

    public Incidente() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }

    public String getPersonaNombreCompleto() {
        return personaNombreCompleto;
    }

    public void setPersonaNombreCompleto(String personaNombreCompleto) {
        this.personaNombreCompleto = personaNombreCompleto;
    }

    public String getPersonaDocIdentidad() {
        return personaDocIdentidad;
    }

    public void setPersonaDocIdentidad(String personaDocIdentidad) {
        this.personaDocIdentidad = personaDocIdentidad;
    }

    public Long getReportadoPorUsuarioId() {
        return reportadoPorUsuarioId;
    }

    public void setReportadoPorUsuarioId(Long reportadoPorUsuarioId) {
        this.reportadoPorUsuarioId = reportadoPorUsuarioId;
    }

    public String getReportadoPorUsername() {
        return reportadoPorUsername;
    }

    public void setReportadoPorUsername(String reportadoPorUsername) {
        this.reportadoPorUsername = reportadoPorUsername;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public NivelGravedad getNivelGravedad() {
        return nivelGravedad;
    }

    public void setNivelGravedad(NivelGravedad nivelGravedad) {
        this.nivelGravedad = nivelGravedad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
