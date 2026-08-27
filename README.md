<div align="center">

# SICA — Sistema Integrado de Control de Acceso
### *Complejo Empresarial "Zona Acme"*

[![Java 21+](https://img.shields.io/badge/JAVA_21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Java Swing](https://img.shields.io/badge/JAVA_SWING-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![Jackson JSON](https://img.shields.io/badge/JACKSON_JSON-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://github.com/FasterXML/jackson)
[![Apache Maven](https://img.shields.io/badge/APACHE_MAVEN-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![MySQL 8.0](https://img.shields.io/badge/MYSQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT Auth](https://img.shields.io/badge/JWT_AUTH-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![FlatLaf UI](https://img.shields.io/badge/FLATLAF_DARK-1E293B?style=for-the-badge&logo=swing&logoColor=white)](https://www.formdev.com/flatlaf/)
[![GitFlow](https://img.shields.io/badge/GITFLOW-F05032?style=for-the-badge&logo=git&logoColor=white)](https://github.com/DMntill4/SICA.git)

<p align="center">
  <b>Sistema de backend de arquitectura hexagonal pura + cliente GUI en Java Swing (FlatLaf Dark) para el control de accesos, visitantes, incidentes y auditoría inmutable en tiempo real.</b>
</p>

</div>

---

## Tabla de Contenidos

- [1. Introducción y Contexto del Problema](#1-introducción-y-contexto-del-problema)
- [2. Características Principales](#2-características-principales)
- [3. Stack Tecnológico](#3-stack-tecnológico)
- [4. Arquitectura Hexagonal y Patrones de Diseño](#4-arquitectura-hexagonal-y-patrones-de-diseño)
- [5. Modelo Entidad-Relación (E-R)](#5-modelo-entidad-relación-e-r)
- [6. Control de Acceso Basado en Roles (RBAC)](#6-control-de-acceso-basado-en-roles-rbac)
- [7. Guía de Instalación y Ejecución](#7-guía-de-instalación-y-ejecución)
- [8. Catálogo de Endpoints REST API](#8-catálogo-de-endpoints-rest-api)
- [9. Pruebas Unitarias y Cobertura QA](#9-pruebas-unitarias-y-cobertura-qa)
- [10. Contribuidores y Autores](#10-contribuidores-y-autores)

---

## 1. Introducción y Contexto del Problema

El Complejo Empresarial **"Zona Acme"** alberga a más de 30 empresas de alto perfil. Sin embargo, su control de acceso manual basado en cuadernos de papel y comunicación por radio generaba cuellos de botella, falta de trazabilidad e imprecisión en emergencias.

**SICA** resuelve estas problemáticas mediante:
- **Automatización de los 4 Flujos de Acceso**: Pre-Registrado, No Anunciado en Tiempo Real, Pase Temporal (Carnet Olvidado) y Regularización Automática de Salida Olvidada.
- **Seguridad Reactiva e Inmediata**: Bloqueo instantáneo en portería al registrar incidentes de gravedad CRÍTICO o ALTO.
- **Auditoría Inmutable**: Registro append-only de todas las operaciones del sistema con IP de origen y contexto de usuario.

---

## 2. Características Principales

| Módulo | Descripción |
|---|---|
| **Autenticación & JWT** | Login con hashing `BCrypt` y tokens JWT de sesión sin estado con revocación en logout (`token_revocado`). |
| **RBAC Granular** | Control de acceso basado en 16 permisos individuales en BD (`crear_persona`, `checkin_visita`, `aprobar_visita`, etc.). |
| **Flujos de Visita** | Pre-registro por funcionarios, solicitudes en tiempo real aprobables desde GUI y regularización de salidas olvidadas (`CERRADA_POR_SISTEMA`). |
| **Gestión de Incidentes** | Bloqueo dinámico de personas a estado `RESTRINGIDO` y botón para **Rehabilitación de Acceso**. |
| **Interfaz Swing FlatLaf** | UI de escritorio moderna, oscura y responsiva estructurada por roles (`GuardiaPanel`, `FuncionarioPanel`, `IncidentesPanel`, `AuditoriaPanel`). |

---

## 3. Stack Tecnológico

<div align="center">

| Tecnología | Rol en la Aplicación |
|---|---|
| **Java 21 (OpenJDK)** | Lenguaje principal de programación con sintaxis moderna (Records, Pattern Matching). |
| **Java Swing + FlatLaf 3.4** | Cliente GUI de escritorio con diseño oscuro (*Dark Theme*) libre de recortes. |
| **JDK Native HttpServer** | Servidor web concurrente integrado (`com.sun.net.httpserver.HttpServer`). |
| **MySQL 8.0 & H2 Database** | Motores de base de datos relacional (Persistencia JDBC nativa sin ORM/JPA). |
| **Jackson Databind** | Parseo y serialización JSON de alta velocidad. |
| **Auth0 java-jwt & jBCrypt** | Seguridad, hashing seguro de contraseñas de un solo sentido y firmas JWT. |
| **Apache Maven** | Gestión de dependencias y empaquetado JAR ejecutable autocontenido (*Shaded JAR*). |
| **Docker & Docker Compose** | Despliegue en contenedores para MySQL 8.0. |

</div>

---

## 4. Arquitectura Hexagonal y Patrones de Diseño

El proyecto implementa una **Arquitectura Hexagonal (Ports & Adapters)** estructurada por paquetes independientes (*Vertical Slice*):

```
com.acme.sica
├── domain/                              (DOMINIO PURO - Sin dependencias externas)
│   ├── model/                           (Persona, Usuario, Visita, Incidente, Empresa, Bitacora)
│   └── enums/                           (EstadoAcceso, EstadoVisita, TipoVisita, NivelGravedad)
│
├── application/                         (CAPA DE APLICACIÓN Y CASOS DE USO)
│   ├── dto/                             (DTOs inmutables de transferencia de datos)
│   ├── port/out/                        (Puertos de Salida: Repositorios e interfaces de infraestructura)
│   └── usecase/                         (AuthUseCase, GestionarVisitaUseCase, RegistrarIncidenteUseCase)
│
└── infrastructure/                      (CAPA DE INFRAESTRUCTURA Y ADAPTADORES)
    ├── adapter/
    │   ├── in/
    │   │   ├── http/                    (Handlers REST API HTTP y Router concurrente)
    │   │   └── gui/                     (Cliente Swing FlatLaf: GuardiaPanel, FuncionarioPanel, etc.)
    │   └── out/persistence/jdbc/        (Adaptadores JDBC concretos para MySQL y H2)
    ├── config/                          (Cargador de variables y config.properties)
    ├── db/                              (ConnectionFactory, SchemaInitializer con migraciones ALTER TABLE)
    └── security/                        (JwtUtil, PasswordHasher, PermissionChecker)
```

### Patrones de Diseño Aplicados:
1. **Abstract Factory Pattern (`db/connection/`)**: Fábricas concretas `MySqlConnectionFactory` y `H2ConnectionFactory` seleccionadas dinámicamente mediante `DatabaseFactoryProvider`.
2. **Factory Pattern (`VisitaFactory`)**: Centraliza la instanciación de visitas asignando estados según la tipología del flujo.
3. **Strategy Pattern (`AccessValidationStrategy`)**: Algoritmos intercambiables de validación de acceso (`RestrictedPersonValidationStrategy`, `PreRegisteredValidationStrategy`, `UnannouncedValidationStrategy`).
4. **State Pattern / Chain**: Gestión de ciclo de vida de visitas y autorización RBAC middleware.

---

## 5. Modelo Entidad-Relación (E-R)

```mermaid
erDiagram
    ROL ||--o{ ROL_PERMISO : "posee"
    PERMISO ||--o{ ROL_PERMISO : "pertenece"
    ROL ||--o{ USUARIO : "asignado_a"
    EMPRESA ||--o{ USUARIO : "emplea"
    EMPRESA ||--o{ PERSONA : "pertenece"
    USUARIO ||--o{ VISITA : "funcionario_anfitrion"
    USUARIO ||--o{ VISITA : "guardia_ingreso"
    USUARIO ||--o{ VISITA : "guardia_salida"
    PERSONA ||--o{ VISITA : "visitante"
    PERSONA ||--o{ INCIDENTE : "involucrado"
    USUARIO ||--o{ INCIDENTE : "reportado_por"
    PUNTO_ACCESO ||--o{ VISITA : "punto_ingreso"
    PUNTO_ACCESO ||--o{ VISITA : "punto_salida"
    USUARIO ||--o{ BITACORA_AUDITORIA : "ejecuta_accion"

    ROL { bigint id PK; varchar nombre UK }
    PERMISO { bigint id PK; varchar nombre UK }
    EMPRESA { bigint id PK; varchar nit UK; varchar nombre; boolean activa }
    USUARIO { bigint id PK; varchar username UK; varchar password_hash; bigint rol_id FK; bigint empresa_id FK }
    PERSONA { bigint id PK; varchar doc_identidad UK; varchar estado_acceso; bigint empresa_id FK }
    VISITA { bigint id PK; bigint persona_id FK; varchar tipo_visita; varchar estado_visita; varchar tipo_cierre }
    INCIDENTE { bigint id PK; bigint persona_id FK; varchar nivel_gravedad }
    PUNTO_ACCESO { bigint id PK; varchar nombre UK }
    BITACORA_AUDITORIA { bigint id PK; varchar accion; varchar username; text detalle; datetime fecha_hora }
```

---

## 6. Control de Acceso Basado en Roles (RBAC)

### Credenciales de Prueba Preconfiguradas:

| Rol | Username | Password | Permisos Principales | Empresa |
|---|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | Control total (1 a 16), gestión de usuarios/empresas, auditoría y limpieza de visitas | N/A |
| **GUARDIA** | `guardia1` | `guardia123` | `crear_persona`, `checkin_visita`, `checkout_visita`, `registrar_incidente`, `generar_reporte` | Recepción |
| **FUNCIONARIO** | `func1` | `func123` | `preregistrar_visita`, `aprobar_visita`, `crear_persona`, `generar_reporte` | Acme Corporation |

---

## 7. Guía de Instalación y Ejecución

### Requisitos Previos
- **Java JDK 17 o 21** instalado.
- **Git** instalado.

### Pasos de Ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/DMntill4/SICA.git
cd SICA

# 2. Copiar la plantilla de entorno
cp .env.example .env

# 3. Compilar y empaquetar con Maven Wrapper
./mvnw clean package

# 4. Ejecutar la aplicación (Backend HTTP + GUI Swing FlatLaf)
java -jar target/sica.jar
```

---

## 8. Catálogo de Endpoints REST API

- `POST /auth/login` — Autenticación de usuario y retorno de JWT.
- `POST /auth/logout` — Cierre de sesión y revocación del token JWT.
- `GET /personas` — Listado de personas/visitantes registrados.
- `POST /personas` — Registrar nueva persona (`crear_persona`).
- `DELETE /personas/{id}` — Eliminar persona de la base de datos (`modificar_persona`).
- `PUT /personas/{id}/rehabilitar` — Rehabilitar acceso levantando restricción de incidente.
- `POST /visitas/preregistrar` — Pre-registro de invitado por funcionario.
- `POST /visitas/no-anunciada` — Registro de visitante inesperado por guardia.
- `POST /visitas/pase-temporal` — Ingreso por carnet olvidado.
- `PUT /visitas/{id}/aprobar` — Aprobación de visita pendiente por funcionario.
- `POST /visitas/{id}/check-in` — Registro de entrada con regularización automática de salida olvidada.
- `POST /visitas/{id}/check-out` — Registro de salida normal.
- `POST /incidentes` — Registro de incidente y bloqueo automático a `RESTRINGIDO`.
- `DELETE /visitas` — Limpiar todo el historial de visitas (Solo Admin).
- `GET /reportes/auditoria` — Consulta de la bitácora inmutable de auditoría.

---

## 9. Pruebas Unitarias y Cobertura QA

El repositorio cuenta con una suite automatizada de pruebas unitarias con JUnit 5:
- `AuthUseCaseTest`: Verificación de intentos fallidos, bloqueos y hashing de contraseñas.
- `VisitaFactoryTest`: Verificación de la creación de visitas según tipología.
- `SalidaOlvidadaTest`: Verificación de la regularización automática de visitas con `CERRADA_POR_SISTEMA`.
- `PermissionCheckerTest`: Pruebas de seguridad RBAC.

```bash
# Ejecutar la suite de pruebas unitarias
./mvnw test
```

---

## 10. Contribuidores y Autores

<div align="center">

| Contribuidor | Rol en el Proyecto | Perfil GitHub |
|---|---|---|
| **Diego Mantilla** | Lead Software Engineer | [@DMntill4](https://github.com/DMntill4) |
| **Andrés Guerra** | Lead Software Engineer | [@andresguerra321](https://github.com/andresguerra321) |

<br/>

[![Diego Mantilla](https://img.shields.io/badge/DIEGO_MANTILLA-LEAD_SOFTWARE_ENGINEER-1E293B?style=for-the-badge&logo=github&logoColor=white)](https://github.com/DMntill4)
[![Andrés Guerra](https://img.shields.io/badge/ANDRES_GUERRA-LEAD_SOFTWARE_ENGINEER-0284C7?style=for-the-badge&logo=github&logoColor=white)](https://github.com/andresguerra321)

<br/>

***

*Sistema Integrado de Control de Acceso (SICA) — Desarrollado para Complejo Empresarial Zona Acme.*

</div>
