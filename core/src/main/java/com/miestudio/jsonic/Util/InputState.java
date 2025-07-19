package com.miestudio.jsonic.Util;

import java.io.Serializable;

/**
 * Representa el estado de los inputs de un cliente en un momento dado.
 * Esta clase es serializable para ser enviada desde el cliente al servidor.
 */
public class InputState implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean up, down, left, right, ability;
    private int playerId;

    // Getters y Setters
    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public boolean isUp() { return up; }
    public void setUp(boolean up) { this.up = up; }

    public boolean isDown() { return down; }
    public void setDown(boolean down) { this.down = down; }

    public boolean isLeft() { return left; }
    public void setLeft(boolean left) { this.left = left; }

    public boolean isRight() { return right; }
    public void setRight(boolean right) { this.right = right; }

    public boolean isAbility() { return ability; }
    public void setAbility(boolean ability) { this.ability = ability; }
}