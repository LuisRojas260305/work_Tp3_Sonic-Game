package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import Objetos.Objetos;

import java.io.Serializable;

/**
 * Representa un objeto de basura que flota en el mapa.
 * Hereda de la clase base Objetos.
 */
public class ObjetoBasura extends Objetos implements Serializable {

    private static final long serialVersionUID = 1L; // Para serialización

    private int id; // ID único para el objeto de basura
    private int spriteIndex; // Índice del sprite para la basura
    private float yOriginal;
    private float tiempoTranscurrido;
    private final float amplitudFlotacion = 5f; // Píxeles que sube y baja
    private final float velocidadFlotacion = 2f;  // Ciclos por segundo

    // Constructor sin argumentos para la deserialización
    public ObjetoBasura() {
        // Inicializar campos a valores por defecto o nulos
        super(0, 0); // Llama al constructor de Objetos con valores por defecto
        this.id = -1; // O algún valor que indique que no está inicializado
        this.spriteIndex = -1;
        this.yOriginal = 0;
        this.tiempoTranscurrido = 0;
        this.hitbox.width = 25f; // Ajustar tamaño del hitbox a 25x25
        this.hitbox.height = 25f;
    }

    /**
     * Constructor para un objeto de basura.
     * @param id El ID único del objeto de basura.
     * @param x La posición inicial en X.
     * @param y La posición inicial en Y.
     * @param spriteIndex El índice del sprite a usar de la lista de texturas de basura.
     */
    public ObjetoBasura(int id, float x, float y, int spriteIndex) {
        // Llama al constructor de la clase padre con null para la textura, se asignará en render
        super(x, y); // No pasamos la textura aquí, se asignará en render
        this.id = id;
        this.spriteIndex = spriteIndex;
        
        this.yOriginal = y;
        this.tiempoTranscurrido = MathUtils.random(0f, 100f); // Desfase aleatorio para que no floten todos a la vez
        this.hitbox.width = 25f; // Ajustar tamaño del hitbox a 25x25
        this.hitbox.height = 25f;
    }

    /**
     * Actualiza la lógica del objeto, principalmente la animación de flotación.
     * @param delta El tiempo transcurrido desde el último fotograma.
     */
    @Override
    public void actualizar(float delta) {
        tiempoTranscurrido += delta;
        // Usa una función seno para crear un movimiento suave de arriba a abajo
        this.y = yOriginal + (float)Math.sin(tiempoTranscurrido * velocidadFlotacion) * amplitudFlotacion;
        this.hitbox.y = this.y;
    }

    public int getId() {
        return id;
    }

    public int getSpriteIndex() {
        return spriteIndex;
    }
}
