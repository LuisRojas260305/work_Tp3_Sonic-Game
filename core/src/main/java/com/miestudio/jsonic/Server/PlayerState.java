package com.miestudio.jsonic.Server;

import java.io.Serializable;

public class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;
    private int playerId;
    private float x;
    private float y;
    private boolean facingRight;
    private String currentAnimationName;
    private float animationStateTime;

    public PlayerState(int playerId, float x, float y, boolean facingRight, String currentAnimationName, float animationStateTime) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.currentAnimationName = currentAnimationName;
        this.animationStateTime = animationStateTime;
    }

    public int getPlayerId() {
        return playerId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public String getCurrentAnimationName() {
        return currentAnimationName;
    }

    public float getAnimationStateTime() {
        return animationStateTime;
    }
}
