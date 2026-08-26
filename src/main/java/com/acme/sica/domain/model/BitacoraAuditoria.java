package com.acme.sica.domain.model;

import java.time.LocalDateTime;

public class BitacoraAuditoria {
    private Long id;
    private Long usuarioId;
    private String username;
    private String accion;
    private String detalle;
    private String ipOrigen;
    private LocalDateTime fechaHora;

    public BitacoraAuditoria() {}

    public BitacoraAuditoria(Long usuarioId, String username, String accion, String detalle, String ipOrigen) {
        this.usuarioId = usuarioId;
        this.username = username != null ? username : "SISTEMA";
        this.accion = accion;
        this.detalle = detalle;
        this.ipOrigen = ipOrigen != null ? ipOrigen : "127.0.0.1";
        this.fechaHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
