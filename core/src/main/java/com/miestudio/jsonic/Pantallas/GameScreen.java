package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.TiledCollisionHelper;

/**
 * Pantalla principal del juego donde se desarrolla la acción.
 * Se encarga de renderizar el estado del juego y enviar los inputs del jugador.
 */
public class GameScreen implements Screen {

    private final JuegoSonic game;
    private final int localPlayerId; // Mantener para futura referencia de qué personaje es el local
    private final boolean isHost;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap tiledMap;

    // Box2D
    private World world;
    private Box2DDebugRenderer debugRenderer;

    /**
     * Constructor de la pantalla de juego.
     *
     * @param game La instancia principal del juego.
     * @param localPlayerId El ID del jugador local (0 para host, 1,2 para clientes).
     */
    public GameScreen(JuegoSonic game, int localPlayerId) {
        Gdx.app.log("GameScreen", "Iniciando GameScreen para Player ID: " + localPlayerId + ", isHost: " + (localPlayerId == 0));
        this.game = game;
        this.localPlayerId = localPlayerId;
        this.isHost = (localPlayerId == 0);

        this.camera = new OrthographicCamera();
        this.batch = new SpriteBatch();

        // Cargar mapa
        tiledMap = game.getAssets().tiledMap;
        if (tiledMap != null){
            mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / Constantes.PPM);
            Gdx.app.log("GameScreen", "Mapa Tiled cargado y renderer inicializado.");
        } else {
            Gdx.app.error("GameScreen", "Error: TiledMap es nulo.");
        }

        if(isHost){
            world = new World(new Vector2(0, -9.8f), true); // Gravedad hacia abajo
            debugRenderer = new Box2DDebugRenderer();
            Gdx.app.log("GameScreen", "Mundo Box2D y debugRenderer inicializados.");
            TiledCollisionHelper.parseTiledCollisionLayer(world, tiledMap);
            Gdx.app.log("GameScreen", "Colisiones del mapa Box2D parseadas.");
        }

        // Configurar cámara para ver todo el mapa
        if (tiledMap != null) {
            float mapWidth = tiledMap.getProperties().get("width", Integer.class) * tiledMap.getProperties().get("tilewidth", Integer.class) / Constantes.PPM;
            float mapHeight = tiledMap.getProperties().get("height", Integer.class) * tiledMap.getProperties().get("tileheight", Integer.class) / Constantes.PPM;
            camera.setToOrtho(false, mapWidth, mapHeight);
            camera.position.set(mapWidth / 2, mapHeight / 2, 0); // Centrar cámara en el mapa
            camera.update();
            Gdx.app.log("GameScreen", "Cámara configurada para ver el mapa. Ancho: " + mapWidth + ", Alto: " + mapHeight);
        } else {
            Gdx.app.error("GameScreen", "No se pudo configurar la cámara: TiledMap es nulo.");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.app.log("GameScreen", "Iniciando renderizado. Delta: " + delta);
        // Limpiar pantalla
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar mundo Box2D (solo en el host)
        if (isHost && world != null) {
            world.step(1 / 60f, 6, 2);
            Gdx.app.log("GameScreen", "Mundo Box2D actualizado.");
        }

        // Renderizar mapa
        if (mapRenderer != null){
            mapRenderer.setView(camera);
            mapRenderer.render();
            Gdx.app.log("GameScreen", "Mapa renderizado.");
        } else {
            Gdx.app.error("GameScreen", "No se pudo renderizar el mapa: mapRenderer es nulo.");
        }

        // Renderizar debug de Box2D (solo en el host)
        if(isHost && debugRenderer != null && world != null){
            debugRenderer.render(world, camera.combined);
            Gdx.app.log("GameScreen", "Debug de Box2D renderizado.");
        } else if (isHost) {
            Gdx.app.log("GameScreen", "Debug de Box2D no renderizado: debugRenderer o world es nulo.");
        }
        Gdx.app.log("GameScreen", "Fin de renderizado.");
    }

    @Override
    public void resize(int width, int height) {
        if (tiledMap != null) {
            float mapWidth = tiledMap.getProperties().get("width", Integer.class) * tiledMap.getProperties().get("tilewidth", Integer.class) / Constantes.PPM;
            float mapHeight = tiledMap.getProperties().get("height", Integer.class) * tiledMap.getProperties().get("tileheight", Integer.class) / Constantes.PPM;

            float aspectRatio = (float) width / height;
            float viewportWidth = mapWidth;
            float viewportHeight = mapWidth / aspectRatio;

            if (viewportHeight > mapHeight) {
                viewportHeight = mapHeight;
                viewportWidth = mapHeight * aspectRatio;
            }
            camera.setToOrtho(false, viewportWidth, viewportHeight);
            camera.position.set(mapWidth / 2, mapHeight / 2, 0); // Recentrar cámara
            camera.update();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if(world != null) world.dispose();
        if(debugRenderer != null) debugRenderer.dispose();
    }

    // Métodos no utilizados de la interfaz Screen
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}