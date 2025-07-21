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
import com.miestudio.jsonic.Utilidades.UtilidadesUI;

/**
 * Pantalla de Lobby que se muestra después de que un jugador ha elegido ser Host o Cliente.
 * Muestra un color de fondo según el personaje y un botón para iniciar la partida (solo para el Host).
 */
public class PantallaLobby implements Screen {

    private final JuegoSonic juego;
    private final Stage escenario;
    private final Color colorJugador;
    private final boolean esHost;

    public PantallaLobby(JuegoSonic juego, Color colorJugador, boolean esHost) {
        this.juego = juego;
        this.colorJugador = colorJugador;
        this.esHost = esHost;
        this.escenario = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(escenario);

        if (esHost) {
            configurarUIHost();
        }
    }

    /**
     * Configura la UI adicional que solo el Host puede ver, como el botón de "Iniciar Partida".
     */
    private void configurarUIHost() {
        TextButton.TextButtonStyle estiloInicio = new TextButton.TextButtonStyle();
        estiloInicio.font = new BitmapFont();
        estiloInicio.up = UtilidadesUI.createColorDrawable(Color.GREEN.cpy().mul(0.8f));
        TextButton botonInicio = new TextButton("Iniciar Partida", estiloInicio);
        botonInicio.setPosition(Gdx.graphics.getWidth() / 2f - 150, Gdx.graphics.getHeight() / 2f - 50);
        botonInicio.setSize(300, 100);
        botonInicio.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("PantallaLobby", "Host iniciando la partida...");
                juego.gestorRed.iniciarJuego();
            }
        });
        escenario.addActor(botonInicio);
    }

    

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(colorJugador.r, colorJugador.g, colorJugador.b, colorJugador.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        escenario.act(delta);
        escenario.draw();
    }

    @Override
    public void resize(int width, int height) {
        escenario.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        escenario.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}