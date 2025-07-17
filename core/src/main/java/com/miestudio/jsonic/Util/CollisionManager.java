package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

public class CollisionManager {
    private Array<Shape2D> collisionShapes;
    private static final String TAG = "CollisionManager"; // Tag for logging

    public CollisionManager(TiledMap map, String objectLayerName) {
        collisionShapes = new Array<>();
        Gdx.app.log(TAG, "Iniciando carga de colisiones desde la capa de objetos: " + objectLayerName);
        // Cargar colisiones desde la Object Layer estándar (si existe)
        MapLayer layer = map.getLayers().get(objectLayerName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    collisionShapes.add(rect);
                    Gdx.app.log(TAG, "Cargado Rectangle desde capa de objetos: " + rect);
                } else if (object instanceof PolygonMapObject) {
                    Polygon poly = ((PolygonMapObject) object).getPolygon();
                    collisionShapes.add(poly);
                    Gdx.app.log(TAG, "Cargado Polygon desde capa de objetos: " + poly);
                } else if (object instanceof EllipseMapObject) {
                    Ellipse e = ((EllipseMapObject) object).getEllipse();
                    Circle circle = new Circle(e.x + e.width/2, e.y + e.height/2, Math.max(e.width, e.height)/2);
                    collisionShapes.add(circle);
                    Gdx.app.log(TAG, "Cargado Circle (desde Ellipse) desde capa de objetos: " + circle);
                }
            }
            Gdx.app.log(TAG, "Carga desde capa de objetos completada. Total de formas: " + collisionShapes.size);
        } else {
            Gdx.app.log(TAG, "La capa de objetos '" + objectLayerName + "' no fue encontrada.");
        }
    }

    /**
     * Añade las colisiones definidas en los tiles de una capa de tiles.
     * Llama este método después de crear CollisionManager si usas colisiones de tiles.
     */
    public void addTileCollisions(TiledMap map, String tileLayerName) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(tileLayerName);
        if (layer == null) {
            Gdx.app.log(TAG, "La capa de tiles '" + tileLayerName + "' no fue encontrada. No se cargarán colisiones de tiles.");
            return;
        }
        Gdx.app.log(TAG, "Iniciando búsqueda de colisiones en la capa de tiles: " + tileLayerName);
        int initialCount = collisionShapes.size;
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell == null) continue;
                TiledMapTile tile = cell.getTile();
                if (tile == null) continue;
                Object colisionProp = tile.getProperties().get("Colisiones");
                boolean tieneColision = colisionProp != null &&
                    (colisionProp.equals(true) || colisionProp.toString().equalsIgnoreCase("true"));

                if (tieneColision) {
                    Gdx.app.log(TAG, "Tile en [" + x + "," + y + "] tiene propiedad 'Colisiones'. Buscando objetos de colisión...");
                    int shapesAddedForTile = 0;
                    for (MapObject object : tile.getObjects()) {
                        if (object instanceof RectangleMapObject) {
                            Rectangle rect = ((RectangleMapObject) object).getRectangle();
                            // Pasar de coordenadas locales del tile a globales del mapa
                            float worldX = x * layer.getTileWidth() + rect.x;
                            float worldY = y * layer.getTileHeight() + rect.y;
                            Rectangle worldRect = new Rectangle(worldX, worldY, rect.width, rect.height);
                            collisionShapes.add(worldRect);
                            Gdx.app.log(TAG, "  - Añadido Rectangle: " + worldRect);
                            shapesAddedForTile++;
                        }
                        if (object instanceof PolygonMapObject) {
                            Polygon poly = ((PolygonMapObject) object).getPolygon();
                            float[] verts = poly.getTransformedVertices();
                            // Transformar cada vértice a posición global
                            float[] worldVerts = new float[verts.length];
                            for (int i = 0; i < verts.length; i += 2) {
                                worldVerts[i] = x * layer.getTileWidth() + verts[i];
                                worldVerts[i + 1] = y * layer.getTileHeight() + verts[i + 1];
                            }
                            Polygon worldPoly = new Polygon(worldVerts);
                            collisionShapes.add(worldPoly);
                            Gdx.app.log(TAG, "  - Añadido Polygon con " + worldVerts.length / 2 + " vértices.");
                            shapesAddedForTile++;
                        }
                        if (object instanceof EllipseMapObject) {
                            Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
                            float worldX = x * layer.getTileWidth() + ellipse.x;
                            float worldY = y * layer.getTileHeight() + ellipse.y;
                            float radius = Math.max(ellipse.width, ellipse.height) / 2f;
                            Circle worldCircle = new Circle(worldX + radius, worldY + radius, radius);
                            collisionShapes.add(worldCircle);
                            Gdx.app.log(TAG, "  - Añadido Circle: " + worldCircle);
                            shapesAddedForTile++;
                        }
                    }
                    if (shapesAddedForTile == 0) {
                        Gdx.app.log(TAG, "  - ADVERTENCIA: El tile tiene 'Colisiones=true' pero no se encontraron objetos de colisión definidos en el tileset.");
                    }
                }
            }
        }
        Gdx.app.log(TAG, "Carga desde capa de tiles completada. Se añadieron " + (collisionShapes.size - initialCount) + " formas. Total de formas: " + collisionShapes.size);
    }

    public boolean collides(Rectangle rect) {
        for (Shape2D shape : collisionShapes) {
            if (shape instanceof Rectangle) {
                if (rect.overlaps((Rectangle) shape)) return true;
            } else if (shape instanceof Polygon) {
                Polygon poly = (Polygon) shape;
                Polygon playerPoly = new Polygon(rectToVertices(rect));
                if (Intersector.overlapConvexPolygons(playerPoly, poly)) return true;
            } else if (shape instanceof Circle) {
                if (Intersector.overlaps((Circle) shape, rect)) return true;
            }
        }
        return false;
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

    // (Opcional) Método para debug visual
    public Array<Shape2D> getCollisionShapes() {
        return collisionShapes;
    }
}
