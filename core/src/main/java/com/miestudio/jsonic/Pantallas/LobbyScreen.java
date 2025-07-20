package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.UIUtils;

/**
 * Pantalla de Lobby que se muestra después de que un jugador ha elegido ser Host o Cliente.
 * Muestra un color de fondo según el personaje y un botón para iniciar la partida (solo para el Host).
 */
public class LobbyScreen implements Screen {

    private final JuegoSonic game;
    private final Stage stage;
    private final Color playerColor;
    private final boolean isHost;

    /**
     * Constructor para la pantalla de Lobby.
     *
     * @param game La instancia principal del juego.
     * @param playerColor El color que se usará para el fondo.
     * @param isHost True si el jugador es el Host, lo que le permitirá ver el botón de inicio.
     */
    public LobbyScreen(JuegoSonic game, Color playerColor, boolean isHost) {
        this.game = game;
        this.playerColor = playerColor;
        this.isHost = isHost;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        if (isHost) {
            setupHostUI();
        }
    }

    /**
     * Configura la UI adicional que solo el Host puede ver, como el botón de "Iniciar Partida".
     */
    private void setupHostUI() {
        TextButton.TextButtonStyle startStyle = new TextButton.TextButtonStyle();
        startStyle.font = new BitmapFont();
        startStyle.up = UIUtils.createColorDrawable(Color.GREEN.cpy().mul(0.8f));
        TextButton startButton = new TextButton("Iniciar Partida", startStyle);
        startButton.setPosition(Gdx.graphics.getWidth() / 2f - 150, Gdx.graphics.getHeight() / 2f - 50);
        startButton.setSize(300, 100);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("LobbyScreen", "Host iniciando la partida...");
                game.networkManager.startGame();
            }
        });
        stage.addActor(startButton);
    }

    

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(playerColor.r, playerColor.g, playerColor.b, playerColor.a);
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

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}