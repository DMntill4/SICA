package com.acme.sica.domain.model;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoCierreVisita;
import com.acme.sica.domain.enums.TipoVisita;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Visita {

    private Long id;
    private Long personaId;
    private String personaNombreCompleto;
    private String personaDocIdentidad;
    
    private Long funcionarioId;
    private String funcionarioNombreCompleto;
    
    private Long guardiaId;
    private String guardiaNombreCompleto;
    private Long guardiaIngresoId;
    private Long guardiaSalidaId;
    
    private Long puntoAccesoId;
    private String puntoAccesoNombre;
    private Long puntoAccesoIngresoId;
    private Long puntoAccesoSalidaId;
    
    private Long visitaGrupoId;
    
    private TipoVisita tipoVisita;
    private EstadoVisita estadoVisita;
    private String motivo;
    
    private LocalDateTime fechaHoraProgramada;
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraSalida;
    private TipoCierreVisita tipoCierre;
    private LocalDateTime creadoEn;

    public Visita() {}

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

    @JsonIgnore
    public String getNombrePersona() {
        return personaNombreCompleto != null ? personaNombreCompleto : ("Persona #" + personaId);
    }

    @JsonIgnore
    public String getDocPersona() {
        return personaDocIdentidad != null ? personaDocIdentidad : "-";
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

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public String getFuncionarioNombreCompleto() {
        return funcionarioNombreCompleto;
    }

    public void setFuncionarioNombreCompleto(String funcionarioNombreCompleto) {
        this.funcionarioNombreCompleto = funcionarioNombreCompleto;
    }

    public Long getGuardiaId() {
        return guardiaId;
    }

    public void setGuardiaId(Long guardiaId) {
        this.guardiaId = guardiaId;
    }

    public String getGuardiaNombreCompleto() {
        return guardiaNombreCompleto;
    }

    public void setGuardiaNombreCompleto(String guardiaNombreCompleto) {
        this.guardiaNombreCompleto = guardiaNombreCompleto;
    }

    public Long getGuardiaIngresoId() {
        return guardiaIngresoId != null ? guardiaIngresoId : guardiaId;
    }

    public void setGuardiaIngresoId(Long guardiaIngresoId) {
        this.guardiaIngresoId = guardiaIngresoId;
        this.guardiaId = guardiaIngresoId;
    }

    public Long getGuardiaSalidaId() {
        return guardiaSalidaId;
    }

    public void setGuardiaSalidaId(Long guardiaSalidaId) {
        this.guardiaSalidaId = guardiaSalidaId;
    }

    public Long getPuntoAccesoIngresoId() {
        return puntoAccesoIngresoId != null ? puntoAccesoIngresoId : puntoAccesoId;
    }

    public void setPuntoAccesoIngresoId(Long puntoAccesoIngresoId) {
        this.puntoAccesoIngresoId = puntoAccesoIngresoId;
        this.puntoAccesoId = puntoAccesoIngresoId;
    }

    public Long getPuntoAccesoSalidaId() {
        return puntoAccesoSalidaId;
    }

    public void setPuntoAccesoSalidaId(Long puntoAccesoSalidaId) {
        this.puntoAccesoSalidaId = puntoAccesoSalidaId;
    }

    public Long getPuntoAccesoId() {
        return puntoAccesoId;
    }

    public void setPuntoAccesoId(Long puntoAccesoId) {
        this.puntoAccesoId = puntoAccesoId;
    }

    public String getPuntoAccesoNombre() {
        return puntoAccesoNombre;
    }

    public void setPuntoAccesoNombre(String puntoAccesoNombre) {
        this.puntoAccesoNombre = puntoAccesoNombre;
    }

    public Long getVisitaGrupoId() {
        return visitaGrupoId;
    }

    public void setVisitaGrupoId(Long visitaGrupoId) {
        this.visitaGrupoId = visitaGrupoId;
    }

    public TipoVisita getTipoVisita() {
        return tipoVisita;
    }

    public void setTipoVisita(TipoVisita tipoVisita) {
        this.tipoVisita = tipoVisita;
    }

    public EstadoVisita getEstadoVisita() {
        return estadoVisita;
    }

    public void setEstadoVisita(EstadoVisita estadoVisita) {
        this.estadoVisita = estadoVisita;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaHoraProgramada() {
        return fechaHoraProgramada;
    }

    public void setFechaHoraProgramada(LocalDateTime fechaHoraProgramada) {
        this.fechaHoraProgramada = fechaHoraProgramada;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
        this.fechaHoraIngreso = fechaHoraIngreso;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public TipoCierreVisita getTipoCierre() {
        return tipoCierre;
    }

    public void setTipoCierre(TipoCierreVisita tipoCierre) {
        this.tipoCierre = tipoCierre;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
