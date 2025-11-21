package com.graphhopper.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.shapes.GHPoint3D;
import com.graphhopper.ResponsePath;
import com.graphhopper.routing.Path;

public class PathMergerTest {

    /**
     * Test normatif vérifiant que doWork() fusionne correctement deux Path contenant des coordonnées extrêmes. Il 
     * sert à présenter et valider la logique de la méthode : cohérence du temps, de la distance, du poids, des 
     * points et des waypoints, sans produire d’erreur dans la ResponsePath.
     */
    @Test
    void doWorkTestWithExtremeCoordinates() {
        double eps = 1e-6;

        // Préparation des mocks
        Graph graph = mock(Graph.class);
        Weighting weighting = mock(Weighting.class);
        when(graph.wrapWeighting(weighting)).thenReturn(weighting);

        EncodedValueLookup evLookup = mock(EncodedValueLookup.class);
        Translation tr = mock(Translation.class);
        when(tr.tr("via", "")).thenReturn("via");

        PathMerger merger = new PathMerger(graph, weighting)
            .setEnableInstructions(false)
            .setSimplifyResponse(false);
    
            
        GHPoint3D P0 = new GHPoint3D(-90, 180, -500); // pôle Sud + limite longitude
        GHPoint3D P1 = new GHPoint3D(90, -180, 10000); // pôle Nord + limite longitude
        GHPoint3D P2 = new GHPoint3D(0, 0, 0); // point normal (origine)

        // Premier Path (P0 -> P1)
        Path path1 = mock(Path.class);
        when(path1.isFound()).thenReturn(true);
        when(path1.getTime()).thenReturn(1000L);
        when(path1.getDistance()).thenReturn(10000.0);
        when(path1.getWeight()).thenReturn(10.0);
        when(path1.getDescription()).thenReturn(Collections.singletonList("path1"));

        PointList points1 = new PointList(2, true);
        points1.add(P0.lat, P0.lon, P0.ele); // P0
        points1.add(P1.lat, P1.lon, P1.ele); // P1
        when(path1.calcPoints()).thenReturn(points1);

        // Seond Path (P1 -> P2)
        Path path2 = mock(Path.class);
        when(path2.isFound()).thenReturn(true);
        when(path2.getTime()).thenReturn(2000L);
        when(path2.getDistance()).thenReturn(5000.0);
        when(path2.getWeight()).thenReturn(20.0);
        when(path2.getDescription()).thenReturn(Collections.singletonList("path2"));

        PointList points2 = new PointList(2, true);
        points2.add(P1.lat, P1.lon, P1.ele); // P1 (même point que fin du path1)
        points2.add(P2.lat, P2.lon, P2.ele); // P2
        when(path2.calcPoints()).thenReturn(points2);

        List<Path> paths = Arrays.asList(path1, path2);

        // Waypoints officiels
        PointList waypoints = new PointList(3, true);
        waypoints.add(P0.lat, P0.lon, P0.ele);
        waypoints.add(P1.lat, P1.lon, P1.ele);
        waypoints.add(P2.lat, P2.lon, P2.ele);

        ResponsePath response = merger.doWork(waypoints, paths, evLookup, tr);

        // Vérifications globales
        assertFalse(response.hasErrors(),
                "La réponse ne devrait contenir aucune erreur dans ce scénario nominal.");
        assertEquals(3000L, response.getTime(),
                "Le temps total doit être la somme des deux segments (1000 + 2000).");
        assertEquals(15000.0, response.getDistance(), eps,
                "La distance totale doit correspondre à la somme exacte des distances des deux Path.");
        assertEquals(Arrays.asList("path1", "path2"), response.getDescription(),
                "La distance totale doit correspondre à la somme exacte des distances des deux Path.");
        assertEquals(30.0, response.getRouteWeight(), eps,
                "Le poids total doit être la somme exacte des poids des deux Paths.");
        
        // Vérification de la fusion des points
        PointList merged = response.getPoints();

        assertEquals(3, merged.size(), 
            "La fusion des deux paths doit contenir exactement 3 points (P0, P1, P2).");
        assertEquals(10500, response.getAscend(), eps,
            "L'ascension totale doit être être cohérente avec les altitudes données.");
        assertEquals(10000, response.getDescend(), eps,
                "La descente totale doit être cohérente avec les altitudes données.");

        // Vérification des waypoints
        PointList snappedWaypoints = response.getWaypoints();

        assertEquals(3, snappedWaypoints.size(),
            "Les waypoints reproduits dans la réponse doivent être exactement ceux fournis.");
        for (int i = 0; i < 3; i++) {
            assertEquals(waypoints.getLat(i), snappedWaypoints.getLat(i), eps,
                "La latitude du waypoint " + i + " doit être reproduite fidèlement.");
            assertEquals(waypoints.getLon(i), snappedWaypoints.getLon(i), eps,
                "La longitude du waypoint " + i + " doit être reproduite fidèlement.");
        }
    }
}
