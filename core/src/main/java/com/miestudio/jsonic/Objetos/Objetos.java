/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/**
 *
 * @author usuario
 */
public abstract class Objetos {
    public float x;
    public float y;
    protected TextureRegion textura;
    protected Rectangle hitbox;
    protected boolean activo = true;

    public Objetos(float x, float y, TextureRegion textura) {
        this.x = x;
        this.y = y;
        this.textura = textura;
        this.hitbox = new Rectangle(x, y, 0.5f, 0.5f); // Tamaño por defecto
    }

    public abstract void actualizar(float delta);
    
    public void renderizar(SpriteBatch batch) {
        if (activo) {
            batch.draw(textura, x, y, hitbox.width, hitbox.height);
        }
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
