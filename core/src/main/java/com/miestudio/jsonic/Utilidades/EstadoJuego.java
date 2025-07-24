package com.miestudio.jsonic.Utilidades;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representa el estado completo del juego en un momento específico.
 * Esta clase es serializable para poder ser enviada desde el servidor a los clientes.
 * Contiene un mapa de los estados de todos los jugadores.
 */
public class EstadoJuego implements Serializable {

    /**
     * Número de versión para la serialización. Ayuda a asegurar que el servidor y el cliente
     * están usando la misma versión de la clase, evitando errores de incompatibilidad.
     */
    private static final long serialVersionUID = 3L; // Version incrementada por cambio de List a Map

    /** Usamos ConcurrentHashMap para manejar el acceso seguro desde múltiples hilos de red. */
    private ConcurrentHashMap<Integer, EstadoJugador> jugadores;
    /** Marca de tiempo (timestamp) de cuando se generó este estado en el servidor. */
    private long timestamp;

    // Constructor por defecto para inicializar el estado.
    public EstadoJuego() {
        this.jugadores = new ConcurrentHashMap<>();
        this.timestamp = System.nanoTime();
    }

    public ConcurrentHashMap<Integer, EstadoJugador> getJugadores() {
        return jugadores;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Añade un nuevo jugador o actualiza el estado de uno existente.
     * @param jugador El estado del jugador a añadir o actualizar.
     */
    public void agregarOActualizarJugador(EstadoJugador jugador) {
        if (jugador != null) {
            jugadores.put(jugador.getIdJugador(), jugador);
        }
    }

    /**
     * Elimina un jugador del estado del juego usando su ID.
     * @param idJugador El ID del jugador a eliminar.
     */
    public void eliminarJugador(int idJugador) {
        jugadores.remove(idJugador);
    }
}
