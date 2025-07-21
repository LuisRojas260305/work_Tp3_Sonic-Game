package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Representa al personaje Tails en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas para Tails.
 */
public class Tails extends Personajes{
    private TextureAtlas atlasTails;
    
    /**
     * Constructor para el personaje Tails.
     * @param playerId El ID del jugador asociado a este Tails.
     * @param atlas El TextureAtlas que contiene las texturas de las animaciones de Tails.
     */
    public Tails(int idJugador, TextureAtlas atlas){
        this.idJugador = idJugador;
        this.atlasTails = atlas;
        cargarAnimaciones();
        animacionActual = idleAnimation;
    }
    
    /**
     * Carga y configura todas las animaciones específicas de Tails desde su TextureAtlas.
     */
    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 1; i < 9; i++) {
            idleFrames.add(atlasTails.findRegion("TE (" + i + ")"));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i < 8; i++) {
            runFrames.add(atlasTails.findRegion("TR (" + i + ")"));
        }
        
        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 1; i < 9; i++){
            ballFrames.add(atlasTails.findRegion("TB (" + i + ")"));
        }
        
        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> jumpFrames = new Array<>();
        jumpFrames.add(atlasTails.findRegion("TJ (3)"));
        jumpFrames.add(atlasTails.findRegion("TJ (2)"));
        jumpFrames.add(atlasTails.findRegion("TJ (1)"));
        jumpFrames.add(atlasTails.findRegion("TJ (4)"));
        jumpFrames.add(atlasTails.findRegion("TJ (5)"));
        
        jumpAnimation = new Animation<>(0.25f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    /**
     * Libera los recursos específicos de Tails.
     * En este caso, el TextureAtlas se gestiona centralmente en la clase Assets, por lo que no hay recursos adicionales que liberar aquí.
     */
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }

    /**
     * Implementación de la habilidad especial de Tails.
     * Actualmente no soportada y lanza una excepción.
     */
    @Override
    public void usarHabilidad() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}