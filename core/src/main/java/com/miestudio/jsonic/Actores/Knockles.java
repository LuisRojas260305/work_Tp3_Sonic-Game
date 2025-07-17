package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;

public class Knockles extends Personajes {
    public Knockles(int playerId, TextureAtlas atlas, World world, float startX, float startY) {
        super(playerId, atlas, world, startX, startY);
        // Inicializa animaciones, estados, etc.
    }

    @Override
    public void update(float delta) {
        // Lógica específica de Knuckles
        super.update(delta);
    }
}
