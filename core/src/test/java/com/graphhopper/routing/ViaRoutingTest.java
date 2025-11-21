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
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.GHPoint;

public class ViaRoutingTest {
    private QueryGraph queryGraph;
    private DirectedEdgeFilter directedEdgeFilter;
    
    @BeforeEach
    void setUp() {
        queryGraph = mock(QueryGraph.class);
        directedEdgeFilter = mock(DirectedEdgeFilter.class);
    }

    @Test
    void calcPathTest() {
        List<GHPoint> points = Arrays.asList(
            new GHPoint(0.0, 0.0),
            new GHPoint(1.0, 1.0),
            new GHPoint(2.0, 2.0)
        );

        Snap snap0 = mock(Snap.class);
        when(snap0.getClosestNode()).thenReturn(10);
        Snap snap1 = mock(Snap.class);
        when(snap1.getClosestNode()).thenReturn(20);
        Snap snap2 = mock(Snap.class);
        when(snap2.getClosestNode()).thenReturn(30);
        List<Snap> snaps = Arrays.asList(snap0, snap1, snap2);

        List<String> emptyCurbsides = Collections.emptyList();
        List<Double> emptyHeadings = Collections.emptyList();
        String curbsideStrictness = "strict"; // ici, ignoré car curbsides est vide
        boolean passThrough = false;

        Path pathLeg0 = mock(Path.class);
        when(pathLeg0.getTime()).thenReturn(1000L);
        when(pathLeg0.getDebugInfo()).thenReturn("leg0");
        Path pathLeg1 = mock(Path.class);
        when(pathLeg1.getTime()).thenReturn(5000L);
        when(pathLeg1.getDebugInfo()).thenReturn("leg1");

        PathCalculator pathCalculator = mock(PathCalculator.class);
        when(pathCalculator.calcPaths(eq(10), eq(20), any(EdgeRestrictions.class))).thenReturn(Collections.singletonList(pathLeg0));
        when(pathCalculator.calcPaths(eq(20), eq(30), any(EdgeRestrictions.class))).thenReturn(Collections.singletonList(pathLeg1));
        when(pathCalculator.getVisitedNodes()).thenReturn(5, 7);
        when(pathCalculator.getDebugString()).thenReturn("pc-debug");

        ViaRouting.Result result = ViaRouting.calcPaths(points, queryGraph, snaps, directedEdgeFilter, pathCalculator,
                emptyCurbsides, curbsideStrictness, emptyHeadings, passThrough);

        assertNotNull(result);
        assertEquals(2, result.paths.size());
        assertSame(pathLeg0, result.paths.get(0));
        assertSame(pathLeg1, result.paths.get(1));
        assertEquals(12L, result.visitedNodes);
        verify(pathCalculator).calcPaths(eq(10), eq(20), any(EdgeRestrictions.class));
        verify(pathCalculator).calcPaths(eq(20), eq(30), any(EdgeRestrictions.class));

        List<String> wrongSizeCurbsides = Collections.singletonList("right");
        assertThrows(IllegalArgumentException.class, () -> ViaRouting.calcPaths(
                points, queryGraph, snaps, directedEdgeFilter, pathCalculator, wrongSizeCurbsides, curbsideStrictness,
                emptyHeadings, passThrough));
        
        List<String> someCurbsides = Arrays.asList("right", "left", "any");
        List<Double> someHeadings = Arrays.asList(0D, 90D, 180D);
        assertThrows(IllegalArgumentException.class, () -> ViaRouting.calcPaths(
            points, queryGraph, snaps, directedEdgeFilter, pathCalculator, someCurbsides, curbsideStrictness, someHeadings, passThrough));
    }
}
