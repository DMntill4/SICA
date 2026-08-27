package com.acme.sica.infrastructure.adapter.in.gui;

import com.acme.sica.SicaApplication;
import com.acme.sica.infrastructure.adapter.in.gui.client.SicaApiClient;
import com.acme.sica.infrastructure.adapter.in.gui.views.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

/**
 * Punto de entrada principal de la Interfaz Grafica de Escritorio (GUI) en Java Swing + FlatLaf.
 */
public class SicaGuiMain {

    public static void main(String[] args) {
        // 1. Configurar Look & Feel Oscuro Moderno (FlatLaf)
        FlatDarkLaf.setup();

        // 2. Levantar el Backend HTTP REST de SICA en un hilo en segundo plano si no esta corriendo
        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("[GUI Launcher] Iniciando Servidor Backend SICA en http://localhost:8080...");
                SicaApplication.main(new String[]{});
            } catch (Exception e) {
                System.err.println("[GUI Launcher Warning] El servidor HTTP ya esta en ejecucion o se lanzo previamente.");
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Esperar 1 segundo para asegurar que el HttpServer este escuchando en el puerto 8080
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // 3. Crear Cliente HTTP REST y lanzar la ventana de Login en el hilo de eventos EDT de Swing
        SicaApiClient apiClient = new SicaApiClient();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(apiClient);
            loginFrame.setVisible(true);
        });
    }
}
