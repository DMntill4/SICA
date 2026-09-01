package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BiometriaHttpHandler implements HttpHandler {

    private final PersonaRepository personaRepository;

    public BiometriaHttpHandler(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            HttpUtils.sendJsonResponse(exchange, 204, null);
            return;
        }

        if ("GET".equalsIgnoreCase(method)) {
            handleGetVerificarDoc(exchange);
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            handlePostVerificarBiometria(exchange);
            return;
        }

        HttpUtils.sendErrorResponse(exchange, 405, "Método no permitido");
    }

    private void handleGetVerificarDoc(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String doc = null;
        if (query != null && query.contains("doc=")) {
            for (String param : query.split("&")) {
                if (param.startsWith("doc=")) {
                    doc = param.substring(4);
                    break;
                }
            }
        }

        if (doc == null || doc.trim().isEmpty()) {
            HttpUtils.sendErrorResponse(exchange, 400, "Parámetro 'doc' es obligatorio");
            return;
        }

        Optional<Persona> optP = personaRepository.findByDocIdentidad(doc.trim());
        Map<String, Object> resp = new HashMap<>();

        if (optP.isPresent()) {
            Persona p = optP.get();
            resp.put("existe", true);
            resp.put("docIdentidad", p.getDocIdentidad());
            resp.put("nombreCompleto", p.getNombre() + " " + p.getApellido());
            resp.put("nombre", p.getNombre());
            resp.put("apellido", p.getApellido());
            resp.put("email", p.getEmail());
            resp.put("telefono", p.getTelefono());
            resp.put("empresaNombre", p.getEmpresaNombre());
            resp.put("fotoUrl", p.getFotoUrl());
        } else {
            resp.put("existe", false);
        }

        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }

    private void handlePostVerificarBiometria(HttpExchange exchange) throws IOException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = HttpUtils.parseJsonRequestBody(exchange, Map.class);

            if (body == null || !body.containsKey("vectorBiometrico")) {
                HttpUtils.sendErrorResponse(exchange, 400, "Se requiere 'vectorBiometrico'");
                return;
            }

            String doc = (String) body.get("docIdentidad");
            String nuevoVectorStr = (String) body.get("vectorBiometrico");

            // Si se pasa documento explícito, verificar si esa persona existe y comparar su vector
            if (doc != null && !doc.trim().isEmpty()) {
                Optional<Persona> optP = personaRepository.findByDocIdentidad(doc.trim());
                if (optP.isPresent()) {
                    Persona p = optP.get();
                    if (p.getVectorBiometrico() != null && !p.getVectorBiometrico().isEmpty()) {
                        double dist = calcularDistanciaEuclidianaStrict(p.getVectorBiometrico(), nuevoVectorStr);
                        // NOTA: El umbral 0.55 es apropiado para vectores de luminancia por zonas del frontend.
                        // Si se migra a FaceNet/ArcFace real, reducir a 0.35.
                        if (dist <= 0.55) {
                            double pct = Math.round((1.0 - dist / 0.55) * 100.0 * 10.0) / 10.0;
                            Map<String, Object> resp = buildPersonaMatchResponse(p, true, pct, "Identidad verificada exitosamente.");
                            HttpUtils.sendJsonResponse(exchange, 200, resp);
                            return;
                        }
                    } else {
                        // Si no tenía vector previo, registrar el nuevo vector facial para esta persona
                        p.setVectorBiometrico(nuevoVectorStr);
                        personaRepository.save(p);
                        Map<String, Object> resp = buildPersonaMatchResponse(p, true, 100.0, "Firma facial vinculada por primera vez.");
                        HttpUtils.sendJsonResponse(exchange, 200, resp);
                        return;
                    }
                }
            }

            // Búsqueda general estricta por comparación de firmas vectoriales faciales 128D
            List<Persona> personas = personaRepository.findAll();
            Persona mejorPersona = null;
            double menorDistancia = 1.0;

            for (Persona p : personas) {
                if (p.getVectorBiometrico() != null && !p.getVectorBiometrico().isEmpty()) {
                    double dist = calcularDistanciaEuclidianaStrict(p.getVectorBiometrico(), nuevoVectorStr);
                    if (dist < menorDistancia) {
                        menorDistancia = dist;
                        mejorPersona = p;
                    }
                }
            }

            // UMBRAL: 0.55 para vectores de luminancia del frontend. Cambiar a 0.35 con embeddings de red neuronal real.
            if (mejorPersona != null && menorDistancia <= 0.55) {
                double porcentaje = Math.round((1.0 - menorDistancia / 0.55) * 100.0 * 10.0) / 10.0;
                Map<String, Object> resp = buildPersonaMatchResponse(mejorPersona, true, porcentaje, "Identidad facial autenticada.");
                HttpUtils.sendJsonResponse(exchange, 200, resp);
            } else {
                // RECHAZO TOTAL DE ROSTROS DESCONOCIDOS O NO COINCIDENTES (SIN DUMMIES NI FALLBACKS DE "JUAN PÉREZ")
                Map<String, Object> resp = new HashMap<>();
                resp.put("coincidencia", false);
                resp.put("distancia", menorDistancia);
                resp.put("mensaje", "⛔ Rostro no reconocido. Tu firma facial no coincide con ningún perfil registrado en SICA.");
                HttpUtils.sendJsonResponse(exchange, 200, resp);
            }

        } catch (Exception e) {
            HttpUtils.sendErrorResponse(exchange, 400, "Error procesando biometría: " + e.getMessage());
        }
    }

    private Map<String, Object> buildPersonaMatchResponse(Persona p, boolean match, double porcentaje, String msg) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("coincidencia", match);
        resp.put("porcentajeCoincidencia", porcentaje);
        resp.put("mensaje", msg);
        resp.put("personaId", p.getId());
        resp.put("docIdentidad", p.getDocIdentidad());
        String apellido = p.getApellido() != null ? p.getApellido().trim() : "";
        String nombreCompleto = p.getNombre() + (!apellido.isEmpty() ? " " + apellido : "");
        resp.put("nombreCompleto", nombreCompleto);
        resp.put("nombre", p.getNombre());
        resp.put("apellido", apellido);

        resp.put("email", p.getEmail() != null ? p.getEmail() : "");
        resp.put("telefono", p.getTelefono() != null ? p.getTelefono() : "");
        resp.put("empresaNombre", p.getEmpresaNombre() != null ? p.getEmpresaNombre() : "General");
        resp.put("fotoUrl", p.getFotoUrl());
        resp.put("estadoAcceso", p.getEstadoAcceso() != null ? p.getEstadoAcceso().name() : "HABILITADO");
        return resp;
    }

    private double calcularDistanciaEuclidianaStrict(String v1Str, String v2Str) {
        if (v1Str == null || v2Str == null) return 1.0;
        try {
            double[] v1 = parseVector(v1Str);
            double[] v2 = parseVector(v2Str);
            if (v1.length == 0 || v2.length == 0) return 1.0;

            int len = Math.min(v1.length, v2.length);
            double sumSq = 0.0;
            for (int i = 0; i < len; i++) {
                double diff = v1[i] - v2[i];
                sumSq += diff * diff;
            }
            double dist = Math.sqrt(sumSq / len);
            return Math.min(1.0, dist);
        } catch (Exception e) {
            return 1.0;
        }
    }

    private double[] parseVector(String vectorStr) {
        String clean = vectorStr.replaceAll("[\\[\\]\" ]", "");
        if (clean.isEmpty()) return new double[0];
        String[] parts = clean.split(",");
        double[] res = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            res[i] = Double.parseDouble(parts[i].trim());
        }
        return res;
    }
}
