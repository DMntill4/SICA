package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.application.dto.LoginRequestDTO;
import com.acme.sica.application.dto.LoginResponseDTO;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.acme.sica.application.AuthenticatedUserContext;
import com.acme.sica.application.usecase.auth.AuthUseCase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class AuthHttpHandler {

    private final AuthUseCase authUseCase;

    public AuthHttpHandler(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        LoginRequestDTO request = HttpUtils.readRequestBody(exchange, LoginRequestDTO.class);
        String ipOrigen = getRemoteIp(exchange);
        LoginResponseDTO response = authUseCase.login(request, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, response);
    }

    public void handleLogout(HttpExchange exchange) throws IOException {
        AuthenticatedUserContext userContext = (AuthenticatedUserContext) exchange.getAttribute("userContext");
        String ipOrigen = getRemoteIp(exchange);
        String tokenJti = userContext != null ? userContext.tokenJti() : null;

        authUseCase.logout(userContext, tokenJti, ipOrigen);
        HttpUtils.sendJsonResponse(exchange, 200, java.util.Map.of("message", "Sesion cerrada exitosamente"));
    }

    private String getRemoteIp(HttpExchange exchange) {
        return exchange.getRemoteAddress() != null ? exchange.getRemoteAddress().getAddress().getHostAddress() : "127.0.0.1";
    }
}
