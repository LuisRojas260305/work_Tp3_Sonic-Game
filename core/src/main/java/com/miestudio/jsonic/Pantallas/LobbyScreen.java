package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;

public class LobbyScreen implements Screen {

    private final JuegoSonic game;
    private final Stage stage;
    private final Color playerColor;
    private final boolean isHost;
    private Label statusLabel;

    public LobbyScreen(JuegoSonic game, Color playerColor, boolean isHost) {
        this.game = game;
        this.playerColor = playerColor;
        this.isHost = isHost;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        setupUI();
    }

    private void setupUI() {
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Lobby de Juego", new Label.LabelStyle(game.getAssets().defaultFont, Color.WHITE));
        titleLabel.setFontScale(2.0f);
        table.add(titleLabel).padBottom(50).row();

        statusLabel = new Label("", new Label.LabelStyle(game.getAssets().defaultFont, Color.WHITE));
        statusLabel.setFontScale(1.2f);
        table.add(statusLabel).padBottom(20).row();

        if (isHost) {
            statusLabel.setText("Esperando jugadores... (Host)");
            
            // Crear estilo para el botón de Iniciar Partida
            TextButton.TextButtonStyle startButtonStyle = new TextButton.TextButtonStyle();
            startButtonStyle.font = game.getAssets().defaultFont; // Usar la fuente generada
            startButtonStyle.up = skin.getDrawable("default-round"); // Usar un drawable del skin
            startButtonStyle.down = skin.getDrawable("default-round-down"); // Usar un drawable del skin
            startButtonStyle.fontColor = Color.WHITE;

            // Usar un TextButton en lugar de una Label para la acción
            TextButton startButton = new TextButton("Iniciar Partida", startButtonStyle);
            startButton.getLabel().setFontScale(1.5f);
            startButton.addListener(new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    game.networkManager.startGame();
                }
            });
            table.add(startButton).padTop(50).width(300).height(60).row();
        } else {
            statusLabel.setText("Esperando al Host para iniciar la partida...");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(playerColor.r * 0.5f, playerColor.g * 0.5f, playerColor.b * 0.5f, 1); // Fondo más oscuro
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
    public void show() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }
}
