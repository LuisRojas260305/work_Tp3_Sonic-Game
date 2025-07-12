/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 *
 * @author usuario
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic;

    public Sonic() {
        cargarAtlas();
        currentAnimation = idleAnimation;
        x = 10;
        y = 20;
    }

    private void cargarAtlas() {
        atlasSonic = new TextureAtlas(Gdx.files.internal("SonicAtlas.txt"));
        
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasSonic.findRegion("SE" + i));
        }
        idleAnimation = new Animation<>(0.08f, idleFrames, Animation.PlayMode.LOOP);
        
        // Animación correr
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            runFrames.add(atlasSonic.findRegion("SR" + i));
        }
        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);
        
        // NUEVA: Animación de bolita (roll)
        Array<TextureRegion> ballFrames = new Array<>();
        ballFrames.add(atlasSonic.findRegion("SB5"));
        ballFrames.add(atlasSonic.findRegion("SB6"));
        ballFrames.add(atlasSonic.findRegion("SB7"));
        rollAnimation = new Animation<>(0.8f, ballFrames, Animation.PlayMode.LOOP);
        
        // Animación saltar
        Array<TextureRegion> jumpFrames = new Array<>();
        jumpFrames.add(atlasSonic.findRegion("SJ0"));
        jumpFrames.add(atlasSonic.findRegion("SJ1"));
        jumpFrames.add(atlasSonic.findRegion("SJ2"));
        jumpFrames.add(atlasSonic.findRegion("SJ3"));
        jumpFrames.add(atlasSonic.findRegion("SJ4"));
        jumpFrames.add(atlasSonic.findRegion("SJ5"));
        jumpFrames.add(atlasSonic.findRegion("SJ6"));
        jumpFrames.add(atlasSonic.findRegion("SJ7"));
        jumpAnimation = new Animation<>(0.2f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    public void dispose() {
        if (atlasSonic != null) {
            atlasSonic.dispose();
        }
    }
}
