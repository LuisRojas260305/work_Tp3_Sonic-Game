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
import com.miestudio.jsonic.Servidor.GestorRed;
import com.miestudio.jsonic.Utilidades.UtilidadesUI;

public class PantallaSeleccionRol implements Screen {

    private final JuegoSonic juego;
    private final Stage stage;
    private Label etiquetaEstado;

    public PantallaSeleccionRol(JuegoSonic juego) {
        this.juego = juego;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        configurarUI();
    }

    private void configurarUI() {
        TextButton.TextButtonStyle estiloBoton = UtilidadesUI.createDefaultButtonStyle();

        TextButton botonHost = new TextButton("Ser Host", estiloBoton);
        botonHost.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        botonHost.setSize(200, 80);
        botonHost.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                etiquetaEstado.setText("Iniciando servidor...");
                juego.gestorRed.iniciarHost();
                juego.setScreen(new PantallaSeleccionPersonaje(juego));
            }
        });

        TextButton botonCliente = new TextButton("Ser Cliente", estiloBoton);
        botonCliente.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 50);
        botonCliente.setSize(200, 80);
        botonCliente.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                etiquetaEstado.setText("Buscando servidor...");
                juego.gestorRed.descubrirYConectar(new GestorRed.CallbackConexion() {
                    @Override
                    public void onConnected() {
                        Gdx.app.postRunnable(() -> juego.setScreen(new PantallaSeleccionPersonaje(juego)));
                    }

                    @Override
                    public void onConnectionFailed(String error) {
                        etiquetaEstado.setText(error);
                    }
                });
            }
        });
        
        Label.LabelStyle labelStyle = new Label.LabelStyle(new com.badlogic.gdx.graphics.g2d.BitmapFont(), Color.WHITE);
        etiquetaEstado = new Label("", labelStyle);
        etiquetaEstado.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 150);
        etiquetaEstado.setSize(200, 40);
        etiquetaEstado.setWrap(true);
        etiquetaEstado.setAlignment(com.badlogic.gdx.utils.Align.center);


        stage.addActor(botonHost);
        stage.addActor(botonCliente);
        stage.addActor(etiquetaEstado);
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
