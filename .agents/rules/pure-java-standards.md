---
trigger: always_on
description: Reglas y estándares de desarrollo para backend en Java Puro (Clean Architecture, JDBC, SOLID, Inyección Manual y Gestión Eficiente de Recursos).
---

# Reglas de Desarrollo: Pure Java Backend

## 1. Arquitectura y Separación de Responsabilidades
- **Dominio Aislado**: Las entidades y modelos de `domain` NO deben tener dependencias de bibliotecas de base de datos (`java.sql.*`), frameworks HTTP o librerías externas.
- **Programar hacia Interfaces**: Los servicios de aplicación deben depender de interfaces de repositorios (`domain.repository`), nunca de implementaciones directas (`infrastructure.persistence.jdbc`).
- **Composition Root**: Centralizar la instanciación e inyección de dependencias en una clase de arranque (`App.java` / `Bootstrap`), inyectando dependencias estrictamente por constructor.

## 2. Gestión de Recursos y Rendimiento
- **Try-With-Resources Obligatorio**: Todo recurso que implemente `AutoCloseable` (`Connection`, `PreparedStatement`, `ResultSet`, `InputStream`, `OutputStream`, `Scanner`, `Socket`) DEBE cerrarse mediante `try-with-resources`.
- **PreparedStatement Exclusivo**: Jamás concatenar cadenas en consultas SQL. Utilizar `PreparedStatement` con parámetros tipados para evitar inyecciones SQL y permitir compilación previa de sentencias.
- **Inmutabilidad**: Utilizar `record` para DTOs y estructuras de datos inmutables en Java 17+.

## 3. Manejo de Excepciones
- No ocultar excepciones (`catch (Exception e) {}` vacío está prohibido).
- Envolver `SQLException` e `IOException` en excepciones de infraestructura/datos descriptivas preservando la traza original (`new DataAccessException("mensaje", cause)`).
- Definir excepciones de dominio específicas (`EntityNotFoundException`, `ValidationException`) para la lógica de negocio.

## 4. Uso de Graphify para Gestión de Recursos y Arquitectura
- Para analizar el impacto de un cambio o entender la relación entre módulos/clases, consultar primero el grafo local con `graphify query "<módulo>"` o `graphify path "<ClaseA>" "<ClaseB>"`.
- Mantener el grafo actualizado ejecutando `graphify update .` después de crear o modificar componentes estructurales.
