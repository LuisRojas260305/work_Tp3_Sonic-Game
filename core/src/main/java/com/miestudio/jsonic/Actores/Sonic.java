/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 *
 * @author usuario
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic;
    public Animation<TextureRegion> spinDashAnimation;
    public boolean isSpinning = false;
    public float spinPower = 0;
    private final float MAX_SPIN_POWER = 500f;

    public Sonic(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        this.atlasSonic = atlas;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
    }

    @Override
    public void usarHabilidad() {
        if (isGrounded && !enHabilidad) {
            isSpinning = true;
            enHabilidad = true;
            spinPower = 0;
            setCurrentAnimation(spinDashAnimation);
        }
    }
    
    @Override
    public void update(float delta) {
        super.update(delta);
        
        if (isSpinning) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                // Cargando poder
                spinPower = Math.min(spinPower + 100 * delta, MAX_SPIN_POWER);
            } else {
                // Liberar habilidad
                float impulso = spinPower * delta;
                x += facingRight ? impulso : -impulso;
                
                isSpinning = false;
                enHabilidad = false;
                
                // Transición suave después de la habilidad
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }
    
    private void cargarAnimaciones() {
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasSonic.findRegion("SE" + i));
        }
        idleAnimation = new Animation<>(0.08f, idleFrames); // Frame time reducido
        
        // Animación correr
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            runFrames.add(atlasSonic.findRegion("SR" + i));
        }
        runAnimation = new Animation<>(0.08f, runFrames); // Frame time reducido
        
        // Animación de bolita (roll)
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 5; i < 9; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) ballFrames.add(region);
        }
        rollAnimation = new Animation<>(0.03f, ballFrames); // Frame time reducido
        
        // Animación saltar
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            TextureRegion region = atlasSonic.findRegion("SJ" + i);
            if (region != null) jumpFrames.add(region);
        }
        jumpAnimation = new Animation<>(0.2f, jumpFrames);
        
        // Animación Spin Dash
        Array<TextureRegion> spinDashFrames = new Array<>();
        for (int i = 9; i < 13; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) spinDashFrames.add(region);
        }
        spinDashAnimation = new Animation<>(0.04f, spinDashFrames); // Frame time reducido
    }
    
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}