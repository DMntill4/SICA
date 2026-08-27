package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.domain.model.Incidente;
import com.acme.sica.application.dto.IncidenteDTO;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.usecase.incidentes.RegistrarIncidenteUseCase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class IncidenteHttpHandler {

    private final RegistrarIncidenteUseCase incidenteUseCase;

    public IncidenteHttpHandler(RegistrarIncidenteUseCase incidenteUseCase) {
        this.incidenteUseCase = incidenteUseCase;
    }

    public void handleCreate(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        IncidenteDTO dto = HttpUtils.readRequestBody(exchange, IncidenteDTO.class);
        Incidente creado = incidenteUseCase.registrarIncidente(dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 201, creado);
    }

    public void handleFindAll(HttpExchange exchange) throws IOException {
        List<Incidente> incidentes = incidenteUseCase.listarTodos();
        HttpUtils.sendJsonResponse(exchange, 200, incidentes);
    }

    private String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
    }
}
