package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Server.NetworkManager;
import com.miestudio.jsonic.Util.UIUtils;

public class RoleSelectionScreen implements Screen {

    private final JuegoSonic game;
    private final Stage stage;
    private Label statusLabel;

    public RoleSelectionScreen(JuegoSonic game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        setupUI();
    }

    private void setupUI() {
        TextButton.TextButtonStyle buttonStyle = UIUtils.createDefaultButtonStyle();

        TextButton hostButton = new TextButton("Ser Host", buttonStyle);
        hostButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        hostButton.setSize(200, 80);
        hostButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                statusLabel.setText("Iniciando servidor...");
                game.networkManager.startHost();
                game.setScreen(new CharacterSelectionScreen(game));
            }
        });

        TextButton clientButton = new TextButton("Ser Cliente", buttonStyle);
        clientButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 50);
        clientButton.setSize(200, 80);
        clientButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                statusLabel.setText("Buscando servidor...");
                game.networkManager.discoverAndConnect(new NetworkManager.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        Gdx.app.postRunnable(() -> game.setScreen(new CharacterSelectionScreen(game)));
                    }

                    @Override
                    public void onConnectionFailed(String error) {
                        statusLabel.setText(error);
                    }
                });
            }
        });
        
        Label.LabelStyle labelStyle = new Label.LabelStyle(new com.badlogic.gdx.graphics.g2d.BitmapFont(), Color.WHITE);
        statusLabel = new Label("", labelStyle);
        statusLabel.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 150);
        statusLabel.setSize(200, 40);
        statusLabel.setWrap(true);
        statusLabel.setAlignment(com.badlogic.gdx.utils.Align.center);


        stage.addActor(hostButton);
        stage.addActor(clientButton);
        stage.addActor(statusLabel);
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
