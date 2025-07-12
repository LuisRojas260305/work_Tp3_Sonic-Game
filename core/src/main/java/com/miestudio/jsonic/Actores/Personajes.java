/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 *
 * @author usuario
 */
public abstract class Personajes extends Actor {
    protected float stateTime, x, y, velocidadY = 0;
    protected boolean facingRight = true, isGrounded = true;
    protected Animation<TextureRegion> currentAnimation;
    protected Animation<TextureRegion> idleAnimation;
    protected Animation<TextureRegion> runAnimation;
    protected Animation<TextureRegion> jumpAnimation;
    protected Animation<TextureRegion> rollAnimation; 
    
    private final float gravedad = -800f;
    private final float fuerzaSalto = 500f;
    private final float suelo = 100;
    private boolean isRolling = false; 

    public void update(float delta) {
        stateTime += delta;
        updatePhysics(delta);
        handleInput();
    }

    private void updatePhysics(float delta) {
        velocidadY += gravedad * delta;
        y += velocidadY * delta;
        
        if (y <= suelo) {
            y = suelo;
            velocidadY = 0;
            isGrounded = true;
            
            if (currentAnimation == jumpAnimation) {
                
                currentAnimation = isRolling ? rollAnimation : idleAnimation;
                stateTime = 0f;
            }
        }
    }

    private void handleInput() {
        boolean isMoving = false;
        
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += 200 * Gdx.graphics.getDeltaTime();
            facingRight = true;
            isMoving = true;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= 200 * Gdx.graphics.getDeltaTime();
            facingRight = false;
            isMoving = true;
        }
        
        
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            isRolling = true;
        } else {
            isRolling = false;
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isGrounded) {
            velocidadY = fuerzaSalto;
            isGrounded = false;
            currentAnimation = jumpAnimation;
            stateTime = 0f;
        }
        
        // Lógica de transición de animaciones
        if (isRolling && isGrounded) {
            currentAnimation = rollAnimation;
            stateTime = 0f;
        }
        else if (isMoving && isGrounded && currentAnimation != runAnimation && !isRolling) {
            currentAnimation = runAnimation;
            stateTime = 0f;
        }
        else if (!isMoving && isGrounded && currentAnimation != idleAnimation && !isRolling) {
            currentAnimation = idleAnimation;
            stateTime = 0f;
        }
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isFacingRight() { return facingRight; }
    public TextureRegion getCurrentFrame() { 
        return currentAnimation.getKeyFrame(stateTime, true); 
    }
}
