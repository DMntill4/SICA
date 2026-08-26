# Script PowerShell para generar el historial completo de Git Flow en SICA

# Configurar usuario local de git si no existe
git config user.name "DMntill4"
git config user.email "dmntill4@example.com"

# 1. RAMA MAIN - Setup Inicial
git add openapi.yaml
git commit -m "docs(spec): add OpenAPI 3.0 specification for SICA API"

git add pom.xml
git commit -m "feat(project): initialize Maven POM configuration with Java 21 and dependencies"

git add .env .gitignore
git commit -m "config(env): add default environment properties and application settings"

git add mvnw mvnw.cmd .mvn
git commit -m "build(wrapper): add Maven Wrapper scripts for cross-platform build"

git add README.md
git commit -m "docs(readme): add technical documentation and system architecture overview"

# 2. RAMA DEVELOP
git checkout -b develop

# 3. RAMA FEATURE/DOMAIN-CORE
git checkout -b feature/domain-core
git add src/main/java/com/acme/sica/domain/model/TipoDocumento.java src/main/java/com/acme/sica/domain/model/EstadoAcceso.java
git commit -m "feat(domain): define value objects for DocumentType and AccessState enums"

git add src/main/java/com/acme/sica/domain/model/Persona.java
git commit -m "feat(domain): implement Persona entity with validation and domain logic"

git add src/main/java/com/acme/sica/domain/model/Usuario.java src/main/java/com/acme/sica/domain/model/Rol.java
git commit -m "feat(domain): implement Usuario entity and Role enum"

git add src/main/java/com/acme/sica/domain/model/Visita.java src/main/java/com/acme/sica/domain/model/EstadoVisita.java src/main/java/com/acme/sica/domain/model/TipoVisita.java
git commit -m "feat(domain): implement Visita entity and VisitStatus state machine"

git add src/main/java/com/acme/sica/domain/model/Incidente.java src/main/java/com/acme/sica/domain/model/NivelGravedad.java
git commit -m "feat(domain): implement Incidente entity and NivelGravedad enum"

git add src/main/java/com/acme/sica/domain/model/PuntoDeAcceso.java src/main/java/com/acme/sica/domain/model/Empresa.java
git commit -m "feat(domain): implement PuntoDeAcceso and Empresa domain entities"

git add src/main/java/com/acme/sica/domain/port/PersonaRepository.java src/main/java/com/acme/sica/domain/port/UsuarioRepository.java
git commit -m "feat(domain): define PersonaRepository and UsuarioRepository port interfaces"

git add src/main/java/com/acme/sica/domain/port/VisitaRepository.java src/main/java/com/acme/sica/domain/port/IncidenteRepository.java
git commit -m "feat(domain): define VisitaRepository and IncidenteRepository port interfaces"

git add src/main/java/com/acme/sica/domain/port/AuditService.java
git commit -m "feat(domain): define AuditService port interface"

# Merge domain-core into develop
git checkout develop
git merge --no-ff feature/domain-core -m "merge(develop): merge feature/domain-core into develop"

# 4. RAMA FEATURE/INFRASTRUCTURE-DB-JDBC
git checkout -b feature/infrastructure-db-jdbc
git add src/main/resources/schema.sql
git commit -m "feat(db): create database schema DDL script for MySQL and H2"

git add src/main/resources/data.sql
git commit -m "feat(db): add initial SQL seed data with default roles, permissions and accounts"

git add src/main/java/com/acme/sica/infrastructure/db/DatabaseConnectionFactory.java
git commit -m "feat(db): implement DatabaseConnectionFactory with connection pooling"

git add src/main/java/com/acme/sica/infrastructure/adapter/out/jdbc/PersonaJdbcAdapter.java
git commit -m "feat(adapter-jdbc): implement PersonaJdbcAdapter for persistence"

git add src/main/java/com/acme/sica/infrastructure/adapter/out/jdbc/UsuarioJdbcAdapter.java
git commit -m "feat(adapter-jdbc): implement UsuarioJdbcAdapter for persistence"

git add src/main/java/com/acme/sica/infrastructure/adapter/out/jdbc/VisitaJdbcAdapter.java
git commit -m "feat(adapter-jdbc): implement VisitaJdbcAdapter with complex query mapping"

git add src/main/java/com/acme/sica/infrastructure/adapter/out/jdbc/IncidenteJdbcAdapter.java
git commit -m "feat(adapter-jdbc): implement IncidenteJdbcAdapter for safety incidents"

git add src/main/java/com/acme/sica/infrastructure/adapter/out/jdbc/BitacoraAuditoriaJdbcAdapter.java
git commit -m "feat(adapter-jdbc): implement BitacoraAuditoriaJdbcAdapter for security logging"

# Merge infrastructure-db-jdbc into develop
git checkout develop
git merge --no-ff feature/infrastructure-db-jdbc -m "merge(develop): merge feature/infrastructure-db-jdbc into develop"

# 5. RAMA FIX/DB-SCHEMA-COLUMN-MIGRATIONS
git checkout -b fix/db-schema-column-migrations
git add src/main/java/com/acme/sica/infrastructure/db/SchemaInitializer.java
git commit -m "fix(db): implement SchemaInitializer automatic ALTER TABLE migrations"

git add src/main/resources/config.properties
git commit -m "fix(config): add database configuration properties loader"

# Merge fix into develop
git checkout develop
git merge --no-ff fix/db-schema-column-migrations -m "merge(develop): merge fix/db-schema-column-migrations into develop"

# 6. RAMA FEATURE/SECURITY-AND-USECASES
git checkout -b feature/security-and-usecases
git add src/main/java/com/acme/sica/shared/security/BCryptPasswordEncoder.java
git commit -m "feat(security): implement BCryptPasswordEncoder for secure password hashing"

git add src/main/java/com/acme/sica/shared/security/JwtTokenManager.java
git commit -m "feat(security): implement JwtTokenManager for stateless HTTP authentication"

git add src/main/java/com/acme/sica/shared/security/PermissionChecker.java src/main/java/com/acme/sica/shared/security/AuthenticatedUserContext.java
git commit -m "feat(security): implement PermissionChecker RBAC authorization logic"

git add src/main/java/com/acme/sica/usecase/auth/AutenticarUsuarioUseCase.java
git commit -m "feat(usecase): implement AutenticarUsuarioUseCase"

git add src/main/java/com/acme/sica/usecase/personas/GestionarPersonaUseCase.java
git commit -m "feat(usecase): implement GestionarPersonaUseCase"

git add src/main/java/com/acme/sica/usecase/usuarios/GestionarUsuarioUseCase.java
git commit -m "feat(usecase): implement GestionarUsuarioUseCase"

git add src/main/java/com/acme/sica/usecase/visitas/GestionarVisitaUseCase.java
git commit -m "feat(usecase): implement GestionarVisitaUseCase with state transitions"

git add src/main/java/com/acme/sica/usecase/incidentes/RegistrarIncidenteUseCase.java
git commit -m "feat(usecase): implement RegistrarIncidenteUseCase with automatic access block"

git add src/main/java/com/acme/sica/usecase/reportes/GenerarReportesUseCase.java
git commit -m "feat(usecase): implement GenerarReportesUseCase"

# Merge security-and-usecases into develop
git checkout develop
git merge --no-ff feature/security-and-usecases -m "merge(develop): merge feature/security-and-usecases into develop"

# 7. RAMA FEATURE/HTTP-API-SERVER
git checkout -b feature/http-api-server
git add src/main/java/com/acme/sica/infrastructure/http/Router.java
git commit -m "feat(http): create lightweight concurrent Router for JDK HttpServer"

git add src/main/java/com/acme/sica/infrastructure/http/HttpUtils.java
git commit -m "feat(http): implement HttpUtils for JSON serialization and HTTP response handling"

git add src/main/java/com/acme/sica/infrastructure/adapter/in/http/AuthHttpHandler.java
git commit -m "feat(http): implement AuthHttpHandler for login and logout endpoints"

git add src/main/java/com/acme/sica/infrastructure/adapter/in/http/PersonaHttpHandler.java
git commit -m "feat(http): implement PersonaHttpHandler for persona endpoints"

git add src/main/java/com/acme/sica/infrastructure/adapter/in/http/UsuarioHttpHandler.java
git commit -m "feat(http): implement UsuarioHttpHandler for user management endpoints"

git add src/main/java/com/acme/sica/infrastructure/adapter/in/http/VisitaHttpHandler.java
git commit -m "feat(http): implement VisitaHttpHandler for visit workflow endpoints"

git add src/main/java/com/acme/sica/infrastructure/adapter/in/http/IncidenteHttpHandler.java
git commit -m "feat(http): implement IncidenteHttpHandler for security incident endpoints"

git add src/main/java/com/acme/sica/infrastructure/adapter/in/http/ReportesHttpHandler.java
git commit -m "feat(http): implement ReportesHttpHandler for audit and reporting endpoints"

# Merge http-api-server into develop
git checkout develop
git merge --no-ff feature/http-api-server -m "merge(develop): merge feature/http-api-server into develop"

# 8. RAMA FEATURE/SWING-GUI-FLATLAF
git checkout -b feature/swing-gui-flatlaf
git add src/main/java/com/acme/sica/gui/client/SicaApiClient.java
git commit -m "feat(gui): initialize SicaApiClient for REST API communication"

git add src/main/java/com/acme/sica/gui/MainFrame.java
git commit -m "feat(gui): implement MainFrame container with FlatLaf Dark theme"

git add src/main/java/com/acme/sica/gui/LoginDialog.java
git commit -m "feat(gui): implement LoginDialog and session context controller"

git add src/main/java/com/acme/sica/gui/views/AuditoriaPanel.java
git commit -m "feat(gui): implement AuditoriaPanel for system user audit log display"

git add src/main/java/com/acme/sica/SicaApplication.java
git commit -m "feat(app): create SicaApplication unified launcher class"

# Merge swing-gui-flatlaf into develop
git checkout develop
git merge --no-ff feature/swing-gui-flatlaf -m "merge(develop): merge feature/swing-gui-flatlaf into develop"

# 9. RAMA REFACTOR/GUI-LAYOUT-AND-USABILITY
git checkout -b refactor/gui-layout-and-usability
git add src/main/java/com/acme/sica/gui/views/GuardiaPanel.java
git commit -m "refactor(gui): reorganize GuardiaPanel into 3 independent rows to prevent button clipping"

git add src/main/java/com/acme/sica/gui/views/FuncionarioPanel.java
git commit -m "refactor(gui): adjust FuncionarioPanel pre-registration grid to eliminate UI overflow"

git add src/main/java/com/acme/sica/gui/views/IncidentesPanel.java
git commit -m "feat(security): add access rehabilitation feature to lift security restrictions"

# Merge refactor into develop
git checkout develop
git merge --no-ff refactor/gui-layout-and-usability -m "merge(develop): merge refactor/gui-layout-and-usability into develop"

# 10. RAMA TEST/UNIT-AND-PERMISSION-CHECKS
git checkout -b test/unit-and-permission-checks
git add src/test/java/com/acme/sica/shared/security/PermissionCheckerTest.java
git commit -m "test(unit): add PermissionCheckerTest unit tests"

git add src/test/java/com/acme/sica/visitas/factory/VisitaFactoryTest.java
git commit -m "test(unit): add VisitaFactoryTest unit tests"

git add src/test/java/com/acme/sica/visitas/service/SalidaOlvidadaTest.java
git commit -m "test(unit): add SalidaOlvidadaTest unit tests"

# Merge test into develop
git checkout develop
git merge --no-ff test/unit-and-permission-checks -m "merge(develop): merge test/unit-and-permission-checks into develop"

# 11. RAMA RELEASE/V1.0.0
git checkout -b release/v1.0.0
git add src/
git commit -m "chore(release): add remaining domain DTOs and factories for v1.0.0 release"

# Merge release into main and develop
git checkout main
git merge --no-ff release/v1.0.0 -m "merge(main): release v1.0.0"

git checkout develop
git merge --no-ff release/v1.0.0 -m "merge(develop): merge release v1.0.0 into develop"
