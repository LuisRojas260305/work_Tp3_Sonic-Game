package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Server.CharacterSelectionListener;
import com.miestudio.jsonic.Util.UIUtils;

import java.util.List;

public class CharacterSelectionScreen implements Screen, CharacterSelectionListener {

    private final JuegoSonic game;
    private final Stage stage;
    private TextButton sonicButton, tailsButton, knucklesButton;

    public CharacterSelectionScreen(JuegoSonic game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        setupUI();
        game.networkManager.setCharacterSelectionListener(this);
    }

    private void setupUI() {
        TextButton.TextButtonStyle sonicStyle = UIUtils.createDefaultButtonStyle(Color.BLUE);
        TextButton.TextButtonStyle tailsStyle = UIUtils.createDefaultButtonStyle(Color.YELLOW);
        TextButton.TextButtonStyle knucklesStyle = UIUtils.createDefaultButtonStyle(Color.RED);

        sonicButton = new TextButton("Sonic", sonicStyle);
        sonicButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        sonicButton.setSize(200, 80);
        sonicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!sonicButton.isDisabled()) {
                    game.networkManager.selectCharacter("Sonic");
                    game.setScreen(new LobbyScreen(game, Color.BLUE, game.networkManager.isHost()));
                }
            }
        });

        tailsButton = new TextButton("Tails", tailsStyle);
        tailsButton.setPosition(Gdx.graphics.getWidth() / 2f - 220, Gdx.graphics.getHeight() / 2f - 50);
        tailsButton.setSize(200, 80);
        tailsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!tailsButton.isDisabled()) {
                    game.networkManager.selectCharacter("Tails");
                    game.setScreen(new LobbyScreen(game, Color.YELLOW, game.networkManager.isHost()));
                }
            }
        });

        knucklesButton = new TextButton("Knuckles", knucklesStyle);
        knucklesButton.setPosition(Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() / 2f - 50);
        knucklesButton.setSize(200, 80);
        knucklesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!knucklesButton.isDisabled()) {
                    game.networkManager.selectCharacter("Knuckles");
                    game.setScreen(new LobbyScreen(game, Color.RED, game.networkManager.isHost()));
                }
            }
        });

        stage.addActor(sonicButton);
        stage.addActor(tailsButton);
        stage.addActor(knucklesButton);
    }

    @Override
    public void onCharacterSelectionChanged(List<String> selectedCharacters) {
        Gdx.app.postRunnable(() -> {
            sonicButton.setDisabled(selectedCharacters.contains("Sonic"));
            tailsButton.setDisabled(selectedCharacters.contains("Tails"));
            knucklesButton.setDisabled(selectedCharacters.contains("Knuckles"));
        });
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

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}