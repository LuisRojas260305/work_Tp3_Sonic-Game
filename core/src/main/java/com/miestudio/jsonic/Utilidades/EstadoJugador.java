package com.miestudio.jsonic.Utilidades;

import java.io.Serializable;

/**
 * Representa el estado de un único jugador.
 * Es serializable para ser incluido en el GameState que se envía a través de la red.
 */
public class EstadoJugador implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idJugador;
    private float x, y;
    private boolean mirandoDerecha;
    private String nombreAnimacionActual;
    private float tiempoEstadoAnimacion;
    private int contadorBasura; // Nuevo campo para el contador de basura

    public EstadoJugador(int idJugador, float x, float y, boolean mirandoDerecha, String nombreAnimacionActual, float tiempoEstadoAnimacion, int contadorBasura) {
        this.idJugador = idJugador;
        this.x = x;
        this.y = y;
        this.mirandoDerecha = mirandoDerecha;
        this.nombreAnimacionActual = nombreAnimacionActual;
        this.tiempoEstadoAnimacion = tiempoEstadoAnimacion;
        this.contadorBasura = contadorBasura;
    }

    public int getIdJugador() { return idJugador; }
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean estaMirandoDerecha() { return mirandoDerecha; }
    public String getNombreAnimacionActual() { return nombreAnimacionActual; }
    public float getTiempoEstadoAnimacion() { return tiempoEstadoAnimacion; }
    public int getContadorBasura() { return contadorBasura; }

    public void setContadorBasura(int contadorBasura) {
        this.contadorBasura = contadorBasura;
    }
}
