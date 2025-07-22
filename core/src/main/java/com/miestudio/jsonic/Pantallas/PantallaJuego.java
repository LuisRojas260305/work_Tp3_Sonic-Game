package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.miestudio.jsonic.Actores.Knockles;
import com.miestudio.jsonic.Actores.Personajes;

import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Utilidades.Recursos;
import com.miestudio.jsonic.Utilidades.GestorColisiones;
import com.miestudio.jsonic.Utilidades.Constantes;
import com.miestudio.jsonic.Utilidades.EstadoJuego;
import com.miestudio.jsonic.Utilidades.EstadoEntrada;
import com.miestudio.jsonic.Utilidades.EstadoJugador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pantalla principal del juego donde se desarrolla la acción.
 * Se encarga de renderizar el estado del juego, gestionar los inputs del jugador,
 * y sincronizar el estado entre el host y los clientes.
 */
public class PantallaJuego implements Screen {

    private final JuegoSonic juego;
    private final int idJugadorLocal;
    private final boolean esHost;

    private final OrthographicCamera camara;
    private final SpriteBatch lote;
    private final ShapeRenderer renderizadorFormas;

    private final ConcurrentHashMap<Integer, Personajes> personajes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, EstadoEntrada> entradasJugador;

    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderizadorMapa;
    private GestorColisiones gestorColisiones;

    private float anchoMapa, altoMapa; // Dimensiones del mapa

    public PantallaJuego(JuegoSonic juego, int idJugadorLocal) {
        this.juego = juego;
        this.idJugadorLocal = idJugadorLocal;
        this.esHost = (idJugadorLocal == 0);

        this.lote = new SpriteBatch();
        this.renderizadorFormas = new ShapeRenderer();

        this.entradasJugador = juego.gestorRed.getEntradasJugador();

        mapa = new TmxMapLoader().load(Constantes.MAPA_PATH + "Mapa.tmx");
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa);

        TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(0);
        anchoMapa = capa.getWidth() * capa.getTileWidth();
        altoMapa = capa.getHeight() * capa.getTileHeight();

        this.camara = new OrthographicCamera();
        camara.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camara.update();

        gestorColisiones = new GestorColisiones(mapa, "Colisiones", anchoMapa, altoMapa);
        gestorColisiones.anadirColisionesTiles(mapa, "Colisiones");

        inicializarPersonajes();

        if (esHost) {
            new Thread(this::bucleJuegoServidor).start();
        }
    }

    /**
     * Inicializa las instancias de los personajes para todos los jugadores.
     * Asigna un personaje a cada ID de jugador y establece sus posiciones iniciales.
     */
    private void inicializarPersonajes() {
        Recursos recursos = juego.getRecursos();

        // Asignar personajes a IDs fijos
        Personajes sonic = new Sonic(0, recursos.sonicAtlas);
        Personajes tails = new Tails(1, recursos.tailsAtlas);
        Personajes knockles = new Knockles(2, recursos.knocklesAtlas);

        // Posicionar Sonic
        Vector2 sonicSpawn = encontrarPuntosAparicion("Sonic").getOrDefault("Sonic", new Vector2(anchoMapa * 0.1f, altoMapa * 0.5f));
        float sueloYSonic = gestorColisiones.obtenerSueloY(new Rectangle(sonicSpawn.x, sonicSpawn.y, sonic.getWidth(), sonic.getHeight()));
        sonic.setPosicion(sonicSpawn.x, sueloYSonic >= 0 ? sueloYSonic : sonicSpawn.y);
        sonic.setPosicionAnterior(sonic.getX(), sonic.getY());
        personajes.put(0, sonic);

        // Posicionar Tails
        Vector2 tailsSpawn = encontrarPuntosAparicion("Tails").getOrDefault("Tails", new Vector2(anchoMapa * 0.2f, altoMapa * 0.5f));
        float sueloYTails = gestorColisiones.obtenerSueloY(new Rectangle(tailsSpawn.x, tailsSpawn.y, tails.getWidth(), tails.getHeight()));
        tails.setPosicion(tailsSpawn.x, sueloYTails >= 0 ? sueloYTails : tailsSpawn.y);
        tails.setPosicionAnterior(tails.getX(), tails.getY());
        personajes.put(1, tails);

        // Posicionar Knuckles
        Vector2 knucklesSpawn = encontrarPuntosAparicion("Knuckles").getOrDefault("Knuckles", new Vector2(anchoMapa * 0.3f, altoMapa * 0.5f));
        float sueloYKnuckles = gestorColisiones.obtenerSueloY(new Rectangle(knucklesSpawn.x, knucklesSpawn.y, knockles.getWidth(), knockles.getHeight()));
        knockles.setPosicion(knucklesSpawn.x, sueloYKnuckles >= 0 ? sueloYKnuckles : knucklesSpawn.y);
        knockles.setPosicionAnterior(knockles.getX(), knockles.getY());
        personajes.put(2, knockles);
    }

    /**
     * Encuentra los puntos de spawn de entidades en la capa "SpawnEntidades" del mapa.
     * Los puntos de spawn se definen como tiles con la propiedad "Spawn" establecida a true,
     * y una propiedad adicional "To" (String) para identificar el tipo de entidad.
     * @param entidadBuscada Opcional. Si se proporciona, solo se buscarán puntos de spawn para esta entidad.
     * @return Un mapa donde la clave es el nombre de la entidad y el valor es su posición de spawn (Vector2).
     */
    private Map<String, Vector2> encontrarPuntosAparicion(String entidadBuscada) {
        Map<String, Vector2> puntosAparicion = new HashMap<>();
        MapLayer capa = mapa.getLayers().get("SpawnEntidades");

        if (capa == null || !(capa instanceof TiledMapTileLayer)) {
            Gdx.app.error("PantallaJuego", "No se encontró la capa de tiles 'SpawnEntidades'.");
            return puntosAparicion;
        }

        TiledMapTileLayer capaTiles = (TiledMapTileLayer) capa;
        float anchoTile = capaTiles.getTileWidth();
        float altoTile = capaTiles.getTileHeight();

        for (int y = 0; y < capaTiles.getHeight(); y++) {
            for (int x = 0; x < capaTiles.getWidth(); x++) {
                TiledMapTileLayer.Cell celda = capaTiles.getCell(x, y);
                if (celda == null || celda.getTile() == null) {
                    continue;
                }

                com.badlogic.gdx.maps.MapProperties propiedades = celda.getTile().getProperties();
                if (propiedades.get("Spawn", false, Boolean.class)) {
                    String nombreEntidad = propiedades.get("To", String.class);

                    if (nombreEntidad != null && (entidadBuscada == null || nombreEntidad.equals(entidadBuscada))) {
                        float spawnX = x * anchoTile;
                        float spawnY = y * altoTile;
                        puntosAparicion.put(nombreEntidad, new Vector2(spawnX, spawnY));
                        Gdx.app.log("PantallaJuego", "Spawn encontrado para " + nombreEntidad + " en (" + spawnX + ", " + spawnY + ")");
                    }
                }
            }
        }
        return puntosAparicion;
    }

    /**
     * Bucle principal del juego que se ejecuta en un hilo separado solo en el servidor (Host).
     * Se encarga de actualizar el estado del juego a una tasa fija (60 FPS).
     */
    private void bucleJuegoServidor() {
        float acumulador = 0f;
        long ultimoTiempo = System.nanoTime();
        final float DELTA_TIEMPO_GDX = 1 / 60f;

        while (esHost) {
            long ahora = System.nanoTime();
            acumulador += (ahora - ultimoTiempo) / 1_000_000_000f;
            ultimoTiempo = ahora;

            while (acumulador >= DELTA_TIEMPO_GDX) {
                synchronized (personajes) {
                    actualizarEstadoJuego(DELTA_TIEMPO_GDX);
                }
                acumulador -= DELTA_TIEMPO_GDX;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Actualiza el estado del juego en el servidor.
     * Procesa los inputs de los jugadores, actualiza la física de los personajes
     * y envía el estado actualizado a todos los clientes.
     *
     * @param delta El tiempo transcurrido desde la última actualización en segundos.
     */
    private void actualizarEstadoJuego(float delta) {
        for (Personajes character : personajes.values()) {
            EstadoEntrada entrada = entradasJugador.get(character.getIdJugador());

            if (entrada != null) {
                character.setPosicionAnterior(character.getX(), character.getY());
                character.update(delta, gestorColisiones);
                character.manejarEntrada(entrada, gestorColisiones, delta);

                float nuevaX = Math.max(0, Math.min(character.getX(), anchoMapa - character.getWidth()));
                float nuevaY = Math.max(0, Math.min(character.getY(), altoMapa - character.getHeight()));
                character.setPosicion(nuevaX, nuevaY);
            }
        }

        ArrayList<EstadoJugador> estadosJugador = new ArrayList<>();
        for (Personajes character : personajes.values()) {
            estadosJugador.add(new EstadoJugador(
                character.getIdJugador(),
                character.getX(), character.getY(),
                character.estaMirandoDerecha(),
                character.getNombreAnimacionActual(),
                character.getTiempoEstadoAnimacion()));
        }
        juego.gestorRed.enviarEstadoJuegoUdpBroadcast(new EstadoJuego(estadosJugador));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Personajes jugadorLocal;
        synchronized (personajes) {
            jugadorLocal = personajes.get(idJugadorLocal);
        }

        if (jugadorLocal != null) {
            float mitadAnchoCamara = camara.viewportWidth / 2;
            float mitadAltoCamara = camara.viewportHeight / 2;

            float camaraX = Math.max(mitadAnchoCamara,
                Math.min(jugadorLocal.getX(), anchoMapa - mitadAnchoCamara));
            float camaraY = Math.max(mitadAltoCamara,
                Math.min(jugadorLocal.getY(), altoMapa - mitadAltoCamara));

            camara.position.set((int)camaraX, (int)camaraY, 0);
        }
        camara.update();

        renderizadorMapa.setView(camara);
        renderizadorMapa.render();

        procesarEntrada(delta);

        lote.setProjectionMatrix(camara.combined);
        lote.begin();
        synchronized (personajes) {
            for (Personajes personaje : personajes.values()) {
                TextureRegion frame = personaje.getFrameActual();

                if (!personaje.estaMirandoDerecha() && !frame.isFlipX()) {
                    frame.flip(true, false);
                } else if (personaje.estaMirandoDerecha() && frame.isFlipX()) {
                    frame.flip(true, false);
                }

                lote.draw(frame, personaje.getX(), personaje.getY());
            }
        }
        lote.end();
    }

    /**
     * Procesa la entrada del usuario, ya sea para el host (registrando el input) o para el cliente
     * (enviando el input al servidor y realizando predicción local).
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     */
    private void procesarEntrada(float delta) {
        if (esHost) {
            EstadoEntrada entradaHost = new EstadoEntrada();
            entradaHost.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            entradaHost.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            entradaHost.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            entradaHost.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            entradaHost.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            entradaHost.setIdJugador(idJugadorLocal);
            entradasJugador.put(idJugadorLocal, entradaHost);
        } else {
            EstadoEntrada entradaLocal = new EstadoEntrada();
            entradaLocal.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            entradaLocal.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            entradaLocal.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            entradaLocal.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            entradaLocal.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            entradaLocal.setIdJugador(idJugadorLocal);
            juego.gestorRed.enviarEstadoEntrada(entradaLocal);

            Personajes jugadorLocal = personajes.get(idJugadorLocal);
            if (jugadorLocal != null) {
                jugadorLocal.update(delta, gestorColisiones);
                jugadorLocal.manejarEntrada(entradaLocal, gestorColisiones, delta);
            }

            actualizarPersonajesDesdeEstado();
        }
    }

    /**
     * Renderiza las formas de colisión para depuración visual.
     * Este método es opcional y solo debe usarse para fines de depuración.
     */
    private void depurarRenderizadoColisiones(){
        renderizadorFormas.setProjectionMatrix(camara.combined);
        renderizadorFormas.begin(ShapeRenderer.ShapeType.Line);

        Array<Shape2D> copiaFormas = new Array<>();
        copiaFormas.addAll(gestorColisiones.getFormasColision());

        for (Shape2D forma : copiaFormas) {
            if (forma instanceof Rectangle) {
                Rectangle rect = (Rectangle) forma;
                renderizadorFormas.rect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        renderizadorFormas.end();
    }

    

    /**
     * Actualiza las posiciones y estados de los personajes en el cliente según el GameState recibido del servidor.
     * Realiza reconciliación para el jugador local y interpolación para los jugadores remotos.
     */
    private void actualizarPersonajesDesdeEstado() {
        EstadoJuego estadoJuego = juego.gestorRed.getEstadoJuegoActual();
        if (estadoJuego != null) {
            synchronized (personajes) {
                for (EstadoJugador estadoJugador : estadoJuego.getJugadores()) {
                    Personajes character = personajes.get(estadoJugador.getIdJugador());
                    if (character == null) continue;

                    if (estadoJugador.getIdJugador() == idJugadorLocal) {
                        float margenError = 0.5f;
                        float factorInterpolacionLocal = 0.7f; // Más agresivo para el jugador local

                        // Interpolación para el jugador local si hay una desviación significativa
                        if (Vector2.dst(character.getX(), character.getY(), estadoJugador.getX(), estadoJugador.getY()) > margenError) {
                            character.setPosicion(
                                character.getX() + (estadoJugador.getX() - character.getX()) * factorInterpolacionLocal,
                                character.getY() + (estadoJugador.getY() - character.getY()) * factorInterpolacionLocal
                            );
                        }
                        // El resto de estados (animación, dirección) se actualizan directamente.
                        character.setMirandoDerecha(estadoJugador.estaMirandoDerecha());

                        switch (estadoJugador.getNombreAnimacionActual()) {
                            case "idle":
                                character.animacionActual = character.idleAnimation;
                                break;
                            case "run":
                                character.animacionActual = character.runAnimation;
                                break;
                            case "jump":
                                character.animacionActual = character.jumpAnimation;
                                break;
                            case "roll":
                                character.animacionActual = character.rollAnimation;
                                break;
                            case "spin":
                                if (character instanceof Sonic) {
                                    character.animacionActual = ((Sonic) character).animacionSpinDash;
                                }
                                break;
                            case "punch":
                                if (character instanceof Knockles) {
                                    character.animacionActual = ((Knockles) character).animacionPunetazo;
                                }
                                break;
                        }

                    } else {
                        float factorInterpolacion = 0.5f; // Aumentado para personajes remotos
                        character.setPosicion(
                            character.getX() + (estadoJugador.getX() - character.getX()) * factorInterpolacion,
                            character.getY() + (estadoJugador.getY() - character.getY()) * factorInterpolacion
                        );
                        character.setMirandoDerecha(estadoJugador.estaMirandoDerecha());

                        switch (estadoJugador.getNombreAnimacionActual()) {
                            case "idle":
                                character.animacionActual = character.idleAnimation;
                                break;
                            case "run":
                                character.animacionActual = character.runAnimation;
                                break;
                            case "jump":
                                character.animacionActual = character.jumpAnimation;
                                break;
                            case "roll":
                                character.animacionActual = character.rollAnimation;
                                break;
                            case "spin":
                                if (character instanceof Sonic) {
                                    character.animacionActual = ((Sonic) character).animacionSpinDash;
                                }
                                break;
                            case "punch":
                                if (character instanceof Knockles) {
                                    character.animacionActual = ((Knockles) character).animacionPunetazo;
                                }
                                break;
                        }
                        character.setTiempoEstadoAnimacion(
                            character.getTiempoEstadoAnimacion() + (estadoJugador.getTiempoEstadoAnimacion() - character.getTiempoEstadoAnimacion()) * factorInterpolacion
                        );
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // Mantener relación de aspecto del mapa
        float relacionAspecto = anchoMapa / altoMapa;
        float anchoViewport = width;
        float altoViewport = width / relacionAspecto;

        if (altoViewport > height) {
            altoViewport = height;
            anchoViewport = height * relacionAspecto;
        }

        camara.viewportWidth = anchoViewport;
        camara.viewportHeight = altoViewport;
        camara.position.set(camara.viewportWidth / 2, camara.viewportHeight / 2, 0);
        camara.update();
    }

    @Override
    public void dispose() {
        lote.dispose();
        renderizadorFormas.dispose();
        renderizadorMapa.dispose();
        for (Personajes personaje : personajes.values()) {
            personaje.dispose();
        }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

