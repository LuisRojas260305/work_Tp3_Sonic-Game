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
public class Knockles extends Personajes{
    private TextureAtlas atlasKnockles;
    public Animation<TextureRegion> PunchAnimation;
    public boolean isPunching = false;
    public float PunchPower = 0;
    private final float MAX_PUNCH_POWER = 500f;
    
    public Knockles(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.atlasKnockles = atlas;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
        
    }
    
    @Override
    public void usarHabilidad() {
        if (isGrounded && !enHabilidad) {
            isPunching = true;
            enHabilidad = true;
            PunchPower = 0;
            setCurrentAnimation(PunchAnimation);
        }
    }
    
    @Override
    public void update(float delta) {
        super.update(delta);
        
        if (isPunching) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                // Cargando poder
                PunchPower = Math.min(PunchPower + 100 * delta, MAX_PUNCH_POWER);
            } else {
                // Liberar habilidad
                float impulso = PunchPower * delta;
                x += facingRight ? impulso : -impulso;
                
                isPunching = false;
                enHabilidad = false;
                
                // Transición suave después de la habilidad
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }
    
    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasKnockles.findRegion("KE" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i < 8; i++) {
            runFrames.add(atlasKnockles.findRegion("KR" + i));
        }
        
        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 4; i++){
            ballFrames.add(atlasKnockles.findRegion("KB" + i));
        }
        
        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> jumpFrames = new Array<>();
        jumpFrames.add(atlasKnockles.findRegion("KJ1"));
        jumpFrames.add(atlasKnockles.findRegion("KJ2"));
        jumpFrames.add(atlasKnockles.findRegion("KJ0"));
        jumpFrames.add(atlasKnockles.findRegion("KJ3"));
        
        jumpAnimation = new Animation<>(0.2f, jumpFrames, Animation.PlayMode.NORMAL);
        
        Array<TextureRegion> PunchFrames = new Array<>();
        for (int i = 1; i < 6; i++){
            PunchFrames.add(atlasKnockles.findRegion("KG" + i));
        }
        
        PunchAnimation = new Animation<>(0.17f, PunchFrames, Animation.PlayMode.LOOP);
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}