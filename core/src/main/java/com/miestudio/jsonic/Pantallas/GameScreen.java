package com.miestudio.jsonic.Pantallas;

/* LibGDX imports */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/* Box2D imports */
import com.badlogic.gdx.physics.box2d.*;

/* Tu juego y utilidades */
import com.miestudio.jsonic.Actores.Knockles;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Util.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameScreen implements Screen {

    private final JuegoSonic game;
    private final int localPlayerId;
    private final boolean isHost;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final ConcurrentHashMap<Integer, Personajes> characters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, InputState> playerInputs;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // Box2D
    private World world;
    private Box2DDebugRenderer debugRenderer;

    public GameScreen(JuegoSonic game, int localPlayerId) {
        this.game = game;
        this.localPlayerId = localPlayerId;
        this.isHost = (localPlayerId == 0);

        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = new SpriteBatch();

        this.playerInputs = game.networkManager.getPlayerInputs();

        // Carga del mapa y renderer
        map = new TmxMapLoader().load(Constantes.MAPA_PATH + "Mapa.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        // Inicializa Box2D world
        world = new World(new Vector2(0, -9.8f), true); // gravedad hacia abajo
        debugRenderer = new Box2DDebugRenderer();

        // Crea los cuerpos de colisión para los tiles
        createCollisionBodiesFromTileset(map, "Colisiones"); // Usa el nombre real de tu capa

        // Inicializa los personajes con físico
        initializeCharacters();

        if (isHost) {
            new Thread(this::serverGameLoop).start();
        }
    }

    /**
     * Convierte los tiles con colisión en cuerpos estáticos de Box2D, usando los shapes del tileset y la propiedad Colisiones=true
     */
    private void createCollisionBodiesFromTileset(TiledMap map, String tileLayerName) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(tileLayerName);
        if (layer == null) return;
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell == null) continue;
                TiledMapTile tile = cell.getTile();
                if (tile == null) continue;
                Object colisionProp = tile.getProperties().get("Colisiones");
                boolean tieneColision = colisionProp != null &&
                    (colisionProp.equals(true) || colisionProp.equals("true"));

                if (tieneColision) {
                    // Para cada objeto de colisión en el tile del tileset
                    for (MapObject object : tile.getObjects()) {
                        BodyDef bodyDef = new BodyDef();
                        bodyDef.type = BodyDef.BodyType.StaticBody;
                        // Tile posición global
                        bodyDef.position.set(x * tileWidth, y * tileHeight);

                        Body body = world.createBody(bodyDef);

                        if (object instanceof RectangleMapObject) {
                            Rectangle rect = ((RectangleMapObject) object).getRectangle();
                            PolygonShape shape = new PolygonShape();
                            // Box2D usa centro, no esquina
                            float centerX = rect.x + rect.width/2;
                            float centerY = rect.y + rect.height/2;
                            shape.setAsBox(rect.width/2, rect.height/2, new Vector2(centerX, centerY), 0);
                            body.createFixture(shape, 0.0f);
                            shape.dispose();
                        }
                        if (object instanceof PolygonMapObject) {
                            Polygon poly = ((PolygonMapObject) object).getPolygon();
                            float[] verts = poly.getTransformedVertices();
                            // Transformar a posición global del tile
                            for (int i = 0; i < verts.length; i += 2) {
                                verts[i] += x * tileWidth;
                                verts[i + 1] += y * tileHeight;
                            }
                            PolygonShape shape = new PolygonShape();
                            shape.set(verts);
                            body.createFixture(shape, 0.0f);
                            shape.dispose();
                        }
                        if (object instanceof EllipseMapObject) {
                            Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
                            CircleShape shape = new CircleShape();
                            shape.setRadius(Math.max(ellipse.width, ellipse.height) / 2f);
                            shape.setPosition(new Vector2(ellipse.x + ellipse.width/2, ellipse.y + ellipse.height/2));
                            body.createFixture(shape, 0.0f);
                            shape.dispose();
                        }
                    }
                }
            }
        }
    }

    /**
     * Inicializa las instancias de los personajes para todos los jugadores, ahora usando Box2D Body
     */
    private void initializeCharacters() {
        Assets assets = game.getAssets();
        // Ejemplo: Sonic
        characters.put(0, new Sonic(0, assets.sonicAtlas, world, 100, 200));
        characters.put(1, new Tails(1, assets.tailsAtlas, world, 140, 200));
        characters.put(2, new Knockles(2, assets.knocklesAtlas, world, 180, 200));
    }

    private void serverGameLoop() {
        float accumulator = 0f;
        long lastTime = System.nanoTime();
        final float Gdx_DELTA_TIME = 1 / 60f;

        while (isHost) {
            long now = System.nanoTime();
            accumulator += (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            while (accumulator >= Gdx_DELTA_TIME) {
                updateGameState(Gdx_DELTA_TIME);
                accumulator -= Gdx_DELTA_TIME;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateGameState(float delta) {
        // Movimiento de personajes usando Box2D
        for (Personajes character : characters.values()) {
            InputState input = playerInputs.get(character.getPlayerId());
            if (input != null) {
                character.handleInput(input); // Ahora mueve el Body
            }
            character.update(delta); // Actualiza animación, estados, etc.
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualiza el mundo de Box2D
        world.step(delta, 6, 2);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Renderiza el mapa Tiled
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Renderiza los personajes según el Body de Box2D
        batch.begin();
        for (Personajes character : characters.values()) {
            character.render(batch); // El render sigue la posición física
        }
        batch.end();

        // Renderiza las físicas (debug)
        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
        mapRenderer.dispose();
        world.dispose();
        debugRenderer.dispose();
        for (Personajes character : characters.values()) {
            character.dispose();
        }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
