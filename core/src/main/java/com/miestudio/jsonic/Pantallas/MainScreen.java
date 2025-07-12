package com.miestudio.jsonic.Pantallas;

// Importar paquetes necesarios
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Personajes.BaseCharacter;
import com.miestudio.jsonic.Personajes.SonicCharacter;
import com.miestudio.jsonic.Personajes.TailsCharacter;
import com.miestudio.jsonic.Personajes.KnucklesCharacter;

/**
 * Pantalla de menu principal del juego
 * Muestra diferentes opciones segun si el jugador es host o cliente
 */
public class MainScreen implements Screen {
    /** Referencia al juego principal */
    private final JuegoSonic game;
    /** Escenario para elementos UI */
    private final Stage stage;
    /** Indica si el jugador es host */
    private final boolean isHost;
    /** ID del jugador (0 para host, 1 para J2, 2 para J3) */
    private final int playerId;
    /** Personaje del jugador */
    private BaseCharacter playerCharacter;
    /** Para dibujar formas */
    private ShapeRenderer shapeRenderer;

    /**
     * Constructor de la pantalla de menu
     *
     * @param game Referencia al juego principal
     * @param isHost True si el jugador es host, false si es cliente
     * @param playerId ID del jugador asignado (0 para host, 1 para J2, 2 para J3)
     */
    public MainScreen(JuegoSonic game, boolean isHost, int playerId){
        this.game = game;
        this.isHost = isHost;
        this.playerId = playerId;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        shapeRenderer = new ShapeRenderer();

        // Asignar el personaje según el ID
        if (isHost) {
            playerCharacter = new SonicCharacter(0); // Servidor es Sonic
        } else if (playerId == 1) {
            playerCharacter = new TailsCharacter(1); // Cliente 1 es Tails
        } else if (playerId == 2) {
            playerCharacter = new KnucklesCharacter(2); // Cliente 2 es Knuckles
        } else {
            // Fallback o personaje por defecto
            playerCharacter = new SonicCharacter(-1); // Usar SonicCharacter para el caso de error, con color por defecto
            playerCharacter.setColor(Color.GRAY); // Asignar color gris para el fallback
        }

        setupUI();
    }

    /**
     * Configurar la interfaz de usuario del menu
     */
    private void setupUI(){
        System.out.println("MainScreen loaded. Role: " + (isHost ? "HOST" : "CLIENTE") + ", Player ID: " + playerId);
    }

    /**
     * Metodo principal de renderizado de la pantalla
     *
     * @param delta Tiempo transcurrido desde el último frame (en segundos)
     */
    @Override
    public void render(float delta){
        Color backgroundColor = playerCharacter.getColor();

        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Dibujar el personaje
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        playerCharacter.draw(shapeRenderer, Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 - 50, 100, 100);
        shapeRenderer.end();

        // Actualizar y dibujar la UI
        stage.act(delta);
        stage.draw();
    }

    /**
     * Se llama cuando la pantalla cambia de tamaño
     *
     * @param width Nuevo ancho de pantalla
     * @param height Nuevo alto de pantalla
     */
    @Override
    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
    }

    /**
     * Libera recursos cuando la pantalla es destruida
     */
    @Override
    public void dispose(){
        stage.dispose();
        shapeRenderer.dispose();
    }

    // Metodos no utilizados de la interfaz Screen
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
