package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.domain.model.Persona;
import com.acme.sica.application.dto.PersonaDTO;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.usecase.personas.GestionarPersonaUseCase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PersonaHttpHandler {

    private final GestionarPersonaUseCase personaUseCase;

    public PersonaHttpHandler(GestionarPersonaUseCase personaUseCase) {
        this.personaUseCase = personaUseCase;
    }

    public void handleFindAll(HttpExchange exchange) throws IOException {
        List<Persona> personas = personaUseCase.listarTodas();
        HttpUtils.sendJsonResponse(exchange, 200, personas);
    }

    public void handleFindById(HttpExchange exchange) throws IOException {
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        Persona persona = personaUseCase.buscarPorId(id);
        HttpUtils.sendJsonResponse(exchange, 200, persona);
    }

    public void handleFindByDoc(HttpExchange exchange) throws IOException {
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        String doc = pathVars.get("doc");
        Persona persona = personaUseCase.buscarPorDocumento(doc);
        HttpUtils.sendJsonResponse(exchange, 200, persona);
    }

    public void handleCreate(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        PersonaDTO dto = HttpUtils.readRequestBody(exchange, PersonaDTO.class);

        if (dto == null || dto.docIdentidad() == null || dto.docIdentidad().trim().isEmpty() ||
            dto.nombre() == null || dto.nombre().trim().isEmpty() ||
            dto.apellido() == null || dto.apellido().trim().isEmpty()) {
            HttpUtils.sendErrorResponse(exchange, 400, "⚠️ Campos obligatorios faltantes: Documento, Nombre y Apellido son requeridos.");
            return;
        }


        Persona creada = personaUseCase.registrarPersona(dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 201, creada);
    }


    public void handleUpdate(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        PersonaDTO dto = HttpUtils.readRequestBody(exchange, PersonaDTO.class);
        Persona actualizada = personaUseCase.actualizarPersona(id, dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, actualizada);
    }

    public void handleDelete(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        personaUseCase.eliminarPersona(id, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, Map.of("message", "Persona eliminada correctamente"));
    }

    public void handleRehabilitar(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        Persona persona = personaUseCase.rehabilitarAcceso(id, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, persona);
    }

    private String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
    }
}
