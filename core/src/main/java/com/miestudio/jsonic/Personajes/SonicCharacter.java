package com.miestudio.jsonic.Personajes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SonicCharacter extends BaseCharacter {
    public SonicCharacter(int playerId) {
        super(playerId, new Color(0.1f, 0.1f, 0.4f, 1f)); // Azul oscuro para Sonic (Servidor)
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, float x, float y, float width, float height) {
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
    }
}