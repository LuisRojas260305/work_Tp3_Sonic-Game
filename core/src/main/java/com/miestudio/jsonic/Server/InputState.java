package com.miestudio.jsonic.Server;

import java.io.Serializable;

public class InputState implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;
    private boolean ability;

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isDown() {
        return down;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public boolean isAbility() {
        return ability;
    }

    public void setAbility(boolean ability) {
        this.ability = ability;
    }
}
