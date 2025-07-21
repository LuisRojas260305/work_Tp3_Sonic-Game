package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Utilidades.GestorColisiones;

/**
 * Representa al personaje Sonic en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial Spin Dash.
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic;
    /** Animación de Spin Dash de Sonic. */
    public Animation<TextureRegion> animacionSpinDash;
    public boolean estaGirando = false;
    public float poderGiro = 0;
    private final float MAX_PODER_GIRO = 500f;

    /**
     * Constructor para el personaje Sonic.
     * @param playerId El ID del jugador asociado a este Sonic.
     * @param atlas El TextureAtlas que contiene las texturas de las animaciones de Sonic.
     */
    public Sonic(int idJugador, TextureAtlas atlas) {
        this.idJugador = idJugador;
        this.atlasSonic = atlas;
        this.velocidadMovimiento = 400f;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosicion(10, 20);
    }

    /**
     * Implementación de la habilidad especial de Sonic: Spin Dash.
     * Solo se puede activar si Sonic está en el suelo y no está ya en otra habilidad.
     */
    public void usarHabilidad() {
        if (estaEnSuelo && !habilidadActiva) {
            estaGirando = true;
            habilidadActiva = true;
            poderGiro = 0;
            setCurrentAnimation(animacionSpinDash);
        }
    }

    /**
     * Actualiza el estado de Sonic, incluyendo la lógica de su Spin Dash.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    @Override
    public void update(float delta, GestorColisiones gestorColisiones) {
        super.update(delta, gestorColisiones);

        if (estaGirando) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                poderGiro = Math.min(poderGiro + 100 * delta, MAX_PODER_GIRO);
            } else {
                float impulso = poderGiro * delta;
                setX(getX() + (mirandoDerecha ? impulso : -impulso));

                estaGirando = false;
                habilidadActiva = false;

                if (estaEnSuelo) {
                    setCurrentAnimation(estaRodando ? rollAnimation : idleAnimation);
                }
            }
        }
    }

    /**
     * Carga y configura todas las animaciones específicas de Sonic desde su TextureAtlas.
     */
    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasSonic.findRegion("SE" + i));
        }
        idleAnimation = new Animation<>(0.08f, idleFrames);

        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            runFrames.add(atlasSonic.findRegion("SR" + i));
        }
        runAnimation = new Animation<>(0.08f, runFrames);

        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 5; i < 9; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) ballFrames.add(region);
        }
        rollAnimation = new Animation<>(0.03f, ballFrames);

        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            TextureRegion region = atlasSonic.findRegion("SJ" + i);
            if (region != null) jumpFrames.add(region);
        }
        jumpAnimation = new Animation<>(0.2f, jumpFrames);

        Array<TextureRegion> spinDashFrames = new Array<>();
        for (int i = 9; i < 13; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) spinDashFrames.add(region);
        }
        animacionSpinDash = new Animation<>(0.04f, spinDashFrames);
    }

    /**
     * Libera los recursos específicos de Sonic.
     * En este caso, el TextureAtlas se gestiona centralmente en la clase Assets, por lo que no hay recursos adicionales que liberar aquí.
     */
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
