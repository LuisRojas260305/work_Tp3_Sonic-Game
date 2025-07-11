package com.miestudio.jsonic.Pantallas;

// Importar paquetes necesarios
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;

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

    /**
     * Constructor de la pantalla de menu
     *
     * @param game Referencia al juego principal
     * @param isHost True si el jugador es host, false si es cliente
     */
    public MainScreen(JuegoSonic game, boolean isHost){
        this.game = game;
        this.isHost = isHost;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        setupUI();
    }

    /**
     * Configurar la interfaz de usuario del menu
     */
    private void setupUI(){
        System.out.println("MainScreen loaded. Role: " + (isHost ? "HOST" : "CLIENTE"));
    }

    /**
     * Metodo principal de renderizado de la pantalla
     *
     * @param delta Tiempo transcurrido desde el último frame (en segundos)
     */
    @Override
    public void render(float delta){
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
    }

    /**
     * Libera recursos cuando la pantalla es destruida
     */
    @Override
    public void dispose(){
        stage.dispose();
    }

    // Metodos no utilizados de la interfaz Screen
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
