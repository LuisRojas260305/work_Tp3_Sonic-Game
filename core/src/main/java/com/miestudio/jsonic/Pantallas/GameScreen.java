package com.miestudio.jsonic.Pantallas;

// Importar paquetes
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;

/**
 * Pantalla principal del juego donde ocurre la accion
 * Actualmente muestra solo un indicador de rol (host/cliente)
 */
public class GameScreen implements Screen{
    /** Atributos */

    /** Referencia al juego principal */
    private final JuegoSonic game;
    /** Cámara para la vista del juego */
    private final OrthographicCamera camera;
    /** Batch para renderizado de sprites */
    private final SpriteBatch batch;
    /** Escenario para elementos UI */
    private final Stage stage;
    /** Indica si el jugador es host */
    private final boolean isHost;

    /**
     * Constructor de la pantalla de juego
     *
     * @param game Referencia al juego principal
     * @param isHost True si el jugador es host, false si es cliente
     */
    public GameScreen(JuegoSonic game, boolean isHost){
        this.game = game;
        this.isHost = isHost;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());

        System.out.println("GameScreen loaded. Role: " + (isHost ? "HOST" : "CLIENTE"));
    }

    /**
     * Configura elementos basicos de UI para la pantalla de juego
     */
    private void setupUI(){
    }

    /**
     * Metodo principal de renderizado del juego
     *
     * @param delta Tiempo transcurrido desde el último frame (en segundos)
     */
    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0,0,0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar la camara
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Renderizado del juego (aqui va la logica del juego)
        batch.begin();
        // TODO: Dibujar elementos del juego
        batch.end();

        // Renderizado de UI
        stage.act(delta);
        stage.draw();
    }

    /**
     * Libera recursos cuando la pantalla es destruida
     */
    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }

    /**
     * Se llama cuando la pantalla cambia de tamaño.
     *
     * @param width Nuevo ancho de pantalla
     * @param height Nuevo alto de pantalla
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        camera.setToOrtho(false, width, height);
    }

    // Métodos no utilizados de la interfaz Screen
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

}
