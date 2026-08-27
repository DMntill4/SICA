package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.domain.model.BitacoraAuditoria;
import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.acme.sica.application.usecase.reportes.GenerarReporteUseCase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportesHttpHandler {

    private final GenerarReporteUseCase reporteUseCase;

    public ReportesHttpHandler(GenerarReporteUseCase reporteUseCase) {
        this.reporteUseCase = reporteUseCase;
    }

    public void handlePersonasDentro(HttpExchange exchange) throws IOException {
        List<Visita> dentro = reporteUseCase.getPersonasActualmenteDentro();
        HttpUtils.sendJsonResponse(exchange, 200, Map.of(
                "totalDentro", dentro.size(),
                "personas", dentro
        ));
    }

    public void handleVisitasRango(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI());
        
        LocalDateTime inicio = parseDateOrDefault(queryParams.get("inicio"), LocalDateTime.now().minusDays(30));
        LocalDateTime fin = parseDateOrDefault(queryParams.get("fin"), LocalDateTime.now());

        List<Visita> visitas = reporteUseCase.getVisitasPorRangoFecha(inicio, fin);
        HttpUtils.sendJsonResponse(exchange, 200, Map.of(
                "rangoInicio", inicio.toString(),
                "rangoFin", fin.toString(),
                "totalVisitas", visitas.size(),
                "visitas", visitas
        ));
    }

    public void handleIncidentes(HttpExchange exchange) throws IOException {
        List<Incidente> incidentes = reporteUseCase.getReporteIncidentes();
        HttpUtils.sendJsonResponse(exchange, 200, Map.of(
                "totalIncidentes", incidentes.size(),
                "incidentes", incidentes
        ));
    }

    public void handleAuditoria(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI());
        int limit = 100;
        if (queryParams.containsKey("limit")) {
            try {
                limit = Integer.parseInt(queryParams.get("limit"));
            } catch (NumberFormatException ignored) {}
        }

        List<BitacoraAuditoria> logs = reporteUseCase.getBitacoraAuditoria(limit);
        HttpUtils.sendJsonResponse(exchange, 200, Map.of(
                "totalRegistros", logs.size(),
                "auditoria", logs
        ));
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getQuery();
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    map.put(pair[0], pair[1]);
                } else if (pair.length == 1) {
                    map.put(pair[0], "");
                }
            }
        }
        return map;
    }

    private LocalDateTime parseDateOrDefault(String dateStr, LocalDateTime defaultDate) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultDate;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr + "T00:00:00");
            } catch (Exception ex) {
                return defaultDate;
            }
        }
    }
}
