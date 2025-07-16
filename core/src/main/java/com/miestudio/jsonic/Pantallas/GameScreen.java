package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.miestudio.jsonic.Actores.Knockles;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.GameState;
import com.miestudio.jsonic.Util.InputState;
import com.miestudio.jsonic.Util.PlayerState;
import com.miestudio.jsonic.Util.Assets;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pantalla principal del juego donde se desarrolla la acción.
 * Se encarga de renderizar el estado del juego y enviar los inputs del jugador.
 */
public class GameScreen implements Screen {

    private final JuegoSonic game;
    private final int localPlayerId;
    private final boolean isHost;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    private final ConcurrentHashMap<Integer, Personajes> characters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, InputState> playerInputs;

    /**
     * Constructor de la pantalla de juego.
     *
     * @param game La instancia principal del juego.
     * @param localPlayerId El ID del jugador local.
     */
    public GameScreen(JuegoSonic game, int localPlayerId) {
        this.game = game;
        this.localPlayerId = localPlayerId;
        this.isHost = (localPlayerId == 0);

        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = new SpriteBatch();

        // Obtener la referencia al mapa de inputs del NetworkManager
        this.playerInputs = game.networkManager.getPlayerInputs();

        initializeCharacters();

        if (isHost) {
            new Thread(this::serverGameLoop).start();
        }
    }

    /**
     * Inicializa las instancias de los personajes para todos los jugadores.
     */
    private void initializeCharacters() {
        Assets assets = game.getAssets(); // Obtener la instancia de Assets
        characters.put(0, new Sonic(0, assets.sonicAtlas));
        characters.put(1, new Tails(1, assets.tailsAtlas));
        characters.put(2, new Knockles(2, assets.knocklesAtlas));
    }

    /**
     * Bucle principal del juego que solo se ejecuta en el servidor (Host).
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
                updateGameState(Gdx_DELTA_TIME);
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
     *
     * @param delta El tiempo transcurrido desde la última actualización.
     */
    private void updateGameState(float delta) {
        for (Personajes character : characters.values()) {
            InputState input = playerInputs.get(character.getPlayerId());
            if (input != null) {
                character.handleInput(input);
            }
            character.update(delta);
        }

        ArrayList<PlayerState> playerStates = new ArrayList<>();
        for (Personajes character : characters.values()) {
            playerStates.add(new PlayerState(character.getPlayerId(), character.getX(), character.getY(), character.isFacingRight(), character.getCurrentAnimationName(), character.getAnimationStateTime()));
        }
        game.networkManager.broadcastGameState(new GameState(playerStates));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isHost) {
            sendInputToServer();
        } else {
            // Si es el host, también procesa sus propios inputs
            InputState hostInput = new InputState();
            hostInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            hostInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            hostInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            hostInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            hostInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            game.networkManager.getPlayerInputs().put(localPlayerId, hostInput);
        }

        updateCharactersFromState();

        Personajes localPlayer = characters.get(localPlayerId);
        if (localPlayer != null) {
            camera.position.set(localPlayer.getX(), localPlayer.getY(), 0);
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        for (Personajes character : characters.values()) {
            // Asegurarse de que la animación se actualice en el cliente también
            character.update(delta); 
            TextureRegion frame = character.getCurrentFrame();
            // Voltear la textura si el personaje no está mirando a la derecha
            if (!character.isFacingRight() && !frame.isFlipX()) {
                frame.flip(true, false);
            } else if (character.isFacingRight() && frame.isFlipX()) {
                frame.flip(true, false);
            }
            batch.draw(frame, character.getX(), character.getY());
        }
        batch.end();
    }

    /**
     * Envía el estado de los inputs del cliente al servidor.
     */
    private void sendInputToServer() {
        InputState input = new InputState();
        input.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
        input.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
        input.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
        input.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
        input.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
        game.networkManager.sendInputState(input);
    }

    /**
     * Actualiza las posiciones de los personajes en el cliente según el GameState recibido.
     */
    private void updateCharactersFromState() {
        GameState gameState = game.networkManager.getCurrentGameState();
        if (gameState != null) {
            for (PlayerState playerState : gameState.getPlayers()) {
                Personajes character = characters.get(playerState.getPlayerId());
                if (character != null) {
                    character.setPosition(playerState.getX(), playerState.getY());
                    character.setFacingRight(playerState.isFacingRight());
                    character.setAnimationByName(playerState.getCurrentAnimationName());
                    character.setAnimationStateTime(playerState.getAnimationStateTime());
                }
            }
        }
    }

    public ConcurrentHashMap<Integer, InputState> getPlayerInputs() {
        return playerInputs;
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (Personajes character : characters.values()) {
            character.dispose();
        }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}