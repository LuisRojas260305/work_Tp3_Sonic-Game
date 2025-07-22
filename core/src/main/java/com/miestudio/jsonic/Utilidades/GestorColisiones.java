package com.miestudio.jsonic.Utilidades;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

public class GestorColisiones {
    private static final float FEET_SENSOR_Y_OFFSET_GROUNDED = -10f;
    private static final float FEET_SENSOR_HEIGHT_GROUNDED = 15f;
    private static final float FEET_SENSOR_Y_OFFSET_GROUNDY = -20f;
    private static final float FEET_SENSOR_HEIGHT_GROUNDY = 25f;

    private Array<Shape2D> formasColision;
    private final float anchoMapa;
    private final float altoMapa;

    public GestorColisiones(TiledMap mapa, String nombreCapaObjeto, float anchoMapa, float altoMapa) {
        this.anchoMapa = anchoMapa;
        this.altoMapa = altoMapa;
        formasColision = new Array<>();
        MapLayer capa = mapa.getLayers().get(nombreCapaObjeto);
        if (capa != null) {
            for (MapObject objeto : capa.getObjects()) {
                if (objeto instanceof RectangleMapObject) {
                    formasColision.add(((RectangleMapObject) objeto).getRectangle());
                }
            }
        }
    }

    /**
     * Añade las colisiones definidas en los tiles de una capa de tiles.
     * Llama este método después de crear CollisionManager si usas colisiones de tiles.
     */
    public void anadirColisionesTiles(TiledMap mapa, String nombreCapaTiles) {
        TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(nombreCapaTiles);

        if (capa == null) return;

        for (int x = 0; x < capa.getWidth(); x++){
            for (int y = 0; y < capa.getHeight(); y++){
                TiledMapTileLayer.Cell celda = capa.getCell(x, y);

                if (celda == null) continue;

                Object propiedadColision = celda.getTile().getProperties().get("Colision");
                boolean colision = false;

                if (propiedadColision instanceof Boolean){
                    colision = (Boolean) propiedadColision;
                } else if (propiedadColision instanceof String) {
                    colision = Boolean.parseBoolean((String) propiedadColision);
                }

                if (colision){

                    Rectangle rectanguloTile = new Rectangle(
                            x * capa.getTileWidth(),
                            y * capa.getTileHeight(),
                            capa.getTileWidth(),
                            capa.getTileHeight()
                    );

                    formasColision.add(rectanguloTile);
                }
            }
        }
    }

    public boolean colisiona(Rectangle rect) {
        if (rect.x < 0 || rect.y < 0 ||
                rect.x + rect.width > anchoMapa ||
                rect.y + rect.height > altoMapa) {
            return true;
        }

        for (Shape2D forma : formasColision) {
            if (forma instanceof Rectangle) {
                if (rect.overlaps((Rectangle) forma)) return true;
            }
        }
        return false;
    }

    public boolean estaEnSuelo(Rectangle rectPersonaje){
        Rectangle sensorPies = new Rectangle(
                rectPersonaje.x + rectPersonaje.width / 4,
                rectPersonaje.y + FEET_SENSOR_Y_OFFSET_GROUNDED,
                rectPersonaje.width / 2,
                FEET_SENSOR_HEIGHT_GROUNDED
        );

        return colisiona(sensorPies);
    }

    public float obtenerSueloY(Rectangle rectPersonaje) {
        Rectangle sensorPies = new Rectangle(
                rectPersonaje.x + rectPersonaje.width / 4,
                rectPersonaje.y + FEET_SENSOR_Y_OFFSET_GROUNDY,
                rectPersonaje.width / 2,
                FEET_SENSOR_HEIGHT_GROUNDY
        );

        float maximoSueloY = -1;

        for (Shape2D forma : formasColision) {
            if (forma instanceof Rectangle) {
                Rectangle rect = (Rectangle) forma;
                if (sensorPies.overlaps(rect)) {
                    float top = rect.y + rect.height;
                    if (top > maximoSueloY) {
                        maximoSueloY = top;
                    }
                }
            }
        }

        if (rectPersonaje.y < 0) {
            maximoSueloY = Math.max(maximoSueloY, 0);
        }

        return maximoSueloY;
    }

    // Helper para convertir Rectangle a array de vértices de Polygon
    private float[] rectToVertices(Rectangle rect) {
        return new float[] {
                rect.x, rect.y,
                rect.x + rect.width, rect.y,
                rect.x + rect.width, rect.y + rect.height,
                rect.x, rect.y + rect.height
        };
    }

    public float getAnchoMapa() {
        return anchoMapa;
    }

    public float getAltoMapa() {
        return altoMapa;
    }

    public Array<Shape2D> getFormasColision() {
        return formasColision;
    }
}
