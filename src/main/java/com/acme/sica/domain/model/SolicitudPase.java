package com.acme.sica.domain.model;

import java.time.LocalDateTime;

public class SolicitudPase {

    public enum EstadoSolicitud {
        PENDIENTE_APROBACION,
        APROBADO,
        RECHAZADO
    }

    private Long id;
    private String nombreCompleto;
    private String docIdentidad;
    private String email;
    private String telefono;
    private String empresaDestino;
    private Long funcionarioDestinoId;
    private String funcionarioNombreCompleto;
    private String motivo;
    private LocalDateTime fechaHoraSolicitada;
    private String vectorBiometrico;
    private String fotoUrl;
    private EstadoSolicitud estado;
    private LocalDateTime creadoEn;

    public SolicitudPase() {
    }

    public SolicitudPase(Long id, String nombreCompleto, String docIdentidad, String email, String telefono,
                         String empresaDestino, Long funcionarioDestinoId, String funcionarioNombreCompleto,
                         String motivo, LocalDateTime fechaHoraSolicitada, String vectorBiometrico,
                         String fotoUrl, EstadoSolicitud estado, LocalDateTime creadoEn) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.docIdentidad = docIdentidad;
        this.email = email;
        this.telefono = telefono;
        this.empresaDestino = empresaDestino;
        this.funcionarioDestinoId = funcionarioDestinoId;
        this.funcionarioNombreCompleto = funcionarioNombreCompleto;
        this.motivo = motivo;
        this.fechaHoraSolicitada = fechaHoraSolicitada;
        this.vectorBiometrico = vectorBiometrico;
        this.fotoUrl = fotoUrl;
        this.estado = estado;
        this.creadoEn = creadoEn;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getDocIdentidad() { return docIdentidad; }
    public void setDocIdentidad(String docIdentidad) { this.docIdentidad = docIdentidad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmpresaDestino() { return empresaDestino; }
    public void setEmpresaDestino(String empresaDestino) { this.empresaDestino = empresaDestino; }

    public Long getFuncionarioDestinoId() { return funcionarioDestinoId; }
    public void setFuncionarioDestinoId(Long funcionarioDestinoId) { this.funcionarioDestinoId = funcionarioDestinoId; }

    public String getFuncionarioNombreCompleto() { return funcionarioNombreCompleto; }
    public void setFuncionarioNombreCompleto(String funcionarioNombreCompleto) { this.funcionarioNombreCompleto = funcionarioNombreCompleto; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFechaHoraSolicitada() { return fechaHoraSolicitada; }
    public void setFechaHoraSolicitada(LocalDateTime fechaHoraSolicitada) { this.fechaHoraSolicitada = fechaHoraSolicitada; }

    public String getVectorBiometrico() { return vectorBiometrico; }
    public void setVectorBiometrico(String vectorBiometrico) { this.vectorBiometrico = vectorBiometrico; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
