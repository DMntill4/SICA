package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.domain.model.Usuario;
import com.acme.sica.application.dto.UsuarioDTO;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.usecase.usuarios.GestionarUsuarioUseCase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UsuarioHttpHandler {

    private final GestionarUsuarioUseCase usuarioUseCase;

    public UsuarioHttpHandler(GestionarUsuarioUseCase usuarioUseCase) {
        this.usuarioUseCase = usuarioUseCase;
    }

    public void handleFindAll(HttpExchange exchange) throws IOException {
        List<Usuario> usuarios = usuarioUseCase.listarTodos();
        HttpUtils.sendJsonResponse(exchange, 200, usuarios);
    }

    public void handleFindById(HttpExchange exchange) throws IOException {
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        Usuario usuario = usuarioUseCase.buscarPorId(id);
        HttpUtils.sendJsonResponse(exchange, 200, usuario);
    }

    public void handleCreate(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        UsuarioDTO dto = HttpUtils.readRequestBody(exchange, UsuarioDTO.class);
        Usuario creado = usuarioUseCase.crearUsuario(dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 201, creado);
    }

    public void handleUpdate(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        UsuarioDTO dto = HttpUtils.readRequestBody(exchange, UsuarioDTO.class);
        Usuario actualizado = usuarioUseCase.actualizarUsuario(id, dto, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, actualizado);
    }

    public void handleDelete(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext actor = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        Map<String, String> pathVars = (Map<String, String>) exchange.getAttribute("pathVariables");
        Long id = Long.parseLong(pathVars.get("id"));
        usuarioUseCase.eliminarUsuario(id, actor, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, Map.of("message", "Usuario eliminado correctamente"));
    }

    private String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
    }
}
