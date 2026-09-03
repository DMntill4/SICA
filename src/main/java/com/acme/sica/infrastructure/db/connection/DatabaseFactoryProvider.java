package com.acme.sica.infrastructure.db.connection;

import com.acme.sica.infrastructure.config.DatabaseConfig;

/**
 * Fabrica Abstracta (Abstract Factory Producer) que determina y provee la Fabrica Concreta de Base de Datos.
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
            return createConnectionFactory(engine);
        } catch (IllegalArgumentException e) {
            System.err.println("[AbstractFactory Warning] Motor no reconocido '" + dbEngineProperty + "'. Usando H2 por defecto.");
            return new H2ConnectionFactory();
        }
    }
}
