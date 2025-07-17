package com.miestudio.jsonic.Util;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

public class CollisionManager {
    private Array<Shape2D> collisionShapes;

    public CollisionManager(TiledMap map, String layerName){
        collisionShapes = new Array<>();
        MapLayer layer = map.getLayers().get(layerName);
        if (layer != null){
            for (MapObject object : layer.getObjects()){
                if (object instanceof RectangleMapObject){
                    collisionShapes.add(((RectangleMapObject) object).getRectangle());
                } else if (object instanceof PolygonMapObject) {
                    collisionShapes.add(((PolygonMapObject) object).getPolygon());
                }
            }
        }
    }

    public boolean collides(Rectangle rect){
        for (Shape2D shape : collisionShapes){
            if (shape instanceof Rectangle){
                if (rect.overlaps((Rectangle) shape)) return true;
            } else if (shape instanceof Polygon) {
                Polygon poly = (Polygon) shape;
                Polygon playerPoly = new Polygon(rectToVertices(rect));
                if (Intersector.overlapConvexPolygons(playerPoly, poly)) return true;
            }
        }
        return false;
    }

    /** Metodo helper para convertir Rectangle a array de vertices de Polygon */
    private float[] rectToVertices(Rectangle rect){
        return new float[] {
            rect.x, rect.y,
            rect.x + rect.width, rect.y,
            rect.x + rect.width, rect.y + rect.height,
            rect.x, rect.y + rect.height
        };
    }
}
