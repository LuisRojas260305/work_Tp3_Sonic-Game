package com.miestudio.jsonic.Personajes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class BaseCharacter {
    protected int playerId;
    protected Color color;

    public BaseCharacter(int playerId, Color color) {
        this.playerId = playerId;
        this.color = color;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public abstract void draw(ShapeRenderer shapeRenderer, float x, float y, float width, float height);
}