package com.acme.sica.infrastructure.adapter.in.http;

import com.acme.sica.infrastructure.adapter.in.dto.LoginRequestDTO;
import com.acme.sica.infrastructure.adapter.in.dto.LoginResponseDTO;
import com.acme.sica.infrastructure.http.HttpUtils;
import com.acme.sica.infrastructure.security.AuthenticatedUserContext;
import com.acme.sica.usecase.auth.AuthUseCase;
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
