package com.acme.sica.infrastructure.adapter.out.persistence.jdbc;

import com.acme.sica.domain.model.Persona;
import com.acme.sica.infrastructure.db.SchemaInitializer;
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

    @BeforeEach
    void setUp() throws Exception {
        ConnectionFactory connectionFactory = new H2ConnectionFactory();
        SchemaInitializer initializer = new SchemaInitializer(connectionFactory);
        initializer.initialize();

        adapter = new PersonaJdbcAdapter(connectionFactory);

        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
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
