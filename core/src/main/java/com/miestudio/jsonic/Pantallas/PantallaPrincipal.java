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
import com.miestudio.jsonic.Utilidades.UtilidadesUI;

/**
 * Pantalla de menú principal que permite al jugador elegir si desea ser el Host o un Cliente.
 * Presenta tres botones: Jugar, Ayuda y Estadísticas.
 */
public class PantallaPrincipal implements Screen {

    /** Referencia a la instancia principal del juego. */
    private final JuegoSonic juego;

    /** El escenario donde se colocan los actores de la UI, como los botones. */
    private final Stage stage;

    /**
     * Constructor para la pantalla principal.
     *
     * @param game La instancia principal del juego, necesaria para acceder al NetworkManager.
     */
    public PantallaPrincipal(JuegoSonic juego) {
        this.juego = juego;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        configurarUI();
    }

    /**
     * Configura la interfaz de usuario, creando y posicionando los botones.
     */
    private void configurarUI() {
        TextButton.TextButtonStyle estiloBoton = UtilidadesUI.createDefaultButtonStyle();

        // Botón Jugar
        TextButton botonJugar = new TextButton("Jugar", estiloBoton);
        botonJugar.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        botonJugar.setSize(200, 80);
        botonJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("PantallaPrincipal", "Botón Jugar presionado. Navegando a PantallaSeleccionRol.");
                juego.setScreen(new PantallaSeleccionRol(juego));
            }
        });

        // Botón Ayuda
        TextButton botonAyuda = new TextButton("Ayuda", estiloBoton);
        botonAyuda.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 40);
        botonAyuda.setSize(200, 80);
        botonAyuda.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("PantallaPrincipal", "Botón Ayuda presionado.");
                // Lógica para la pantalla de ayuda
            }
        });

        // Botón Estadísticas
        TextButton botonEstadisticas = new TextButton("Estadísticas", estiloBoton);
        botonEstadisticas.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 130);
        botonEstadisticas.setSize(200, 80);
        botonEstadisticas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("PantallaPrincipal", "Botón Estadísticas presionado.");
                // Lógica para la pantalla de estadísticas
            }
        });

        stage.addActor(botonJugar);
        stage.addActor(botonAyuda);
        stage.addActor(botonEstadisticas);
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