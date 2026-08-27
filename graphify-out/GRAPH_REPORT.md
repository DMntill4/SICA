# Graph Report - SICA  (2026-08-26)

## Corpus Check
- 84 files · ~24,357 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 682 nodes · 1620 edges · 31 communities (24 shown, 7 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 214 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1536ac29`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Persona
- Usuario
- GuardiaPanel
- SicaApiClient
- com.sun.net.httpserver.HttpExchange
- DatabaseConfig
- Visita
- SicaApplication.java
- AuthenticatedUserContext
- IncidentesPanel
- Permiso
- GestionarVisitaUseCase
- org.junit.jupiter.api.DisplayName
- BitacoraAuditoria
- com.acme.sica.application.port.out.AuditService
- SICA - Sistema Integrado de Control de Acceso (Zona Acme)
- .createVisita
- PuntoAcceso
- GestionarVisitaUseCase.java
- SalidaOlvidadaTest.java
- .registrarNoAnunciada
- .testCheckIn_SalidaOlvidadaAutoClose
- mvnw
- com.acme:sica
- Pure Java Backend Architecture & Best Practices
- Reglas de Desarrollo: Pure Java Backend
- rules/graphify.md
- workflows/graphify.md

## God Nodes (most connected - your core abstractions)
1. `Visita` - 91 edges
2. `Persona` - 55 edges
3. `SicaApiClient` - 47 edges
4. `AuthenticatedUserContext` - 36 edges
5. `Usuario` - 35 edges
6. `Incidente` - 34 edges
7. `GuardiaPanel` - 26 edges
8. `GestionarVisitaUseCase` - 22 edges
9. `FuncionarioPanel` - 21 edges
10. `BitacoraAuditoria` - 20 edges

## Surprising Connections (you probably didn't know these)
- `EmpresaHttpHandler` --references--> `GestionarEmpresaUseCase`  [EXTRACTED]
  src/main/java/com/acme/sica/infrastructure/adapter/in/http/handlers/EmpresaHttpHandler.java → src/main/java/com/acme/sica/application/usecase/GestionarEmpresaUseCase.java
- `AuthHttpHandler` --references--> `AuthUseCase`  [EXTRACTED]
  src/main/java/com/acme/sica/infrastructure/adapter/in/http/handlers/AuthHttpHandler.java → src/main/java/com/acme/sica/application/usecase/auth/AuthUseCase.java
- `IncidenteHttpHandler` --references--> `RegistrarIncidenteUseCase`  [EXTRACTED]
  src/main/java/com/acme/sica/infrastructure/adapter/in/http/handlers/IncidenteHttpHandler.java → src/main/java/com/acme/sica/application/usecase/incidentes/RegistrarIncidenteUseCase.java
- `PersonaHttpHandler` --references--> `GestionarPersonaUseCase`  [EXTRACTED]
  src/main/java/com/acme/sica/infrastructure/adapter/in/http/handlers/PersonaHttpHandler.java → src/main/java/com/acme/sica/application/usecase/personas/GestionarPersonaUseCase.java
- `UsuarioHttpHandler` --references--> `GestionarUsuarioUseCase`  [EXTRACTED]
  src/main/java/com/acme/sica/infrastructure/adapter/in/http/handlers/UsuarioHttpHandler.java → src/main/java/com/acme/sica/application/usecase/usuarios/GestionarUsuarioUseCase.java

## Import Cycles
- None detected.

## Communities (31 total, 7 thin omitted)

### Community 0 - "Persona"
Cohesion: 0.08
Nodes (9): com.acme.sica.application.port.out.PersonaRepository, PersonaDTO, RegistrarIncidenteUseCase, GestionarPersonaUseCase, Override, EstadoAcceso, HABILITADO, RESTRINGIDO (+1 more)

### Community 1 - "Usuario"
Cohesion: 0.09
Nodes (5): LoginRequestDTO, UsuarioDTO, GestionarUsuarioUseCase, Usuario, PasswordHasher

### Community 2 - "GuardiaPanel"
Cohesion: 0.09
Nodes (19): javax.swing.table.DefaultTableModel, JPanel, AuditoriaPanel, DefaultTableModel, JButton, JLabel, JTable, FuncionarioPanel (+11 more)

### Community 3 - "SicaApiClient"
Cohesion: 0.07
Nodes (11): Builder, JFrame, JPasswordField, LoginResponseDTO, SessionContext, SicaApiClient, JButton, JLabel (+3 more)

### Community 4 - "com.sun.net.httpserver.HttpExchange"
Cohesion: 0.09
Nodes (9): com.sun.net.httpserver.HttpExchange, EmpresaDTO, AuthHttpHandler, EmpresaHttpHandler, IncidenteHttpHandler, PersonaHttpHandler, UsuarioHttpHandler, VisitaHttpHandler (+1 more)

### Community 5 - "DatabaseConfig"
Cohesion: 0.13
Nodes (7): java.sql.Connection, DatabaseConfig, H2ConnectionFactory, Override, Override, MySqlConnectionFactory, H2ConnectionFactory

### Community 7 - "SicaApplication.java"
Cohesion: 0.06
Nodes (23): com.acme.sica.application.port.out.UsuarioRepository, com.auth0.jwt.algorithms.Algorithm, com.auth0.jwt.interfaces.DecodedJWT, com.auth0.jwt.interfaces.JWTVerifier, com.sun.net.httpserver.HttpHandler, FunctionalInterface, java.sql.Statement, java.util.regex.Pattern (+15 more)

### Community 8 - "AuthenticatedUserContext"
Cohesion: 0.07
Nodes (13): com.fasterxml.jackson.databind.ObjectMapper, java.net.http.HttpClient, java.net.http.HttpResponse, java.net.URI, IncidenteDTO, NivelGravedad, ALTO, BAJO (+5 more)

### Community 9 - "IncidentesPanel"
Cohesion: 0.27
Nodes (7): JTextArea, IncidentesPanel, DefaultTableModel, JButton, JComboBox, JTable, JTextField

### Community 10 - "Permiso"
Cohesion: 0.10
Nodes (3): Override, Permiso, Rol

### Community 11 - "GestionarVisitaUseCase"
Cohesion: 0.15
Nodes (3): GestionarVisitaUseCase, AuditService, SalidaOlvidadaTest

### Community 12 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.28
Nodes (5): org.junit.jupiter.api.BeforeEach, org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test, VisitaFactoryTest, PermissionCheckerTest

### Community 13 - "BitacoraAuditoria"
Cohesion: 0.09
Nodes (6): com.acme.sica.application.port.out.AuditRepository, com.acme.sica.application.port.out.IncidenteRepository, com.acme.sica.application.port.out.VisitaRepository, GenerarReporteUseCase, BitacoraAuditoria, ReportesHttpHandler

### Community 14 - "com.acme.sica.application.port.out.AuditService"
Cohesion: 0.16
Nodes (4): com.acme.sica.application.port.out.AuditService, com.acme.sica.application.port.out.EmpresaRepository, GestionarEmpresaUseCase, Empresa

### Community 15 - "SICA - Sistema Integrado de Control de Acceso (Zona Acme)"
Cohesion: 0.14
Nodes (13): 1. Justificación de Arquitectura (Sin Framework), 2. Estructura del Proyecto (Arquitectura Hexagonal Explícita), 3. Modelo de Datos Relacional (E-R), 4. Credenciales de Prueba por Rol, 5. Instrucciones de Compilación y Ejecución, 6. Endpoints Principales (API REST), Credenciales de Prueba Preconfiguradas:, 🚀 Instrucciones de Instalación y Ejecución (+5 more)

### Community 16 - ".createVisita"
Cohesion: 0.22
Nodes (5): VisitaFactory, TipoVisita, NO_ANUNCIADA, PASE_TEMPORAL, PRE_REGISTRADA

### Community 18 - "GestionarVisitaUseCase.java"
Cohesion: 0.21
Nodes (6): AccessValidationStrategy, Override, PreRegisteredValidationStrategy, RestrictedPersonValidationStrategy, Override, UnannouncedValidationStrategy

### Community 19 - "SalidaOlvidadaTest.java"
Cohesion: 0.16
Nodes (10): EstadoVisita, APROBADO, DENTRO, FINALIZADO, PENDIENTE_APROBACION, PENDIENTE_APROBACION_OLVIDO, RECHAZADO, TipoCierreVisita (+2 more)

### Community 20 - ".registrarNoAnunciada"
Cohesion: 0.20
Nodes (3): PaseTemporalDTO, PreregistroVisitaDTO, VisitaNoAnunciadaDTO

### Community 26 - "Pure Java Backend Architecture & Best Practices"
Cohesion: 0.18
Nodes (10): 1. Filosofía y Principios Fundamentales, 2. Arquitectura de Capas y Estructura de Paquetes, 3. Inyección de Dependencias Manual (Composition Root), 4. Patrón DAO y Acceso a Datos con JDBC Puro, 5. Manejo de Excepciones y Resiliencia, 6. Concurrencia y Gestión de Memoria, 7. Integración con Graphify, Implementación Ejemplo: (+2 more)

### Community 27 - "Reglas de Desarrollo: Pure Java Backend"
Cohesion: 0.33
Nodes (5): 1. Arquitectura y Separación de Responsabilidades, 2. Gestión de Recursos y Rendimiento, 3. Manejo de Excepciones, 4. Uso de Graphify para Gestión de Recursos y Arquitectura, Reglas de Desarrollo: Pure Java Backend

## Knowledge Gaps
- **42 isolated node(s):** `com.acme:sica`, `HABILITADO`, `RESTRINGIDO`, `APROBADO`, `PENDIENTE_APROBACION` (+37 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Visita` connect `Visita` to `Persona`, `GuardiaPanel`, `SicaApiClient`, `AuthenticatedUserContext`, `GestionarVisitaUseCase`, `org.junit.jupiter.api.DisplayName`, `BitacoraAuditoria`, `.createVisita`, `GestionarVisitaUseCase.java`, `SalidaOlvidadaTest.java`, `.registrarNoAnunciada`, `.testCheckIn_SalidaOlvidadaAutoClose`?**
  _High betweenness centrality (0.190) - this node is a cross-community bridge._
- **Why does `Persona` connect `Persona` to `GuardiaPanel`, `SicaApiClient`, `AuthenticatedUserContext`, `IncidentesPanel`, `GestionarVisitaUseCase.java`, `SalidaOlvidadaTest.java`, `.testCheckIn_SalidaOlvidadaAutoClose`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Why does `AuthenticatedUserContext` connect `AuthenticatedUserContext` to `Persona`, `Usuario`, `SicaApplication.java`, `GestionarVisitaUseCase`, `com.acme.sica.application.port.out.AuditService`, `GestionarVisitaUseCase.java`, `SalidaOlvidadaTest.java`, `.registrarNoAnunciada`, `.testCheckIn_SalidaOlvidadaAutoClose`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `AuthenticatedUserContext` (e.g. with `.intercept()` and `.testCheckIn_SalidaOlvidadaAutoClose()`) actually correct?**
  _`AuthenticatedUserContext` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `com.acme:sica`, `HABILITADO`, `RESTRINGIDO` to the rest of the system?**
  _42 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Persona` be split into smaller, more focused modules?**
  _Cohesion score 0.07624113475177305 - nodes in this community are weakly interconnected._
- **Should `Usuario` be split into smaller, more focused modules?**
  _Cohesion score 0.08695652173913043 - nodes in this community are weakly interconnected._