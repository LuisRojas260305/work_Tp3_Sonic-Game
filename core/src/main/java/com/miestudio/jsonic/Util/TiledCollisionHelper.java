package com.miestudio.jsonic.Util;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TiledCollisionHelper {

    public static void parseTiledCollisionLayer(World world, TiledMap tiledMap) {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Colisiones");
        if (collisionLayer == null) {
            Gdx.app.log("TiledCollisionHelper", "Capa 'Colisiones' no encontrada en el mapa.");
            return;
        }

        // Paso 1: Extraer todos los bordes de los objetos de colisión
        Map<Edge, Integer> edgeCounts = new HashMap<>();

        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                float rx = rect.x / Constantes.PPM;
                float ry = rect.y / Constantes.PPM;
                float rw = rect.width / Constantes.PPM;
                float rh = rect.height / Constantes.PPM;

                Vector2 p1 = new Vector2(rx, ry);
                Vector2 p2 = new Vector2(rx + rw, ry);
                Vector2 p3 = new Vector2(rx + rw, ry + rh);
                Vector2 p4 = new Vector2(rx, ry + rh);

                addEdge(edgeCounts, p1, p2);
                addEdge(edgeCounts, p2, p3);
                addEdge(edgeCounts, p3, p4);
                addEdge(edgeCounts, p4, p1);

            } else if (object instanceof PolygonMapObject) {
                PolygonMapObject polyObject = (PolygonMapObject) object;
                float[] vertices = polyObject.getPolygon().getTransformedVertices();

                for (int i = 0; i < vertices.length; i += 2) {
                    Vector2 p1 = new Vector2(vertices[i] / Constantes.PPM, vertices[i + 1] / Constantes.PPM);
                    Vector2 p2;
                    if (i + 2 < vertices.length) {
                        p2 = new Vector2(vertices[i + 2] / Constantes.PPM, vertices[i + 3] / Constantes.PPM);
                    } else {
                        p2 = new Vector2(vertices[0] / Constantes.PPM, vertices[1] / Constantes.PPM);
                    }
                    addEdge(edgeCounts, p1, p2);
                }
            }
            // TODO: Handle PolylineMapObject if needed
        }

        // Paso 2: Filtrar los bordes internos (aquellos que aparecen dos veces)
        List<Edge> externalEdges = new ArrayList<>();
        for (Map.Entry<Edge, Integer> entry : edgeCounts.entrySet()) {
            if (entry.getValue() == 1) {
                externalEdges.add(entry.getKey());
            }
        }

        // Paso 3: Encadenar los bordes externos para formar polígonos o cadenas
        List<List<Vector2>> chains = new ArrayList<>();
        Set<Edge> usedEdges = new HashSet<>();

        while (usedEdges.size() < externalEdges.size()) {
            Edge currentEdge = null;
            for (Edge edge : externalEdges) {
                if (!usedEdges.contains(edge)) {
                    currentEdge = edge;
                    break;
                }
            }

            if (currentEdge == null) break; // No more unused edges

            List<Vector2> currentChain = new ArrayList<>();
            currentChain.add(currentEdge.p1);
            currentChain.add(currentEdge.p2);
            usedEdges.add(currentEdge);

            Vector2 lastPoint = currentEdge.p2;
            boolean chainExtended = true;

            while (chainExtended) {
                chainExtended = false;
                Edge nextEdge = findNextEdge(externalEdges, usedEdges, lastPoint);
                if (nextEdge != null) {
                    if (nextEdge.p1.equals(lastPoint)) {
                        currentChain.add(nextEdge.p2);
                        lastPoint = nextEdge.p2;
                    } else { // nextEdge.p2.equals(lastPoint)
                        currentChain.add(nextEdge.p1);
                        lastPoint = nextEdge.p1;
                    }
                    usedEdges.add(nextEdge);
                    chainExtended = true;
                }
            }
            chains.add(currentChain);
        }

        // Paso 4: Crear cuerpos de Box2D a partir de las cadenas
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = Constantes.CATEGORY_GROUND;
        fixtureDef.filter.maskBits = Constantes.CATEGORY_PLAYER;

        for (List<Vector2> chain : chains) {
            if (chain.size() < 2) continue; // Need at least 2 points for a chain

            Vector2[] vertices = chain.toArray(new Vector2[0]);
            ChainShape chainShape = new ChainShape();

            // Check if it's a closed loop or an open chain
            if (vertices[0].equals(vertices[vertices.length - 1])) {
                // It's a closed loop, remove the duplicate last point
                Vector2[] loopVertices = new Vector2[vertices.length - 1];
                System.arraycopy(vertices, 0, loopVertices, 0, vertices.length - 1);
                chainShape.createLoop(loopVertices);
            } else {
                chainShape.createChain(vertices);
            }

            fixtureDef.shape = chainShape;
            world.createBody(bodyDef).createFixture(fixtureDef);
            chainShape.dispose();
        }
        Gdx.app.log("TiledCollisionHelper", "Colisiones del mapa creadas.");
    }

    private static void addEdge(Map<Edge, Integer> edgeCounts, Vector2 p1, Vector2 p2) {
        Edge edge = new Edge(p1, p2);
        edgeCounts.put(edge, edgeCounts.getOrDefault(edge, 0) + 1);
    }

    private static Edge findNextEdge(List<Edge> externalEdges, Set<Edge> usedEdges, Vector2 point) {
        for (Edge edge : externalEdges) {
            if (!usedEdges.contains(edge)) {
                if (edge.p1.equals(point) || edge.p2.equals(point)) {
                    return edge;
                }
            }
        }
        return null;
    }

    // Clase interna para representar un borde (arista)
    private static class Edge {
        Vector2 p1, p2;

        public Edge(Vector2 p1, Vector2 p2) {
            // Normalizar el orden de los puntos para que (A,B) sea igual a (B,A)
            if (p1.x < p2.x || (p1.x == p2.x && p1.y < p2.y)) {
                this.p1 = p1;
                this.p2 = p2;
            } else {
                this.p1 = p2;
                this.p2 = p1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return (p1.equals(edge.p1) && p2.equals(edge.p2)) || (p1.equals(edge.p2) && p2.equals(edge.p1));
        }

        @Override
        public int hashCode() {
            // Un hash code simétrico para que (A,B) y (B,A) tengan el mismo hash
            return p1.hashCode() + p2.hashCode();
        }
    }
}