package com.miestudio.jsonic;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.miestudio.jsonic.Pantallas.MainScreen;
import com.miestudio.jsonic.Server.NetworkManager;
import com.miestudio.jsonic.Util.Assets;

/**
 * Clase principal del juego que extiende la clase Game de LibGDX.
 * Controla la gestión global del juego, inicializa el NetworkManager
 * y establece la pantalla inicial.
 */
public class JuegoSonic extends Game {

    /** Gestor de red para manejar la creación del host y la conexión de clientes. */
    public NetworkManager networkManager;
    /** Gestor de assets para cargar y liberar recursos. */
    private Assets assets;

    /**
     * Método principal de inicialización del juego.
     * Crea el gestor de red y establece la pantalla de menú principal.
     */
    @Override
    public void create() {
        assets = new Assets();
        assets.load(); // Cargar todos los assets al inicio

        networkManager = new NetworkManager(this);
        // Al iniciar, siempre mostramos la pantalla para elegir rol.
        setScreen(new MainScreen(this));
    }

    /**
     * Libera los recursos del juego cuando es destruido.
     * Se encarga de liberar los recursos de red y los assets.
     */
    @Override
    public void dispose() {
        if (networkManager != null) {
            networkManager.dispose();
            // Si este es el host, asegúrate de que la aplicación se cierre completamente.
            // Gdx.app.exit() debe ser llamado desde el hilo principal de LibGDX.
            // El dispose() de JuegoSonic ya se llama desde el hilo principal.
            if (networkManager.isHost()) {
                Gdx.app.exit();
            }
        }
        if (assets != null) {
            assets.dispose();
        }
        super.dispose();
    }

    /**
     * Obtiene la instancia del gestor de assets.
     * @return La instancia de la clase Assets.
     */
    public Assets getAssets() {
        return assets;
    }
}