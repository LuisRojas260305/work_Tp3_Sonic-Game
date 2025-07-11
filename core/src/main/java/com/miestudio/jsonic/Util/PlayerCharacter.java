package com.miestudio.jsonic.Util;

import com.badlogic.gdx.graphics.Color;

public class PlayerCharacter {
    private int playerId;
    private Color color;

    public PlayerCharacter(int playerId, Color color) {
        this.playerId = playerId;
        this.color = color;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Color getColor() {
        return color;
    }
}