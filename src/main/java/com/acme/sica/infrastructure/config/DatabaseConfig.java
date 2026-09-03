package com.acme.sica.infrastructure.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Cargador Automatico de Configuracion de Base de Datos.
 * Lee prioritariamente config.properties o .env sin necesidad de comandos por consola.
 */
public class DatabaseConfig {

    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    static {
        loadConfig();
    }

    private static synchronized void loadConfig() {
        if (loaded) return;

        // 1. Cargar desde config.properties en classpath
        try (InputStream is = DatabaseConfig.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                properties.load(is);
                System.out.println("[Config] Archivo config.properties cargado desde classpath.");
            }
        } catch (Exception e) {
            System.err.println("[Config Warning] No se pudo cargar config.properties del classpath: " + e.getMessage());
        }

        // 2. Cargar desde archivo config.properties en la raiz del proyecto si existe
        File externalConfig = new File("config.properties");
        if (externalConfig.exists()) {
            try (FileInputStream fis = new FileInputStream(externalConfig)) {
                properties.load(fis);
                System.out.println("[Config] Archivo config.properties cargado desde directorio raiz.");
            } catch (Exception e) {
                System.err.println("[Config Warning] Error al leer config.properties externo: " + e.getMessage());
            }
        }

        // 3. Soporte para archivo .env en la raiz del proyecto
        File envFile = new File(".env");
        if (envFile.exists()) {
            try {
                for (String line : Files.readAllLines(Paths.get(".env"))) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        String[] parts = trimmed.split("=", 2);
                        String key = parts[0].trim().toLowerCase().replace("_", ".");
                        String value = parts[1].trim();
                        properties.setProperty(key, value);
                    }
                }
                System.out.println("[Config] Archivo .env cargado exitosamente.");
            } catch (Exception e) {
                System.err.println("[Config Warning] Error al leer .env: " + e.getMessage());
            }
        }

        loaded = true;
    }

    public static String getDbEngine() {
        return getProperty("db.engine", "H2");
    }

    public static String getDbUrl() {
        return getProperty("db.url", "jdbc:h2:mem:sica;DB_CLOSE_DELAY=-1;MODE=MySQL");
    }

    public static String getDbUser() {
        return getProperty("db.user", "sa");
    }

    public static String getDbPassword() {
        return getProperty("db.password", "");
    }


    private static String getProperty(String key, String defaultValue) {
        // Prioridad: System.getProperty -> properties -> env variable -> defaultValue
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) return sysProp;

        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.trim().isEmpty()) return propVal;

        String envKey = key.toUpperCase().replace(".", "_");
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) return envVal;

        return defaultValue;
    }
}
