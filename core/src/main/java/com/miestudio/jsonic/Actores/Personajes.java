package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Personajes {
    protected int playerId;
    protected TextureAtlas atlas;
    protected Body body;
    protected float width = 32, height = 32;

    public Personajes(int playerId, TextureAtlas atlas, World world, float startX, float startY) {
        this.playerId = playerId;
        this.atlas = atlas;
        createPhysicsBody(world, startX, startY);
    }

    protected void createPhysicsBody(World world, float startX, float startY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        body.createFixture(shape, 1.0f);
        shape.dispose();
    }

    public void handleInput(com.miestudio.jsonic.Util.InputState input) {
        Vector2 vel = body.getLinearVelocity();
        float moveSpeed = 120;
        if (input.isLeft()) {
            body.setLinearVelocity(-moveSpeed, vel.y);
        } else if (input.isRight()) {
            body.setLinearVelocity(moveSpeed, vel.y);
        } else {
            body.setLinearVelocity(0, vel.y);
        }
        if (input.isUp()) {
            body.applyLinearImpulse(0, 300, body.getPosition().x, body.getPosition().y, true);
        }
    }

    public void update(float delta) {
        // Animación, lógica de personaje, etc.
    }

    // Renderiza el personaje según el Body
    public void render(Batch batch) {
        Vector2 pos = body.getPosition();
        // Ejemplo: batch.draw(atlas.findRegion("idle"), pos.x - width/2, pos.y - height/2, width, height);
    }

    public int getPlayerId() {
        return playerId;
    }

    public void dispose() {
        // Libera recursos si es necesario
    }
}
