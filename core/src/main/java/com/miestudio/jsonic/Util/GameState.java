package com.miestudio.jsonic.Util;

import java.io.Serializable;
import java.util.List;

/**
 * Representa el estado completo del juego en un momento específico.
 * Esta clase es serializable para poder ser enviada desde el servidor a los clientes.
 * Contiene una lista de los estados de todos los jugadores.
 */
public class GameState implements Serializable {

    /**
     * Número de versión para la serialización. Ayuda a asegurar que el servidor y el cliente
     * están usando la misma versión de la clase, evitando errores de incompatibilidad.
     */
    private static final long serialVersionUID = 1L;

    /** La lista de los estados de cada jugador en la partida. */
    private List<PlayerState> players;

    public GameState(List<PlayerState> players) {
        this.players = players;
    }

    public List<PlayerState> getPlayers() {
        return players;
    }
}