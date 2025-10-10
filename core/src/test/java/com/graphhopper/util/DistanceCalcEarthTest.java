/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class DistanceCalcEarthTest {
    private DistanceCalc dc = new DistanceCalcEarth();

    @Test
    public void testCalcCircumference() {
        assertEquals(DistanceCalcEarth.C, dc.calcCircumference(0), 1e-7);
    }

    @Test
    public void testDistance() {
        float lat = 24.235f;
        float lon = 47.234f;
        DistanceCalc approxDist = new DistancePlaneProjection();
        double res = 15051;
        assertEquals(res, dc.calcDist(lat, lon, lat - 0.1, lon + 0.1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 0.1, lon + 0.1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat - 0.1, lon + 0.1), 1);

        res = 15046;
        assertEquals(res, dc.calcDist(lat, lon, lat + 0.1, lon - 0.1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 0.1, lon - 0.1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 0.1, lon - 0.1), 1);

        res = 150748;
        assertEquals(res, dc.calcDist(lat, lon, lat - 1, lon + 1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 1, lon + 1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat - 1, lon + 1), 10);

        res = 150211;
        assertEquals(res, dc.calcDist(lat, lon, lat + 1, lon - 1), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 1, lon - 1), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 1, lon - 1), 10);

        res = 1527919;
        assertEquals(res, dc.calcDist(lat, lon, lat - 10, lon + 10), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 10, lon + 10), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat - 10, lon + 10), 10000);

        res = 1474016;
        assertEquals(res, dc.calcDist(lat, lon, lat + 10, lon - 10), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 10, lon - 10), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 10, lon - 10), 10000);

        res = 1013735.28;
        assertEquals(res, dc.calcDist(lat, lon, lat, lon - 10), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat, lon - 10), 1);
        // 1013952.659
        assertEquals(res, approxDist.calcDist(lat, lon, lat, lon - 10), 1000);

        // if we have a big distance for latitude only then PlaneProjection is exact!!
        res = 1111949.3;
        assertEquals(res, dc.calcDist(lat, lon, lat + 10, lon), 1);
        assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 10, lon), 1);
        assertEquals(res, approxDist.calcDist(lat, lon, lat + 10, lon), 1);
    }

    @Test
    public void testEdgeDistance() {
        double dist = dc.calcNormalizedEdgeDistance(49.94241, 11.544356,
                49.937964, 11.541824,
                49.942272, 11.555643);
        double expectedDist = dc.calcNormalizedDist(49.94241, 11.544356,
                49.9394, 11.54681);
        assertEquals(expectedDist, dist, 1e-4);

        // test identical lats
        dist = dc.calcNormalizedEdgeDistance(49.936299, 11.543992,
                49.9357, 11.543047,
                49.9357, 11.549227);
        expectedDist = dc.calcNormalizedDist(49.936299, 11.543992,
                49.9357, 11.543992);
        assertEquals(expectedDist, dist, 1e-4);
    }

    @Test
    public void testEdgeDistance3d() {
        double dist = dc.calcNormalizedEdgeDistance3D(49.94241, 11.544356, 0,
                49.937964, 11.541824, 0,
                49.942272, 11.555643, 0);
        double expectedDist = dc.calcNormalizedDist(49.94241, 11.544356,
                49.9394, 11.54681);
        assertEquals(expectedDist, dist, 1e-4);

        // test identical lats
        dist = dc.calcNormalizedEdgeDistance3D(49.936299, 11.543992, 0,
                49.9357, 11.543047, 0,
                49.9357, 11.549227, 0);
        expectedDist = dc.calcNormalizedDist(49.936299, 11.543992,
                49.9357, 11.543992);
        assertEquals(expectedDist, dist, 1e-4);
    }

    @Test
    public void testEdgeDistance3dEarth() {
        double dist = dc.calcNormalizedEdgeDistance3D(0, 0.5, 10,
                0, 0, 0,
                0, 1, 0);
        assertEquals(10, dc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistance3dEarthNaN() {
        double dist = dc.calcNormalizedEdgeDistance3D(0, 0.5, Double.NaN,
                0, 0, 0,
                0, 1, 0);
        assertEquals(0, dc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistance3dPlane() {
        DistanceCalc calc = new DistancePlaneProjection();
        double dist = calc.calcNormalizedEdgeDistance3D(0, 0.5, 10,
                0, 0, 0,
                0, 1, 0);
        assertEquals(10, calc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistanceStartEndSame() {
        DistanceCalc calc = new DistancePlaneProjection();
        // just change elevation
        double dist = calc.calcNormalizedEdgeDistance3D(0, 0, 10,
                0, 0, 0,
                0, 0, 0);
        assertEquals(10, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lat
        dist = calc.calcNormalizedEdgeDistance3D(1, 0, 0,
                0, 0, 0,
                0, 0, 0);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lon
        dist = calc.calcNormalizedEdgeDistance3D(0, 1, 0,
                0, 0, 0,
                0, 0, 0);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testEdgeDistanceStartEndDifferentElevation() {
        DistanceCalc calc = new DistancePlaneProjection();
        // just change elevation
        double dist = calc.calcNormalizedEdgeDistance3D(0, 0, 10,
                0, 0, 0,
                0, 0, 1);
        assertEquals(0, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lat
        dist = calc.calcNormalizedEdgeDistance3D(1, 0, 0,
                0, 0, 0,
                0, 0, 1);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
        // just change lon
        dist = calc.calcNormalizedEdgeDistance3D(0, 1, 0,
                0, 0, 0,
                0, 0, 1);
        assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
    }

    @Test
    public void testValidEdgeDistance() {
        assertTrue(dc.validEdgeDistance(49.94241, 11.544356, 49.937964, 11.541824, 49.942272, 11.555643));
        assertTrue(dc.validEdgeDistance(49.936624, 11.547636, 49.937964, 11.541824, 49.942272, 11.555643));
        assertTrue(dc.validEdgeDistance(49.940712, 11.556069, 49.937964, 11.541824, 49.942272, 11.555643));

        // left bottom of the edge
        assertFalse(dc.validEdgeDistance(49.935119, 11.541649, 49.937964, 11.541824, 49.942272, 11.555643));
        // left top of the edge
        assertFalse(dc.validEdgeDistance(49.939317, 11.539675, 49.937964, 11.541824, 49.942272, 11.555643));
        // right top of the edge
        assertFalse(dc.validEdgeDistance(49.944482, 11.555446, 49.937964, 11.541824, 49.942272, 11.555643));
        // right bottom of the edge
        assertFalse(dc.validEdgeDistance(49.94085, 11.557356, 49.937964, 11.541824, 49.942272, 11.555643));

        // rounding error
        // assertFalse(dc.validEdgeDistance(0.001, 0.001, 0.001, 0.002, 0.00099987, 0.00099987));
    }

    @Test
    public void testPrecisionBug() {
        DistanceCalc dist = new DistancePlaneProjection();
//        DistanceCalc dist = new DistanceCalc();
        double queryLat = 42.56819, queryLon = 1.603231;
        double lat16 = 42.56674481705006, lon16 = 1.6023790821964834;
        double lat17 = 42.56694505140808, lon17 = 1.6020622462495173;
        double lat18 = 42.56715199128878, lon18 = 1.601682266630581;

        // segment 18
        assertEquals(171.487, dist.calcDist(queryLat, queryLon, lat18, lon18), 1e-3);
        // segment 17
        assertEquals(168.298, dist.calcDist(queryLat, queryLon, lat17, lon17), 1e-3);
        // segment 16
        assertEquals(175.188, dist.calcDist(queryLat, queryLon, lat16, lon16), 1e-3);

        assertEquals(167.385, dist.calcDenormalizedDist(dist.calcNormalizedEdgeDistance(queryLat, queryLon, lat16, lon16, lat17, lon17)), 1e-3);

        assertEquals(168.213, dist.calcDenormalizedDist(dist.calcNormalizedEdgeDistance(queryLat, queryLon, lat17, lon17, lat18, lon18)), 1e-3);

        // 16_17
        assertEquals(new GHPoint(42.567048, 1.6019), dist.calcCrossingPointToEdge(queryLat, queryLon, lat16, lon16, lat17, lon17));
        // 17_18
        // assertEquals(new GHPoint(42.566945,1.602062), dist.calcCrossingPointToEdge(queryLat, queryLon, lat17, lon17, lat18, lon18));
    }

    @Test
    public void testPrecisionBug2() {
        DistanceCalc distCalc = new DistancePlaneProjection();
        double queryLat = 55.818994, queryLon = 37.595354;
        double tmpLat = 55.81777239183573, tmpLon = 37.59598350366913;
        double wayLat = 55.818839128736535, wayLon = 37.5942968784488;
        assertEquals(68.25, distCalc.calcDist(wayLat, wayLon, queryLat, queryLon), .1);

        assertEquals(60.88, distCalc.calcDenormalizedDist(distCalc.calcNormalizedEdgeDistance(queryLat, queryLon,
                tmpLat, tmpLon, wayLat, wayLon)), .1);

        assertEquals(new GHPoint(55.81863, 37.594626), distCalc.calcCrossingPointToEdge(queryLat, queryLon,
                tmpLat, tmpLon, wayLat, wayLon));
    }

    @Test
    public void testDistance3dEarth() {
        DistanceCalc distCalc = new DistanceCalcEarth();
        assertEquals(1, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 1
        ), 1e-6);
    }

    @Test
    public void testDistance3dEarthNaN() {
        DistanceCalc distCalc = new DistanceCalcEarth();
        assertEquals(0, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, Double.NaN
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, 10
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, Double.NaN
        ), 1e-6);
    }

    @Test
    public void testDistance3dPlane() {
        DistancePlaneProjection distCalc = new DistancePlaneProjection();
        assertEquals(1, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 1
        ), 1e-6);
        assertEquals(10, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, 10
        ), 1e-6);
    }

    @Test
    public void testDistance3dPlaneNaN() {
        DistancePlaneProjection distCalc = new DistancePlaneProjection();
        assertEquals(0, distCalc.calcDist3D(
                0, 0, 0,
                0, 0, Double.NaN
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, 10
        ), 1e-6);
        assertEquals(0, distCalc.calcDist3D(
                0, 0, Double.NaN,
                0, 0, Double.NaN
        ), 1e-6);
    }

    @Test
    public void testIntermediatePoint() {
        DistanceCalc distCalc = new DistanceCalcEarth();
        GHPoint point = distCalc.intermediatePoint(0, 0, 0, 0, 0);
        assertEquals(0, point.getLat(), 1e-5);
        assertEquals(0, point.getLon(), 1e-5);

        point = distCalc.intermediatePoint(0.5, 0, 0, 10, 0);
        assertEquals(5, point.getLat(), 1e-5);
        assertEquals(0, point.getLon(), 1e-5);

        point = distCalc.intermediatePoint(0.5, 0, 0, 0, 10);
        assertEquals(0, point.getLat(), 1e-5);
        assertEquals(5, point.getLon(), 1e-5);

        // cross international date line going west
        point = distCalc.intermediatePoint(0.5, 45, -179, 45, 177);
        assertEquals(45, point.getLat(), 1);
        assertEquals(179, point.getLon(), 1e-5);

        // cross international date line going east
        point = distCalc.intermediatePoint(0.5, 45, 179, 45, -177);
        assertEquals(45, point.getLat(), 1);
        assertEquals(-179, point.getLon(), 1e-5);

        // cross north pole
        point = distCalc.intermediatePoint(0.25, 45, -90, 45, 90);
        assertEquals(67.5, point.getLat(), 1e-1);
        assertEquals(-90, point.getLon(), 1e-5);
        point = distCalc.intermediatePoint(0.75, 45, -90, 45, 90);
        assertEquals(67.5, point.getLat(), 1e-1);
        assertEquals(90, point.getLon(), 1e-5);
    }

    @Test
    void testCreateBBoxInvalidAndExtremeDistance() {
        DistanceCalcEarth calc = new DistanceCalcEarth();

        // Cas invalides : rayon nul ou négatif
        assertThrows(IllegalArgumentException.class, () -> calc.createBBox(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> calc.createBBox(45, 90, -100));

        // Cas limite : distance extrêmement grande (~demi-circonférence terrestre)
        double largeDist = 20_000_000;
        BBox bbox = calc.createBBox(0, 0, largeDist);

        // Pas d'inversion : min < max
        assertTrue(bbox.minLat < bbox.maxLat, "minLat doit être inférieur à maxLat");
        assertTrue(bbox.minLon < bbox.maxLon, "minLon doit être inférieur à maxLon");

        // Pas de valeurs infinies ou NaN
        assertFalse(Double.isNaN(bbox.minLat) || Double.isNaN(bbox.maxLat));
        assertFalse(Double.isInfinite(bbox.minLat) || Double.isInfinite(bbox.maxLat));

        // Vérifie cohérence : plus la distance est grande, plus la bbox est large
        BBox smallBox = calc.createBBox(0, 0, 1000);
        double latRangeSmall = smallBox.maxLat - smallBox.minLat;
        double latRangeLarge = bbox.maxLat - bbox.minLat;
        assertTrue(latRangeLarge > latRangeSmall, "Une plus grande distance doit donner une boîte plus large");

        // Test polaire : valeurs extrêmes mais cohérentes
        BBox polarBox = calc.createBBox(89.9999, 0, 10000);
        assertTrue(polarBox.maxLat >= polarBox.minLat);
        assertFalse(Double.isNaN(polarBox.maxLon));

        // Cas minuscule : distance d'un mètre
        BBox tinyBox = calc.createBBox(0, 0, 1);
        assertTrue(tinyBox.maxLat > tinyBox.minLat);
        assertTrue(tinyBox.maxLon > tinyBox.minLon);
    }

    @Test
    void testIsDateLineCrossOverBoundaryCases() {
        DistanceCalcEarth calc = new DistanceCalcEarth();
        // Cas typique : franchissement de 180°
        assertTrue(calc.isDateLineCrossOver(179.9, -179.9));
        // Cas limite : exactement 180°
        assertFalse(calc.isDateLineCrossOver(0, 180));
        // Cas sans franchissement
        assertFalse(calc.isDateLineCrossOver(10, 20));
    }
    
    // Inspiré de ChatGPT
    // Source de double vérification des latitudes/longitudes: https://www.omnicalculator.com/other/latitude-longitude-distance 
    @Test
    void testProjectCoordinateCardinalDirections() {
        DistanceCalcEarth calc = new DistanceCalcEarth();
        double changedDelta = 1e-3;
        double sameDelta = 1e-6;
        double distKm = 1000.0; // 1 km
        double distDegrees = .009;

        // POINT DE DÉPART : ÉQUATEUR

        // Point de départ à l'équateur
        double lat0 = 0.0;
        double lon0 = 0.0;

        // Nord : latitude augmente
        GHPoint north = calc.projectCoordinate(lat0, lon0, distKm, 0);
        assertEquals(distDegrees, north.lat, changedDelta);
        assertEquals(lon0, north.lon, sameDelta, "Longitude doit rester quasi constante vers le nord");

        // Sud : latitude diminue
        GHPoint south = calc.projectCoordinate(lat0, lon0, distKm, 180);
        assertEquals(-distDegrees, south.lat, changedDelta);
        assertEquals(lon0, south.lon, sameDelta, "Longitude doit rester quasi constante vers le sud");

        // Est : longitude augmente
        GHPoint east = calc.projectCoordinate(lat0, lon0, distKm, 90);
        assertEquals(distDegrees, east.lon, changedDelta);
        assertEquals(lat0, east.lat, sameDelta, "Latitude doit rester quasi constante vers l'est");

        // Ouest : longitude diminue
        GHPoint west = calc.projectCoordinate(lat0, lon0, distKm, 270);
        assertEquals(-distDegrees, west.lon, changedDelta);
        assertEquals(lat0, west.lat, sameDelta, "Latitude doit rester quasi constante vers l'ouest");

        // Angle = 360
        GHPoint north2 = calc.projectCoordinate(lat0, lon0, distKm, 360);
        assertEquals(distDegrees, north2.lat, changedDelta);
        assertEquals(lon0, north2.lon, sameDelta, "Latitude doit rester quasi constante vers l'ouest");

        // Angle > 360
        GHPoint east2 = calc.projectCoordinate(lat0, lon0, distKm, 450);
        assertEquals(distDegrees, east2.lon, changedDelta);
        assertEquals(lat0, east2.lat, sameDelta, "Latitude doit rester quasi constante vers l'est");

        // Angle < 0
        GHPoint west2 = calc.projectCoordinate(lat0, lon0, distKm, -90);
        assertEquals(-distDegrees, west2.lon, changedDelta);
        assertEquals(lat0, west2.lat, sameDelta, "Latitude doit rester quasi constante vers l'ouest");

        // Angle non carré
        GHPoint northEast = calc.projectCoordinate(lat0, lon0, distKm, 45);
        assertEquals(.006389, northEast.lat, changedDelta);
        assertEquals(.006389, northEast.lon, changedDelta);

        // POINT DE DÉPART : PÔLE NORD

        // Point de départ au Nord
        lat0 = 90.0;
        lon0 = 0.0;
        distKm = 1000.0; // 1 km, pareil

        // Point d'arrivée: nombre de degrés pour mouvement d'1km dans un angle droit
        distDegrees = .009; // pareil

        // 90 degrés : latitude diminue, longitude d'arrivée est opposée
        south = calc.projectCoordinate(lat0, lon0, distKm, 90);
        assertEquals(90 - distDegrees, south.lat, changedDelta);
        assertEquals(lon0 + 90, south.lon, sameDelta, "Le pôle nord est franchi, la longitude est opposée");

        // 0 degrés : latitude diminue
        south = calc.projectCoordinate(lat0, lon0, distKm, 0);
        assertEquals(90 - distDegrees, south.lat, changedDelta);
        assertEquals(lon0, south.lon, sameDelta, "Le pôle nord n'est pas franchi, la longitude est la même");

    }

    @Test 
    void testCalcDistancePointList() {
        // Note: les données sont prises de testDistance()
        DistanceCalcEarth dce = new DistanceCalcEarth();
        PointList pl = new PointList();
        float lat = 24.235f;
        float lon = 47.234f;
        double res = 15051;

        pl.add(lat, lon);
        pl.add(lat - 0.1, lon + 0.1);
        assertEquals(res, dce.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat - 0.1, lon + 0.1), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);

        pl.setNode(1, lat + 0.1, lon - 0.1);
        res = 15046;
        assertEquals(res, dce.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat + 0.1, lon - 0.1), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);

        res = 150748;
        pl.setNode(1, lat - 1, lon + 1);
        assertEquals(res, dce.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat - 1, lon + 1), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);

        res = 150211;
        pl.setNode(1, lat + 1, lon - 1);
        assertEquals(res, dc.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat + 1, lon - 1), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);

        res = 1527919;
        pl.setNode(1, lat - 10, lon + 10);
        assertEquals(res, dce.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat - 10, lon + 10), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);

        res = 1474016;
        pl.setNode(1, lat + 10, lon - 10);
        assertEquals(res, dce.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat + 10, lon - 10), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);

        res = 1013735.28;
        pl.setNode(1, lat, lon - 10);
        assertEquals(res, dce.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat, lon - 10), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);

        // // if we have a big distance for latitude only then PlaneProjection is exact!!
        res = 1111949.3;
        pl.setNode(1, lat + 10, lon);
        assertEquals(res, dce.calcDistance(pl), 1);
        assertEquals(dce.calcNormalizedDist(res), dce.calcNormalizedDist(lat, lon, lat + 10, lon), 1);
        assertEquals(res, DistanceCalcEarth.calcDistance(pl, false), 1);
    
    }
    
}
