package com.acme.sica.infrastructure.audit;

import com.acme.sica.domain.model.BitacoraAuditoria;
import com.acme.sica.domain.port.AuditRepository;

import java.time.LocalDateTime;

public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void log(Long usuarioId, String username, String accion, String detalle, String ipOrigen) {
        BitacoraAuditoria log = new BitacoraAuditoria();
        log.setUsuarioId(usuarioId);
        log.setUsername(username != null ? username : "ANONYMOUS");
        log.setAccion(accion);
        log.setDetalle(detalle);
        log.setIpOrigen(ipOrigen != null ? ipOrigen : "127.0.0.1");
        log.setFechaHora(LocalDateTime.now());

        auditRepository.save(log);
    }

    public void logSalidaOlvidada(Long personaId, String personaDoc, Long visitaIdPrevia, String ipOrigen) {
        log(null, "SISTEMA", "ANOMALIA_SALIDA_OLVIDADA",
                "Regularizacion automatica: Cierre CERRADA_POR_SISTEMA para la visita previa ID #" + visitaIdPrevia +
                        " de la persona con doc: " + personaDoc, ipOrigen);
    }
}
