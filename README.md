<div align="center">

# SICA — Sistema Integrado de Control de Acceso
### *Complejo Empresarial "Zona Acme"*

[![Java 21+](https://img.shields.io/badge/JAVA_21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![IA & Computer Vision](https://img.shields.io/badge/IA_&_COMPUTER_VISION-FF6F00?style=for-the-badge&logo=openai&logoColor=white)](#3-biometría-facial-e-inteligencia-de-acceso-128d-ia)
[![MediaPipe Neural AI](https://img.shields.io/badge/MEDIAPIPE_NEURAL_AI-00979D?style=for-the-badge&logo=google&logoColor=white)](https://google.github.io/mediapipe/)
[![Biometría Facial 128D](https://img.shields.io/badge/BIOMETR%C3%8DA_128D-06B6D4?style=for-the-badge&logo=opencv&logoColor=white)](#3-biometría-facial-e-inteligencia-de-acceso-128d-ia)
[![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)](https://developer.mozilla.org/es/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)](https://developer.mozilla.org/es/docs/Web/CSS)
[![JavaScript ES6+](https://img.shields.io/badge/JAVASCRIPT_ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)](https://developer.mozilla.org/es/docs/Web/JavaScript)
[![Java Swing](https://img.shields.io/badge/JAVA_SWING-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![MySQL 8.0](https://img.shields.io/badge/MYSQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT Auth](https://img.shields.io/badge/JWT_AUTH-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![FlatLaf Dark](https://img.shields.io/badge/FLATLAF_DARK-1E293B?style=for-the-badge&logo=swing&logoColor=white)](https://www.formdev.com/flatlaf/)
[![GitFlow](https://img.shields.io/badge/GITFLOW-F05032?style=for-the-badge&logo=git&logoColor=white)](https://github.com/DMntill4/SICA.git)

<p align="center">
  <b>Sistema de arquitectura hexagonal pura + Reconocimiento Biométrico Facial impulsado por Inteligencia Artificial (IA & Computer Vision 128D) en vivo + Portal Web de Autoservicio de Visitantes (HTML5, CSS3, JavaScript) y cliente GUI de escritorio en Java Swing (FlatLaf Dark) para el control de accesos, incidentes y auditoría inmutable en tiempo real.</b>
</p>

</div>

---

## Tabla de Contenidos

- [1. Introducción y Contexto del Problema](#1-introducción-y-contexto-del-problema)
- [2. Características Principales](#2-características-principales)
- [3. Biometría Facial e Inteligencia de Acceso (128D - IA)](#3-biometría-facial-e-inteligencia-de-acceso-128d-ia)
- [4. Stack Tecnológico](#4-stack-tecnológico)
- [5. Arquitectura Hexagonal y Patrones de Diseño](#5-arquitectura-hexagonal-y-patrones-de-diseño)
- [6. Control de Acceso Basado en Roles (RBAC)](#6-control-de-acceso-basado-en-roles-rbac)
- [7. Guía de Instalación y Ejecución](#7-guía-de-instalación-y-ejecución)
- [8. Catálogo de Endpoints REST API](#8-catálogo-de-endpoints-rest-api)
- [9. Pruebas Unitarias y Cobertura QA](#9-pruebas-unitarias-y-cobertura-qa)
- [10. Recomendaciones de Despliegue y Seguridad](#10-recomendaciones-de-despliegue-y-seguridad)
- [11. Contribuidores y Autores](#11-contribuidores-y-autores)

---

## 1. Introducción y Contexto del Problema

El Complejo Empresarial **"Zona Acme"** alberga a más de 30 empresas de alto perfil. Sin embargo, su control de acceso manual basado en cuadernos de papel y comunicación por radio generaba cuellos de botella, falta de trazabilidad e imprecisión en emergencias.

**SICA** resuelve estas problemáticas mediante:
- **Reconocimiento Biométrico Facial mediante Inteligencia Artificial (IA)**: Redes neuronales de visión computacional que capturan 468 landmarks faciales y sintetizan una firma vectorial 128D para autenticación por cámara en vivo en 5 segundos ($\text{Dist} \le 0.35$).
- **Portal Web de Autoservicio (`/portal`)**: Desarrollado en HTML5, Vanilla CSS3 y JavaScript ES6+ para registro independiente de invitados, emisión de pases web y Hub de Usuario Frecuente.
- **Automatización de los 4 Flujos de Acceso**: Pre-Registrado, No Anunciado / Express (Visita Rápida), Pase Temporal (Carnet Olvidado) y Regularización Automática de Salida Olvidada.
- **Seguridad Reactiva e Inmediata**: Bloqueo instantáneo en portería al registrar incidentes de gravedad CRÍTICO o ALTO (`RESTRINGIDO`).
- **Auditoría Inmutable**: Registro append-only de todas las operaciones del sistema con IP de origen y contexto de usuario.

---

## 2. Características Principales

| Módulo | Descripción |
|---|---|
| **Reconocimiento Facial IA 128D** | Escaneo en vivo con redes neuronales MediaPipe Face Mesh, cálculo vectorial de distancia euclidiana y verificación anti-duplicados por rostro. |
| **Portal Web de Autoservicio (HTML5/CSS3/JS)** | Interfaz web responsiva con Hub de Usuario Frecuente (`ESTADO: HABILITADO` / `ESTADO: RESTRINGIDO`), solicitud y cancelación de pases. |
| **Autenticación & JWT** | Login con hashing `BCrypt` y tokens JWT de sesión sin estado con revocación en logout (`token_revocado`). |
| **RBAC Granular** | Control de acceso basado en 16 permisos individuales en BD (`crear_persona`, `checkin_visita`, `aprobar_visita`, etc.). |
| **Flujos de Visita & Express** | Pre-registro, visitas exprés de paquetería `[EXPRESS]`, aprobación de pases y regularización de salidas olvidadas (`CERRADA_POR_SISTEMA`). |
| **Gestión de Incidentes** | Bloqueo dinámico de personas a estado `RESTRINGIDO`, histórico de alertas y botón de **Rehabilitación de Acceso**. |
| **Interfaz Swing FlatLaf Dark** | UI de escritorio moderna estructurada por roles (`GuardiaPanel`, `FuncionarioPanel`, `IncidentesPanel`, `AuditoriaPanel`, `SparklineChartPanel`). |

---

## 3. Biometría Facial e Inteligencia de Acceso (128D - IA)

El sistema integra **Inteligencia Artificial (IA) y Visión Computacional** de alta precisión para el reconocimiento de personas mediante la extracción de vectores biométricos multidimensionales (128D):

- **Redes Neuronales de Visión (IA)**: Inferencia en tiempo real utilizando modelos de redes neuronales convolucionales (MediaPipe AI Neural Vision) que rastrean 468 puntos de referencia faciales de la persona frente a la cámara.
- **Verificación Vectorial**: Comparación euclidiana entre la firma facial escaneada por la IA y la base de datos de personas.
  $$\text{Distancia Euclidiana} = \sqrt{\sum_{i=1}^{128} (v_{1,i} - v_{2,i})^2} \le 0.35 \implies \text{Similitud} \ge 85\%$$
- **Malla Facial 3D Snug-Fit**: Ajuste anatómico proporcional de la visualización wireframe de la IA sobre el rostro del visitante.
- **Ruta A (Usuario Frecuente)**: Reconocimiento directo por cámara e IA en 5 segundos con acceso al Hub de Usuario Frecuente.
- **Ruta B (Nuevo Registro)**: Verificación preventiva contra documentos y vectores preexistentes antes de autorizar un nuevo perfil.

---

## 4. Stack Tecnológico

<div align="center">

| Tecnología | Rol en la Aplicación |
|---|---|
| **Java 21 (OpenJDK)** | Lenguaje principal de programación con sintaxis moderna (Records, Pattern Matching). |
| **Inteligencia Artificial (IA) & Computer Vision** | Modelos neuronales de visión computacional para biometría facial de 128 dimensiones. |
| **MediaPipe Neural AI Vision** | Extracción biométrica facial con IA de 468 puntos de referencia en el navegador. |
| **HTML5 / CSS3 / JavaScript (ES6+)** | Frontend del Portal Web de Autoservicio (`/portal`), modales custom Glassmorphism y dinámicas SPA de la cámara. |
| **Java Swing + FlatLaf 3.4** | Cliente GUI de escritorio con diseño oscuro (*Dark Theme*) libre de recortes. |
| **JDK Native HttpServer** | Servidor web concurrente integrado (`com.sun.net.httpserver.HttpServer`). |
| **MySQL 8.0 & H2 Database** | Motores de base de datos relacional (Persistencia JDBC nativa sin ORM/JPA). |
| **Jackson Databind** | Parseo y serialización JSON de alta velocidad. |
| **Auth0 java-jwt & jBCrypt** | Seguridad, hashing seguro de contraseñas de un solo sentido y firmas JWT. |
| **Apache Maven** | Gestión de dependencias y empaquetado JAR ejecutable autocontenido (*Shaded JAR*). |

</div>



---

## 5. Arquitectura Hexagonal y Patrones de Diseño

El proyecto implementa una **Arquitectura Hexagonal (Ports & Adapters)** estructurada por paquetes independientes (*Vertical Slice*):

```
com.acme.sica
├── domain/                              (DOMINIO PURO - Sin dependencias externas)
│   ├── model/                           (Persona, Usuario, Visita, SolicitudPase, Incidente, Empresa, Bitacora)
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
    │   │   ├── http/                    (Handlers REST API HTTP: Biometria, SolicitudPase, Persona, Visita)
    │   │   └── gui/                     (Cliente Swing FlatLaf: GuardiaPanel, FuncionarioPanel, SicaTheme)
    │   └── out/persistence/jdbc/        (Adaptadores JDBC concretos para MySQL con cascada transaccional)
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
- **Java JDK 21** instalado.
- **Servidor MySQL 8.0** activo (Base de datos `sica`).
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

# 4. Ejecutar la aplicación (Backend HTTP + Portal Web + GUI Swing)
java -jar target/sica.jar
```

- **Portal Web Autoservicio Visitantes**: `http://localhost:8080/portal`
- **Endpoint Base REST API**: `http://localhost:8080/api`

---

## 8. Catálogo de Endpoints REST API

- `POST /api/auth/login` — Autenticación de usuario y retorno de JWT.
- `POST /api/auth/logout` — Cierre de sesión y revocación del token JWT.
- `POST /api/biometria` — Identificación biométrica facial por vector 128D.
- `POST /api/biometria/verificar-doc` — Verificación cruzada de documento y firma facial.
- `POST /api/pases/solicitar` — Solicitud de pase web de visitante con vector facial.
- `GET /api/pases/pendientes` — Listado de solicitudes de pases web pendientes.
- `GET /api/pases/persona/{doc}` — Listado de pases por número de documento.
- `POST /api/pases/{id}/aprobar` — Aprobación de pase por funcionario.
- `POST /api/pases/{id}/cancelar` — Cancelación de pase y actualización a estado `CANCELADA`.
- `GET /api/personas` — Listado de personas/visitantes registrados.
- `POST /api/personas` — Registrar nueva persona (`crear_persona`).
- `DELETE /api/personas/{id}` — Eliminar persona con borrado transaccional en cascada.
- `PUT /api/personas/{id}/rehabilitar` — Rehabilitar acceso levantando restricción de incidente.
- `POST /api/visitas/preregistrar` — Pre-registro de invitado por funcionario.
- `POST /api/visitas/no-anunciada` — Registro de visitante inesperado / Express en portería.
- `POST /api/visitas/pase-temporal` — Ingreso por carnet olvidado.
- `POST /api/visitas/{id}/check-in` — Registro de entrada con regularización automática de salida olvidada.
- `POST /api/visitas/{id}/check-out` — Registro de salida normal.
- `POST /api/incidentes` — Registro de incidente y bloqueo automático a `RESTRINGIDO`.
- `GET /api/reportes/auditoria` — Consulta de la bitácora inmutable de auditoría.

---

## 9. Pruebas Unitarias y Cobertura QA

El repositorio cuenta con una suite automatizada de pruebas unitarias con JUnit 5 (16 / 16 tests passing):
- `AuthUseCaseTest`: Verificación de intentos fallidos, bloqueos y hashing de contraseñas.
- `GestionarRolUseCaseTest`: Verificación de creación, modificación y asignación de permisos a roles.
- `VisitaFactoryTest`: Verificación de la creación de visitas según tipología.
- `SalidaOlvidadaTest`: Verificación de la regularización automática de visitas con `CERRADA_POR_SISTEMA`.
- `PermissionCheckerTest`: Pruebas de seguridad RBAC.

```bash
# Ejecutar la suite de pruebas unitarias
./mvnw test
```

---

## 10. Recomendaciones de Despliegue y Seguridad

> [!IMPORTANT]
> **Variables de Entorno**: Asegúrate de mantener la clave secreta `JWT_SECRET` y las credenciales de base de datos en el archivo `.env` sin subirlo al control de versiones.

> [!TIP]
> **Cámara HTTPS**: Para habilitar el escáner facial por cámara web en redes externas o producción, el portal web debe servirse a través de protocolo **HTTPS** (o `localhost` para pruebas locales).

> [!NOTE]
> **Persistencia Continua**: El sistema preserva todos los registros biométricos y de personas en MySQL a través de reinicios del servidor. Para restablecer datos de prueba de forma manual, utiliza las funciones de eliminación directa de la consola Swing como Administrador.

---

## 11. Contribuidores y Autores

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
