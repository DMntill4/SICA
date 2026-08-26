package com.acme.sica.infrastructure.db.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interfaz Abstract Factory / Provider para la obtencion de conexiones JDBC.
 */
public interface ConnectionFactory {
    Connection getConnection() throws SQLException;
    String getDatabaseType();
}
