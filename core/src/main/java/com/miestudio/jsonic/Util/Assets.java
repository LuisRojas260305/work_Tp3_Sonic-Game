package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class Assets {
    public final AssetManager manager = new AssetManager();
    public TiledMap tiledMap;
    public TextureAtlas sonicAtlas;
    public TextureAtlas tailsAtlas;
    public TextureAtlas knocklesAtlas;

    public void load() {
        Gdx.app.log("Assets", "Iniciando carga de TextureAtlas...");
        // Cargar TextureAtlas
        manager.load("Personajes/SonicAtlas.txt", TextureAtlas.class);
        manager.load("Personajes/TailsAtlas.txt", TextureAtlas.class);
        manager.load("Personajes/KnocklesAtlas.txt", TextureAtlas.class);

        // Cargar TiledMap
        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load("Mapas/Mapa.tmx", TiledMap.class);

        manager.finishLoading(); // Bloquea hasta que todos los assets estén cargados

        // Obtener los assets cargados
        sonicAtlas = manager.get("Personajes/SonicAtlas.txt", TextureAtlas.class);
        tailsAtlas = manager.get("Personajes/TailsAtlas.txt", TextureAtlas.class);
        knocklesAtlas = manager.get("Personajes/KnocklesAtlas.txt", TextureAtlas.class);
        tiledMap = manager.get("Mapas/Mapa.tmx", TiledMap.class);

        Gdx.app.log("Assets", "Todos los TextureAtlas y TiledMap cargados correctamente.");
    }

    public void dispose() {
        manager.dispose();
    }
}
