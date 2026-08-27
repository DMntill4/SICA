package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.dto.RolDTO;
import com.acme.sica.application.usecase.roles.GestionarRolUseCase;
import com.acme.sica.domain.model.Permiso;
import com.acme.sica.domain.model.Rol;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RolHttpHandler {

    private final GestionarRolUseCase rolUseCase;

    public RolHttpHandler(GestionarRolUseCase rolUseCase) {
        this.rolUseCase = rolUseCase;
    }

    public void handleFindAllRoles(HttpExchange exchange) throws IOException {
        List<Rol> roles = rolUseCase.listarRoles();
        HttpUtils.sendJsonResponse(exchange, 200, roles);
    }

    public void handleFindAllPermisos(HttpExchange exchange) throws IOException {
        List<Permiso> permisos = rolUseCase.listarPermisos();
        HttpUtils.sendJsonResponse(exchange, 200, permisos);
    }

    public void handleCreateRol(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        RolDTO dto = HttpUtils.readRequestBody(exchange, RolDTO.class);
        Rol creado = rolUseCase.crearRol(dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 201, creado);
    }

    public void handleUpdatePermisos(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        RolDTO dto = HttpUtils.readRequestBody(exchange, RolDTO.class);
        rolUseCase.actualizarPermisosRol(id, dto.permisoIds(), actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, Map.of("message", "Permisos actualizados correctamente"));
    }

    public void handleDeleteRol(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        rolUseCase.eliminarRol(id, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, Map.of("message", "Rol eliminado correctamente"));
    }

    private String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
    }
}
