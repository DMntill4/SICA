package com.acme.sica.infrastructure.db.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica Concreta de Conexiones para H2 (modo archivo / fallback).
 */
public class H2ConnectionFactory implements ConnectionFactory {

    private static final String JDBC_URL = "jdbc:h2:file:./data/sicadb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de la BD H2", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
