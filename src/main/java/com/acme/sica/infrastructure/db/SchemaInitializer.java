package com.acme.sica.infrastructure.db;

import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaInitializer {

    private final ConnectionFactory connectionFactory;

    public SchemaInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void initialize() {
        System.out.println("[DB Init] Inicializando esquema de base de datos (" + connectionFactory.getDatabaseType() + ")...");

        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean isMySql = "MYSQL".equalsIgnoreCase(connectionFactory.getDatabaseType());
            String checkSql = isMySql
                    ? "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'usuario'"
                    : "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'USUARIO'";

            boolean tableExists = false;
            try (ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next()) {
                    tableExists = rs.getInt(1) > 0;
                }
            }

            if (!tableExists) {
                executeSqlScript(stmt, "/schema.sql");
                executeSqlScript(stmt, "/data.sql");
                System.out.println("[DB Init] Tablas creadas y datos iniciales insertados con exito en " + connectionFactory.getDatabaseType() + ".");
            } else {
                System.out.println("[DB Init] Base de datos " + connectionFactory.getDatabaseType() + " lista y verificada.");
            }

            // Garantizar migración de columnas en tabla visita si la base de datos ya existía
            try {
                stmt.executeUpdate("ALTER TABLE visita ADD COLUMN punto_acceso_ingreso_id BIGINT NULL");
            } catch (Exception ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE visita ADD COLUMN punto_acceso_salida_id BIGINT NULL");
            } catch (Exception ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE visita ADD COLUMN guardia_ingreso_id BIGINT NULL");
            } catch (Exception ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE visita ADD COLUMN guardia_salida_id BIGINT NULL");
            } catch (Exception ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE incidente CHANGE usuario_reporta_id reportado_por_usuario_id BIGINT NOT NULL");
            } catch (Exception ignored) {}
            try {
                stmt.executeUpdate("INSERT IGNORE INTO rol_permiso (rol_id, permiso_id) VALUES (3, 4)");
            } catch (Exception ignored) {}

            // Garantizar contraseñas semilla válidas y desbloqueadas para las pruebas de QA
            try {
                String hashAdmin = com.acme.sica.infrastructure.security.PasswordHasher.hashPassword("admin123");
                String hashGuardia = com.acme.sica.infrastructure.security.PasswordHasher.hashPassword("guardia123");
                String hashFunc = com.acme.sica.infrastructure.security.PasswordHasher.hashPassword("func123");

                stmt.executeUpdate("UPDATE usuario SET password_hash = '" + hashAdmin + "', bloqueado = FALSE, intentos_fallidos = 0 WHERE username = 'admin'");
                stmt.executeUpdate("UPDATE usuario SET password_hash = '" + hashGuardia + "', bloqueado = FALSE, intentos_fallidos = 0 WHERE username = 'guardia1'");
                stmt.executeUpdate("UPDATE usuario SET password_hash = '" + hashFunc + "', bloqueado = FALSE, intentos_fallidos = 0 WHERE username = 'func1'");
                System.out.println("[DB Init] Contraseñas semilla verificadas (admin/admin123, guardia1/guardia123, func1/func123).");
            } catch (Exception e) {
                System.err.println("[DB Init Warning] No se pudieron actualizar contraseñas semilla: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[FATAL DB Init] Error inicializando BD " + connectionFactory.getDatabaseType() + ": " + e.getMessage());
            System.err.println("[INFO] Asegurate de que el servidor MySQL este en ejecucion o que las credenciales sean correctas.");
        }
    }

    private void executeSqlScript(Statement stmt, String scriptPath) throws Exception {
        InputStream is = getClass().getResourceAsStream(scriptPath);
        if (is == null) {
            throw new IllegalArgumentException("Script SQL no encontrado en classpath: " + scriptPath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }
                int commentIndex = line.indexOf("--");
                if (commentIndex != -1) {
                    line = line.substring(0, commentIndex);
                }
                sb.append(line).append("\n");
            }

            String[] sqlStatements = sb.toString().split(";");
            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    try {
                        stmt.execute(sql.trim());
                    } catch (Exception e) {
                        System.err.println("[DB Init Warning] Warning ejecutando SQL: " + e.getMessage());
                    }
                }
            }
        }
    }
}
