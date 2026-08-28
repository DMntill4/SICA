package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StaticFileHttpHandler implements HttpHandler {

    private final String resourcePrefix;

    public StaticFileHttpHandler(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix.endsWith("/") ? resourcePrefix : resourcePrefix + "/";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String subPath = path.startsWith("/portal") ? path.substring("/portal".length()) : path;

        if (subPath.isEmpty() || subPath.equals("/")) {
            subPath = "/index.html";
        }
        if (!subPath.startsWith("/")) {
            subPath = "/" + subPath;
        }

        String resourcePath = resourcePrefix + subPath.substring(1);
        InputStream is = getClass().getResourceAsStream(resourcePath);


        if (is == null) {
            String errorMsg = "404 File Not Found: " + path;
            exchange.sendResponseHeaders(404, errorMsg.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorMsg.getBytes());
            }
            return;
        }

        String contentType = getContentType(resourcePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0);

        try (OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }
}
