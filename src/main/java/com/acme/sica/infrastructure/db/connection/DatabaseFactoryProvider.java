package com.acme.sica.infrastructure.db.connection;

import com.acme.sica.infrastructure.config.DatabaseConfig;

import java.sql.Connection;

/**
 * Fabrica Abstracta (Abstract Factory Producer) que determina y provee la Fabrica Concreta de Base de Datos.
 * Implementa resiliencia y conmutacion automatica (Failover) a H2 si MySQL no esta disponible.
 */
public class DatabaseFactoryProvider {

    public enum DatabaseEngine {
        MYSQL,
        H2
    }

    public static ConnectionFactory createConnectionFactory(DatabaseEngine engine) {
        return switch (engine) {
            case MYSQL -> new MySqlConnectionFactory();
            case H2 -> new H2ConnectionFactory();
        };
    }

    public static ConnectionFactory getDefaultConnectionFactory() {
        String dbEngineProperty = DatabaseConfig.getDbEngine().toUpperCase();
        try {
            DatabaseEngine engine = DatabaseEngine.valueOf(dbEngineProperty);
            ConnectionFactory factory = createConnectionFactory(engine);

            // Verificacion preventiva de conexion (Failover Resiliente)
            try (Connection testConn = factory.getConnection()) {
                System.out.println("[DatabaseFactoryProvider] Conexión establecida exitosamente con motor: " + factory.getDatabaseType());
                return factory;
            } catch (Exception e) {
                if (engine == DatabaseEngine.MYSQL) {
                    System.out.println("[DatabaseFactoryProvider Warning] No se pudo conectar a MySQL: " + e.getMessage());
                    System.out.println("[DatabaseFactoryProvider Info] Conmutando automáticamente a H2 en memoria RAM (Zero-Config Mode)...");
                    return new H2ConnectionFactory();
                }
                return factory;
            }
        } catch (Exception e) {
            System.err.println("[AbstractFactory Warning] Motor no reconocido '" + dbEngineProperty + "'. Usando H2 por defecto.");
            return new H2ConnectionFactory();
        }
    }
}

