package com.acme.sica.infrastructure.db;

import com.acme.sica.infrastructure.db.connection.ConnectionFactory;
import com.acme.sica.infrastructure.db.connection.H2ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SchemaInitializerTest {

    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        connectionFactory = new H2ConnectionFactory();
    }

    @Test
    void testInicializacionCreaTodasLasTablasYDatosSemilla() throws Exception {
        SchemaInitializer initializer = new SchemaInitializer(connectionFactory);
        initializer.initialize();

        Set<String> tablasExistentes = new HashSet<>();
        try (Connection conn = connectionFactory.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tablasExistentes.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        assertTrue(tablasExistentes.contains("usuario"), "La tabla SQL 'usuario' debe existir");
        assertTrue(tablasExistentes.contains("persona"), "La tabla SQL 'persona' debe existir");
        assertTrue(tablasExistentes.contains("visita"), "La tabla SQL 'visita' debe existir");
        assertTrue(tablasExistentes.contains("solicitud_pase"), "La tabla SQL 'solicitud_pase' debe existir");
        assertTrue(tablasExistentes.contains("incidente"), "La tabla SQL 'incidente' debe existir");
        assertTrue(tablasExistentes.contains("empresa"), "La tabla SQL 'empresa' debe existir");
        assertTrue(tablasExistentes.contains("punto_acceso"), "La tabla SQL 'punto_acceso' debe existir");
        assertTrue(tablasExistentes.contains("rol"), "La tabla SQL 'rol' debe existir");
        assertTrue(tablasExistentes.contains("permiso"), "La tabla SQL 'permiso' debe existir");
        assertTrue(tablasExistentes.contains("bitacora_auditoria"), "La tabla SQL 'bitacora_auditoria' debe existir");
    }
}
