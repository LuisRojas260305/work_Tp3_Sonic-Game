package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Knockles en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial de puñetazo cargado.
 */
public class Knockles extends Personajes{
    private TextureAtlas atlasKnockles;
    /** Animación de puñetazo de Knockles. */
    public Animation<TextureRegion> PunchAnimation;
    /** Indica si Knockles está realizando un puñetazo cargado. */
    public boolean isPunching = false;
    /** El poder actual del puñetazo cargado. */
    public float PunchPower = 0;
    /** El poder máximo que puede alcanzar el puñetazo cargado. */
    private final float MAX_PUNCH_POWER = 500f;

    /**
     * Constructor para el personaje Knockles.
     * @param playerId El ID del jugador asociado a este Knockles.
     * @param atlas El TextureAtlas que contiene las texturas de las animaciones de Knockles.
     */
    public Knockles(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.atlasKnockles = atlas;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);

    }

    /**
     * Implementación de la habilidad especial de Knockles: un puñetazo cargado.
     * Solo se puede activar si Knockles está en el suelo y no está ya en otra habilidad.
     */
    @Override
    public void useAbility() {
        if (isGrounded && !isAbilityActive) {
            isPunching = true;
            isAbilityActive = true;
            PunchPower = 0;
            setCurrentAnimation(PunchAnimation);
        }
    }

    /**
     * Actualiza el estado de Knockles, incluyendo la lógica de su puñetazo cargado.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);

        if (isPunching) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                // Cargando poder
                PunchPower = Math.min(PunchPower + 100 * delta, MAX_PUNCH_POWER);
            } else {
                // Liberar habilidad
                float impulso = PunchPower * delta;
                x += facingRight ? impulso : -impulso;

                isPunching = false;
                isAbilityActive = false;

                // Transición suave después de la habilidad
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }

    /**
     * Carga y configura todas las animaciones específicas de Knockles desde su TextureAtlas.
     */
    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasKnockles.findRegion("KE" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i < 8; i++) {
            runFrames.add(atlasKnockles.findRegion("KR" + i));
        }

        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 4; i++){
            ballFrames.add(atlasKnockles.findRegion("KB" + i));
        }

        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> jumpFrames = new Array<>();
        jumpFrames.add(atlasKnockles.findRegion("KJ1"));
        jumpFrames.add(atlasKnockles.findRegion("KJ2"));
        jumpFrames.add(atlasKnockles.findRegion("KJ0"));
        jumpFrames.add(atlasKnockles.findRegion("KJ3"));

        jumpAnimation = new Animation<>(0.2f, jumpFrames, Animation.PlayMode.NORMAL);

        Array<TextureRegion> PunchFrames = new Array<>();
        for (int i = 1; i < 6; i++){
            PunchFrames.add(atlasKnockles.findRegion("KG" + i));
        }

        PunchAnimation = new Animation<>(0.17f, PunchFrames, Animation.PlayMode.LOOP);
    }

    /**
     * Libera los recursos específicos de Knockles.
     * En este caso, el TextureAtlas se gestiona centralmente en la clase Assets, por lo que no hay recursos adicionales que liberar aquí.
     */
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
