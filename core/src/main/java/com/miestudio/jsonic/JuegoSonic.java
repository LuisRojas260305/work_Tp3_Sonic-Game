package com.miestudio.jsonic;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.miestudio.jsonic.Pantallas.PantallaPrincipal;
import com.miestudio.jsonic.Servidor.GestorRed;
import com.miestudio.jsonic.Utilidades.Recursos;
import com.miestudio.jsonic.Utilidades.PaqueteApagado;

/**
 * Clase principal del juego que extiende la clase Game de LibGDX.
 * Controla la gestión global del juego, inicializa el NetworkManager
 * y establece la pantalla inicial.
 */
public class JuegoSonic extends Game {

    /** Gestor de red para manejar la creación del host y la conexión de clientes. */
    public GestorRed gestorRed;
    /** Gestor de assets para cargar y liberar recursos. */
    private Recursos recursos;

    /**
     * Método principal de inicialización del juego.
     * Crea el gestor de red y establece la pantalla de menú principal.
     */
    @Override
    public void create() {
        recursos = new Recursos();
        recursos.cargar(); // Cargar todos los assets al inicio

        gestorRed = new GestorRed(this);
        // Al iniciar, siempre mostramos la pantalla para elegir rol.
        setScreen(new PantallaPrincipal(this));
    }

    /**
     * Libera los recursos del juego cuando es destruido.
     * Se encarga de liberar los recursos de red y los assets.
     */
    @Override
    public void dispose() {
        if (gestorRed != null) {
            if (gestorRed.esHost()) {
                gestorRed.enviarMensajeTcpBroadcast(new PaqueteApagado());
            }
            gestorRed.dispose();
            Gdx.app.exit();
        }
        if (recursos != null) {
            recursos.dispose();
        }
        super.dispose();
    }

    /**
     * Obtiene la instancia del gestor de assets.
     * @return La instancia de la clase Assets.
     */
    public Recursos getRecursos() {
        return recursos;
    }
}