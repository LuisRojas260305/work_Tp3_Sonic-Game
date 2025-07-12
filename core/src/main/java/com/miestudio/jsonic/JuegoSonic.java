package com.miestudio.jsonic;

// Importar paquetes
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.miestudio.jsonic.Server.NetworkManager;

/**
 * Clase principal del juego que extiende la clase Game de LibGDX
 * Controla la gestion global del juego y las diferentes pantallas
 */
public class JuegoSonic extends Game{
    /** Atributos */

    /** Gestor de red para manejar conexiones LAN */
    public NetworkManager networkManager;

    /**
     * Metodo principal de inicializacion dej juego
     * Crea el gestor de red y verifica el estado de la red
     */
    @Override
    public void create() {
        networkManager = new NetworkManager(this);
        networkManager.checkNetworkStatus();
    }

    /**
     * Libera los recursos del juego cuando es destruido.
     * Se encarga de liberar los recursos de red.
     */
    @Override
    public void dispose() {
        networkManager.dispose();
        super.dispose();
    }
}
