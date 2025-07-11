package com.miestudio.jsonic.Personajes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TailsCharacter extends BaseCharacter {
    public TailsCharacter(int playerId) {
        super(playerId, Color.ORANGE); // Naranja para Tails (Cliente 1)
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, float x, float y, float width, float height) {
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
    }
}