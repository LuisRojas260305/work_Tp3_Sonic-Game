package Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.io.Serializable;

/**
 *
 * @author usuario
 */
public class Anillo extends Objetos implements Serializable {
    private static final long serialVersionUID = 1L;

    private Animation<TextureRegion> animacion;
    private float tiempoEstado;
    private transient TextureRegion texturaActual; // Marcado como transient para no serializar

    public Anillo(float x, float y, Animation<TextureRegion> animacion) {
        super(x, y); // Llama al constructor de Objetos sin TextureRegion
        this.animacion = animacion;
        this.hitbox = new Rectangle(x, y, 15f, 15f); // Tamaño específico
        this.texturaActual = animacion.getKeyFrame(0); // Asignar la textura inicial
    }

    @Override
    public void actualizar(float delta) {
        if (activo) {
            tiempoEstado += delta;
            texturaActual = animacion.getKeyFrame(tiempoEstado, true);
        }
    }

    @Override
    public void renderizar(SpriteBatch batch, TextureRegion textura) {
        // Usar la textura actual del anillo para renderizar
        super.renderizar(batch, texturaActual);
    }
}