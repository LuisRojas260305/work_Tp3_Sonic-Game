package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Clase para gestionar la carga y descarga de todos los assets del juego.
 * Centraliza la gestión de recursos para una mejor organización y mantenimiento.
 */
public class Assets {

    // Atlas de texturas para los personajes
    public TextureAtlas sonicAtlas;
    public TextureAtlas tailsAtlas;
    public TextureAtlas knocklesAtlas;

    /**
     * Carga todos los assets del juego.
     * Este método debe ser llamado al inicio de la aplicación.
     */
    public void load() {
        try {
            sonicAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "SonicAtlas.txt"));
            tailsAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "TailsAtlas.txt"));
            knocklesAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "KnocklesAtlas.txt"));
            Gdx.app.log("Assets", "Todos los TextureAtlas cargados correctamente.");
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error al cargar TextureAtlas: " + e.getMessage());
            // Aquí podrías manejar el error de forma más robusta, como cargar assets de fallback
        }
    }

    /**
     * Libera todos los assets cargados.
     * Este método debe ser llamado al finalizar la aplicación para evitar fugas de memoria.
     */
    public void dispose() {
        if (sonicAtlas != null) sonicAtlas.dispose();
        if (tailsAtlas != null) tailsAtlas.dispose();
        if (knocklesAtlas != null) knocklesAtlas.dispose();
        Gdx.app.log("Assets", "Todos los TextureAtlas liberados.");
    }
}