package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Sonic en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial Spin Dash.
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic;
    /** Animación de Spin Dash de Sonic. */
    public Animation<TextureRegion> spinDashAnimation;
    /** Indica si Sonic está realizando un Spin Dash. */
    public boolean isSpinning = false;
    /** El poder actual del Spin Dash cargado. */
    public float spinPower = 0;
    /** El poder máximo que puede alcanzar el Spin Dash cargado. */
    private final float MAX_SPIN_POWER = 500f;

    /**
     * Constructor para el personaje Sonic.
     * @param playerId El ID del jugador asociado a este Sonic.
     * @param atlas El TextureAtlas que contiene las texturas de las animaciones de Sonic.
     */
    public Sonic(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        this.atlasSonic = atlas;
        this.moveSpeed = 400f;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
    }

    /**
     * Implementación de la habilidad especial de Sonic: Spin Dash.
     * Solo se puede activar si Sonic está en el suelo y no está ya en otra habilidad.
     */
    @Override
    public void useAbility() {
        if (isGrounded && !isAbilityActive) {
            isSpinning = true;
            isAbilityActive = true;
            spinPower = 0;
            setCurrentAnimation(spinDashAnimation);
        }
    }

    /**
     * Actualiza el estado de Sonic, incluyendo la lógica de su Spin Dash.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);

        if (isSpinning) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                // Cargando poder
                spinPower = Math.min(spinPower + 100 * delta, MAX_SPIN_POWER);
            } else {
                // Liberar habilidad
                float impulso = spinPower * delta;
                x += facingRight ? impulso : -impulso;

                isSpinning = false;
                isAbilityActive = false;

                // Transición suave después de la habilidad
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }

    /**
     * Carga y configura todas las animaciones específicas de Sonic desde su TextureAtlas.
     */
    private void cargarAnimaciones() {
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasSonic.findRegion("SE" + i));
        }
        idleAnimation = new Animation<>(0.08f, idleFrames); // Frame time reducido

        // Animación correr
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            runFrames.add(atlasSonic.findRegion("SR" + i));
        }
        runAnimation = new Animation<>(0.08f, runFrames); // Frame time reducido

        // Animación de bolita (roll)
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 5; i < 9; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) ballFrames.add(region);
        }
        rollAnimation = new Animation<>(0.03f, ballFrames); // Frame time reducido

        // Animación saltar
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            TextureRegion region = atlasSonic.findRegion("SJ" + i);
            if (region != null) jumpFrames.add(region);
        }
        jumpAnimation = new Animation<>(0.2f, jumpFrames);

        // Animación Spin Dash
        Array<TextureRegion> spinDashFrames = new Array<>();
        for (int i = 9; i < 13; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) spinDashFrames.add(region);
        }
        spinDashAnimation = new Animation<>(0.04f, spinDashFrames); // Frame time reducido
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
