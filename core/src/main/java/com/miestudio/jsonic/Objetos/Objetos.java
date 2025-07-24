package Objetos;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.io.Serializable;

public abstract class Objetos implements Serializable {
    public float x;
    public float y;
    public Rectangle hitbox;
    protected boolean activo = true;

    public Objetos(float x, float y) {
        this.x = x;
        this.y = y;
        this.hitbox = new Rectangle(x, y, 0.5f, 0.5f); // Tamaño por defecto
    }

    public abstract void actualizar(float delta);
    
    public void renderizar(SpriteBatch batch, TextureRegion textura) {
        if (activo && textura != null) {
            batch.draw(textura, x, y, textura.getRegionWidth(), textura.getRegionHeight());
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