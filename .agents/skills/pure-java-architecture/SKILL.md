---
name: pure-java-architecture
description: Guía de arquitectura de software y mejores prácticas para desarrollo Backend en Java Puro (Java SE nativo sin frameworks invasivos). Abarca Arquitectura Limpia/Hexagonal, Capas desacopladas, DAO/JDBC robusto, Inyección de Dependencias manual (Composition Root), Concurrencia y Gestión Eficiente de Recursos.
---

# Pure Java Backend Architecture & Best Practices

Esta skill define la guía arquitectónica, estándares y patrones de diseño para construir sistemas backend robustos, escalables y mantenibles en **Java Puro** (sin depender de frameworks mágicos o invasivos como Spring Boot/Jakarta EE salvo que se especifique).

---

## 1. Filosofía y Principios Fundamentales

- **SOLID & Clean Architecture**: Separación estricta de responsabilidades mediante interfaces y desacoplamiento de capas.
- **Sin Magia / Reflexión Oculta**: Control explícito del flujo, ciclo de vida de objetos y dependencias claras.
- **Inyección de Dependencias Manual (Composition Root)**: Toda dependencia se inyecta vía constructores; el armado de instancias se centraliza en una clase de arranque (`AppFactory` o `Bootstrap`).
- **Resource Safety (Zero Leaks)**: Uso exhaustivo de `try-with-resources` (`AutoCloseable`) para conexiones de BD, sockets, streams y threads.
- **Inmutabilidad y Tipado Fuerte**: Preferencia por `record` para DTOs y Value Objects, colecciones inmutables (`List.copyOf()`, `Collections.unmodifiableList()`), y `Optional<T>` para retornos potencialmente ausentes.

---

## 2. Arquitectura de Capas y Estructura de Paquetes

Se adopta una **Arquitectura Limpia / Hexagonal (Puertos y Adaptadores)**:

```
com.sica/
├── domain/                    # Núcleo de negocio (cero dependencias externas)
│   ├── model/                 # Entidades y Value Objects (Records / Clases ricas)
│   ├── repository/            # Interfaces de persistencia (Puertos de salida)
│   └── exception/             # Excepciones de negocio (DomainException)
│
├── application/               # Casos de uso y lógica de aplicación
│   ├── service/               # Servicios de aplicación que orquestan el dominio
│   └── dto/                   # Data Transfer Objects (Request / Response)
│
├── infrastructure/            # Adaptadores e implementaciones técnicas
│   ├── persistence/
│   │   ├── jdbc/              # Implementaciones DAO usando JDBC puro
│   │   └── mapper/            # Mapeadores de ResultSet a Entidad (Funcionales)
│   ├── config/                # Carga de propiedades (.properties / .env)
│   └── db/                    # Gestión de DataSource / Connection Pool
│
└── presentation/              # Puertos de entrada (API REST / HTTP Server / CLI)
    ├── handler/               # Controladores o Handlers HTTP (ej. com.sun.net.httpserver)
    └── App.java               # Punto de entrada y Composition Root (Bootstrap)
```

---

## 3. Inyección de Dependencias Manual (Composition Root)

En Java puro no se requiere un contenedor pesado de IoC; se utiliza el patrón **Composition Root** en el `main`:

```java
public class App {
    public static void main(String[] args) {
        // 1. Infraestructura
        DatabaseConfig dbConfig = DatabaseConfig.fromProperties("db.properties");
        DataSource dataSource = ConnectionPoolFactory.create(dbConfig);
        
        // 2. Repositorios / DAOs (Adaptadores de salida)
        UsuarioRepository usuarioRepo = new JdbcUsuarioRepository(dataSource);
        
        // 3. Servicios de Aplicación (Casos de uso)
        UsuarioService usuarioService = new UsuarioServiceImpl(usuarioRepo);
        
        // 4. Presentación / Servidor
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/usuarios", new UsuarioHttpHandler(usuarioService));
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor()); // Java 21+ o ThreadPool
        server.start();
        
        // 5. Hook de apagado limpio (Graceful Shutdown)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            dataSource.close();
        }));
    }
}
```

---

## 4. Patrón DAO y Acceso a Datos con JDBC Puro

### Reglas de Oro en JDBC:
1. **Siempre usar `PreparedStatement`** (prevención de inyección SQL).
2. **Siempre usar `try-with-resources`** para `Connection`, `PreparedStatement` y `ResultSet`.
3. **Mapeo Funcional con RowMapper**: Desacoplar la lectura de columnas a la creación del objeto.
4. **Gestión Transaccional Explícita**: `connection.setAutoCommit(false)`, `commit()` y `rollback()` en bloques `catch`.

### Implementación Ejemplo:

```java
public class JdbcUsuarioRepository implements UsuarioRepository {
    private final DataSource dataSource;

    public JdbcUsuarioRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource cannot be null");
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        String sql = "SELECT id, username, email, activo FROM usuarios WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUsuario(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error consultando usuario por id: " + id, e);
        }
    }

    private Usuario mapRowToUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getBoolean("activo")
        );
    }
}
```

---

## 5. Manejo de Excepciones y Resiliencia

- **Excepciones de Dominio**: Crear una jerarquía limpia no acoplada a infraestructura (`DomainException`, `EntityNotFoundException`, `BusinessRuleViolationException`).
- **Excepciones de Infraestructura**: Envolver `SQLException` / `IOException` en `DataAccessException` o `InfrastructureException` preservando la causa raíz (`cause`).
- **Error Handlers Centralizados**: En la capa de presentación/handler, interceptar excepciones y traducirlas a códigos HTTP (400, 404, 409, 500) con mensajes JSON claros.

---

## 6. Concurrencia y Gestión de Memoria

- **Pool de Conexiones**: Usar un Connection Pool ligero (ej. HikariCP o implementación básica `BlockingQueue<Connection>`) en lugar de abrir conexiones `DriverManager` por petición.
- **Inmutabilidad por Defecto**: Clases inmutables evitan condiciones de carrera (`race conditions`) y facilitan la concurrencia.
- **Hilos y Ejecutores**: Usar `ExecutorService` con shutdown controlado (`awaitTermination`) y nunca instanciar `new Thread()` de forma descontrolada.

---

## 7. Integración con Graphify

Para mantener la calidad arquitectónica y el control del impacto en cambios:
1. **Analizar acoplamiento**: Usar `graphify query "<ClaseOServicio>"` antes de refactorizar para ver dependencias entrantes y salientes.
2. **Evitar God Nodes**: Comprobar periódicamente con `graphify god-nodes` que ninguna clase o controlador centralice excesiva responsabilidad.
3. **Actualización continua**: Ejecutar `graphify update .` tras añadir nuevos módulos o paquetes para mantener el mapa de arquitectura sincronizado.
