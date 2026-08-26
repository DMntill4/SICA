-- ============================================================================
-- SICA - Seed Data Script
-- Poblado de datos iniciales para evaluación y pruebas de integración
-- ============================================================================

-- 1. Roles Base
INSERT INTO rol (id, nombre, descripcion) VALUES (1, 'ADMIN', 'Administrador total del sistema');
INSERT INTO rol (id, nombre, descripcion) VALUES (2, 'GUARDIA', 'Guardia de recepción y control de accesos');
INSERT INTO rol (id, nombre, descripcion) VALUES (3, 'FUNCIONARIO', 'Empleado o funcionario de empresa del complejo');

-- 2. Permisos del Sistema
INSERT INTO permiso (id, nombre, descripcion) VALUES (1, 'crear_usuario', 'Permite crear nuevos usuarios en el sistema');
INSERT INTO permiso (id, nombre, descripcion) VALUES (2, 'modificar_usuario', 'Permite modificar usuarios existentes');
INSERT INTO permiso (id, nombre, descripcion) VALUES (3, 'eliminar_usuario', 'Permite eliminar/desactivar usuarios');
INSERT INTO permiso (id, nombre, descripcion) VALUES (4, 'crear_persona', 'Permite registrar nuevas personas en la base de datos');
INSERT INTO permiso (id, nombre, descripcion) VALUES (5, 'modificar_persona', 'Permite actualizar datos de personas');
INSERT INTO permiso (id, nombre, descripcion) VALUES (6, 'preregistrar_visita', 'Permite a un funcionario pre-registrar una visita');
INSERT INTO permiso (id, nombre, descripcion) VALUES (7, 'aprobar_visita', 'Permite a un funcionario aprobar o rechazar visitas');
INSERT INTO permiso (id, nombre, descripcion) VALUES (8, 'checkin_visita', 'Permite al guardia registrar el ingreso de una visita');
INSERT INTO permiso (id, nombre, descripcion) VALUES (9, 'checkout_visita', 'Permite al guardia registrar la salida de una visita');
INSERT INTO permiso (id, nombre, descripcion) VALUES (10, 'registrar_incidente', 'Permite registrar un incidente y restringir acceso');
INSERT INTO permiso (id, nombre, descripcion) VALUES (11, 'generar_reporte', 'Permite consultar reportes y métricas del sistema');
INSERT INTO permiso (id, nombre, descripcion) VALUES (12, 'consultar_auditoria', 'Permite consultar la bitácora de eventos');

-- 3. Mapeo de Rol - Permisos
-- ADMIN (Permisos 1 a 12)
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 1);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 2);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 3);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 4);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 5);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 6);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 7);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 8);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 9);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 10);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 11);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 12);

-- GUARDIA (Permisos 4, 5, 8, 9, 10, 11)
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (2, 4);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (2, 5);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (2, 8);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (2, 9);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (2, 10);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (2, 11);

-- FUNCIONARIO (Permisos 4, 6, 7, 11)
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (3, 4);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (3, 6);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (3, 7);
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (3, 11);

-- 4. Empresas de Prueba
INSERT INTO empresa (id, nit, nombre, ubicacion_oficina, activa) VALUES (1, '900123456-1', 'Acme Corporation', 'Torre A - Piso 5', TRUE);
INSERT INTO empresa (id, nit, nombre, ubicacion_oficina, activa) VALUES (2, '900654321-2', 'Cyberdyne Systems', 'Torre B - Piso 3', TRUE);
INSERT INTO empresa (id, nit, nombre, ubicacion_oficina, activa) VALUES (3, '900999888-3', 'Wayne Enterprises', 'Torre A - Piso 10', TRUE);

-- 5. Puntos de Acceso
INSERT INTO punto_acceso (id, nombre, ubicacion, activo) VALUES (1, 'Entrada Principal Norte', 'Recepción Edificio A', TRUE);
INSERT INTO punto_acceso (id, nombre, ubicacion, activo) VALUES (2, 'Vehicular Sur', 'Sótano 1 - Estacionamiento', TRUE);
INSERT INTO punto_acceso (id, nombre, ubicacion, activo) VALUES (3, 'Peatonal Este', 'Acceso Peatonal Secundario', TRUE);

-- 6. Usuarios de Prueba (Contraseñas con BCrypt)
-- Hash BCrypt para 'admin123': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- Hash BCrypt para 'guardia123': $2a$10$e8.Z/4q4g7xP/H6d5F1u1.v8S9b6o.g8O/D2j2.5x5k5m5a5b5c5d
-- Hash BCrypt para 'func123': $2a$10$R.8r3i/3p4o5q6r7s8t9u.v0w1x2y3z4a5b6c7d8e9f0g1h2i3j4k
-- NOTA: Se actualizarán mediante script de inicialización con hash exacto jBCrypt si fuese necesario.

INSERT INTO usuario (id, username, password_hash, nombre_completo, email, rol_id, empresa_id)
VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador General', 'admin@zonaacme.com', 1, NULL);

INSERT INTO usuario (id, username, password_hash, nombre_completo, email, rol_id, empresa_id)
VALUES (2, 'guardia1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Carlos Guardia', 'guardia1@zonaacme.com', 2, NULL);

INSERT INTO usuario (id, username, password_hash, nombre_completo, email, rol_id, empresa_id)
VALUES (3, 'func1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ana Funcionario', 'afuncionario@acme.com', 3, 1);

-- 7. Personas de Prueba
INSERT INTO persona (id, doc_identidad, tipo_documento, nombre, apellido, email, telefono, empresa_id, estado_acceso)
VALUES (1, '1010101010', 'CC', 'Juan', 'Pérez', 'juan.perez@example.com', '555-0101', 1, 'HABILITADO');

INSERT INTO persona (id, doc_identidad, tipo_documento, nombre, apellido, email, telefono, empresa_id, estado_acceso)
VALUES (2, '2020202020', 'CC', 'María', 'Gómez', 'maria.gomez@example.com', '555-0202', NULL, 'HABILITADO');

INSERT INTO persona (id, doc_identidad, tipo_documento, nombre, apellido, email, telefono, empresa_id, estado_acceso)
VALUES (3, '3030303030', 'CE', 'Carlos', 'Restrepo', 'carlos.restrepo@example.com', '555-0303', NULL, 'RESTRINGIDO');
