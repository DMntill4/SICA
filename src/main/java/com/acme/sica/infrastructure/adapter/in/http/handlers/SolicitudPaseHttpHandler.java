package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.application.port.out.SolicitudPaseRepository;
import com.acme.sica.application.port.out.VisitaRepository;
import com.acme.sica.domain.enums.EstadoAcceso;
import com.acme.sica.domain.enums.EstadoVisita;
import com.acme.sica.domain.enums.TipoVisita;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.SolicitudPase;
import com.acme.sica.domain.model.Visita;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SolicitudPaseHttpHandler implements HttpHandler {

    private final SolicitudPaseRepository solicitudRepository;
    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;

    public SolicitudPaseHttpHandler(SolicitudPaseRepository solicitudRepository,
                                    PersonaRepository personaRepository,
                                    VisitaRepository visitaRepository) {
        this.solicitudRepository = solicitudRepository;
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equalsIgnoreCase(method)) {
                HttpUtils.sendJsonResponse(exchange, 204, null);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                if (path.contains("/persona/")) {
                    handleListarPorPersonaDoc(exchange, path);
                    return;
                }
                if (path.endsWith("/pendientes")) {
                    List<SolicitudPase> pendientes = solicitudRepository.listarPendientes();
                    HttpUtils.sendJsonResponse(exchange, 200, pendientes);
                } else {
                    List<SolicitudPase> todas = solicitudRepository.listarTodas();
                    HttpUtils.sendJsonResponse(exchange, 200, todas);
                }
                return;
            }


            if ("POST".equalsIgnoreCase(method)) {
                if (path.endsWith("/solicitar")) {
                    handleSolicitarPase(exchange);
                    return;
                }

                if (path.contains("/aprobar")) {
                    handleAprobarPase(exchange, path);
                    return;
                }

                if (path.contains("/rechazar")) {
                    handleRechazarPase(exchange, path);
                    return;
                }

                if (path.contains("/cancelar")) {
                    handleCancelarPase(exchange, path);
                    return;
                }

                if (path.endsWith("/anomalia")) {
                    handleReportarAnomalia(exchange);
                    return;
                }
            }



            HttpUtils.sendErrorResponse(exchange, 404, "Endpoint no encontrado");

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.sendErrorResponse(exchange, 400, e.getMessage() != null ? e.getMessage() : "Error en servidor");
        }

    }

    private void handleSolicitarPase(HttpExchange exchange) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = HttpUtils.parseJsonRequestBody(exchange, Map.class);

        if (body == null || !body.containsKey("nombreCompleto") || !body.containsKey("docIdentidad")) {
            HttpUtils.sendErrorResponse(exchange, 400, "Campos 'nombreCompleto' y 'docIdentidad' son obligatorios");
            return;
        }

        String nombre = (String) body.get("nombreCompleto");
        String doc = (String) body.get("docIdentidad");
        String email = (String) body.getOrDefault("email", "visitante@sica.local");
        String telefono = (String) body.getOrDefault("telefono", "");
        String empresa = (String) body.getOrDefault("empresaDestino", "General");
        String motivo = (String) body.getOrDefault("motivo", "Visita Programada");
        String vectorBiometrico = (String) body.get("vectorBiometrico");
        String fotoUrl = (String) body.get("fotoUrl");

        Long funcionarioId = null;
        if (body.get("funcionarioDestinoId") != null) {
            funcionarioId = Long.valueOf(body.get("funcionarioDestinoId").toString());
        } else {
            funcionarioId = 1L; // Fallback admin
        }

        // Guardar fotoUrl limpia sin truncamiento gracias a columnas LONGTEXT


        // 1. Guardar SolicitudPase
        SolicitudPase s = new SolicitudPase();
        s.setNombreCompleto(nombre);
        s.setDocIdentidad(doc);
        s.setEmail(email);
        s.setTelefono(telefono);
        s.setEmpresaDestino(empresa);
        s.setFuncionarioDestinoId(funcionarioId);
        s.setMotivo(motivo);
        s.setFechaHoraSolicitada(LocalDateTime.now().plusDays(1));
        s.setVectorBiometrico(vectorBiometrico);
        s.setFotoUrl(fotoUrl);
        s.setEstado(SolicitudPase.EstadoSolicitud.PENDIENTE_APROBACION);

        SolicitudPase guardada = solicitudRepository.guardar(s);

        // 2. REGISTRO/ACTUALIZACIÓN AUTOMÁTICA DE PERSONA EN SICA BD PARA SINCRONIZACIÓN EN VIVO CON PORTERÍA
        Optional<Persona> optP = personaRepository.findByDocIdentidad(doc);
        Persona persona;
        if (optP.isPresent()) {
            // BUG #3 FIX: usar update() para personas existentes, no save() (que podría hacer INSERT)
            persona = optP.get();
            if (vectorBiometrico != null) persona.setVectorBiometrico(vectorBiometrico);
            if (fotoUrl != null) persona.setFotoUrl(fotoUrl);
            personaRepository.update(persona);
        } else {
            persona = new Persona();
            String[] partes = nombre.split(" ", 2);
            persona.setNombre(partes[0]);
            persona.setApellido(partes.length > 1 ? partes[1] : "");

            persona.setDocIdentidad(doc);
            persona.setTipoDocumento("CC");
            persona.setEmail(email);
            persona.setTelefono(telefono);
            persona.setEstadoAcceso(EstadoAcceso.HABILITADO);
            persona.setVectorBiometrico(vectorBiometrico);
            persona.setFotoUrl(fotoUrl);
            persona = personaRepository.save(persona);
        }

        // 3. REGISTRO AUTOMÁTICO DE VISITA EN PORTERÍA (REQUIERE APROBACIÓN DE FUNCIONARIO)
        try {
            Visita v = new Visita();
            v.setPersonaId(persona.getId());
            v.setFuncionarioId(funcionarioId != null ? funcionarioId : 1L);
            v.setTipoVisita(motivo.contains("[EXPRESS]") ? TipoVisita.NO_ANUNCIADA : TipoVisita.PRE_REGISTRADA);
            v.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION);
            v.setMotivo(motivo);
            v.setFechaHoraProgramada(LocalDateTime.now().plusHours(2));
            visitaRepository.save(v);

        } catch (Exception ex) {
            System.err.println("[SolicitudPase Warning] Visita automatica no registrada en porteria, pero pase guardado: " + ex.getMessage());
        }

        HttpUtils.sendJsonResponse(exchange, 201, guardada);
    }


    private void handleAprobarPase(HttpExchange exchange, String path) throws IOException {
        Long id = extractIdFromPath(path, "/aprobar");
        if (id == null) {
            HttpUtils.sendErrorResponse(exchange, 400, "ID de solicitud no válido");
            return;
        }

        Optional<SolicitudPase> opt = solicitudRepository.buscarPorId(id);
        if (opt.isEmpty()) {
            HttpUtils.sendErrorResponse(exchange, 404, "Solicitud de pase no encontrada");
            return;
        }

        SolicitudPase sol = opt.get();
        solicitudRepository.actualizarEstado(id, SolicitudPase.EstadoSolicitud.APROBADO);

        // Sincronizar estado APROBADO en la tabla visita
        Optional<Persona> optP = personaRepository.findByDocIdentidad(sol.getDocIdentidad());
        if (optP.isPresent()) {
            Persona p = optP.get();
            try {
                visitaRepository.findAll().stream()
                        .filter(v -> v.getPersonaId() != null && v.getPersonaId().equals(p.getId()) && v.getEstadoVisita() == EstadoVisita.PENDIENTE_APROBACION)
                        .forEach(v -> {
                            v.setEstadoVisita(EstadoVisita.APROBADO);
                            visitaRepository.update(v);
                        });
            } catch (Exception ex) {
                System.err.println("[SolicitudPase Warning] Error al actualizar estado APROBADO en visita: " + ex.getMessage());
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Solicitud aprobada exitosamente.");
        resp.put("solicitudId", sol.getId());

        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }

    private void handleRechazarPase(HttpExchange exchange, String path) throws IOException {
        Long id = extractIdFromPath(path, "/rechazar");
        if (id == null) {
            HttpUtils.sendErrorResponse(exchange, 400, "ID de solicitud no válido");
            return;
        }

        // BUG #4 FIX: cargar la solicitud ANTES de actualizar el estado
        Optional<SolicitudPase> optS = solicitudRepository.buscarPorId(id);
        if (optS.isEmpty()) {
            HttpUtils.sendErrorResponse(exchange, 404, "Solicitud de pase no encontrada");
            return;
        }

        solicitudRepository.actualizarEstado(id, SolicitudPase.EstadoSolicitud.RECHAZADO);

        // Sincronizar estado RECHAZADO en la tabla visita
        SolicitudPase s = optS.get();
        Optional<Persona> optP = personaRepository.findByDocIdentidad(s.getDocIdentidad());
        if (optP.isPresent()) {
            Persona p = optP.get();
            try {
                visitaRepository.findAll().stream()
                        .filter(v -> v.getPersonaId() != null && v.getPersonaId().equals(p.getId()) && v.getEstadoVisita() == EstadoVisita.PENDIENTE_APROBACION)
                        .forEach(v -> {
                            v.setEstadoVisita(EstadoVisita.RECHAZADO);
                            visitaRepository.update(v);
                        });
            } catch (Exception ex) {
                System.err.println("[SolicitudPase Warning] Error al actualizar estado RECHAZADO en visita: " + ex.getMessage());
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Solicitud rechazada");
        resp.put("solicitudId", id);

        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }


    private void handleCancelarPase(HttpExchange exchange, String path) throws IOException {
        Long id = extractIdFromPath(path, "/cancelar");
        if (id != null) {
            solicitudRepository.actualizarEstado(id, SolicitudPase.EstadoSolicitud.RECHAZADO);

            // Sincronizar cancelación en la tabla visita
            // BUG #5 FIX: solo cancelar visitas PENDIENTE_APROBACION para no afectar
            // visitas DENTRO u APROBADAS de otros pases de la misma persona
            Optional<SolicitudPase> optS = solicitudRepository.buscarPorId(id);
            if (optS.isPresent()) {
                SolicitudPase s = optS.get();
                Optional<Persona> optP = personaRepository.findByDocIdentidad(s.getDocIdentidad());
                if (optP.isPresent()) {
                    Persona p = optP.get();
                    try {
                        visitaRepository.findAll().stream()
                                .filter(v -> v.getPersonaId() != null
                                        && v.getPersonaId().equals(p.getId())
                                        && (v.getEstadoVisita() == EstadoVisita.PENDIENTE_APROBACION
                                            || v.getEstadoVisita() == EstadoVisita.APROBADO))
                                .forEach(v -> {
                                    v.setEstadoVisita(EstadoVisita.CANCELADA);
                                    visitaRepository.update(v);
                                });
                    } catch (Exception ex) {
                        System.err.println("[SolicitudPase Warning] Error al actualizar estado CANCELADA en visita: " + ex.getMessage());
                    }
                }
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Solicitud de pase cancelada exitosamente.");
        if (id != null) resp.put("solicitudId", id);

        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }


    private void handleListarPorPersonaDoc(HttpExchange exchange, String path) throws IOException {
        String doc = path.substring(path.lastIndexOf("/") + 1).trim();
        List<SolicitudPase> todas = solicitudRepository.listarTodas();
        List<SolicitudPase> filtradas = todas.stream()
                .filter(s -> s.getDocIdentidad() != null && s.getDocIdentidad().trim().equalsIgnoreCase(doc))
                .toList();

        // Sincronización proactiva: Si la visita en la tabla 'visita' ya fue APROBADA o realizada, actualizar el pase
        try {
            Optional<Persona> optP = personaRepository.findByDocIdentidad(doc);
            if (optP.isPresent()) {
                Long personaId = optP.get().getId();
                List<Visita> misVisitas = visitaRepository.findAll().stream()
                        .filter(v -> v.getPersonaId() != null && v.getPersonaId().equals(personaId))
                        .toList();

                for (SolicitudPase sp : filtradas) {
                    if (sp.getEstado() == SolicitudPase.EstadoSolicitud.PENDIENTE_APROBACION) {
                        boolean estaAprobada = misVisitas.stream()
                                .anyMatch(v -> v.getEstadoVisita() == EstadoVisita.APROBADO || v.getEstadoVisita() == EstadoVisita.DENTRO || v.getEstadoVisita() == EstadoVisita.FINALIZADO);
                        
                        boolean estaRechazada = misVisitas.stream()
                                .anyMatch(v -> v.getEstadoVisita() == EstadoVisita.RECHAZADO);

                        if (estaAprobada) {
                            sp.setEstado(SolicitudPase.EstadoSolicitud.APROBADO);
                            solicitudRepository.actualizarEstado(sp.getId(), SolicitudPase.EstadoSolicitud.APROBADO);
                        } else if (estaRechazada) {
                            sp.setEstado(SolicitudPase.EstadoSolicitud.RECHAZADO);
                            solicitudRepository.actualizarEstado(sp.getId(), SolicitudPase.EstadoSolicitud.RECHAZADO);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[SolicitudPase Sync Warning] Error al sincronizar estado de pase con visitas: " + ex.getMessage());
        }

        HttpUtils.sendJsonResponse(exchange, 200, filtradas);
    }


    private void handleReportarAnomalia(HttpExchange exchange) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = HttpUtils.parseJsonRequestBody(exchange, Map.class);

        if (body == null || !body.containsKey("docIdentidad") || !body.containsKey("tipoAnomalia")) {
            HttpUtils.sendErrorResponse(exchange, 400, "Campos 'docIdentidad' y 'tipoAnomalia' son requeridos");
            return;
        }

        String doc = (String) body.get("docIdentidad");
        String tipo = (String) body.get("tipoAnomalia");
        String desc = (String) body.getOrDefault("descripcion", "Reporte generado desde el Portal Web de Autoservicio");

        Optional<Persona> optP = personaRepository.findByDocIdentidad(doc);
        if (optP.isEmpty()) {
            HttpUtils.sendErrorResponse(exchange, 404, "Persona no encontrada con documento: " + doc);
            return;
        }

        Persona p = optP.get();

        SolicitudPase s = new SolicitudPase();
        s.setNombreCompleto(p.getNombre() + " " + p.getApellido());
        s.setDocIdentidad(doc);
        s.setEmail(p.getEmail() != null ? p.getEmail() : "visitante@sica.local");
        s.setTelefono(p.getTelefono() != null ? p.getTelefono() : "");
        s.setEmpresaDestino("Reporte de Novedades Web");
        s.setFuncionarioDestinoId(3L);
        s.setMotivo("🚨 [NOVEDAD WEB] " + tipo + ": " + desc);
        s.setFechaHoraSolicitada(LocalDateTime.now());
        s.setFotoUrl(p.getFotoUrl());
        s.setEstado(SolicitudPase.EstadoSolicitud.PENDIENTE_APROBACION);

        SolicitudPase guardada = solicitudRepository.guardar(s);

        try {
            Visita v = new Visita();
            v.setPersonaId(p.getId());
            v.setFuncionarioId(3L);
            v.setTipoVisita(TipoVisita.PASE_TEMPORAL);
            v.setEstadoVisita(EstadoVisita.PENDIENTE_APROBACION_OLVIDO);
            v.setMotivo("🚨 Reporte Web: " + tipo + " - " + desc);
            v.setFechaHoraProgramada(LocalDateTime.now());
            visitaRepository.save(v);
        } catch (Exception ex) {
            System.err.println("[Anomalia Warning] No se creo la visita temporal: " + ex.getMessage());
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", guardada.getId());
        resp.put("mensaje", "Reporte de novedad registrado exitosamente con firma biométrica.");
        resp.put("tipoAnomalia", tipo);

        HttpUtils.sendJsonResponse(exchange, 201, resp);
    }

    private Long extractIdFromPath(String path, String actionSuffix) {



        try {
            String sub = path.substring(0, path.indexOf(actionSuffix));
            String idStr = sub.substring(sub.lastIndexOf("/") + 1);
            return Long.parseLong(idStr);
        } catch (Exception e) {
            return null;
        }
    }
}
