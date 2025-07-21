package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.miestudio.jsonic.Utilidades.GestorColisiones;
import com.miestudio.jsonic.Utilidades.Constantes;
import com.miestudio.jsonic.Utilidades.EstadoEntrada;

/**
 * Clase base abstracta para todos los personajes jugables en el juego.
 * Proporciona la lógica fundamental para la física, el estado y la gestión de animaciones.
 */
public abstract class Personajes extends Actor {

    

    /** Tiempo de estado actual de la animación. */
    public float tiempoEstado;
    protected float xAnterior;
    protected float yAnterior;
    protected boolean mirandoDerecha = true;
    protected boolean estaEnSuelo = true;
    public Animation<TextureRegion> animacionActual;
    public Animation<TextureRegion> idleAnimation;
    public Animation<TextureRegion> runAnimation;
    public Animation<TextureRegion> jumpAnimation;
    public Animation<TextureRegion> rollAnimation;

    protected float velocidadY = 0;
    protected final float gravedad = -800f;
    protected final float fuerzaSalto = 500f;

    public boolean estaRodando = false;
    protected boolean habilidadActiva = false;
    protected int idJugador;
    protected float velocidadMovimiento = 300f;

    /**
     * Ejecuta la habilidad especial del personaje.
     * Esta es una implementación abstracta que debe ser definida por las subclases.
     */
    public abstract void usarHabilidad();
    /**
     * Libera los recursos asociados al personaje.
     * Las subclases deben implementar este método si tienen recursos propios que liberar.
     */
    public abstract void dispose();

    /**
     * Actualiza el estado del personaje, incluyendo la física y el tiempo de animación.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    public void update(float delta, GestorColisiones gestorColisiones) {
        tiempoEstado += delta;
        actualizarFisica(delta, gestorColisiones);
    }

    /**
     * Aplica la física al personaje, incluyendo gravedad y detección de suelo.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    private void actualizarFisica(float delta, GestorColisiones gestorColisiones){
        velocidadY += gravedad * delta;

        float siguienteY = getY() + velocidadY * delta;

        Rectangle limitesActuales = new Rectangle(getX(), getY(), getWidth(), getHeight());
        float sueloY = gestorColisiones.obtenerSueloY(limitesActuales);

        if (sueloY >= 0 && getY() >= sueloY && siguienteY <= sueloY) {
            setY(sueloY);
            velocidadY = 0;
            estaEnSuelo = true;
        } else {
            setY(siguienteY);
            estaEnSuelo = false;
        }
    }

    /**
     * Maneja los inputs del jugador para actualizar el estado del personaje.
     * Este método es llamado por el servidor (Host) basado en los InputState recibidos.
     * @param input El estado de los botones del jugador.
     */
    public void manejarEntrada(EstadoEntrada entrada, GestorColisiones gestorColisiones, float delta) {

        if (habilidadActiva) return;

        boolean moviendo = false;

        if (entrada.isRight()){
            float siguienteX = getX() + velocidadMovimiento * delta;
            Rectangle chequeoHorizontal = new Rectangle(
                siguienteX, getY(), getWidth(), getHeight()
            );

            if (!gestorColisiones.colisiona(chequeoHorizontal)){
                setX(siguienteX);
                mirandoDerecha = true;
            }

            moviendo = true;
        }

        if (entrada.isLeft()){
            float siguienteX = getX() - velocidadMovimiento * delta;
            Rectangle chequeoHorizontal = new Rectangle(
                siguienteX, getY(), getWidth(), getHeight()
            );

            if (!gestorColisiones.colisiona(chequeoHorizontal)){
                setX(siguienteX);
                mirandoDerecha = false;
            }

            moviendo = true;
        }

        setX(Math.max(0, Math.min(getX(), gestorColisiones.getAnchoMapa() - getWidth())));
        setY(Math.max(0, Math.min(getY(), gestorColisiones.getAltoMapa() - getHeight())));

        estaRodando = entrada.isDown();

        Rectangle limitesPersonaje = new Rectangle(getX(), getY(), getWidth(), getHeight());

        estaEnSuelo = gestorColisiones.estaEnSuelo(limitesPersonaje);

        if (entrada.isUp() && estaEnSuelo){
            velocidadY = fuerzaSalto;
            estaEnSuelo = false;
            setCurrentAnimation(jumpAnimation);
        }

        if (entrada.isAbility()){
            usarHabilidad();
        }

        if (estaRodando && estaEnSuelo){
            setCurrentAnimation(rollAnimation);
        } else if (moviendo && estaEnSuelo) {
            setCurrentAnimation(runAnimation);
        } else if (!estaEnSuelo) {
            setCurrentAnimation(jumpAnimation);
        } else {
            setCurrentAnimation(idleAnimation);
        }
    }

    protected void setCurrentAnimation(Animation<TextureRegion> nuevaAnimacion) {
        if (animacionActual != nuevaAnimacion) {
            animacionActual = nuevaAnimacion;
            tiempoEstado = 0f;
        }
    }

    

    // Getters y Setters
    /**
     * Obtiene la posición X actual del personaje.
     * @return La posición X del personaje.
     */
    public float getXAnterior() { return xAnterior; }
    public float getYAnterior() { return yAnterior; }
    public float getVelocidadMovimiento() { return velocidadMovimiento; }
    public void setVelocidadMovimiento(float velocidad) { this.velocidadMovimiento = velocidad; }
    public boolean estaMirandoDerecha() { return mirandoDerecha; }
    public void setMirandoDerecha(boolean mirandoDerecha) { this.mirandoDerecha = mirandoDerecha; }
    public int getIdJugador() { return idJugador; }
    public void setPosicion(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setPosicionAnterior(float x, float y) {
        this.xAnterior = x;
        this.yAnterior = y;
    }

    public TextureRegion getFrameActual() {
        return animacionActual.getKeyFrame(tiempoEstado, true);
    }

    public String getNombreAnimacionActual() {
        if (animacionActual == idleAnimation) return "idle";
        if (animacionActual == runAnimation) return "run";
        if (animacionActual == jumpAnimation) return "jump";
        if (animacionActual == rollAnimation) return "roll";
        if (this instanceof Sonic && ((Sonic) this).estaGirando) return "spin";
        if (this instanceof Knockles && ((Knockles) this).estaPunetazo) return "punch";
        return "unknown";
    }

    public float getTiempoEstadoAnimacion() {
        return tiempoEstado;
    }

    public void setTiempoEstadoAnimacion(float tiempoEstado) {
        this.tiempoEstado = tiempoEstado;
    }

    public Rectangle getLimites() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public float getWidth() {
        return animacionActual.getKeyFrame(0).getRegionWidth();
    }

    public float getHeight() {
        return animacionActual.getKeyFrame(0).getRegionHeight();
    }
}
