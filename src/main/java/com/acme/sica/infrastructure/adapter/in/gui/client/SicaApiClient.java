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
        Map<String, Object> body = Map.of(
                "docIdentidad", docIdentidad,
                "tipoDocumento", tipoDocumento,
                "nombre", nombre,
                "apellido", apellido,
                "email", email,
                "telefono", telefono
        );
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
        Map<String, Object> body = Map.of(
                "username", username,
                "password", password,
                "nombreCompleto", nombreCompleto,
                "email", email,
                "rolId", rolId
        );
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

    public void eliminarUsuario(Long id) throws Exception {
        HttpRequest request = buildAuthRequest("/usuarios/" + id)
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
