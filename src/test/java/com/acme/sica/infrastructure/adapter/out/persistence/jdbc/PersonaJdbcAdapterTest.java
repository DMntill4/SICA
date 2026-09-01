package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.model.Persona;
import com.acme.sica.infrastructure.db.connection.ConnectionFactory;
import com.acme.sica.infrastructure.db.connection.H2ConnectionFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonaJdbcAdapterTest {

    private PersonaJdbcAdapter adapter;
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() throws Exception {
        connectionFactory = new H2ConnectionFactory();
        adapter = new PersonaJdbcAdapter(connectionFactory);

        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS persona (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "doc_identidad VARCHAR(50), " +
                    "tipo_documento VARCHAR(20), " +
                    "nombre VARCHAR(100), " +
                    "apellido VARCHAR(100), " +
                    "email VARCHAR(100), " +
                    "telefono VARCHAR(50), " +
                    "empresa_id BIGINT, " +
                    "estado_acceso VARCHAR(20), " +
                    "vector_biometrico CLOB, " +
                    "foto_url VARCHAR(255), " +
                    "creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("DELETE FROM persona");
            stmt.execute("INSERT INTO persona (doc_identidad, tipo_documento, nombre, apellido, email, telefono, estado_acceso) " +
                    "VALUES ('12345678', 'CC', 'Juan', 'Perez', 'juan@example.com', '555-1234', 'HABILITADO')");
        }
    }

    @Test
    void testMapeoMapeaNombreYApellidoCorrectamente() {
        List<Persona> personas = adapter.findAll();

        assertFalse(personas.isEmpty(), "La base de datos debe retornar al menos una persona");
        Persona p = personas.get(0);

        assertEquals("Juan", p.getNombre(), "El campo 'nombre' debe coincidir con el valor de la columna SQL 'nombre'");
        assertEquals("Perez", p.getApellido(), "El campo 'apellido' debe coincidir con el valor de la columna SQL 'apellido'");
    }
}
