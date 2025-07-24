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
import com.miestudio.jsonic.Objetos.ObjetoBasura;
import com.miestudio.jsonic.Utilidades.Recursos;
import com.miestudio.jsonic.Utilidades.GestorColisiones;
import com.miestudio.jsonic.Utilidades.Constantes;
import com.miestudio.jsonic.Utilidades.EstadoJuego;
import com.miestudio.jsonic.Utilidades.EstadoEntrada;
import com.miestudio.jsonic.Utilidades.EstadoJugador;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

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
    private final ConcurrentHashMap<Integer, ObjetoBasura> basuras = new ConcurrentHashMap<>(); // Cambiado a ConcurrentHashMap
    private final ConcurrentHashMap<Integer, EstadoEntrada> entradasJugador;

    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderizadorMapa;
    private GestorColisiones gestorColisiones;

    private float anchoMapa, altoMapa; // Dimensiones del mapa

    private BitmapFont font; // Fuente para el contador
    private int nextBasuraId = 0; // ID para asignar a los objetos de basura

    public PantallaJuego(JuegoSonic juego, int idJugadorLocal) {
        this.juego = juego;
        this.idJugadorLocal = idJugadorLocal;
        this.esHost = (idJugadorLocal == 0);

        this.lote = new SpriteBatch();
        this.renderizadorFormas = new ShapeRenderer();
        this.font = new BitmapFont(); // Inicializar la fuente
        this.font.setColor(Color.WHITE);

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
        inicializarBasuras(); 

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
        Map<String, Vector2> puntosAparicion = encontrarPuntosAparicion();

        // Asignar personajes a IDs fijos
        Personajes sonic = new Sonic(0, recursos.sonicAtlas);
        Personajes tails = new Tails(1, recursos.tailsAtlas);
        Personajes knockles = new Knockles(2, recursos.knocklesAtlas);

        // Posicionar Sonic
        Vector2 sonicSpawn = puntosAparicion.getOrDefault("Sonic", new Vector2(anchoMapa * 0.1f, altoMapa * 0.5f));
        float sueloYSonic = gestorColisiones.obtenerSueloY(new Rectangle(sonicSpawn.x, sonicSpawn.y, sonic.getWidth(), sonic.getHeight()));
        sonic.setPosicion(sonicSpawn.x, sueloYSonic >= 0 ? sueloYSonic : sonicSpawn.y);
        sonic.setPosicionAnterior(sonic.getX(), sonic.getY());
        personajes.put(0, sonic);

        // Posicionar Tails
        Vector2 tailsSpawn = puntosAparicion.getOrDefault("Tails", new Vector2(anchoMapa * 0.2f, altoMapa * 0.5f));
        float sueloYTails = gestorColisiones.obtenerSueloY(new Rectangle(tailsSpawn.x, tailsSpawn.y, tails.getWidth(), tails.getHeight()));
        tails.setPosicion(tailsSpawn.x, sueloYTails >= 0 ? sueloYTails : tailsSpawn.y);
        tails.setPosicionAnterior(tails.getX(), tails.getY());
        personajes.put(1, tails);

        // Posicionar Knuckles
        Vector2 knucklesSpawn = puntosAparicion.getOrDefault("Knuckles", new Vector2(anchoMapa * 0.3f, altoMapa * 0.5f));
        float sueloYKnuckles = gestorColisiones.obtenerSueloY(new Rectangle(knucklesSpawn.x, knucklesSpawn.y, knockles.getWidth(), knockles.getHeight()));
        knockles.setPosicion(knucklesSpawn.x, sueloYKnuckles >= 0 ? sueloYKnuckles : knucklesSpawn.y);
        knockles.setPosicionAnterior(knockles.getX(), knockles.getY());
        personajes.put(2, knockles);
    }

    private void inicializarBasuras() {
        Recursos recursos = juego.getRecursos();
        if (recursos.texturasBasura == null || recursos.texturasBasura.size == 0) {
            Gdx.app.error("PantallaJuego", "No hay texturas de basura cargadas en Recursos.");
            return;
        }
        Array<Vector2> spawns = encontrarSpawnsDeBasura();
        Gdx.app.log("PantallaJuego", "Inicializando basuras. Spawns encontrados: " + spawns.size);
        for (Vector2 spawn : spawns) {
            // Asignar un ID único a cada basura y un spriteIndex aleatorio
            int spriteIndex = MathUtils.random(recursos.texturasBasura.size - 1);
            ObjetoBasura nuevaBasura = new ObjetoBasura(nextBasuraId++, spawn.x, spawn.y, spriteIndex);
            basuras.put(nuevaBasura.getId(), nuevaBasura);
            Gdx.app.log("PantallaJuego", "Basura creada con ID " + nuevaBasura.getId() + " en: " + spawn.x + ", " + spawn.y + " (Sprite Index: " + spriteIndex + ")");
        }
    }

    private Array<Vector2> encontrarSpawnsDeBasura() {
        Array<Vector2> puntosAparicion = new Array<>();
        MapLayer capa = mapa.getLayers().get("SpawnObjetos"); 

        if (capa == null || !(capa instanceof TiledMapTileLayer)) {
            Gdx.app.error("PantallaJuego", "No se encontró la capa de tiles 'SpawnObjetos'.");
            return puntosAparicion;
        }

        TiledMapTileLayer capaTiles = (TiledMapTileLayer) capa;
        float anchoTile = capaTiles.getTileWidth();
        float altoTile = capaTiles.getTileHeight();

        for (int y = 0; y < capaTiles.getHeight(); y++) {
            for (int x = 0; x < capaTiles.getWidth(); x++) {
                TiledMapTileLayer.Cell celda = capaTiles.getCell(x, y);
                if (celda == null || celda.getTile() == null) continue;

                com.badlogic.gdx.maps.MapProperties propiedades = celda.getTile().getProperties();
                if (propiedades.get("Objetos", false, Boolean.class) && propiedades.get("Plastico", false, Boolean.class)) {
                    float spawnX = x * anchoTile + (anchoTile / 2f) - 8f; 
                    float spawnY = y * altoTile + (altoTile / 2f) - 8f; 
                    puntosAparicion.add(new Vector2(spawnX, spawnY));
                    Gdx.app.log("PantallaJuego", "Spawn de basura encontrado en tile: " + x + ", " + y + " -> Posición: " + spawnX + ", " + spawnY);
                }
            }
        }
        return puntosAparicion;
    }

    /**
     * Encuentra los puntos de spawn de los jugadores en la capa "SpawnJugadores" del mapa.
     * Un punto de spawn se define por un tile con la propiedad "Spawn"=true y una propiedad con el nombre del personaje (ej. "Sonic")=true.
     * @return Un mapa donde la clave es el nombre del personaje y el valor es su posición de spawn (Vector2).
     */
    private Map<String, Vector2> encontrarPuntosAparicion() {
        Map<String, Vector2> puntosAparicion = new HashMap<>();
        MapLayer capa = mapa.getLayers().get("SpawnJugadores");

        if (capa == null || !(capa instanceof TiledMapTileLayer)) {
            Gdx.app.error("PantallaJuego", "No se encontró la capa de tiles 'SpawnJugadores'. Usando posiciones por defecto.");
            return puntosAparicion;
        }

        TiledMapTileLayer capaTiles = (TiledMapTileLayer) capa;
        float anchoTile = capaTiles.getTileWidth();
        float altoTile = capaTiles.getTileHeight();
        String[] nombresPersonajes = {"Sonic", "Tails", "Knuckles"};

        for (int y = 0; y < capaTiles.getHeight(); y++) {
            for (int x = 0; x < capaTiles.getWidth(); x++) {
                TiledMapTileLayer.Cell celda = capaTiles.getCell(x, y);
                if (celda == null || celda.getTile() == null) {
                    continue;
                }

                com.badlogic.gdx.maps.MapProperties propiedades = celda.getTile().getProperties();
                if (propiedades.get("Spawn", false, Boolean.class)) {
                    for (String nombrePersonaje : nombresPersonajes) {
                        if (propiedades.get(nombrePersonaje, false, Boolean.class)) {
                            float spawnX = x * anchoTile;
                            float spawnY = y * altoTile;
                            puntosAparicion.put(nombrePersonaje, new Vector2(spawnX, spawnY));
                            Gdx.app.log("PantallaJuego", "Spawn encontrado para " + nombrePersonaje + " en (" + spawnX + ", " + spawnY + ")");
                        }
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
        // Actualizar personajes y detectar colisiones con basura
        Iterator<Map.Entry<Integer, ObjetoBasura>> itBasura = basuras.entrySet().iterator();
        while (itBasura.hasNext()) {
            Map.Entry<Integer, ObjetoBasura> entryBasura = itBasura.next();
            ObjetoBasura basura = entryBasura.getValue();
            basura.actualizar(delta); // Actualizar la posición de flotación

            for (Personajes character : personajes.values()) {
                // Crear un hitbox para el personaje para la detección de colisiones
                Rectangle hitboxPersonaje = new Rectangle(character.getX(), character.getY(), character.getWidth(), character.getHeight());
                
                // Usar el hitbox de la basura directamente
                Rectangle hitboxBasura = basura.hitbox;

                if (hitboxPersonaje.overlaps(hitboxBasura)) {
                    // Colisión detectada
                    Gdx.app.log("PantallaJuego", "Colisión detectada entre " + character.getClass().getSimpleName() + " y basura ID: " + basura.getId());
                    
                    // Incrementar contador de basura del jugador
                    EstadoJugador estadoJugador = juego.gestorRed.getEstadoJuegoActual().getJugadores().get(character.getIdJugador());
                    if (estadoJugador != null) {
                        estadoJugador.setContadorBasura(estadoJugador.getContadorBasura() + 1);
                        Gdx.app.log("PantallaJuego", "Jugador " + character.getIdJugador() + " basura: " + estadoJugador.getContadorBasura());
                    }
                    itBasura.remove(); // Eliminar basura del mapa local del host
                    break; // Un personaje solo puede recoger una basura a la vez
                }
            }
        }

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

        // Crear y enviar el EstadoJuego actualizado
        EstadoJuego estadoJuego = new EstadoJuego();
        // Copiar el estado de los jugadores
        for (Personajes character : personajes.values()) {
            estadoJuego.agregarOActualizarJugador(new EstadoJugador(
                character.getIdJugador(),
                character.getX(), character.getY(),
                character.estaMirandoDerecha(),
                character.getNombreAnimacionActual(),
                character.getTiempoEstadoAnimacion(),
                juego.gestorRed.getEstadoJuegoActual().getJugadores().get(character.getIdJugador()).getContadorBasura() // Obtener el contador actualizado
            ));
        }
        // Copiar el estado de las basuras activas
        estadoJuego.setBasurasActivas(new ConcurrentHashMap<>(basuras)); // Enviar el mapa de basuras actual

        juego.gestorRed.enviarEstadoJuegoUdpBroadcast(estadoJuego);
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
        // Dibujar basuras
        for (ObjetoBasura basura : basuras.values()) { // Iterar sobre los valores del mapa
            // NO LLAMAR basura.actualizar(delta) AQUÍ. Se actualiza en actualizarEstadoJuego
            basura.renderizar(lote, juego.getRecursos().texturasBasura.get(basura.getSpriteIndex())); // Pasar la textura correcta
        }

        // Dibujar contador de basura (dentro del mismo lote.begin/end)
        int yOffset = Gdx.graphics.getHeight() - 20; // Posición inicial Y para el texto
        for (EstadoJugador jugadorEstado : juego.gestorRed.getEstadoJuegoActual().getJugadores().values()) {
            font.draw(lote, "Jugador " + jugadorEstado.getIdJugador() + ": " + jugadorEstado.getContadorBasura() + " basura(s)", 10, yOffset);
            yOffset -= 20; // Mover hacia abajo para el siguiente jugador
        }
        lote.end();

        actualizarPersonajesDesdeEstado();
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
            // Sincronizar basuras activas
            basuras.clear(); // Limpiar el mapa local
            basuras.putAll(estadoJuego.getBasurasActivas()); // Copiar las basuras del servidor

            synchronized (personajes) {
                for (EstadoJugador estadoJugador : estadoJuego.getJugadores().values()) {
                    Personajes character = personajes.get(estadoJugador.getIdJugador());
                    if (character == null) continue;

                    // Actualizar contador de basura del personaje local
                    // No es necesario un setter en Personajes, ya que el EstadoJugador es el que se sincroniza
                    // y el Personaje se actualiza a partir de él.

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
        font.dispose(); // Liberar la fuente
        for (Personajes personaje : personajes.values()) {
            personaje.dispose();
        }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
