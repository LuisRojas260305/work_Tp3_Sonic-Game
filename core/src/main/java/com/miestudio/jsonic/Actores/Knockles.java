/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
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
    
    public Knockles(){
        cargarAtlas();
        currentAnimation = idleAnimation;
        
    }
    
    private void cargarAtlas() {
        atlasKnockles = new TextureAtlas(Gdx.files.internal("KnocklesAtlas.txt"));
        
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
        for (int i = 0; i < 5; i++){
            ballFrames.add(atlasKnockles.findRegion("KB" + i));
        }
        
        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> jumpFrames = new Array<>();
        jumpFrames.add(atlasKnockles.findRegion("KJ1"));
        jumpFrames.add(atlasKnockles.findRegion("KJ2"));
        jumpFrames.add(atlasKnockles.findRegion("KJ0"));
        jumpFrames.add(atlasKnockles.findRegion("KJ3"));
        
        jumpAnimation = new Animation<>(0.2f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    public void dispose() {
        if (atlasKnockles != null) {
            atlasKnockles.dispose();
        }
    }
}
