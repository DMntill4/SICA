package com.acme.sica.application.port.out;

import com.acme.sica.domain.model.SolicitudPase;
import java.util.List;
import java.util.Optional;

public interface SolicitudPaseRepository {
    SolicitudPase guardar(SolicitudPase solicitud);
    Optional<SolicitudPase> buscarPorId(Long id);
    List<SolicitudPase> listarTodas();
    List<SolicitudPase> listarPendientes();
    void actualizarEstado(Long id, SolicitudPase.EstadoSolicitud estado);
}
