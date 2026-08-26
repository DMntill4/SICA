package com.acme.sica.infrastructure.db.connection;

import com.acme.sica.infrastructure.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica Concreta de Conexiones para MySQL.
 * Lee las credenciales automaticamente desde DatabaseConfig (config.properties / .env).
 */
public class MySqlConnectionFactory implements ConnectionFactory {

    private final String url;
    private final String user;
    private final String password;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontro el driver JDBC de MySQL en el classpath", e);
        }
    }

    public MySqlConnectionFactory() {
        this(
            DatabaseConfig.getDbUrl(),
            DatabaseConfig.getDbUser(),
            DatabaseConfig.getDbPassword()
        );
    }

    public MySqlConnectionFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public String getDatabaseType() {
        return "MYSQL";
    }
}
