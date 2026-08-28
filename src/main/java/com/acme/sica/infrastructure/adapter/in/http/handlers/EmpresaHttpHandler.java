package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.domain.model.Empresa;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.acme.sica.application.usecase.empresas.GestionarEmpresaUseCase;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.dto.EmpresaDTO;
import com.sun.net.httpserver.HttpExchange;


import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EmpresaHttpHandler {
    private final GestionarEmpresaUseCase empresaUseCase;

    public EmpresaHttpHandler(GestionarEmpresaUseCase empresaUseCase) {
        this.empresaUseCase = empresaUseCase;
    }

    private String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    public void handleFindAll(HttpExchange exchange) throws IOException {
        try {
            List<Empresa> empresas = empresaUseCase.findAll();
            HttpUtils.sendJsonResponse(exchange, 200, empresas);
        } catch (Exception e) {
            HttpUtils.sendJsonResponse(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    public void handleFindById(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
            Long id = Long.parseLong(pathVars.get("id"));
            Empresa empresa = empresaUseCase.findById(id);
            HttpUtils.sendJsonResponse(exchange, 200, empresa);
        } catch (IllegalArgumentException e) {
            HttpUtils.sendJsonResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendJsonResponse(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    public void handleCreate(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
            EmpresaDTO dto = HttpUtils.readRequestBody(exchange, EmpresaDTO.class);
            String ipOrigen = getRemoteIp(exchange);
            
            Empresa nueva = new Empresa(null, dto.getNit(), dto.getNombre(), dto.getUbicacionOficina(), dto.isActiva());
            Empresa saved = empresaUseCase.crearEmpresa(nueva, actor, ipOrigen);
            HttpUtils.sendJsonResponse(exchange, 201, saved);
        } catch (IllegalArgumentException e) {
            HttpUtils.sendJsonResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendJsonResponse(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    public void handleUpdate(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
            Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
            Long id = Long.parseLong(pathVars.get("id"));
            EmpresaDTO dto = HttpUtils.readRequestBody(exchange, EmpresaDTO.class);
            String ipOrigen = getRemoteIp(exchange);

            Empresa datosUpdate = new Empresa(null, dto.getNit(), dto.getNombre(), dto.getUbicacionOficina(), dto.isActiva());
            Empresa updated = empresaUseCase.actualizarEmpresa(id, datosUpdate, actor, ipOrigen);
            HttpUtils.sendJsonResponse(exchange, 200, updated);
        } catch (IllegalArgumentException e) {
            HttpUtils.sendJsonResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendJsonResponse(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    public void handleDelete(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
            Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
            Long id = Long.parseLong(pathVars.get("id"));
            String ipOrigen = getRemoteIp(exchange);

            empresaUseCase.eliminarEmpresa(id, actor, ipOrigen);
            HttpUtils.sendJsonResponse(exchange, 204, "");
        } catch (IllegalArgumentException e) {
            HttpUtils.sendJsonResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtils.sendJsonResponse(exchange, 500, "Error interno: " + e.getMessage());
        }
    }
}
