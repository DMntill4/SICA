package com.acme.sica.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Router implements HttpHandler {

    private final List<Route> routes = new ArrayList<>();
    private BiFunction<HttpExchange, Route, Boolean> authInterceptor;

    public void setAuthInterceptor(BiFunction<HttpExchange, Route, Boolean> authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    public void register(String method, String path, RouteHandler handler, boolean requiresAuth, String requiredPermission) {
        routes.add(new Route(method, path, handler, requiresAuth, requiredPermission));
    }

    public void get(String path, RouteHandler handler, String requiredPermission) {
        register("GET", path, handler, true, requiredPermission);
    }

    public void post(String path, RouteHandler handler, String requiredPermission) {
        register("POST", path, handler, true, requiredPermission);
    }

    public void postPublic(String path, RouteHandler handler) {
        register("POST", path, handler, false, null);
    }

    public void put(String path, RouteHandler handler, String requiredPermission) {
        register("PUT", path, handler, true, requiredPermission);
    }

    public void delete(String path, RouteHandler handler, String requiredPermission) {
        register("DELETE", path, handler, true, requiredPermission);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();

        for (Route route : routes) {
            if (route.matches(requestMethod, requestPath)) {
                try {
                    if (authInterceptor != null) {
                        boolean allowed = authInterceptor.apply(exchange, route);
                        if (!allowed) {
                            return;
                        }
                    }

                    Map<String, String> pathVariables = route.extractPathVariables(requestPath);
                    exchange.setAttribute("pathVariables", pathVariables);

                    route.getHandler().handle(exchange);
                    return;
                } catch (SecurityException e) {
                    HttpUtils.sendErrorResponse(exchange, 403, e.getMessage());
                    return;
                } catch (IllegalArgumentException e) {
                    HttpUtils.sendErrorResponse(exchange, 400, e.getMessage());
                    return;
                } catch (IllegalStateException e) {
                    HttpUtils.sendErrorResponse(exchange, 409, e.getMessage());
                    return;
                } catch (Exception e) {
                    System.err.println("[Router Error] Error no controlado: " + e.getMessage());
                    e.printStackTrace();
                    HttpUtils.sendErrorResponse(exchange, 500, "Error interno del servidor: " + e.getMessage());
                    return;
                }
            }
        }

        HttpUtils.sendErrorResponse(exchange, 404, "Ruta no encontrada o metodo HTTP no soportado: " + requestMethod + " " + requestPath);
    }
}
