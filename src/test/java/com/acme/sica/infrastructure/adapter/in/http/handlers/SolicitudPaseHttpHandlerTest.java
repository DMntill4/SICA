package com.acme.sica.infrastructure.adapter.in.http.handlers;

import com.acme.sica.application.port.out.PersonaRepository;
import com.acme.sica.application.port.out.SolicitudPaseRepository;
import com.acme.sica.application.port.out.VisitaRepository;
import com.acme.sica.domain.model.SolicitudPase;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitudPaseHttpHandlerTest {

    private SolicitudPaseRepository solicitudRepo;
    private PersonaRepository personaRepo;
    private VisitaRepository visitaRepo;
    private SolicitudPaseHttpHandler handler;

    @BeforeEach
    void setUp() {
        solicitudRepo = mock(SolicitudPaseRepository.class);
        personaRepo = mock(PersonaRepository.class);
        visitaRepo = mock(VisitaRepository.class);
        handler = new SolicitudPaseHttpHandler(solicitudRepo, personaRepo, visitaRepo);
    }

    @Test
    void testCrearSolicitudPasePortalWebHTTP() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/portal/solicitar"));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());

        String jsonInput = """
            {
                "nombreCompleto": "Maria Rodriguez",
                "docIdentidad": "55443322",
                "email": "maria@example.com",
                "telefono": "555-1111",
                "empresaDestino": "Acme Corp",
                "funcionarioDestinoId": 3,
                "motivo": "Reunion de Negocios"
            }
        """;

        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(jsonInput.getBytes(StandardCharsets.UTF_8)));

        SolicitudPase guardada = new SolicitudPase();
        guardada.setId(100L);
        guardada.setNombreCompleto("Maria Rodriguez");
        guardada.setEstado(SolicitudPase.EstadoSolicitud.PENDIENTE_APROBACION);

        when(solicitudRepo.guardar(any(SolicitudPase.class))).thenReturn(guardada);

        handler.handle(exchange);

        verify(exchange, times(1)).sendResponseHeaders(eq(201), anyLong());
    }
}
