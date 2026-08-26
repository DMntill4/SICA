-- ============================================================================
-- SICA - Sistema Integrado de Control de Acceso (Zona Acme)
-- Database Schema Script (H2 Database Compatible)
-- Order of creation: Parents before Children
-- ============================================================================

-- 1. Tabla: ROL
CREATE TABLE IF NOT EXISTS rol (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

-- 2. Tabla: PERMISO
CREATE TABLE IF NOT EXISTS permiso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

-- 3. Tabla Intermedia: ROL_PERMISO
CREATE TABLE IF NOT EXISTS rol_permiso (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (rol_id) REFERENCES rol(id) ON DELETE CASCADE,
    CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (permiso_id) REFERENCES permiso(id) ON DELETE CASCADE
);

-- 4. Tabla: EMPRESA
CREATE TABLE IF NOT EXISTS empresa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nit VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    ubicacion_oficina VARCHAR(100),
    activa BOOLEAN DEFAULT TRUE
);

-- 5. Tabla: USUARIO
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    rol_id BIGINT NOT NULL,
    empresa_id BIGINT NULL,
    intentos_fallidos INT DEFAULT 0,
    bloqueado BOOLEAN DEFAULT FALSE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id),
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id) ON DELETE SET NULL
);

-- 6. Tabla: TOKEN_REVOCADO (Logout blacklist Nivel 2)
CREATE TABLE IF NOT EXISTS token_revocado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_jti VARCHAR(255) NOT NULL UNIQUE,
    expira_en TIMESTAMP NOT NULL,
    revocado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tabla: PERSONA
CREATE TABLE IF NOT EXISTS persona (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_identidad VARCHAR(30) NOT NULL UNIQUE,
    tipo_documento VARCHAR(10) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    empresa_id BIGINT NULL,
    estado_acceso VARCHAR(20) NOT NULL DEFAULT 'HABILITADO', -- HABILITADO, RESTRINGIDO
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_persona_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id) ON DELETE SET NULL
);

-- 8. Tabla: PUNTO_ACCESO
CREATE TABLE IF NOT EXISTS punto_acceso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    ubicacion VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT TRUE
);

-- 9. Tabla: VISITA_GRUPO
CREATE TABLE IF NOT EXISTS visita_grupo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_grupo VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    creado_por_usuario_id BIGINT NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visita_grupo_creador FOREIGN KEY (creado_por_usuario_id) REFERENCES usuario(id)
);

-- 10. Tabla: VISITA
CREATE TABLE IF NOT EXISTS visita (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    funcionario_id BIGINT NOT NULL,
    punto_acceso_ingreso_id BIGINT NULL,
    punto_acceso_salida_id BIGINT NULL,
    guardia_ingreso_id BIGINT NULL,
    guardia_salida_id BIGINT NULL,
    visita_grupo_id BIGINT NULL,
    tipo_visita VARCHAR(30) NOT NULL, -- PRE_REGISTRADA, NO_ANUNCIADA, PASE_TEMPORAL
    estado_visita VARCHAR(30) NOT NULL, -- APROBADO, PENDIENTE_APROBACION, PENDIENTE_APROBACION_OLVIDO, RECHAZADO, DENTRO, FINALIZADO
    motivo VARCHAR(255),
    fecha_hora_programada TIMESTAMP NULL,
    fecha_hora_ingreso TIMESTAMP NULL,
    fecha_hora_salida TIMESTAMP NULL,
    tipo_cierre VARCHAR(30) NULL, -- NORMAL, CERRADA_POR_SISTEMA
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visita_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_visita_funcionario FOREIGN KEY (funcionario_id) REFERENCES usuario(id),
    CONSTRAINT fk_visita_guardia_ingreso FOREIGN KEY (guardia_ingreso_id) REFERENCES usuario(id),
    CONSTRAINT fk_visita_guardia_salida FOREIGN KEY (guardia_salida_id) REFERENCES usuario(id),
    CONSTRAINT fk_visita_pa_ingreso FOREIGN KEY (punto_acceso_ingreso_id) REFERENCES punto_acceso(id),
    CONSTRAINT fk_visita_pa_salida FOREIGN KEY (punto_acceso_salida_id) REFERENCES punto_acceso(id),
    CONSTRAINT fk_visita_grupo FOREIGN KEY (visita_grupo_id) REFERENCES visita_grupo(id) ON DELETE SET NULL
);

-- 11. Tabla: CODIGO_QR
CREATE TABLE IF NOT EXISTS codigo_qr (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visita_id BIGINT NOT NULL UNIQUE,
    codigo_hash VARCHAR(255) NOT NULL UNIQUE,
    expira_en TIMESTAMP NOT NULL,
    usado BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_codigo_qr_visita FOREIGN KEY (visita_id) REFERENCES visita(id) ON DELETE CASCADE
);

-- 12. Tabla: INCIDENTE
CREATE TABLE IF NOT EXISTS incidente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    reportado_por_usuario_id BIGINT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    nivel_gravedad VARCHAR(20) NOT NULL, -- BAJO, MEDIO, ALTO, CRITICO
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incidente_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_incidente_usuario FOREIGN KEY (reportado_por_usuario_id) REFERENCES usuario(id)
);

-- 13. Tabla: NOTIFICACION
CREATE TABLE IF NOT EXISTS notificacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_destinatario_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_destinatario_id) REFERENCES usuario(id)
);

-- 14. Tabla: BITACORA_AUDITORIA (Append-Only Log)
CREATE TABLE IF NOT EXISTS bitacora_auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NULL,
    username VARCHAR(50) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    detalle TEXT,
    ip_origen VARCHAR(45),
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
