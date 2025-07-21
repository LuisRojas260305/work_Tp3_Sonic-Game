package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.UIUtils;

/**
 * Pantalla de menú principal que permite al jugador elegir si desea ser el Host o un Cliente.
 * Presenta tres botones: Jugar, Ayuda y Estadísticas.
 */
public class MainScreen implements Screen {

    /** Referencia a la instancia principal del juego. */
    private final JuegoSonic game;

    /** El escenario donde se colocan los actores de la UI, como los botones. */
    private final Stage stage;

    /**
     * Constructor para la pantalla principal.
     *
     * @param game La instancia principal del juego, necesaria para acceder al NetworkManager.
     */
    public MainScreen(JuegoSonic game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        setupUI();
    }

    /**
     * Configura la interfaz de usuario, creando y posicionando los botones.
     */
    private void setupUI() {
        TextButton.TextButtonStyle buttonStyle = UIUtils.createDefaultButtonStyle();

        // Botón Jugar
        TextButton playButton = new TextButton("Jugar", buttonStyle);
        playButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        playButton.setSize(200, 80);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainScreen", "Botón Jugar presionado. Navegando a RoleSelectionScreen.");
                game.setScreen(new RoleSelectionScreen(game));
            }
        });

        // Botón Ayuda
        TextButton helpButton = new TextButton("Ayuda", buttonStyle);
        helpButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 40);
        helpButton.setSize(200, 80);
        helpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainScreen", "Botón Ayuda presionado.");
                // Lógica para la pantalla de ayuda
            }
        });

        // Botón Estadísticas
        TextButton statsButton = new TextButton("Estadísticas", buttonStyle);
        statsButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 130);
        statsButton.setSize(200, 80);
        statsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainScreen", "Botón Estadísticas presionado.");
                // Lógica para la pantalla de estadísticas
            }
        });

        stage.addActor(playButton);
        stage.addActor(helpButton);
        stage.addActor(statsButton);
    }

    

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}