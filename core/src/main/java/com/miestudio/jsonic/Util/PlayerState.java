package com.miestudio.jsonic.Util;

import java.io.Serializable;

/**
 * Representa el estado de un único jugador.
 * Es serializable para ser incluido en el GameState que se envía a través de la red.
 */
public class PlayerState implements Serializable {

    private static final long serialVersionUID = 1L;

    private int playerId;
    private float x, y;
    private boolean facingRight;
    private String currentAnimationName; // Nombre de la animación actual (ej. "idle", "run", "jump")
    private float animationStateTime; // stateTime de la animación

    public PlayerState(int playerId, float x, float y, boolean facingRight, String currentAnimationName, float animationStateTime) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.currentAnimationName = currentAnimationName;
        this.animationStateTime = animationStateTime;
    }

    // Getters para que el cliente pueda leer el estado
    public int getPlayerId() { return playerId; }
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isFacingRight() { return facingRight; }
    public String getCurrentAnimationName() { return currentAnimationName; }
    public float getAnimationStateTime() { return animationStateTime; }
}