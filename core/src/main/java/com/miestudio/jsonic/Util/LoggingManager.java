package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;

/**
 * Gestor de logging para inicializar y destruir el logger de archivo.
 */
public class LoggingManager {

    private static FileLogger fileLogger;

    public static void initialize(boolean isHost) {
        String fileName = isHost ? "server_debug.log" : "client_debug.log";
        try {
            fileLogger = new FileLogger(fileName);
            Gdx.app.setApplicationLogger(fileLogger);
            Gdx.app.log("LoggingManager", "Logging inicializado en: " + Gdx.files.local(fileName).path());
        } catch (Exception e) {
            System.err.println("Error al inicializar el logger de archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void dispose() {
        if (fileLogger != null) {
            Gdx.app.log("LoggingManager", "Cerrando el logger de archivo.");
            // No hay un método dispose explícito en el logger, ya que FileHandle se gestiona automáticamente.
            // Sin embargo, si tuviéramos un PrintWriter o similar, lo cerraríamos aquí.
            fileLogger = null;
        }
    }
}
