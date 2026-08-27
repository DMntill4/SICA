# Graph Report - SICA  (2026-08-26)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 654 nodes · 1654 edges · 26 communities (19 shown, 7 thin omitted)
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 271 edges (avg confidence: 0.8)
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
- ConnectionFactory
- Visita
- .main
- Incidente
- AuthenticatedUserContext
- Permiso
- org.junit.jupiter.api.DisplayName
- .log
- Empresa
- GenerarReporteUseCase
- .createVisita
- PuntoAcceso
- NivelGravedad
- EstadoVisita
- TipoCierreVisita
- mvnw
- com.acme:sica

## God Nodes (most connected - your core abstractions)
1. `Visita` - 99 edges
2. `Persona` - 61 edges
3. `SicaApiClient` - 47 edges
4. `Usuario` - 41 edges
5. `Incidente` - 38 edges
6. `AuthenticatedUserContext` - 31 edges
7. `GuardiaPanel` - 26 edges
8. `AuditService` - 24 edges
9. `UsuarioRepository` - 24 edges
10. `BitacoraAuditoria` - 24 edges

## Surprising Connections (you probably didn't know these)
- `FuncionarioPanel` --references--> `Persona`  [EXTRACTED]
  src/main/java/com/acme/sica/gui/views/FuncionarioPanel.java → src/main/java/com/acme/sica/domain/model/Persona.java
- `GuardiaPanel` --references--> `Persona`  [EXTRACTED]
  src/main/java/com/acme/sica/gui/views/GuardiaPanel.java → src/main/java/com/acme/sica/domain/model/Persona.java
- `IncidentesPanel` --references--> `Persona`  [EXTRACTED]
  src/main/java/com/acme/sica/gui/views/IncidentesPanel.java → src/main/java/com/acme/sica/domain/model/Persona.java
- `GenerarReporteUseCase` --references--> `VisitaRepository`  [EXTRACTED]
  src/main/java/com/acme/sica/usecase/reportes/GenerarReporteUseCase.java → src/main/java/com/acme/sica/domain/port/VisitaRepository.java
- `AuditService` --references--> `AuditRepository`  [EXTRACTED]
  src/main/java/com/acme/sica/infrastructure/audit/AuditService.java → src/main/java/com/acme/sica/domain/port/AuditRepository.java

## Import Cycles
- None detected.

## Communities (26 total, 7 thin omitted)

### Community 0 - "Persona"
Cohesion: 0.06
Nodes (19): org.junit.jupiter.api.BeforeEach, EstadoAcceso, HABILITADO, RESTRINGIDO, Persona, PersonaRepository, VisitaRepository, CheckInDTO (+11 more)

### Community 1 - "Usuario"
Cohesion: 0.06
Nodes (13): com.auth0.jwt.algorithms.Algorithm, com.auth0.jwt.interfaces.DecodedJWT, com.auth0.jwt.interfaces.JWTVerifier, Usuario, UsuarioRepository, LoginRequestDTO, UsuarioDTO, AuthMiddleware (+5 more)

### Community 2 - "GuardiaPanel"
Cohesion: 0.07
Nodes (26): javax.swing.table.DefaultTableModel, JPanel, JTextArea, AuditoriaPanel, DefaultTableModel, JButton, JLabel, JTable (+18 more)

### Community 3 - "SicaApiClient"
Cohesion: 0.07
Nodes (12): Builder, JFrame, JPasswordField, SessionContext, SicaApiClient, JButton, JLabel, JTextField (+4 more)

### Community 4 - "com.sun.net.httpserver.HttpExchange"
Cohesion: 0.12
Nodes (8): com.sun.net.httpserver.HttpExchange, AuthHttpHandler, IncidenteHttpHandler, PersonaHttpHandler, ReportesHttpHandler, UsuarioHttpHandler, VisitaHttpHandler, Override

### Community 5 - "ConnectionFactory"
Cohesion: 0.09
Nodes (14): java.sql.Connection, java.sql.Statement, DatabaseConfig, ConnectionFactory, DatabaseEngine, H2, MYSQL, DatabaseFactoryProvider (+6 more)

### Community 7 - ".main"
Cohesion: 0.12
Nodes (7): com.sun.net.httpserver.HttpHandler, FunctionalInterface, java.util.regex.Pattern, SicaGuiMain, Route, RouteHandler, Router

### Community 9 - "AuthenticatedUserContext"
Cohesion: 0.16
Nodes (9): com.fasterxml.jackson.databind.ObjectMapper, java.net.http.HttpClient, java.net.http.HttpResponse, java.net.URI, PaseTemporalDTO, PreregistroVisitaDTO, VisitaNoAnunciadaDTO, HttpUtils (+1 more)

### Community 10 - "Permiso"
Cohesion: 0.10
Nodes (3): Override, Permiso, Rol

### Community 12 - "org.junit.jupiter.api.DisplayName"
Cohesion: 0.22
Nodes (6): org.junit.jupiter.api.DisplayName, org.junit.jupiter.api.Test, Override, Override, PermissionCheckerTest, VisitaFactoryTest

### Community 15 - "GenerarReporteUseCase"
Cohesion: 0.22
Nodes (3): AuditRepository, IncidenteRepository, GenerarReporteUseCase

### Community 16 - ".createVisita"
Cohesion: 0.23
Nodes (5): TipoVisita, NO_ANUNCIADA, PASE_TEMPORAL, PRE_REGISTRADA, VisitaFactory

### Community 18 - "NivelGravedad"
Cohesion: 0.22
Nodes (6): NivelGravedad, ALTO, BAJO, CRITICO, MEDIO, IncidenteDTO

### Community 19 - "EstadoVisita"
Cohesion: 0.25
Nodes (7): EstadoVisita, APROBADO, DENTRO, FINALIZADO, PENDIENTE_APROBACION, PENDIENTE_APROBACION_OLVIDO, RECHAZADO

### Community 21 - "TipoCierreVisita"
Cohesion: 0.33
Nodes (3): TipoCierreVisita, CERRADA_POR_SISTEMA, NORMAL

## Knowledge Gaps
- **20 isolated node(s):** `HABILITADO`, `RESTRINGIDO`, `NO_ANUNCIADA`, `PASE_TEMPORAL`, `PRE_REGISTRADA` (+15 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Visita` connect `Visita` to `Persona`, `GuardiaPanel`, `SicaApiClient`, `AuthenticatedUserContext`, `.checkIn`, `org.junit.jupiter.api.DisplayName`, `GenerarReporteUseCase`, `.createVisita`, `EstadoVisita`, `.preregistrarVisita`, `TipoCierreVisita`?**
  _High betweenness centrality (0.216) - this node is a cross-community bridge._
- **Why does `Persona` connect `Persona` to `GuardiaPanel`, `SicaApiClient`, `Incidente`, `AuthenticatedUserContext`, `.checkIn`, `org.junit.jupiter.api.DisplayName`?**
  _High betweenness centrality (0.131) - this node is a cross-community bridge._
- **Why does `SicaApiClient` connect `SicaApiClient` to `AuthenticatedUserContext`, `GuardiaPanel`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **What connects `HABILITADO`, `RESTRINGIDO`, `NO_ANUNCIADA` to the rest of the system?**
  _20 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Persona` be split into smaller, more focused modules?**
  _Cohesion score 0.058544303797468354 - nodes in this community are weakly interconnected._
- **Should `Usuario` be split into smaller, more focused modules?**
  _Cohesion score 0.06054054054054054 - nodes in this community are weakly interconnected._
- **Should `GuardiaPanel` be split into smaller, more focused modules?**
  _Cohesion score 0.0673076923076923 - nodes in this community are weakly interconnected._