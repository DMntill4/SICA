# SICA - Sistema Integrado de Control de Acceso (Zona Acme)

**Backend Java ejecutable (API REST autocontenida sin framework de aplicación)**

SICA es un sistema de backend diseñado para automatizar y asegurar el control de ingresos, salidas, incidentes y visitas en el Complejo Empresarial "Zona Acme" (con más de 30 empresas).

---

## 1. Justificación de Arquitectura (Sin Framework)

El desarrollo del backend fue construido utilizando **Java Estándar puro** (`com.sun.net.httpserver.HttpServer` nativo del JDK) y persistencia mediante **JDBC puro** sobre una base de datos **H2 en modo archivo** (`./data/sicadb`).

### ¿Por qué no se usó Spring Boot / JPA?
- **Control Total y Transparencia**: En lugar de depender de "magia" o configuración automática de Spring (`@RestController`, `@Autowired`, `@Entity`), la aplicación construye a mano el servidor HTTP, el enrutador de peticiones, el manejo de hilos concurrentes, la inyección de dependencias por constructor y las consultas SQL mediante `PreparedStatement` y `ResultSet`.
- **Diferencia entre Framework y Librerías Puntuales**:
  - Un **framework** (Spring/Quarkus) impone la arquitectura global de la aplicación.
  - Las **librerías puntuales** resuelven tareas matemáticas o algorítmicas del estándar de la industria que no deben reimplementarse a mano por seguridad (Jackson para parseo JSON, `jBCrypt` para hashing de contraseñas de un solo sentido, y Auth0 `java-jwt` para la firma y verificación de tokens).

---

## 2. Estructura del Proyecto (Arquitectura Hexagonal Explícita)

```
com.acme.sica
├── domain/                      (CAPA DE DOMINIO PURA)
│   ├── model/                   (Usuario, Persona, Visita, Incidente, Bitacora, etc.)
│   ├── enums/                   (EstadoAcceso, EstadoVisita, TipoVisita, etc.)
│   └── port/                    (Interfaces / Puertos: UsuarioRepository, VisitaRepository, etc.)
│
├── usecase/                     (CAPA DE APLICACIÓN / CASOS DE USO)
│   ├── auth/                    (AuthUseCase: login, logout, control de intentos)
│   ├── personas/                (GestionarPersonaUseCase)
│   ├── usuarios/                (GestionarUsuarioUseCase)
│   ├── incidentes/              (RegistrarIncidenteUseCase)
│   ├── visitas/                 (GestionarVisitaUseCase, VisitaFactory, Strategies)
│   └── reportes/                (GenerarReporteUseCase)
│
└── infrastructure/              (CAPA DE INFRAESTRUCTURA Y ADAPTADORES)
    ├── adapter/
    │   ├── in/http/             (ADAPTADORES DE ENTRADA HTTP / HANDLERS)
    │   ├── in/dto/              (DTOs de Petición y Respuesta)
    │   └── out/jdbc/            (ADAPTADORES DE SALIDA PERSISTENCIA JDBC)
    ├── db/                      (H2ConnectionFactory, SchemaInitializer)
    ├── http/                    (Router, Route, HttpUtils)
    └── security/                (JwtUtil, PasswordHasher, AuthMiddleware, PermissionChecker)
```

### Patrones de Diseño Aplicados
1. **Abstract Factory Pattern (`connection/`)**:
   - `ConnectionFactory`: Interfaz del producto de conexiones JDBC.
   - `MySqlConnectionFactory`: Fábrica concreta de conexiones MySQL (`jdbc:mysql://localhost:3306/sicadb`).
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

Orden de creación relacional en `schema.sql`:
1. `rol` $\leftrightarrow$ `permiso` $\rightarrow$ `rol_permiso` (Tabla intermedia RBAC).
2. `empresa` $\rightarrow$ `usuario` (Conexión por `empresa_id`).
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
| `admin` | `admin123` | **ADMIN** | Todos los permisos (1 a 12), CRUD completo y auditoría | N/A |
| `guardia1` | `guardia123` | **GUARDIA** | `crear_persona`, `checkin_visita`, `checkout_visita`, `registrar_incidente`, `generar_reporte` | Recepción |
| `func1` | `func123` | **FUNCIONARIO** | `preregistrar_visita`, `aprobar_visita`, `generar_reporte` | Acme Corporation |

---

## 5. Instrucciones de Compilación y Ejecución

### Requisitos
- JDK 21 o superior.
- No requiere tener instalado Maven ni PostgreSQL/MySQL (la BD H2 y Maven Wrapper están autocontenidos).

---

## 🧪 Matriz de Pruebas de QA (50 Casos de Prueba)

El proyecto cuenta con una **Matriz de Pruebas de Control de Calidad** exhaustiva de **50 Casos de Prueba (CP-01 al CP-50)** estructurados en 9 módulos de prueba:

1. **Módulo 1: Autenticación, JWT, Logout y Sesiones (CP-01 al CP-10)**
2. **Módulo 2: Flujo 1 — Invitado Pre-registrado (CP-11 al CP-16)**
3. **Módulo 3: Flujo 2 — Invitado No Anunciado y Aprobaciones en Tiempo Real (CP-17 al CP-22)**
4. **Módulo 4: Flujo 3 — Carnet Olvidado / Pase Temporal (CP-23 al CP-27)**
5. **Módulo 5: Flujo 4 — Salida Olvidada y Regularización Automática (CP-28 al CP-32)**
6. **Módulo 6: Gestión de Incidentes y Restricciones de Acceso (CP-33 al CP-38)**
7. **Módulo 7: Control de Acceso Basado en Roles (RBAC Granular) (CP-39 al CP-43)**
8. **Módulo 8: Bitácora Inmutable de Auditoría y Reportes (CP-44 al CP-47)**
9. **Módulo 9: Conexión BD, Abstract Factory & Concurrencia (CP-48 al CP-50)**

---

## 🚀 Instrucciones de Instalación y Ejecución

```bash
# 1. Compilar y empaquetar el proyecto
./mvnw clean package

# 2. Ejecutar la aplicación (Backend HTTP + GUI Swing FlatLaf)
java -jar target/sica.jar
```

### Credenciales de Prueba Preconfiguradas:
- 👑 **ADMIN**: `admin` / `admin123`
- 🛡️ **GUARDIA**: `guardia1` / `guardia123`
- 👔 **FUNCIONARIO**: `func1` / `func123`

=================================================
 Servidor SICA iniciado exitosamente en puerto 8080
 Endpoint base: http://localhost:8080
=================================================

---

## 6. Endpoints Principales (API REST)

- `POST /auth/login` - Autenticación y obtención de JWT.
- `POST /auth/logout` - Cierre de sesión y revocación del token.
- `POST /visitas/preregistrar` - Pre-registro por funcionario.
- `POST /visitas/no-anunciada` - Registro de visitante inesperado por guardia.
- `POST /visitas/pase-temporal` - Ingreso puntual por carnet olvidado.
- `PUT /visitas/{id}/aprobar` - Aprobación por funcionario.
- `POST /visitas/{id}/check-in` - Ingreso con regularización automática de salida olvidada (`CERRADA_POR_SISTEMA`).
- `POST /visitas/{id}/check-out` - Salida normal.
- `POST /incidentes` - Registro de incidente (restringe inmediatamente a la persona).
- `GET /reportes/personas-dentro` - Listado de personas actualmente en el complejo.
- `GET /reportes/auditoria` - Consulta de bitácora de auditoría inmutable.
