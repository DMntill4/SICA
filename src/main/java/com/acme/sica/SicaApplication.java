package com.acme.sica;

import com.acme.sica.infrastructure.adapter.in.http.*;
import com.acme.sica.infrastructure.adapter.out.jdbc.*;
import com.acme.sica.infrastructure.audit.AuditService;
import com.acme.sica.infrastructure.db.SchemaInitializer;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;
import com.acme.sica.infrastructure.db.connection.DatabaseFactoryProvider;
import com.acme.sica.infrastructure.http.Router;
import com.acme.sica.infrastructure.security.AuthMiddleware;
import com.acme.sica.infrastructure.security.JwtUtil;
import com.acme.sica.infrastructure.security.PermissionChecker;
import com.acme.sica.usecase.auth.AuthUseCase;
import com.acme.sica.usecase.incidentes.RegistrarIncidenteUseCase;
import com.acme.sica.usecase.personas.GestionarPersonaUseCase;
import com.acme.sica.usecase.reportes.GenerarReporteUseCase;
import com.acme.sica.usecase.usuarios.GestionarUsuarioUseCase;
import com.acme.sica.usecase.visitas.GestionarVisitaUseCase;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Punto de Entrada Principal (Composition Root) del Backend SICA.
 * Aplica Arquitectura Hexagonal y Abstract Factory Pattern para la gestion de conexiones a BD (MySQL / H2).
 */
public class SicaApplication {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" SICA - Sistema Integrado de Control de Acceso");
        System.out.println(" Arquitectura Hexagonal + Abstract Factory (MySQL)");
        System.out.println("=================================================");

        try {
            // 1. Obtener Fabrica de Conexiones usando Abstract Factory Provider
            ConnectionFactory connectionFactory = DatabaseFactoryProvider.getDefaultConnectionFactory();
            SchemaInitializer initializer = new SchemaInitializer(connectionFactory);
            initializer.initialize();

            // 2. Output Adapters (Persistencia JDBC)
            AuditJdbcAdapter auditRepo = new AuditJdbcAdapter(connectionFactory);
            UsuarioJdbcAdapter usuarioRepo = new UsuarioJdbcAdapter(connectionFactory);
            PersonaJdbcAdapter personaRepo = new PersonaJdbcAdapter(connectionFactory);
            VisitaJdbcAdapter visitaRepo = new VisitaJdbcAdapter(connectionFactory);
            IncidenteJdbcAdapter incidenteRepo = new IncidenteJdbcAdapter(connectionFactory);

            // 3. Security & Infrastructure Services
            JwtUtil jwtUtil = new JwtUtil();
            AuditService auditService = new AuditService(auditRepo);
            PermissionChecker permissionChecker = new PermissionChecker(usuarioRepo);
            AuthMiddleware authMiddleware = new AuthMiddleware(jwtUtil, usuarioRepo, permissionChecker);

            // 4. Use Cases (Application Layer)
            AuthUseCase authUseCase = new AuthUseCase(usuarioRepo, jwtUtil, auditService);
            GestionarPersonaUseCase personaUseCase = new GestionarPersonaUseCase(personaRepo, auditService);
            GestionarUsuarioUseCase usuarioUseCase = new GestionarUsuarioUseCase(usuarioRepo, auditService);
            RegistrarIncidenteUseCase incidenteUseCase = new RegistrarIncidenteUseCase(incidenteRepo, personaRepo, auditService);
            GestionarVisitaUseCase visitaUseCase = new GestionarVisitaUseCase(visitaRepo, personaRepo, auditService);
            GenerarReporteUseCase reporteUseCase = new GenerarReporteUseCase(visitaRepo, incidenteRepo, auditRepo);

            // 5. Input Adapters (HTTP Handlers)
            AuthHttpHandler authHandler = new AuthHttpHandler(authUseCase);
            PersonaHttpHandler personaHandler = new PersonaHttpHandler(personaUseCase);
            UsuarioHttpHandler usuarioHandler = new UsuarioHttpHandler(usuarioUseCase);
            IncidenteHttpHandler incidenteHandler = new IncidenteHttpHandler(incidenteUseCase);
            VisitaHttpHandler visitaHandler = new VisitaHttpHandler(visitaUseCase);
            ReportesHttpHandler reportesHandler = new ReportesHttpHandler(reporteUseCase);

            // 6. HTTP Router Configuration
            Router router = new Router();
            router.setAuthInterceptor((exchange, route) -> {
                try {
                    return authMiddleware.intercept(exchange, route);
                } catch (Exception e) {
                    return false;
                }
            });

            // Rutas Auth
            router.postPublic("/auth/login", authHandler::handleLogin);
            router.post("/auth/logout", authHandler::handleLogout, null);

            // Rutas Usuarios
            router.get("/usuarios", usuarioHandler::handleFindAll, "crear_usuario");
            router.get("/usuarios/{id}", usuarioHandler::handleFindById, "crear_usuario");
            router.post("/usuarios", usuarioHandler::handleCreate, "crear_usuario");
            router.put("/usuarios/{id}", usuarioHandler::handleUpdate, "modificar_usuario");
            router.delete("/usuarios/{id}", usuarioHandler::handleDelete, "eliminar_usuario");

            // Rutas Personas
            router.get("/personas", personaHandler::handleFindAll, null);
            router.get("/personas/{id}", personaHandler::handleFindById, null);
            router.get("/personas/buscar/{doc}", personaHandler::handleFindByDoc, null);
            router.post("/personas", personaHandler::handleCreate, "crear_persona");
            router.put("/personas/{id}", personaHandler::handleUpdate, "modificar_persona");
            router.put("/personas/{id}/rehabilitar", personaHandler::handleRehabilitar, "modificar_persona");
            router.delete("/personas/{id}", personaHandler::handleDelete, "modificar_persona");

            // Rutas Incidentes
            router.post("/incidentes", incidenteHandler::handleCreate, "registrar_incidente");
            router.get("/incidentes", incidenteHandler::handleFindAll, "generar_reporte");

            // Rutas Visitas
            router.get("/visitas", visitaHandler::handleFindAll, "generar_reporte");
            router.get("/visitas/{id}", visitaHandler::handleFindById, "generar_reporte");
            router.post("/visitas/preregistrar", visitaHandler::handlePreregistrar, "preregistrar_visita");
            router.post("/visitas/no-anunciada", visitaHandler::handleNoAnunciada, "checkin_visita");
            router.post("/visitas/pase-temporal", visitaHandler::handlePaseTemporal, "checkin_visita");
            router.put("/visitas/{id}/aprobar", visitaHandler::handleAprobar, "aprobar_visita");
            router.put("/visitas/{id}/rechazar", visitaHandler::handleRechazar, "aprobar_visita");
            router.post("/visitas/{id}/check-in", visitaHandler::handleCheckIn, "checkin_visita");
            router.post("/visitas/{id}/check-out", visitaHandler::handleCheckOut, "checkout_visita");
            router.delete("/visitas", visitaHandler::handleDeleteAll, null);

            // Rutas Reportes
            router.get("/reportes/personas-dentro", reportesHandler::handlePersonasDentro, "generar_reporte");
            router.get("/reportes/visitas-rango", reportesHandler::handleVisitasRango, "generar_reporte");
            router.get("/reportes/incidentes", reportesHandler::handleIncidentes, "generar_reporte");
            router.get("/reportes/auditoria", reportesHandler::handleAuditoria, "consultar_auditoria");

            // 7. HttpServer Concurrente
            int port = 8080;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", router);
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            System.out.println("=================================================");
            System.out.println(" Servidor SICA iniciado exitosamente en puerto " + port);
            System.out.println(" Endpoint base: http://localhost:" + port);
            System.out.println("=================================================");

            // 8. Lanzar Interfaz Gráfica GUI (Swing + FlatLaf) automáticamente
            boolean isHeadless = false;
            for (String arg : args) {
                if ("--headless".equalsIgnoreCase(arg)) {
                    isHeadless = true;
                    break;
                }
            }

            if (!isHeadless && !java.awt.GraphicsEnvironment.isHeadless()) {
                System.out.println("[GUI Launcher] Desplegando Interfaz Grafica Swing FlatLaf...");
                com.formdev.flatlaf.FlatDarkLaf.setup();
                com.acme.sica.gui.client.SicaApiClient apiClient = new com.acme.sica.gui.client.SicaApiClient();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    com.acme.sica.gui.views.LoginFrame loginFrame = new com.acme.sica.gui.views.LoginFrame(apiClient);
                    loginFrame.setVisible(true);
                });
            } else {
                System.out.println("[GUI Launcher] Modo headless detectado. Ejecutando unicamene backend HTTP.");
            }

        } catch (Exception e) {
            System.err.println("[FATAL] Error al iniciar el servidor SICA: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
