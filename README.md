# SICA - Sistema Integrado de Control de Acceso (Zona Acme)

**Backend Java ejecutable (API REST autocontenida sin framework de aplicación)**

SICA es un sistema de backend diseñado para automatizar y asegurar el control de ingresos, salidas, incidentes y visitas en el Complejo Empresarial "Zona Acme" (con más de 30 empresas).

---

## 1. Justificación de Arquitectura (Sin Framework)

El desarrollo del backend fue construido utilizando **Java Estándar puro** (`com.sun.net.httpserver.HttpServer` nativo del JDK) y persistencia mediante **JDBC puro** sobre una base de datos **MySQL 8.3** (vía Docker) con fallback a **H2 en modo archivo**.

### ¿Por qué no se usó Spring Boot / JPA?
- **Control Total y Transparencia**: En lugar de depender de "magia" o configuración automática de Spring (`@RestController`, `@Autowired`, `@Entity`), la aplicación construye a mano el servidor HTTP, el enrutador de peticiones, el manejo de hilos concurrentes, la inyección de dependencias por constructor y las consultas SQL mediante `PreparedStatement` y `ResultSet`.
- **Diferencia entre Framework y Librerías Puntuales**:
  - Un **framework** (Spring/Quarkus) impone la arquitectura global de la aplicación.
  - Las **librerías puntuales** resuelven tareas matemáticas o algorítmicas del estándar de la industria que no deben reimplementarse a mano por seguridad (Jackson para parseo JSON, `jBCrypt` para hashing de contraseñas de un solo sentido, y Auth0 `java-jwt` para la firma y verificación de tokens).

---

## 2. Estructura del Proyecto (Arquitectura Hexagonal Explícita)

```
com.acme.sica
├── domain/                              (CAPA DE DOMINIO PURA - Sin dependencias externas)
│   ├── model/                           (Entidades: Usuario, Persona, Visita, Empresa, Incidente, Bitacora, etc.)
│   └── enums/                           (EstadoAcceso, EstadoVisita, TipoVisita, NivelGravedad, etc.)
│
├── application/                         (CAPA DE APLICACIÓN / CASOS DE USO)
│   ├── AuthenticatedUserContext.java    (Record del contexto de usuario autenticado)
│   ├── dto/                             (DTOs de Petición y Respuesta)
│   ├── port/
│   │   └── out/                         (Puertos de Salida: Interfaces de Repositorios y AuditService)
│   └── usecase/
│       ├── auth/                        (AuthUseCase: login, logout, control de intentos)
│       ├── empresas/                    (GestionarEmpresaUseCase)
│       ├── personas/                    (GestionarPersonaUseCase)
│       ├── usuarios/                    (GestionarUsuarioUseCase)
│       ├── incidentes/                  (RegistrarIncidenteUseCase)
│       ├── visitas/                     (GestionarVisitaUseCase, VisitaFactory, Strategies)
│       └── reportes/                    (GenerarReporteUseCase)
│
└── infrastructure/                      (CAPA DE INFRAESTRUCTURA Y ADAPTADORES)
    ├── adapter/
    │   ├── in/
    │   │   ├── http/                    (ADAPTADORES DE ENTRADA HTTP)
    │   │   │   ├── handlers/            (AuthHttpHandler, VisitaHttpHandler, EmpresaHttpHandler, etc.)
    │   │   │   └── router/              (Router, Route, HttpUtils, RouteHandler)
    │   │   └── gui/                     (ADAPTADOR DE ENTRADA SWING)
    │   │       ├── client/              (SicaApiClient - Cliente HTTP que consume la API REST)
    │   │       └── views/               (Paneles Swing: GuardiaPanel, FuncionarioPanel, etc.)
    │   └── out/
    │       └── persistence/jdbc/        (ADAPTADORES DE SALIDA - Implementaciones JDBC de los Repositorios)
    ├── config/                          (DatabaseConfig)
    ├── db/                              (SchemaInitializer, ConnectionFactory, DatabaseFactoryProvider)
    └── security/                        (JwtUtil, PasswordHasher, AuthMiddleware, PermissionChecker)
```

### Patrones de Diseño Aplicados
1. **Abstract Factory Pattern (`db/connection/`)**:
   - `ConnectionFactory`: Interfaz del producto de conexiones JDBC.
   - `MySqlConnectionFactory`: Fábrica concreta de conexiones MySQL (`jdbc:mysql://localhost:3306/sica`).
   - `H2ConnectionFactory`: Fábrica concreta de conexiones H2 para pruebas/fallback.
   - `DatabaseFactoryProvider`: Proveedor que selecciona y retorna la fábrica de conexiones activa según las propiedades del sistema (`db.engine=MYSQL`).
2. **Factory Pattern (`VisitaFactory`)**:
   - Centraliza la instanciación de visitas asignando su estado y tipo según el flujo (`PRE_REGISTRADA`, `NO_ANUNCIADA`, `PASE_TEMPORAL`).
3. **Strategy Pattern (`AccessValidationStrategy` & `PermissionChecker`)**:
   - `RestrictedPersonValidationStrategy`: Bloquea inmediatamente el acceso a personas con `estadoAcceso == RESTRINGIDO`.
   - `PreRegisteredValidationStrategy` & `UnannouncedValidationStrategy`: Validan ventanas horarias y aprobaciones de funcionarios.
   - `PermissionChecker`: Evalúa dinámicamente si el rol del usuario posee el permiso requerido en `rol_permiso`.

---

## 3. Modelo de Datos Relacional (E-R)

```mermaid
erDiagram
    ROL ||--o{ ROL_PERMISO : tiene
    PERMISO ||--o{ ROL_PERMISO : asignado_a
    ROL ||--o{ USUARIO : asignado_a
    EMPRESA ||--o{ USUARIO : emplea
    EMPRESA ||--o{ PERSONA : pertenece
    USUARIO ||--o{ VISITA : funcionario
    USUARIO ||--o{ VISITA : guardia_ingreso
    USUARIO ||--o{ VISITA : guardia_salida
    PERSONA ||--o{ VISITA : visitante
    PERSONA ||--o{ INCIDENTE : involucrado
    USUARIO ||--o{ INCIDENTE : reportado_por
    PUNTO_ACCESO ||--o{ VISITA : punto_ingreso
    PUNTO_ACCESO ||--o{ VISITA : punto_salida
    VISITA ||--o| CODIGO_QR : genera

    ROL { bigint id PK; varchar nombre UK }
    PERMISO { bigint id PK; varchar nombre UK }
    EMPRESA { bigint id PK; varchar nit UK; varchar nombre; boolean activa }
    USUARIO { bigint id PK; varchar username UK; varchar password_hash; bigint rol_id FK; bigint empresa_id FK; boolean bloqueado }
    PERSONA { bigint id PK; varchar doc_identidad UK; varchar estado_acceso; bigint empresa_id FK }
    VISITA { bigint id PK; bigint persona_id FK; varchar tipo_visita; varchar estado_visita; varchar tipo_cierre }
    INCIDENTE { bigint id PK; bigint persona_id FK; varchar nivel_gravedad }
    PUNTO_ACCESO { bigint id PK; varchar nombre UK }
    BITACORA_AUDITORIA { bigint id PK; varchar accion; varchar username; text detalle }
```

Orden de creación relacional en `schema.sql`:
1. `rol` ↔ `permiso` → `rol_permiso` (Tabla intermedia RBAC).
2. `empresa` → `usuario` (Conexión por `empresa_id`).
3. `token_revocado` (Lista negra para Logout JWT).
4. `persona` (Doc. Identidad, Nombre, Apellido, `estado_acceso`: HABILITADO / RESTRINGIDO).
5. `punto_acceso` (Puntos de control físico).
6. `visita` (Conecta persona, funcionario, guardia, punto de acceso, tipo y estado de visita).
7. `incidente` (Registra incidentes y restringe `persona.estado_acceso`).
8. `bitacora_auditoria` (Bitácora inmutable Append-Only para todas las acciones críticas).

---

## 4. Credenciales de Prueba por Rol

| Usuario | Contraseña | Rol | Permisos Principales | Empresa |
|---|---|---|---|---|
| `admin` | `admin123` | **ADMIN** | Todos los permisos (1 a 16), CRUD completo, auditoría y limpieza de historial | N/A |
| `guardia1` | `guardia123` | **GUARDIA** | `crear_persona`, `checkin_visita`, `checkout_visita`, `registrar_incidente`, `generar_reporte` | Recepción |
| `func1` | `func123` | **FUNCIONARIO** | `preregistrar_visita`, `aprobar_visita`, `generar_reporte` | Acme Corporation |

---

## 5. Instrucciones de Compilación y Ejecución

### Requisitos
- JDK 17 o superior.
- Docker y Docker Compose (para la base de datos MySQL).

### Base de Datos con Docker

```bash
# 1. Levantar MySQL 8.3 en Docker
docker-compose up -d

# Verificar que el contenedor está corriendo
docker ps
```

### Conexión desde DBeaver
| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `3306` |
| Base de datos | `sica` |
| Usuario (root) | `root` / `-3ta9}OK`4[Y` |
| Usuario (app) | `sica_user` / `sica_pass_2026` |
| Driver | MySQL 8 |

> **Nota:** En las propiedades del driver de DBeaver, establece `allowPublicKeyRetrieval=true` y `useSSL=false`.

### Compilar y Ejecutar

```bash
# 1. Compilar y empaquetar el proyecto
./mvnw clean package

# 2. Ejecutar la aplicación (Backend HTTP + GUI Swing FlatLaf)
java -jar target/sica.jar

# 3. Ejecutar solo backend (sin GUI)
java -jar target/sica.jar --headless
```

---

## 6. Endpoints Principales (API REST)

- `POST /auth/login` - Autenticación y obtención de JWT.
- `POST /auth/logout` - Cierre de sesión y revocación del token.
- `GET /empresas` - Listar empresas registradas.
- `POST /empresas` - Registrar nueva empresa (requiere permiso `crear_empresa`).
- `PUT /empresas/{id}` - Actualizar empresa (requiere permiso `modificar_empresa`).
- `DELETE /empresas/{id}` - Eliminar empresa (requiere permiso `eliminar_empresa`).
- `POST /visitas/preregistrar` - Pre-registro por funcionario.
- `POST /visitas/no-anunciada` - Registro de visitante inesperado por guardia.
- `POST /visitas/pase-temporal` - Ingreso puntual por carnet olvidado.
- `PUT /visitas/{id}/aprobar` - Aprobación por funcionario.
- `POST /visitas/{id}/check-in` - Ingreso con regularización automática de salida olvidada (`CERRADA_POR_SISTEMA`).
- `POST /visitas/{id}/check-out` - Salida normal.
- `POST /incidentes` - Registro de incidente (restringe inmediatamente a la persona).
- `GET /reportes/personas-dentro` - Listado de personas actualmente en el complejo.
- `GET /reportes/auditoria` - Consulta de bitácora de auditoría inmutable.
- `DELETE /visitas` - Limpiar historial de visitas (requiere permiso `limpiar_historial`).
