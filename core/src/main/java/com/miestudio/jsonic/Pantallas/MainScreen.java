package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;

/**
 * Pantalla de selección de rol.
 * Permite al jugador elegir si desea ser Host o Cliente.
 */
public class MainScreen implements Screen {

    private final JuegoSonic game;
    private final Stage stage;

    /**
     * Constructor para la pantalla de selección de rol.
     * @param game Referencia al juego principal.
     */
    public MainScreen(JuegoSonic game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        setupUI();
    }

    /**
     * Configura la interfaz de usuario con los botones de Host y Cliente.
     */
    private void setupUI() {
        System.out.println("Debug: Cargando pantalla de menú principal (MainScreen).");

        // Estilo base para los botones
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.getAssets().defaultFont; // Usar la fuente generada
        buttonStyle.up = createColorDrawable(Color.BLUE);
        buttonStyle.down = createColorDrawable(Color.DARK_GRAY);
        buttonStyle.fontColor = Color.WHITE;

        // Botón Jugar
        TextButton playButton = new TextButton("Jugar", buttonStyle);
        playButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        playButton.setSize(200, 80);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Debug: Botón Jugar presionado. Iniciando NetworkManager.");
                // Aquí se inicia el proceso de red para determinar si es host o cliente
                game.networkManager.checkNetworkStatus();
            }
        });

        // Botón Ayuda
        TextButton helpButton = new TextButton("Ayuda", buttonStyle);
        helpButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 40);
        helpButton.setSize(200, 80);
        helpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Debug: Botón Ayuda presionado.");
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
                System.out.println("Debug: Botón Estadísticas presionado.");
                // Lógica para la pantalla de estadísticas
            }
        });

        stage.addActor(playButton);
        stage.addActor(helpButton);
        stage.addActor(statsButton);
    }

    /**
     * Crea un drawable de un color sólido.
     * @param color El color para el drawable.
     * @return Un drawable del color especificado.
     */
    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        com.badlogic.gdx.scenes.scene2d.utils.Drawable drawable = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
        return drawable;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1); // Fondo gris oscuro
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

    // Métodos no utilizados de la interfaz Screen
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}