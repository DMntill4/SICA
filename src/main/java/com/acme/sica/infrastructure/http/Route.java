package com.acme.sica.infrastructure.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private final String method;
    private final String pathPatternStr;
    private final Pattern pattern;
    private final List<String> variableNames;
    private final RouteHandler handler;
    private final boolean requiresAuth;
    private final String requiredPermission;

    public Route(String method, String pathPatternStr, RouteHandler handler, boolean requiresAuth, String requiredPermission) {
        this.method = method.toUpperCase();
        this.pathPatternStr = pathPatternStr;
        this.handler = handler;
        this.requiresAuth = requiresAuth;
        this.requiredPermission = requiredPermission;
        this.variableNames = new ArrayList<>();

        String regex = pathPatternStr;
        Matcher m = Pattern.compile("\\{([a-zA-Z0-9_]+)}").matcher(pathPatternStr);
        while (m.find()) {
            variableNames.add(m.group(1));
        }
        regex = regex.replaceAll("\\{[a-zA-Z0-9_]+}", "([^/]+)");
        this.pattern = Pattern.compile("^" + regex + "$");
    }

    public boolean matches(String requestMethod, String requestPath) {
        if (!this.method.equalsIgnoreCase(requestMethod)) {
            return false;
        }
        return pattern.matcher(requestPath).matches();
    }

    public Map<String, String> extractPathVariables(String requestPath) {
        Map<String, String> vars = new HashMap<>();
        Matcher m = pattern.matcher(requestPath);
        if (m.matches()) {
            for (int i = 0; i < variableNames.size(); i++) {
                vars.put(variableNames.get(i), m.group(i + 1));
            }
        }
        return vars;
    }

    public String getMethod() { return method; }
    public String getPathPatternStr() { return pathPatternStr; }
    public RouteHandler getHandler() { return handler; }
    public boolean requiresAuth() { return requiresAuth; }
    public String requiredPermission() { return requiredPermission; }
}
