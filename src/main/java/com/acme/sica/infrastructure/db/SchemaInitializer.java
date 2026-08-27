package com.acme.sica.infrastructure.db;

import com.acme.sica.infrastructure.db.connection.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.acme.sica.infrastructure.security.PasswordHasher;

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
            try {
                stmt.executeUpdate("INSERT IGNORE INTO permiso (id, nombre, descripcion) VALUES (17, 'gestionar_roles', 'Permite crear roles, modificar permisos de un rol y eliminar roles')");
            } catch (Exception ignored) {}
            try {
                stmt.executeUpdate("INSERT IGNORE INTO rol_permiso (rol_id, permiso_id) VALUES (1, 17)");
            } catch (Exception ignored) {}


            // 2. Sembrar Usuarios (las contraseñas se hashean)
            PasswordHasher hasher = new PasswordHasher();
            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT IGNORE INTO usuario (id, username, password_hash, rol_id, empresa_id, bloqueado, nombre_completo, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

                // ADMIN (admin / admin123)
                pst.setLong(1, 1L);
                pst.setString(2, "admin");
                pst.setString(3, hasher.hashPassword("admin123"));
                pst.setLong(4, 1L); // ROL ADMIN
                pst.setNull(5, java.sql.Types.BIGINT); // Sin empresa
                pst.setBoolean(6, false);
                pst.setString(7, "Administrador del Sistema");
                pst.setString(8, "admin@zonaacme.com");
                pst.addBatch();

                // GUARDIA 1 (guardia1 / guardia123)
                pst.setLong(1, 2L);
                pst.setString(2, "guardia1");
                pst.setString(3, hasher.hashPassword("guardia123"));
                pst.setLong(4, 2L); // ROL GUARDIA
                pst.setLong(5, 1L); // Recepcion
                pst.setBoolean(6, false);
                pst.setString(7, "Guardia Principal");
                pst.setString(8, "guardia1@zonaacme.com");
                pst.addBatch();

                // FUNCIONARIO 1 (func1 / func123)
                pst.setLong(1, 3L);
                pst.setString(2, "func1");
                pst.setString(3, hasher.hashPassword("func123"));
                pst.setLong(4, 3L); // ROL FUNCIONARIO
                pst.setLong(5, 2L); // Acme Corp
                pst.setBoolean(6, false);
                pst.setString(7, "Funcionario Ejemplo");
                pst.setString(8, "funcionario@acmecorp.com");
                pst.addBatch();

                pst.executeBatch();
            }

            // 3. Garantizar que las contraseñas semilla existentes tengan los hashes válidos sin alterar su estado de bloqueo
            try (PreparedStatement updatePst = conn.prepareStatement(
                    "UPDATE usuario SET password_hash = ? WHERE username = ?")) {
                updatePst.setString(1, hasher.hashPassword("admin123"));
                updatePst.setString(2, "admin");
                updatePst.executeUpdate();

                updatePst.setString(1, hasher.hashPassword("guardia123"));
                updatePst.setString(2, "guardia1");
                updatePst.executeUpdate();

                updatePst.setString(1, hasher.hashPassword("func123"));
                updatePst.setString(2, "func1");
                updatePst.executeUpdate();
                System.out.println("[DB Init] Hashes de usuarios semilla verificados (admin, guardia1, func1).");
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
