package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.miestudio.jsonic.Actores.Knockles;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Actores.Personajes.AnimationType;
import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.Assets;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.GameState;
import com.miestudio.jsonic.Util.InputState;
import com.miestudio.jsonic.Util.PlayerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pantalla principal del juego donde se desarrolla la acción.
 * Se encarga de renderizar el estado del juego, gestionar los inputs del jugador,
 * y sincronizar el estado entre el host y los clientes.
 */
public class GameScreen implements Screen {

    private final JuegoSonic game;
    private final int localPlayerId;
    private final boolean isHost;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private final ConcurrentHashMap<Integer, Personajes> characters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, InputState> playerInputs;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CollisionManager collisionManager;

    private float mapWidth, mapHeight; // Dimensiones del mapa

    /**
     * Constructor de la pantalla de juego.
     *
     * @param game La instancia principal del juego.
     * @param localPlayerId El ID del jugador local (0 para el host, >0 para clientes).
     */
    public GameScreen(JuegoSonic game, int localPlayerId) {
        this.game = game;
        this.localPlayerId = localPlayerId;
        this.isHost = (localPlayerId == 0);

        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

        this.playerInputs = game.networkManager.getPlayerInputs();

        // Cargar el mapa
        map = new TmxMapLoader().load(Constantes.MAPA_PATH + "Mapa.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        // Obtener tamaño del mapa
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        mapWidth = layer.getWidth() * layer.getTileWidth();
        mapHeight = layer.getHeight() * layer.getTileHeight();

        // Configurar cámara
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        // Crear sistema de colisiones
        collisionManager = new CollisionManager(map, "Colisiones", mapWidth, mapHeight);
        collisionManager.addTileCollisions(map, "Colisiones");

        // Inicializar personajes
        initializeCharacters();

        if (isHost) {
            new Thread(this::serverGameLoop).start();
        }
    }

    /**
     * Inicializa las instancias de los personajes para todos los jugadores.
     * Asigna un personaje a cada ID de jugador y establece sus posiciones iniciales.
     */
    private void initializeCharacters() {
        Assets assets = game.getAssets();

        characters.put(0, new Sonic(0, assets.sonicAtlas));
        characters.put(1, new Tails(1, assets.tailsAtlas));
        characters.put(2, new Knockles(2, assets.knocklesAtlas));

        // Establecer posiciones de spawn
        Map<String, Vector2> spawnPoints = findSpawnPoints();

        // Sonic
        Personajes sonic = characters.get(0);
        Vector2 sonicSpawn = spawnPoints.getOrDefault("Sonic", new Vector2(mapWidth * 0.1f, mapHeight * 0.5f));
        float groundYSonic = collisionManager.getGroundY(new Rectangle(sonicSpawn.x, sonicSpawn.y, sonic.getWidth(), sonic.getHeight()));
        sonic.setPosition(sonicSpawn.x, groundYSonic >= 0 ? groundYSonic : sonicSpawn.y);
        sonic.setPreviousPosition(sonic.getX(), sonic.getY());

        // Tails
        Personajes tails = characters.get(1);
        Vector2 tailsSpawn = spawnPoints.getOrDefault("Tails", new Vector2(mapWidth * 0.2f, mapHeight * 0.5f));
        float groundYTails = collisionManager.getGroundY(new Rectangle(tailsSpawn.x, tailsSpawn.y, tails.getWidth(), tails.getHeight()));
        tails.setPosition(tailsSpawn.x, groundYTails >= 0 ? groundYTails : tailsSpawn.y);
        tails.setPreviousPosition(tails.getX(), tails.getY());

        // Knuckles
        Personajes knuckles = characters.get(2);
        Vector2 knucklesSpawn = spawnPoints.getOrDefault("Knuckles", new Vector2(mapWidth * 0.3f, mapHeight * 0.5f));
        float groundYKnuckles = collisionManager.getGroundY(new Rectangle(knucklesSpawn.x, knucklesSpawn.y, knuckles.getWidth(), knuckles.getHeight()));
        knuckles.setPosition(knucklesSpawn.x, groundYKnuckles >= 0 ? groundYKnuckles : knucklesSpawn.y);
        knuckles.setPreviousPosition(knuckles.getX(), knuckles.getY());
    }

    /**
     * Encuentra los puntos de spawn de los jugadores en la capa "SpawnJugadores" del mapa.
     * Los puntos de spawn se definen como tiles con la propiedad "Spawn" establecida a true,
     * y una propiedad adicional ("Sonic", "Tails", "Knuckles") para identificar al personaje.
     * @return Un mapa donde la clave es el nombre del personaje y el valor es su posición de spawn (Vector2).
     */
    private Map<String, Vector2> findSpawnPoints() {
        Map<String, Vector2> spawnPoints = new HashMap<>();
        MapLayer layer = map.getLayers().get("SpawnJugadores");

        if (layer == null || !(layer instanceof TiledMapTileLayer)) {
            Gdx.app.error("GameScreen", "No se encontró la capa de tiles 'SpawnJugadores'.");
            return spawnPoints;
        }

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
        float tileWidth = tileLayer.getTileWidth();
        float tileHeight = tileLayer.getTileHeight();

        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                com.badlogic.gdx.maps.MapProperties properties = cell.getTile().getProperties();
                if (properties.get("Spawn", false, Boolean.class)) {
                    String characterName = null;
                    if (properties.get("Sonic", false, Boolean.class)) {
                        characterName = "Sonic";
                    } else if (properties.get("Tails", false, Boolean.class)) {
                        characterName = "Tails";
                    } else if (properties.get("Knuckles", false, Boolean.class)) {
                        characterName = "Knuckles";
                    }

                    if (characterName != null) {
                        float spawnX = x * tileWidth;
                        float spawnY = y * tileHeight;
                        spawnPoints.put(characterName, new Vector2(spawnX, spawnY));
                        Gdx.app.log("GameScreen", "Spawn encontrado para " + characterName + " en (" + spawnX + ", " + spawnY + ")");
                    }
                }
            }
        }
        return spawnPoints;
    }

    /**
     * Bucle principal del juego que se ejecuta en un hilo separado solo en el servidor (Host).
     * Se encarga de actualizar el estado del juego a una tasa fija (60 FPS).
     */
    private void serverGameLoop() {
        float accumulator = 0f;
        long lastTime = System.nanoTime();
        final float Gdx_DELTA_TIME = 1 / 60f;

        while (isHost) {
            long now = System.nanoTime();
            accumulator += (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            while (accumulator >= Gdx_DELTA_TIME) {
                synchronized (characters) {
                    updateGameState(Gdx_DELTA_TIME);
                }
                accumulator -= Gdx_DELTA_TIME;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Actualiza el estado del juego en el servidor.
     * Procesa los inputs de los jugadores, actualiza la física de los personajes
     * y envía el estado actualizado a todos los clientes.
     *
     * @param delta El tiempo transcurrido desde la última actualización en segundos.
     */
    private void updateGameState(float delta) {
        for (Personajes character : characters.values()) {
            InputState input = playerInputs.get(character.getPlayerId());

            if (input != null) {
                // Guardar posición anterior para interpolación
                character.setPreviousPosition(character.getX(), character.getY());

                // Actualizar física básica
                character.update(delta, collisionManager);

                // Manejar input con colisiones
                character.handleInput(input, collisionManager, delta);

                // Limitar personajes dentro del mapa
                float newX = Math.max(0, Math.min(character.getX(), mapWidth - character.getWidth()));
                float newY = Math.max(0, Math.min(character.getY(), mapHeight - character.getHeight()));
                character.setPosition(newX, newY);
            }
        }

        // Enviar estado actualizado a todos los clientes
        ArrayList<PlayerState> playerStates = new ArrayList<>();
        for (Personajes character : characters.values()) {
            playerStates.add(new PlayerState(
                character.getPlayerId(),
                character.getX(), character.getY(),
                character.isFacingRight(),
                character.getCurrentAnimationName(),
                character.getAnimationStateTime()));
        }
        game.networkManager.broadcastUdpGameState(new GameState(playerStates));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Obtener jugador local sincronizado
        Personajes localPlayer;
        synchronized (characters) {
            localPlayer = characters.get(localPlayerId);
        }

        // Actualizar cámara
        if (localPlayer != null) {
            // Centrar cámara en el jugador local
            float cameraHalfWidth = camera.viewportWidth / 2;
            float cameraHalfHeight = camera.viewportHeight / 2;

            float cameraX = Math.max(cameraHalfWidth,
                Math.min(localPlayer.getX(), mapWidth - cameraHalfWidth));
            float cameraY = Math.max(cameraHalfHeight,
                Math.min(localPlayer.getY(), mapHeight - cameraHalfHeight));

            // Usar coordenadas enteras para evitar artefactos visuales
            camera.position.set((int)cameraX, (int)cameraY, 0);
        }
        camera.update();

        // Renderizar mapa
        mapRenderer.setView(camera);
        mapRenderer.render();

        // --- Lógica de Sincronización y Predicción ---
        processInput(delta); // Mover la lógica de input a un método separado

        // --- Renderizado ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        synchronized (characters) {
            for (Personajes character : characters.values()) {
                // En el cliente, el jugador local ya se actualizó. Los remotos se actualizan por interpolación.
                // El stateTime de los remotos se actualiza en updateCharactersFromState.

                TextureRegion frame = character.getCurrentFrame();

                if (!character.isFacingRight() && !frame.isFlipX()) {
                    frame.flip(true, false);
                } else if (character.isFacingRight() && frame.isFlipX()) {
                    frame.flip(true, false);
                }

                batch.draw(frame, character.getX(), character.getY());
            }
        }
        batch.end();

        // Renderizar colisiones para debug (opcional)
        // debugRenderCollisions();
    }

    /**
     * Procesa la entrada del usuario, ya sea para el host (registrando el input) o para el cliente
     * (enviando el input al servidor y realizando predicción local).
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     */
    private void processInput(float delta) {
        if (isHost) {
            // El Host solo necesita registrar su propio input para que el serverGameLoop lo procese.
            InputState hostInput = new InputState();
            hostInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            hostInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            hostInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            hostInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            hostInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            hostInput.setPlayerId(localPlayerId);
            playerInputs.put(localPlayerId, hostInput);
        } else {
            // El Cliente realiza la predicción local y envía su input al servidor.
            InputState localInput = new InputState();
            localInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            localInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            localInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            localInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            localInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            localInput.setPlayerId(localPlayerId);
            game.networkManager.sendInputState(localInput);

            // Predicción del lado del cliente: aplica la física y el input al jugador local.
            Personajes localPlayer = characters.get(localPlayerId);
            if (localPlayer != null) {
                localPlayer.update(delta, collisionManager); // Aplica gravedad y actualiza stateTime
                localPlayer.handleInput(localInput, collisionManager, delta); // Aplica movimiento del input
            }

            // Actualiza los demás personajes basándose en el estado del servidor (interpolación).
            updateCharactersFromState();
        }
    }

    /**
     * Renderiza las formas de colisión para depuración visual.
     * Este método es opcional y solo debe usarse para fines de depuración.
     */
    private void debugRenderCollisions(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Crear copia para evitar problemas de concurrencia
        Array<Shape2D> shapesCopy = new Array<>();
        shapesCopy.addAll(collisionManager.getCollisionShapes());

        for (Shape2D shape : shapesCopy) {
            if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        shapeRenderer.end();
    }

    /**
     * Envía el estado de los inputs del cliente al servidor.
     * @deprecated Este método es redundante ya que la lógica de envío de input
     * ha sido integrada en {@link #processInput(float)}.
     */
    private void sendInputToServer() {
        InputState input = new InputState();
        input.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
        input.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
        input.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
        input.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
        input.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
        input.setPlayerId(localPlayerId); // Añadir el ID del jugador
        game.networkManager.sendInputState(input);
    }

    /**
     * Actualiza las posiciones y estados de los personajes en el cliente según el GameState recibido del servidor.
     * Realiza reconciliación para el jugador local y interpolación para los jugadores remotos.
     */
    private void updateCharactersFromState() {
        GameState gameState = game.networkManager.getCurrentGameState();
        if (gameState != null) {
            synchronized (characters) {
                for (PlayerState playerState : gameState.getPlayers()) {
                    Personajes character = characters.get(playerState.getPlayerId());
                    if (character == null) continue;

                    if (playerState.getPlayerId() == localPlayerId) {
                        // Reconciliación para el jugador local
                        float errorMargin = 0.5f; // Pequeño margen de error
                        if (Vector2.dst(character.getX(), character.getY(), playerState.getX(), playerState.getY()) > errorMargin) {
                            // Corrección suave si la predicción fue muy diferente
                            character.setPosition(playerState.getX(), playerState.getY());
                        }
                        // El resto de estados (animación, dirección) se actualizan directamente.
                        character.setFacingRight(playerState.isFacingRight());
                        character.setAnimation(Personajes.AnimationType.valueOf(playerState.getCurrentAnimationName().toUpperCase()));

                    } else {
                        // Interpolación para los otros jugadores
                        float interpolationFactor = 0.2f; // Ajusta este valor para un movimiento más suave o más rápido
                        character.setPosition(
                            character.getX() + (playerState.getX() - character.getX()) * interpolationFactor,
                            character.getY() + (playerState.getY() - character.getY()) * interpolationFactor
                        );
                        character.setFacingRight(playerState.isFacingRight());
                        character.setAnimation(Personajes.AnimationType.valueOf(playerState.getCurrentAnimationName().toUpperCase()));
                        character.setAnimationStateTime(
                            character.getAnimationStateTime() + (playerState.getAnimationStateTime() - character.getAnimationStateTime()) * interpolationFactor
                        );
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // Mantener relación de aspecto del mapa
        float aspectRatio = mapWidth / mapHeight;
        float viewportWidth = width;
        float viewportHeight = width / aspectRatio;

        if (viewportHeight > height) {
            viewportHeight = height;
            viewportWidth = height * aspectRatio;
        }

        // Centrar la cámara
        camera.viewportWidth = viewportWidth;
        camera.viewportHeight = viewportHeight;
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        mapRenderer.dispose();
        for (Personajes character : characters.values()) {
            character.dispose();
        }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

