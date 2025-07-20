package com.miestudio.jsonic.Util;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

public class CollisionManager {
    private static final float FEET_SENSOR_Y_OFFSET_GROUNDED = -10f;
    private static final float FEET_SENSOR_HEIGHT_GROUNDED = 15f;
    private static final float FEET_SENSOR_Y_OFFSET_GROUNDY = -20f;
    private static final float FEET_SENSOR_HEIGHT_GROUNDY = 25f;

    private Array<Shape2D> collisionShapes;
    private final float mapWidth;
    private final float mapHeight;

    public CollisionManager(TiledMap map, String objectLayerName, float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        collisionShapes = new Array<>();
        // Cargar colisiones desde la Object Layer estándar (si existe)
        MapLayer layer = map.getLayers().get(objectLayerName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    collisionShapes.add(((RectangleMapObject) object).getRectangle());
                }
            }
        }
    }

    /**
     * Añade las colisiones definidas en los tiles de una capa de tiles.
     * Llama este método después de crear CollisionManager si usas colisiones de tiles.
     */
    public void addTileCollisions(TiledMap map, String tileLayerName) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(tileLayerName);

        if (layer == null) return;

        for (int x = 0; x < layer.getWidth(); x++){
            for (int y = 0; y < layer.getHeight(); y++){
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);

                if (cell == null) continue;

                Object collisionProp = cell.getTile().getProperties().get("Colisiones");
                boolean colision = false;

                if (collisionProp instanceof Boolean){
                    colision = (Boolean) collisionProp;
                } else if (collisionProp instanceof String) {
                    colision = Boolean.parseBoolean((String) collisionProp);
                }

                if (colision){

                    Rectangle tileRect = new Rectangle(
                            x * layer.getTileWidth(),
                            y * layer.getTileHeight(),
                            layer.getTileWidth(),
                            layer.getTileHeight()
                    );

                    collisionShapes.add(tileRect);
                }
            }
        }
    }

    public boolean collides(Rectangle rect) {
        // Verificar bordes del mapa
        if (rect.x < 0 || rect.y < 0 ||
                rect.x + rect.width > mapWidth ||
                rect.y + rect.height > mapHeight) {
            return true;
        }

        // Verificar colisiones con objetos
        for (Shape2D shape : collisionShapes) {
            if (shape instanceof Rectangle) {
                if (rect.overlaps((Rectangle) shape)) return true;
            }
        }
        return false;
    }

    public boolean isOnGround(Rectangle characterRect){
        // Área de detección más grande para mejor precisión
        Rectangle feetSensor = new Rectangle(
                characterRect.x + characterRect.width / 4,
                characterRect.y + FEET_SENSOR_Y_OFFSET_GROUNDED,  // Mayor área de detección
                characterRect.width / 2,
                FEET_SENSOR_HEIGHT_GROUNDED  // Mayor altura para mejor detección
        );

        return collides(feetSensor);
    }

    public float getGroundY(Rectangle characterRect) {
        // Área de detección más grande para mejor precisión
        Rectangle feetSensor = new Rectangle(
                characterRect.x + characterRect.width / 4,
                characterRect.y + FEET_SENSOR_Y_OFFSET_GROUNDY,  // Mayor área de detección
                characterRect.width / 2,
                FEET_SENSOR_HEIGHT_GROUNDY  // Mayor altura para mejor detección
        );

        float maxGroundY = -1; // Valor inicial

        // Verificar colisiones con objetos
        for (Shape2D shape : collisionShapes) {
            if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                if (feetSensor.overlaps(rect)) {
                    // La parte superior del rectángulo de colisión
                    float top = rect.y + rect.height;
                    if (top > maxGroundY) {
                        maxGroundY = top;
                    }
                }
            }
        }

        // Verificar borde inferior del mapa
        if (characterRect.y < 0) {
            maxGroundY = Math.max(maxGroundY, 0);
        }

        return maxGroundY;
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

    public float getMapWidth() {
        return mapWidth;
    }

    public float getMapHeight() {
        return mapHeight;
    }

    // (Optional) Method for visual debugging
    public Array<Shape2D> getCollisionShapes() {
        return collisionShapes;
    }
}
