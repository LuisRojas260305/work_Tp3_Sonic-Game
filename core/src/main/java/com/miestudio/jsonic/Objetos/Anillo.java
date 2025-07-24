/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 *
 * @author usuario
 */
public class Anillo extends Objetos {
    private Animation<TextureRegion> animacion;
    private float tiempoEstado;

    public Anillo(float x, float y, Animation<TextureRegion> animacion) {
        super(x, y, animacion.getKeyFrame(0));
        this.animacion = animacion;
        this.hitbox = new Rectangle(x, y, 15f, 15f); // Tamaño específico
    }

    @Override
    public void actualizar(float delta) {
        if (activo) {
            tiempoEstado += delta;
            textura = animacion.getKeyFrame(tiempoEstado, true);
        }
    }
}
