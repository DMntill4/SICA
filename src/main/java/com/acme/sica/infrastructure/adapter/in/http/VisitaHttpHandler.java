package com.acme.sica.infrastructure.adapter.in.http;

import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.adapter.in.dto.CheckInDTO;
import com.acme.sica.infrastructure.adapter.in.dto.PaseTemporalDTO;
import com.acme.sica.infrastructure.adapter.in.dto.PreregistroVisitaDTO;
import com.acme.sica.infrastructure.adapter.in.dto.VisitaNoAnunciadaDTO;
import com.acme.sica.infrastructure.http.HttpUtils;
import com.acme.sica.infrastructure.security.AuthenticatedUserContext;
import com.acme.sica.usecase.visitas.GestionarVisitaUseCase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class VisitaHttpHandler {

    private final GestionarVisitaUseCase visitaUseCase;

    public VisitaHttpHandler(GestionarVisitaUseCase visitaUseCase) {
        this.visitaUseCase = visitaUseCase;
    }

    public void handleFindAll(HttpExchange exchange) throws IOException {
        List<Visita> visitas = visitaUseCase.findAll();
        HttpUtils.sendJsonResponse(exchange, 200, visitas);
    }

    public void handleFindById(HttpExchange exchange) throws IOException {
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        Visita visita = visitaUseCase.findById(id);
        HttpUtils.sendJsonResponse(exchange, 200, visita);
    }

    public void handlePreregistrar(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        PreregistroVisitaDTO dto = HttpUtils.readRequestBody(exchange, PreregistroVisitaDTO.class);
        Visita creada = visitaUseCase.preregistrarVisita(dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 201, creada);
    }

    public void handleNoAnunciada(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        VisitaNoAnunciadaDTO dto = HttpUtils.readRequestBody(exchange, VisitaNoAnunciadaDTO.class);
        Visita creada = visitaUseCase.registrarNoAnunciada(dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 201, creada);
    }

    public void handlePaseTemporal(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        PaseTemporalDTO dto = HttpUtils.readRequestBody(exchange, PaseTemporalDTO.class);
        Visita creada = visitaUseCase.registrarPaseTemporal(dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 201, creada);
    }

    public void handleAprobar(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        Visita aprobada = visitaUseCase.aprobarVisita(id, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, aprobada);
    }

    public void handleRechazar(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        Visita rechazada = visitaUseCase.rechazarVisita(id, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, rechazada);
    }

    public void handleCheckIn(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext guardiaContext = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        CheckInDTO dto = null;
        try {
            dto = HttpUtils.readRequestBody(exchange, CheckInDTO.class);
        } catch (Exception ignored) {}

        Visita checkInDone = visitaUseCase.checkIn(id, dto, guardiaContext, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, checkInDone);
    }

    public void handleCheckOut(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext guardiaContext = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));

        Visita checkOutDone = visitaUseCase.checkOut(id, guardiaContext, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, checkOutDone);
    }

    public void handleDeleteAll(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        visitaUseCase.limpiarVisitas(actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, Map.of("message", "Historial de visitas limpiado correctamente"));
    }

    private String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
    }
}
