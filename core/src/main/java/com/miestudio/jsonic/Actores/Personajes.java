package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.miestudio.jsonic.Util.InputState;

/**
 * Clase base abstracta para todos los personajes jugables.
 * Maneja la física básica, el estado y las animaciones.
 */
public abstract class Personajes extends Actor {

    public float stateTime;
    protected float x, y, velocidadY = 0;
    protected boolean facingRight = true, isGrounded = true;
    public Animation<TextureRegion> currentAnimation;
    public Animation<TextureRegion> idleAnimation;
    public Animation<TextureRegion> runAnimation;
    public Animation<TextureRegion> jumpAnimation;
    public Animation<TextureRegion> rollAnimation;

    private final float gravedad = -800f;
    private final float fuerzaSalto = 500f;
    private final float suelo = 100;
    public boolean isRolling = false;
    protected boolean enHabilidad = false;
    protected int playerId;
    protected float velocidadMovimiento = 300f;

    public abstract void usarHabilidad();
    public abstract void dispose();

    public void update(float delta) {
        stateTime += delta;
        updatePhysics(delta);
    }

    private void updatePhysics(float delta) {
        velocidadY += gravedad * delta;
        y += velocidadY * delta;

        if (y <= suelo) {
            y = suelo;
            velocidadY = 0;
            isGrounded = true;
        }
    }

    /**
     * Maneja los inputs del jugador para actualizar el estado del personaje.
     * Este método es llamado por el servidor (Host) basado en los InputState recibidos.
     * @param input El estado de los botones del jugador.
     */
    public void handleInput(InputState input) {
        if (enHabilidad) return; // No procesar inputs normales durante una habilidad

        boolean isMoving = false;

        if (input.isRight()) {
            x += 300 * Gdx.graphics.getDeltaTime();
            facingRight = true;
            isMoving = true;
        }
        if (input.isLeft()) {
            x -= 300 * Gdx.graphics.getDeltaTime();
            facingRight = false;
            isMoving = true;
        }

        isRolling = input.isDown();

        if (input.isUp() && isGrounded) {
            velocidadY = fuerzaSalto;
            isGrounded = false;
            setCurrentAnimation(jumpAnimation);
        }

        if (input.isAbility()) {
            usarHabilidad();
        }

        // Transiciones de animación
        if (isRolling && isGrounded) {
            setCurrentAnimation(rollAnimation);
        } else if (isMoving && isGrounded) {
            setCurrentAnimation(runAnimation);
        } else if (isGrounded) {
            setCurrentAnimation(idleAnimation);
        }
    }

    protected void setCurrentAnimation(Animation<TextureRegion> newAnimation) {
        if (currentAnimation != newAnimation) {
            currentAnimation = newAnimation;
            stateTime = 0f;
        }
    }

    /**
     * Establece la animación actual del personaje basándose en un nombre.
     * Útil para sincronizar animaciones entre el servidor y el cliente.
     * @param animationName El nombre de la animación a establecer.
     */
    public void setAnimationByName(String animationName) {
        switch (animationName) {
            case "idle": setCurrentAnimation(idleAnimation); break;
            case "run": setCurrentAnimation(runAnimation); break;
            case "jump": setCurrentAnimation(jumpAnimation); break;
            case "roll": setCurrentAnimation(rollAnimation); break;
            // Añadir casos para animaciones específicas de cada personaje si es necesario
        }
    }

    // Getters y Setters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getSpeed() { return velocidadMovimiento; }
    public void setSpeed(float speed) { this.velocidadMovimiento = speed; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public int getPlayerId() { return playerId; }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public TextureRegion getCurrentFrame() {
        return currentAnimation.getKeyFrame(stateTime, true);
    }

    /**
     * Devuelve el nombre de la animación actual.
     * @return El nombre de la animación actual como String.
     */
    public String getCurrentAnimationName() {
        if (currentAnimation == idleAnimation) return "idle";
        if (currentAnimation == runAnimation) return "run";
        if (currentAnimation == jumpAnimation) return "jump";
        if (currentAnimation == rollAnimation) return "roll";
        return "unknown"; // O manejar de otra forma
    }

    public float getAnimationStateTime() {
        return stateTime;
    }

    public void setAnimationStateTime(float stateTime) {
        this.stateTime = stateTime;
    }
}
