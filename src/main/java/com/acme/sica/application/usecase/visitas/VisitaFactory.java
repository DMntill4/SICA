package com.acme.sica.application.usecase.visitas;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Visita;

import java.time.LocalDateTime;

/**
 * Patrón Factory para centralizar la construcción e inicialización de estados
 * de visitas.
 */
public class VisitaFactory {

    public static Visita createVisita(TipoVisita tipo, Long personaId, Long funcionarioId, String motivo,
            LocalDateTime fechaProgramada) {
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setFuncionarioId(funcionarioId);
        visita.setTipoVisita(tipo);
        visita.setMotivo(motivo);

        switch (tipo) {
            // REGLA DE NEGOCIO: Visita pre-registrada por funcionario inicia en estado
            // APROBADO
            case PRE_REGISTRADA -> {
                visita.setEstadoVisita(EstadoVisita.APROBADO);
                visita.setFechaHoraProgramada(fechaProgramada != null ? fechaProgramada : LocalDateTime.now());
            }
            // REGLA DE NEGOCIO: Visita Walk-In no anunciada inicia en PENDIENTE_APROBACION
            // por el funcionario
            case NO_ANUNCIADA -> {
                visita.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION);
                visita.setFechaHoraProgramada(LocalDateTime.now());
            }
            // REGLA DE NEGOCIO: Pase temporal por carnet olvidado inicia en
            // PENDIENTE_APROBACION_OLVIDO
            case PASE_TEMPORAL -> {
                visita.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION_OLVIDO);
                visita.setFechaHoraProgramada(LocalDateTime.now());
            }
        }
        return visita;
    }
}
