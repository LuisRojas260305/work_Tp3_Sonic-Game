package com.miestudio.jsonic.Personajes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class KnucklesCharacter extends BaseCharacter {
    public KnucklesCharacter(int playerId) {
        super(playerId, Color.RED); // Rojo para Knuckles (Cliente 2)
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, float x, float y, float width, float height) {
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
    }
}