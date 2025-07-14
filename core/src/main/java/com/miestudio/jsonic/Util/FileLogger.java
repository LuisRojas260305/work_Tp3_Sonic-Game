package com.miestudio.jsonic.Util;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Una implementación de ApplicationLogger que escribe los logs en un archivo.
 */
public class FileLogger implements ApplicationLogger {

    private final FileHandle file;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FileLogger(String fileName) {
        FileHandle logDir = Gdx.files.absolute(Gdx.files.getExternalStoragePath() + "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        this.file = logDir.child(fileName);
        // Limpiar el archivo de log anterior al iniciar
        file.writeString("", false);
    }

    private void log(String level, String tag, String message) {
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] [%s] %s: %s\n", timestamp, level, tag, message);
        file.writeString(logMessage, true);
    }

    private void log(String level, String tag, String message, Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] [%s] %s: %s\n%s\n", timestamp, level, tag, message, stackTrace);
        file.writeString(logMessage, true);
    }

    @Override
    public void log(String tag, String message) {
        log("INFO", tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        log("INFO", tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        log("ERROR", tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        log("ERROR", tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        log("DEBUG", tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        log("DEBUG", tag, message, exception);
    }
}