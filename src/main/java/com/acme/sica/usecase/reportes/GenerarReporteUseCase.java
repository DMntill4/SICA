package com.acme.sica.usecase.reportes;

import com.acme.sica.domain.model.BitacoraAuditoria;
import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.domain.port.AuditRepository;
import com.acme.sica.domain.port.IncidenteRepository;
import com.acme.sica.domain.port.VisitaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Caso de Uso para la generacion de reportes filtrados con Stream API de Java.
 */
public class GenerarReporteUseCase {

    private final VisitaRepository visitaRepository;
    private final IncidenteRepository incidenteRepository;
    private final AuditRepository auditRepository;

    public GenerarReporteUseCase(VisitaRepository visitaRepository, IncidenteRepository incidenteRepository, AuditRepository auditRepository) {
        this.visitaRepository = visitaRepository;
        this.incidenteRepository = incidenteRepository;
        this.auditRepository = auditRepository;
    }

    public List<Visita> getPersonasActualmenteDentro() {
        return visitaRepository.findCurrentlyInside().stream()
                .filter(v -> v.getFechaHoraSalida() == null)
                .toList();
    }

    public List<Visita> getVisitasPorRangoFecha(LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime start = inicio != null ? inicio : LocalDateTime.now().minusDays(30);
        LocalDateTime end = fin != null ? fin : LocalDateTime.now();

        return visitaRepository.findByDateRange(start, end).stream()
                .sorted((v1, v2) -> v2.getCreadoEn().compareTo(v1.getCreadoEn()))
                .toList();
    }

    public List<Incidente> getReporteIncidentes() {
        return incidenteRepository.findAll().stream()
                .sorted((i1, i2) -> i2.getFechaHora().compareTo(i1.getFechaHora()))
                .toList();
    }

    public List<BitacoraAuditoria> getBitacoraAuditoria(int limit) {
        int max = limit > 0 ? limit : 100;
        return auditRepository.findAllRecent(max);
    }
}
