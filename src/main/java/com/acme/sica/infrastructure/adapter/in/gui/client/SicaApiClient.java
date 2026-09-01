package com.acme.sica.infrastructure.adapter.in.gui.client;

import com.acme.sica.domain.enums.NivelGravedad;

import com.acme.sica.domain.model.Incidente;
import com.acme.sica.domain.model.Permiso;
import com.acme.sica.domain.model.Persona;
import com.acme.sica.domain.model.Rol;
import com.acme.sica.domain.model.Usuario;
import com.acme.sica.domain.model.Visita;


import com.acme.sica.application.dto.*;
import com.acme.sica.infrastructure.adapter.in.http.router.HttpUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Cliente HTTP desacoplado para consumir la API REST de SICA en http://localhost:8080.
 */
public class SicaApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient client;

    public SicaApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public LoginResponseDTO login(String username, String password) throws Exception {
        LoginRequestDTO body = new LoginRequestDTO(username, password);
        String json = HttpUtils.objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            LoginResponseDTO res = HttpUtils.objectMapper.readValue(response.body(), LoginResponseDTO.class);
            SessionContext.getInstance().setSession(res);
            return res;
        } else {
            throw parseErrorResponse(response);
        }
    }

    public void logout() {
        try {
            HttpRequest request = buildAuthRequest("/auth/logout")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
        } finally {
            SessionContext.getInstance().clear();
        }
    }

    public Persona buscarPersonaPorDoc(String docIdentidad) throws Exception {
        HttpRequest request = buildAuthRequest("/personas/buscar/" + docIdentidad)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Persona.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public List<Persona> listarPersonas() throws Exception {
        HttpRequest request = buildAuthRequest("/personas")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Persona>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public List<Map<String, Object>> listarUsuarios() throws Exception {
        HttpRequest request = buildAuthRequest("/usuarios")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Persona crearPersona(String docIdentidad, String tipoDocumento, String nombre, String apellido, String email, String telefono) throws Exception {
        return crearPersona(docIdentidad, tipoDocumento, nombre, apellido, email, telefono, null, null);
    }

    public Persona crearPersona(String docIdentidad, String tipoDocumento, String nombre, String apellido, String email, String telefono, Long empresaId, String fotoUrl) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("docIdentidad", docIdentidad);
        body.put("tipoDocumento", tipoDocumento);
        body.put("nombre", nombre);
        body.put("apellido", apellido);
        body.put("email", email);
        body.put("telefono", telefono);
        if (empresaId != null) body.put("empresaId", empresaId);
        if (fotoUrl != null) body.put("fotoUrl", fotoUrl);

        String json = HttpUtils.objectMapper.writeValueAsString(body);

        HttpRequest request = buildAuthRequest("/personas")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), Persona.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Persona actualizarPersona(Long id, String docIdentidad, String tipoDocumento, String nombre, String apellido, String email, String telefono, Long empresaId, String fotoUrl) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("docIdentidad", docIdentidad);
        body.put("tipoDocumento", tipoDocumento);
        body.put("nombre", nombre);
        body.put("apellido", apellido);
        body.put("email", email);
        body.put("telefono", telefono);
        if (empresaId != null) body.put("empresaId", empresaId);
        if (fotoUrl != null) body.put("fotoUrl", fotoUrl);

        String json = HttpUtils.objectMapper.writeValueAsString(body);

        HttpRequest request = buildAuthRequest("/personas/" + id)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Persona.class);
        } else {
            throw parseErrorResponse(response);
        }
    }



    public void eliminarPersona(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/personas/" + id)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw parseErrorResponse(response);
        }
    }

    public Persona rehabilitarPersona(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/personas/" + id + "/rehabilitar")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Persona.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Map<String, Object> crearUsuario(String username, String password, String nombreCompleto, String email, Long rolId) throws Exception {
        return crearUsuario(username, password, nombreCompleto, email, rolId, null);
    }

    public Map<String, Object> crearUsuario(String username, String password, String nombreCompleto, String email, Long rolId, String fotoUrl) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("nombreCompleto", nombreCompleto);
        body.put("email", email);
        body.put("rolId", rolId);
        if (fotoUrl != null) body.put("fotoUrl", fotoUrl);

        String json = HttpUtils.objectMapper.writeValueAsString(body);

        HttpRequest request = buildAuthRequest("/usuarios")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }


    public Map<String, Object> actualizarUsuario(Long id, String nombreCompleto, String email, Long rolId) throws Exception {
        Map<String, Object> body = Map.of(
                "nombreCompleto", nombreCompleto,
                "email", email,
                "rolId", rolId
        );
        String json = HttpUtils.objectMapper.writeValueAsString(body);

        HttpRequest request = buildAuthRequest("/usuarios/" + id)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public void eliminarUsuario(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/usuarios/" + id)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw parseErrorResponse(response);
        }
    }

    public List<Map<String, Object>> listarEmpresas() throws Exception {
        HttpRequest request = buildAuthRequest("/empresas")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Map<String, Object> crearEmpresa(String nit, String nombre, String ubicacionOficina) throws Exception {
        Map<String, Object> body = Map.of(
                "nit", nit,
                "nombre", nombre,
                "ubicacionOficina", ubicacionOficina,
                "activa", true
        );
        String json = HttpUtils.objectMapper.writeValueAsString(body);

        HttpRequest request = buildAuthRequest("/empresas")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public void eliminarEmpresa(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/empresas/" + id)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw parseErrorResponse(response);
        }
    }


    public Visita preregistrarVisita(Long personaId, String motivo, LocalDateTime fechaProgramada) throws Exception {
        PreregistroVisitaDTO dto = new PreregistroVisitaDTO(personaId, motivo, fechaProgramada);
        String json = HttpUtils.objectMapper.writeValueAsString(dto);

        HttpRequest request = buildAuthRequest("/visitas/preregistrar")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), Visita.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Visita registrarNoAnunciada(Long personaId, Long funcionarioId, String motivo) throws Exception {
        VisitaNoAnunciadaDTO dto = new VisitaNoAnunciadaDTO(personaId, funcionarioId, 1L, motivo);
        String json = HttpUtils.objectMapper.writeValueAsString(dto);

        HttpRequest request = buildAuthRequest("/visitas/no-anunciada")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), Visita.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Visita registrarPaseTemporal(Long personaId, Long funcionarioId, String motivo) throws Exception {
        PaseTemporalDTO dto = new PaseTemporalDTO(personaId, funcionarioId, 1L, motivo);
        String json = HttpUtils.objectMapper.writeValueAsString(dto);

        HttpRequest request = buildAuthRequest("/visitas/pase-temporal")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), Visita.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Visita aprobarVisita(Long visitaId) throws Exception {
        HttpRequest request = buildAuthRequest("/visitas/" + visitaId + "/aprobar")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Visita.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Visita rechazarVisita(Long visitaId) throws Exception {
        HttpRequest request = buildAuthRequest("/visitas/" + visitaId + "/rechazar")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Visita.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Visita checkIn(Long visitaId, Long puntoAccesoId) throws Exception {
        CheckInDTO dto = new CheckInDTO(puntoAccesoId != null ? puntoAccesoId : 1L);
        String json = HttpUtils.objectMapper.writeValueAsString(dto);

        HttpRequest request = buildAuthRequest("/visitas/" + visitaId + "/check-in")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Visita.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Visita checkOut(Long visitaId) throws Exception {
        HttpRequest request = buildAuthRequest("/visitas/" + visitaId + "/check-out")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Visita.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Persona crearPersona(String nombre, String apellido, String docIdentidad, String email) throws Exception {
        return crearPersona(docIdentidad, "CC", nombre, apellido, email, "");
    }

    public Visita checkInVisita(Long visitaId) throws Exception {
        return checkIn(visitaId, 1L);
    }

    public Visita checkOutVisita(Long visitaId) throws Exception {
        return checkOut(visitaId);
    }

    public void crearVisitaRapidaPrueba() throws Exception {
        Persona p = buscarPersonaPorDoc("1010101010");
        if (p == null) {
            p = crearPersona("Juan", "Pérez", "1010101010", "juan.perez@email.local");
        }
        preregistrarVisita(p.getId(), "Visita de Prueba Rápida", LocalDateTime.now().plusHours(1));
    }

    public Visita registrarVisitaNoAnunciada(String docIdentidad, String motivo) throws Exception {
        return registrarVisitaNoAnunciada(docIdentidad, "Visitante", "No Anunciado", "visitante@sica.local", 3L, motivo, null);
    }

    public Visita registrarVisitaNoAnunciada(String docIdentidad, String nombre, String apellido, String email, Long funcionarioId, String motivo, String fotoUrl) throws Exception {
        Persona p = null;
        try {
            p = buscarPersonaPorDoc(docIdentidad);
        } catch (Exception ignored) {}

        if (p == null) {
            p = crearPersona(docIdentidad, "CC", nombre, apellido, email, "", null, fotoUrl);
        } else if ((nombre != null && !nombre.trim().isEmpty()) || (fotoUrl != null && !fotoUrl.trim().isEmpty())) {
            try {
                actualizarPersona(p.getId(), docIdentidad, "CC",
                        (nombre != null && !nombre.trim().isEmpty()) ? nombre : p.getNombre(),
                        (apellido != null && !apellido.trim().isEmpty()) ? apellido : p.getApellido(),
                        (email != null && !email.trim().isEmpty()) ? email : p.getEmail(),
                        "", p.getEmpresaId(), fotoUrl != null ? fotoUrl : p.getFotoUrl());
            } catch (Exception ignored) {}
        }

        Long fId = (funcionarioId != null && funcionarioId > 0) ? funcionarioId : 3L;
        return registrarNoAnunciada(p.getId(), fId, motivo != null ? motivo : "Visita No Anunciada");
    }


    public Visita emitirPaseTemporal(String docIdentidad) throws Exception {
        return emitirPaseTemporal(docIdentidad, "Trabajador", "Pase Temporal", "temporal@sica.local", 3L, "Pase Temporal Emitido en Porteria", null);
    }

    public Visita emitirPaseTemporal(String docIdentidad, String nombre, String apellido, String email, Long funcionarioId, String motivo, String fotoUrl) throws Exception {
        Persona p = null;
        try {
            p = buscarPersonaPorDoc(docIdentidad);
        } catch (Exception ignored) {}

        if (p == null) {
            p = crearPersona(docIdentidad, "CC", nombre, apellido, email, "", null, fotoUrl);
        } else if ((nombre != null && !nombre.trim().isEmpty()) || (fotoUrl != null && !fotoUrl.trim().isEmpty())) {
            try {
                actualizarPersona(p.getId(), docIdentidad, "CC",
                        (nombre != null && !nombre.trim().isEmpty()) ? nombre : p.getNombre(),
                        (apellido != null && !apellido.trim().isEmpty()) ? apellido : p.getApellido(),
                        (email != null && !email.trim().isEmpty()) ? email : p.getEmail(),
                        "", p.getEmpresaId(), fotoUrl != null ? fotoUrl : p.getFotoUrl());
            } catch (Exception ignored) {}
        }

        Long fId = (funcionarioId != null && funcionarioId > 0) ? funcionarioId : 3L;
        return registrarPaseTemporal(p.getId(), fId, motivo != null ? motivo : "Pase Temporal Emitido por Olvido de Carnet");
    }


    public List<Incidente> listarIncidentesPorPersona(Long personaId) throws Exception {
        HttpRequest request = buildAuthRequest("/incidentes/persona/" + personaId)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Incidente>>() {});
        } else {
            return List.of();
        }
    }

    public void limpiarVisitas() throws Exception {

        HttpRequest request = buildAuthRequest("/visitas")
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw parseErrorResponse(response);
        }
    }

    public List<Visita> listarVisitas() throws Exception {
        HttpRequest request = buildAuthRequest("/visitas")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Visita>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Incidente registrarIncidente(Long personaId, String titulo, String descripcion, NivelGravedad nivelGravedad) throws Exception {
        IncidenteDTO dto = new IncidenteDTO(personaId, titulo, descripcion, nivelGravedad);
        String json = HttpUtils.objectMapper.writeValueAsString(dto);

        HttpRequest request = buildAuthRequest("/incidentes")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), Incidente.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public List<Incidente> listarIncidentes() throws Exception {
        HttpRequest request = buildAuthRequest("/incidentes")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Incidente>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Usuario toggleBloqueoUsuario(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/usuarios/" + id + "/toggle-bloqueo")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), Usuario.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Map<String, Object> getPersonasDentro() throws Exception {

        HttpRequest request = buildAuthRequest("/reportes/personas-dentro")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Map<String, Object> getAuditoria(int limit) throws Exception {
        HttpRequest request = buildAuthRequest("/reportes/auditoria?limit=" + limit)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public List<Rol> listarRoles() throws Exception {
        HttpRequest request = buildAuthRequest("/roles")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Rol>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public List<Permiso> listarPermisos() throws Exception {
        HttpRequest request = buildAuthRequest("/permisos")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Permiso>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Rol crearRol(String nombre, String descripcion, List<Long> permisoIds) throws Exception {
        RolDTO dto = new RolDTO(null, nombre, descripcion, permisoIds);
        String json = HttpUtils.objectMapper.writeValueAsString(dto);
        HttpRequest request = buildAuthRequest("/roles")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return HttpUtils.objectMapper.readValue(response.body(), Rol.class);
        } else {
            throw parseErrorResponse(response);
        }
    }

    public void actualizarPermisosRol(Long rolId, List<Long> permisoIds) throws Exception {
        RolDTO dto = new RolDTO(rolId, null, null, permisoIds);
        String json = HttpUtils.objectMapper.writeValueAsString(dto);
        HttpRequest request = buildAuthRequest("/roles/" + rolId + "/permisos")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw parseErrorResponse(response);
        }
    }

    public void eliminarRol(Long rolId) throws Exception {
        HttpRequest request = buildAuthRequest("/roles/" + rolId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw parseErrorResponse(response);
        }
    }

    public List<Map<String, Object>> listarSolicitudesPasePendientes() throws Exception {
        HttpRequest request = buildAuthRequest("/pases/pendientes")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Map<String, Object> aprobarSolicitudPase(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/pases/" + id + "/aprobar")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    public Map<String, Object> rechazarSolicitudPase(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/pases/" + id + "/rechazar")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } else {
            throw parseErrorResponse(response);
        }
    }

    private HttpRequest.Builder buildAuthRequest(String path) {


        String token = SessionContext.getInstance().getToken();
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + (token != null ? token : ""));
    }

    private Exception parseErrorResponse(HttpResponse<String> response) {
        try {
            Map<String, Object> err = HttpUtils.objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            String msg = (String) err.get("message");
            return new RuntimeException(msg != null ? msg : "Error HTTP " + response.statusCode());
        } catch (Exception e) {
            return new RuntimeException("Error (" + response.statusCode() + "): " + response.body());
        }
    }
}
