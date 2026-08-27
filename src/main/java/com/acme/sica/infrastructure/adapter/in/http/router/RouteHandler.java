package com.acme.sica.infrastructure.adapter.in.http.router;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

@FunctionalInterface
public interface RouteHandler {
    void handle(HttpExchange exchange) throws IOException;
}
