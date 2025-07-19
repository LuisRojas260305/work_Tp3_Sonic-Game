package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.InputState;

/**
 * Clase base abstracta para todos los personajes jugables.
 * Maneja la física básica, el estado y las animaciones.
 */
public abstract class Personajes extends Actor {

    public float stateTime;
    protected float x, y;
    protected float prevX, prevY; // Para interpolación
    protected boolean facingRight = true, isGrounded = true;
    public Animation<TextureRegion> currentAnimation;
    public Animation<TextureRegion> idleAnimation;
    public Animation<TextureRegion> runAnimation;
    public Animation<TextureRegion> jumpAnimation;
    public Animation<TextureRegion> rollAnimation;

    protected float velocidadY = 0;
    protected final float gravedad = -800f;
    protected final float fuerzaSalto = 500f;

    public boolean isRolling = false;
    protected boolean enHabilidad = false;
    protected int playerId;
    protected float velocidadMovimiento = 300f;

    public abstract void usarHabilidad();
    public abstract void dispose();

    public void update(float delta, CollisionManager collisionManager) {
        stateTime += delta;
        updatePhysics(delta, collisionManager);
    }

    private void updatePhysics(float delta, CollisionManager collisionManager){
        velocidadY += gravedad * delta;
        y += velocidadY * delta;

        // Verificar colisión con el suelo después de aplicar gravedad
        Rectangle characterBounds = new Rectangle(x, y, getWidth(), getHeight());

        float groundY = collisionManager.getGroundY(characterBounds);

        if (groundY >= 0) {
            if (y <= groundY){
                y = groundY;
                velocidadY = 0;
                isGrounded = true;
            }
        } else {
            isGrounded = false;
        }
    }

    /**
     * Maneja los inputs del jugador para actualizar el estado del personaje.
     * Este método es llamado por el servidor (Host) basado en los InputState recibidos.
     * @param input El estado de los botones del jugador.
     */
    public void handleInput(InputState input, CollisionManager collisionManager) {

        if (enHabilidad) return;

        boolean isMoving = false;
        float delta = Gdx.graphics.getDeltaTime();

        if (input.isRight()){
            float nextX = x + velocidadMovimiento * delta;
            Rectangle horizontalCheck = new Rectangle(
                nextX, y, getWidth(), getHeight()
            );

            if (!collisionManager.collides(horizontalCheck)){
                x = nextX;
                facingRight = true;
            }

            isMoving = true;
        }

        if (input.isLeft()){
            float nextX = x - velocidadMovimiento * delta;
            Rectangle horizontalCheck = new Rectangle(
                nextX, y, getWidth(), getHeight()
            );

            if (!collisionManager.collides(horizontalCheck)){
                x = nextX;
                facingRight = false;
            }

            isMoving = true;
        }

        // Limitar posición dentro del mapa
        x = Math.max(0, Math.min(x, collisionManager.getMapWidth() - getWidth()));
        y = Math.max(0, Math.min(y, collisionManager.getMapHeight() - getHeight()));

        isRolling = input.isDown();

        Rectangle characterBounds = new Rectangle(x, y, getWidth(), getHeight());

        isGrounded = collisionManager.isOnGround(characterBounds);

        if (input.isUp() && isGrounded){
            velocidadY = fuerzaSalto;
            isGrounded = false;
            setCurrentAnimation(jumpAnimation);
        }

        if (input.isAbility()){
            usarHabilidad();
        }

        if (isRolling && isGrounded){
            setCurrentAnimation(rollAnimation);
        } else if (isMoving && isGrounded) {
            setCurrentAnimation(runAnimation);
        } else if (!isGrounded) {
            setCurrentAnimation(jumpAnimation);
        } else {
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
    public float getPrevX() { return prevX; }
    public float getPrevY() { return prevY; }
    public float getSpeed() { return velocidadMovimiento; }
    public void setSpeed(float speed) { this.velocidadMovimiento = speed; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public int getPlayerId() { return playerId; }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setPreviousPosition(float x, float y) {
        this.prevX = x;
        this.prevY = y;
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

    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    public float getWidth() {
        return currentAnimation.getKeyFrame(0).getRegionWidth();
    }

    public float getHeight() {
        return currentAnimation.getKeyFrame(0).getRegionHeight();
    }
}
