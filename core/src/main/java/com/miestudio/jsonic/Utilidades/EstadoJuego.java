package com.miestudio.jsonic.Utilidades;

import java.io.Serializable;
import java.util.List;

/**
 * Representa el estado completo del juego en un momento específico.
 * Esta clase es serializable para poder ser enviada desde el servidor a los clientes.
 * Contiene una lista de los estados de todos los jugadores.
 */
public class EstadoJuego implements Serializable {

    /**
     * Número de versión para la serialización. Ayuda a asegurar que el servidor y el cliente
     * están usando la misma versión de la clase, evitando errores de incompatibilidad.
     */
    private static final long serialVersionUID = 2L;

    /** La lista de los estados de cada jugador en la partida. */
    private List<EstadoJugador> jugadores;
    /** Marca de tiempo (timestamp) de cuando se generó este estado en el servidor. */
    private long timestamp;

    public EstadoJuego(List<EstadoJugador> jugadores, long timestamp) {
        this.jugadores = jugadores;
        this.timestamp = timestamp;
    }

    public EstadoJuego(List<EstadoJugador> jugadores) {
        this(jugadores, System.nanoTime()); // Constructor para compatibilidad, usa el tiempo actual
    }

    public List<EstadoJugador> getJugadores() {
        return jugadores;
    }

    public long getTimestamp() {
        return timestamp;
    }
}