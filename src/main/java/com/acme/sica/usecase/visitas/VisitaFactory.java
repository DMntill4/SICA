package com.acme.sica.usecase.visitas;

import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Visita;

import java.time.LocalDateTime;

/**
 * Patrón Factory para centralizar la construcción e inicialización de estados de visitas.
 */
public class VisitaFactory {

    public static Visita createVisita(TipoVisita tipo, Long personaId, Long funcionarioId, String motivo, LocalDateTime fechaProgramada) {
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setFuncionarioId(funcionarioId);
        visita.setTipoVisita(tipo);
        visita.setMotivo(motivo);

        switch (tipo) {
            case PRE_REGISTRADA -> {
                visita.setEstadoVisita(EstadoVisita.APROBADO);
                visita.setFechaHoraProgramada(fechaProgramada != null ? fechaProgramada : LocalDateTime.now());
            }
            case NO_ANUNCIADA -> {
                visita.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION);
                visita.setFechaHoraProgramada(LocalDateTime.now());
            }
            case PASE_TEMPORAL -> {
                visita.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION_OLVIDO);
                visita.setFechaHoraProgramada(LocalDateTime.now());
            }
        }
        return visita;
    }
}
