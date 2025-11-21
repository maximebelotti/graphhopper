package com.graphhopper.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.shapes.GHPoint3D;
import com.graphhopper.ResponsePath;
import com.graphhopper.routing.Path;

import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;

// @ExtendWith(MockitoExtension.class)
public class PathMergerTest {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void doWorkTestWithExtremeCoordinates() {
        double eps = 1e-6;

        Graph graph = mock(Graph.class);
        Weighting weighting = mock(Weighting.class);
        when(graph.wrapWeighting(weighting)).thenReturn(weighting);

        EncodedValueLookup evLookup = mock(EncodedValueLookup.class);
        Translation tr = mock(Translation.class);
        when(tr.tr("via", "")).thenReturn("via");

        PathMerger merger = new PathMerger(graph, weighting)
            .setEnableInstructions(false)
            .setSimplifyResponse(false);
    
            
        GHPoint3D P0 = new GHPoint3D(-90 + eps, 180 - eps, -500);
        GHPoint3D P1 = new GHPoint3D(90 - eps, -180 + eps, 10000);
        GHPoint3D P2 = new GHPoint3D(0, 0, 0);

        Path path1 = mock(Path.class);
        when(path1.isFound()).thenReturn(true);
        when(path1.getTime()).thenReturn(1000L);
        when(path1.getDistance()).thenReturn(1000000.0);
        when(path1.getWeight()).thenReturn(10.0);
        when(path1.getDescription()).thenReturn(Collections.singletonList("path1"));

        PointList points1 = new PointList(2, true);
        points1.add(P0.lat, P0.lon, P0.ele); // P0
        points1.add(P1.lat, P1.lon, P1.ele); // P1
        when(path1.calcPoints()).thenReturn(points1);

        Path path2 = mock(Path.class);
        when(path2.isFound()).thenReturn(true);
        when(path2.getTime()).thenReturn(2000L);
        when(path2.getDistance()).thenReturn(5.0);
        when(path2.getWeight()).thenReturn(5.0);
        when(path2.getDescription()).thenReturn(Collections.singletonList("path2"));

        PointList points2 = new PointList(2, true);
        points2.add(P1.lat, P1.lon, P1.ele); // P1 (même point que fin du path1)
        points2.add(P2.lat, P2.lon, P2.ele); // P2
        when(path2.calcPoints()).thenReturn(points2);

        List<Path> paths = Arrays.asList(path1, path2);

        PointList waypoints = new PointList(3, true);
        waypoints.add(P0.lat, P0.lon, P0.ele);
        waypoints.add(P1.lat, P1.lon, P1.ele);
        waypoints.add(P2.lat, P2.lon, P2.ele);

        ResponsePath response = merger.doWork(waypoints, paths, evLookup, tr);

        assertFalse(response.hasErrors());
        assertEquals(3000L, response.getTime());
        assertEquals(1000005.0, response.getDistance(), eps);
        assertEquals(Arrays.asList("path1", "path2"), response.getDescription());
        
        PointList merged = response.getPoints();
        
        assertEquals(3, merged.size());
        assertEquals(10500, response.getAscend(), eps);
        assertEquals(10000, response.getDescend(), eps);


        PointList snappedWaypoints = response.getWaypoints();

        assertEquals(3, snappedWaypoints.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(waypoints.getLat(i), snappedWaypoints.getLat(i), eps);
            assertEquals(waypoints.getLon(i), snappedWaypoints.getLon(i), eps);
        }
    }
}
