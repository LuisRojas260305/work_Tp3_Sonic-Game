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
public class Tails extends Personajes{
    private TextureAtlas altasTails;
    
    public Tails(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.altasTails = atlas;
        cargarAnimaciones();
        currentAnimation = idleAnimation;
    }
    
    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 1; i < 9; i++) {
            idleFrames.add(altasTails.findRegion("TE (" + i + ")"));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i < 8; i++) {
            runFrames.add(altasTails.findRegion("TR (" + i + ")"));
        }
        
        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 1; i < 9; i++){
            ballFrames.add(altasTails.findRegion("TB (" + i + ")"));
        }
        
        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> jumpFrames = new Array<>();
        jumpFrames.add(altasTails.findRegion("TJ (3)"));
        jumpFrames.add(altasTails.findRegion("TJ (2)"));
        jumpFrames.add(altasTails.findRegion("TJ (1)"));
        jumpFrames.add(altasTails.findRegion("TJ (4)"));
        jumpFrames.add(altasTails.findRegion("TJ (5)"));
        
        jumpAnimation = new Animation<>(0.25f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }

    @Override
    public void usarHabilidad() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}