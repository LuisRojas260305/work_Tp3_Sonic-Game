package com.miestudio.jsonic.Utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Clase para gestionar la carga y descarga de todos los assets del juego.
 * Centraliza la gestión de recursos para una mejor organización y mantenimiento.
 */
public class Recursos {

    // Atlas de texturas para los personajes
    public TextureAtlas sonicAtlas;
    public TextureAtlas tailsAtlas;
    public TextureAtlas knocklesAtlas;

    // Texturas para los objetos de basura
    public Array<TextureRegion> texturasBasura;

    /**
     * Carga todos los assets del juego.
     * Este método debe ser llamado al inicio de la aplicación.
     */
    public void cargar() {
        try {
            sonicAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "SonicAtlas.txt"));
            tailsAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "TailsAtlas.txt"));
            knocklesAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "KnocklesAtlas.txt"));
            Gdx.app.log("Recursos", "Todos los TextureAtlas cargados correctamente.");
        } catch (Exception e) {
            Gdx.app.error("Recursos", "Error al cargar TextureAtlas: " + e.getMessage());
        }

        try {
            texturasBasura = new Array<>();
            for (int i = 1; i <= 6; i++) {
                String path = "Objetos/Sprite-000" + i + ".png";
                Texture textura = new Texture(Gdx.files.internal(path));
                texturasBasura.add(new TextureRegion(textura));
            }
            Gdx.app.log("Recursos", "Texturas de basura cargadas correctamente.");
        } catch (Exception e) {
            Gdx.app.error("Recursos", "Error al cargar las texturas de basura: " + e.getMessage());
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
        Gdx.app.log("Recursos", "Todos los TextureAtlas liberados.");

        if (texturasBasura != null) {
            for (TextureRegion region : texturasBasura) {
                region.getTexture().dispose();
            }
            Gdx.app.log("Recursos", "Texturas de basura liberadas.");
        }
    }
}
