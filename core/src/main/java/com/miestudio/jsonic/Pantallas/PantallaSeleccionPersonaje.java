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
import com.miestudio.jsonic.Servidor.ListenerSeleccionPersonaje;
import com.miestudio.jsonic.Utilidades.UtilidadesUI;

import java.util.List;

public class PantallaSeleccionPersonaje implements Screen, ListenerSeleccionPersonaje {

    private final JuegoSonic juego;
    private final Stage stage;
    private TextButton sonicButton, tailsButton, knucklesButton;

    public PantallaSeleccionPersonaje(JuegoSonic juego) {
        this.juego = juego;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        configurarUI();
        juego.gestorRed.setListenerSeleccionPersonaje(this);
    }

    private void configurarUI() {
        TextButton.TextButtonStyle estiloSonic = UtilidadesUI.createDefaultButtonStyle(Color.BLUE);
        TextButton.TextButtonStyle estiloTails = UtilidadesUI.createDefaultButtonStyle(Color.YELLOW);
        TextButton.TextButtonStyle estiloKnuckles = UtilidadesUI.createDefaultButtonStyle(Color.RED);

        sonicButton = new TextButton("Sonic", estiloSonic);
        sonicButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        sonicButton.setSize(200, 80);
        sonicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!sonicButton.isDisabled()) {
                    juego.gestorRed.seleccionarPersonaje("Sonic");
                    juego.setScreen(new PantallaLobby(juego, Color.BLUE, juego.gestorRed.esHost()));
                }
            }
        });

        tailsButton = new TextButton("Tails", estiloTails);
        tailsButton.setPosition(Gdx.graphics.getWidth() / 2f - 220, Gdx.graphics.getHeight() / 2f - 50);
        tailsButton.setSize(200, 80);
        tailsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!tailsButton.isDisabled()) {
                    juego.gestorRed.seleccionarPersonaje("Tails");
                    juego.setScreen(new PantallaLobby(juego, Color.YELLOW, juego.gestorRed.esHost()));
                }
            }
        });

        knucklesButton = new TextButton("Knuckles", estiloKnuckles);
        knucklesButton.setPosition(Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() / 2f - 50);
        knucklesButton.setSize(200, 80);
        knucklesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!knucklesButton.isDisabled()) {
                    juego.gestorRed.seleccionarPersonaje("Knuckles");
                    juego.setScreen(new PantallaLobby(juego, Color.RED, juego.gestorRed.esHost()));
                }
            }
        });

        stage.addActor(sonicButton);
        stage.addActor(tailsButton);
        stage.addActor(knucklesButton);
    }

    @Override
    public void onSeleccionPersonajeCambiada(List<String> selectedCharacters) {
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