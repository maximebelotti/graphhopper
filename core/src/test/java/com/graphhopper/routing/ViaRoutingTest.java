package com.graphhopper.routing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.DirectedEdgeFilter;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.GHPoint;

public class ViaRoutingTest {
    private QueryGraph queryGraph;
    private DirectedEdgeFilter directedEdgeFilter;
    
    /**
     * Initialise les mocks communs à tous les tests (QueryGraph et DirectedEdgeFilter).
     */
    @BeforeEach
    void setUp() {
        queryGraph = mock(QueryGraph.class);
        directedEdgeFilter = mock(DirectedEdgeFilter.class);
    }

    /**
     * Vérifie que calcPaths() découpe correctement l’itinéraire en deux segments, appelle PathCalculator pour chaque 
     * segment et agrège la liste des chemins et des nœuds visités. Ce test normatif vérifie et illustre la logique 
     * principale de la méthode, puis contrôle deux cas d’erreur liés aux paramètres curbsides et headings.
     */
    @Test
    void calcPathTest() {
        // Points demandés
        List<GHPoint> points = Arrays.asList(
            new GHPoint(0.0, 0.0),
            new GHPoint(1.0, 1.0),
            new GHPoint(2.0, 2.0)
        );

        // Snaps associés à chaque point, avec des identifiants de nœuds distincts
        Snap snap0 = mock(Snap.class);
        when(snap0.getClosestNode()).thenReturn(10);
        Snap snap1 = mock(Snap.class);
        when(snap1.getClosestNode()).thenReturn(20);
        Snap snap2 = mock(Snap.class);
        when(snap2.getClosestNode()).thenReturn(30);
        List<Snap> snaps = Arrays.asList(snap0, snap1, snap2);

        // Aucun curbside ni heading (scénario nominal)
        List<String> emptyCurbsides = Collections.emptyList();
        List<Double> emptyHeadings = Collections.emptyList();
        String curbsideStrictness = "strict"; // ici, ignoré car curbsides est vide
        boolean passThrough = false;

        Path pathLeg0 = mock(Path.class);
        when(pathLeg0.getTime()).thenReturn(1000L);
        when(pathLeg0.getDebugInfo()).thenReturn("leg0");
        Path pathLeg1 = mock(Path.class);
        when(pathLeg1.getTime()).thenReturn(4000L);
        when(pathLeg1.getDebugInfo()).thenReturn("leg1");

        PathCalculator pathCalculator = mock(PathCalculator.class);

        // Premier segment (10-20) renvoie pathLeg0
        when(pathCalculator.calcPaths(eq(10), eq(20), any(EdgeRestrictions.class)))
                .thenReturn(Collections.singletonList(pathLeg0));

        // Second segment (20-30) renvoie pathLeg0
        when(pathCalculator.calcPaths(eq(20), eq(30), any(EdgeRestrictions.class)))
            .thenReturn(Collections.singletonList(pathLeg1));
        when(pathCalculator.getVisitedNodes()).thenReturn(3, 7);
        when(pathCalculator.getDebugString()).thenReturn("PathCalculator: calcul des segments");

        ViaRouting.Result result = ViaRouting.calcPaths(points, queryGraph, snaps, directedEdgeFilter, pathCalculator,
                emptyCurbsides, curbsideStrictness, emptyHeadings, passThrough);

        // Scénario nominal
        assertNotNull(result,
            "Le résultat ne doit jamais être null pour ce scénario nominal");
        assertEquals(2, result.paths.size(),
            "On attend exactement deux chemins, un par segment (10-20 et 20-30)");
        assertSame(pathLeg0, result.paths.get(0),
            "Le premier chemin doit correspondre au segment 10-20");
        assertSame(pathLeg1, result.paths.get(1),
            "Le second chemin doit correspondre au segment 20-30");
        assertEquals(10L, result.visitedNodes,
            "visitedNodes doit être la somme des visites sur chaque segment");
        
        // PathCalculator doit être appelé une fois par segment avec les bons nœuds
        verify(pathCalculator).calcPaths(eq(10), eq(20), any(EdgeRestrictions.class));
        verify(pathCalculator).calcPaths(eq(20), eq(30), any(EdgeRestrictions.class));

        // Cas d’erreur: taille de curbsides incohérente
        List<String> wrongSizeCurbsides = Collections.singletonList("right");
        assertThrows(IllegalArgumentException.class, () -> ViaRouting.calcPaths(
                points, queryGraph, snaps, directedEdgeFilter, pathCalculator, wrongSizeCurbsides, curbsideStrictness,
                emptyHeadings, passThrough),
            "Si la taille de curbsides diffère du nombre de points, calcPaths() doit lever une IllegalArgumentException");
        
        List<String> someCurbsides = Arrays.asList("right", "left", "any");
        List<Double> someHeadings = Arrays.asList(0D, 90D, 180D);
        assertThrows(IllegalArgumentException.class, () -> ViaRouting.calcPaths(
                points, queryGraph, snaps, directedEdgeFilter, pathCalculator, someCurbsides, curbsideStrictness,
                someHeadings, passThrough),
            "Si des curbsides et des headings sont fournis en même temps, calcPaths() doit lever une IllegalArgumentException");
    }
}
