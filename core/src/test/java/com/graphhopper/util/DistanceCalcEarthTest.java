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

import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.BBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */

// public class DistanceCalcEarthTest {
//         private DistanceCalc dc = new DistanceCalcEarth();

//         @Test
//         public void testCalcCircumference() {
//                 assertEquals(DistanceCalcEarth.C, dc.calcCircumference(0), 1e-7);
//         }

//         @Test
//         public void testDistance() {
//                 float lat = 24.235f;
//                 float lon = 47.234f;
//                 DistanceCalc approxDist = new DistancePlaneProjection();
//                 double res = 15051;
//                 assertEquals(res, dc.calcDist(lat, lon, lat - 0.1, lon + 0.1), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 0.1, lon + 0.1), 1);
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat - 0.1, lon + 0.1), 1);

//                 res = 15046;
//                 assertEquals(res, dc.calcDist(lat, lon, lat + 0.1, lon - 0.1), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 0.1, lon - 0.1), 1);
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat + 0.1, lon - 0.1), 1);

//                 res = 150748;
//                 assertEquals(res, dc.calcDist(lat, lon, lat - 1, lon + 1), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 1, lon + 1), 1);
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat - 1, lon + 1), 10);

//                 res = 150211;
//                 assertEquals(res, dc.calcDist(lat, lon, lat + 1, lon - 1), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 1, lon - 1), 1);
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat + 1, lon - 1), 10);

//                 res = 1527919;
//                 assertEquals(res, dc.calcDist(lat, lon, lat - 10, lon + 10), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat - 10, lon + 10), 1);
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat - 10, lon + 10), 10000);

//                 res = 1474016;
//                 assertEquals(res, dc.calcDist(lat, lon, lat + 10, lon - 10), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 10, lon - 10), 1);
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat + 10, lon - 10), 10000);

//                 res = 1013735.28;
//                 assertEquals(res, dc.calcDist(lat, lon, lat, lon - 10), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat, lon - 10), 1);
//                 // 1013952.659
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat, lon - 10), 1000);

//                 // if we have a big distance for latitude only then PlaneProjection is exact!!
//                 res = 1111949.3;
//                 assertEquals(res, dc.calcDist(lat, lon, lat + 10, lon), 1);
//                 assertEquals(dc.calcNormalizedDist(res), dc.calcNormalizedDist(lat, lon, lat + 10, lon), 1);
//                 assertEquals(res, approxDist.calcDist(lat, lon, lat + 10, lon), 1);
//         }

//         @Test
//         public void testEdgeDistance() {
//                 double dist = dc.calcNormalizedEdgeDistance(49.94241, 11.544356,
//                                 49.937964, 11.541824,
//                                 49.942272, 11.555643);
//                 double expectedDist = dc.calcNormalizedDist(49.94241, 11.544356,
//                                 49.9394, 11.54681);
//                 assertEquals(expectedDist, dist, 1e-4);

//                 // test identical lats
//                 dist = dc.calcNormalizedEdgeDistance(49.936299, 11.543992,
//                                 49.9357, 11.543047,
//                                 49.9357, 11.549227);
//                 expectedDist = dc.calcNormalizedDist(49.936299, 11.543992,
//                                 49.9357, 11.543992);
//                 assertEquals(expectedDist, dist, 1e-4);
//         }

//         @Test
//         public void testEdgeDistance3d() {
//                 double dist = dc.calcNormalizedEdgeDistance3D(49.94241, 11.544356, 0,
//                                 49.937964, 11.541824, 0,
//                                 49.942272, 11.555643, 0);
//                 double expectedDist = dc.calcNormalizedDist(49.94241, 11.544356,
//                                 49.9394, 11.54681);
//                 assertEquals(expectedDist, dist, 1e-4);

//                 // test identical lats
//                 dist = dc.calcNormalizedEdgeDistance3D(49.936299, 11.543992, 0,
//                                 49.9357, 11.543047, 0,
//                                 49.9357, 11.549227, 0);
//                 expectedDist = dc.calcNormalizedDist(49.936299, 11.543992,
//                                 49.9357, 11.543992);
//                 assertEquals(expectedDist, dist, 1e-4);
//         }

//         @Test
//         public void testEdgeDistance3dEarth() {
//                 double dist = dc.calcNormalizedEdgeDistance3D(0, 0.5, 10,
//                                 0, 0, 0,
//                                 0, 1, 0);
//                 assertEquals(10, dc.calcDenormalizedDist(dist), 1e-4);
//         }

//         @Test
//         public void testEdgeDistance3dEarthNaN() {
//                 double dist = dc.calcNormalizedEdgeDistance3D(0, 0.5, Double.NaN,
//                                 0, 0, 0,
//                                 0, 1, 0);
//                 assertEquals(0, dc.calcDenormalizedDist(dist), 1e-4);
//         }

//         @Test
//         public void testEdgeDistance3dPlane() {
//                 DistanceCalc calc = new DistancePlaneProjection();
//                 double dist = calc.calcNormalizedEdgeDistance3D(0, 0.5, 10,
//                                 0, 0, 0,
//                                 0, 1, 0);
//                 assertEquals(10, calc.calcDenormalizedDist(dist), 1e-4);
//         }

//         @Test
//         public void testEdgeDistanceStartEndSame() {
//                 DistanceCalc calc = new DistancePlaneProjection();
//                 // just change elevation
//                 double dist = calc.calcNormalizedEdgeDistance3D(0, 0, 10,
//                                 0, 0, 0,
//                                 0, 0, 0);
//                 assertEquals(10, calc.calcDenormalizedDist(dist), 1e-4);
//                 // just change lat
//                 dist = calc.calcNormalizedEdgeDistance3D(1, 0, 0,
//                                 0, 0, 0,
//                                 0, 0, 0);
//                 assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
//                 // just change lon
//                 dist = calc.calcNormalizedEdgeDistance3D(0, 1, 0,
//                                 0, 0, 0,
//                                 0, 0, 0);
//                 assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
//         }

//         @Test
//         public void testEdgeDistanceStartEndDifferentElevation() {
//                 DistanceCalc calc = new DistancePlaneProjection();
//                 // just change elevation
//                 double dist = calc.calcNormalizedEdgeDistance3D(0, 0, 10,
//                                 0, 0, 0,
//                                 0, 0, 1);
//                 assertEquals(0, calc.calcDenormalizedDist(dist), 1e-4);
//                 // just change lat
//                 dist = calc.calcNormalizedEdgeDistance3D(1, 0, 0,
//                                 0, 0, 0,
//                                 0, 0, 1);
//                 assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
//                 // just change lon
//                 dist = calc.calcNormalizedEdgeDistance3D(0, 1, 0,
//                                 0, 0, 0,
//                                 0, 0, 1);
//                 assertEquals(DistanceCalcEarth.METERS_PER_DEGREE, calc.calcDenormalizedDist(dist), 1e-4);
//         }

//         @Test
//         public void testValidEdgeDistance() {
//                 assertTrue(dc.validEdgeDistance(49.94241, 11.544356, 49.937964, 11.541824, 49.942272, 11.555643));
//                 assertTrue(dc.validEdgeDistance(49.936624, 11.547636, 49.937964, 11.541824, 49.942272, 11.555643));
//                 assertTrue(dc.validEdgeDistance(49.940712, 11.556069, 49.937964, 11.541824, 49.942272, 11.555643));

//                 // left bottom of the edge
//                 assertFalse(dc.validEdgeDistance(49.935119, 11.541649, 49.937964, 11.541824, 49.942272, 11.555643));
//                 // left top of the edge
//                 assertFalse(dc.validEdgeDistance(49.939317, 11.539675, 49.937964, 11.541824, 49.942272, 11.555643));
//                 // right top of the edge
//                 assertFalse(dc.validEdgeDistance(49.944482, 11.555446, 49.937964, 11.541824, 49.942272, 11.555643));
//                 // right bottom of the edge
//                 assertFalse(dc.validEdgeDistance(49.94085, 11.557356, 49.937964, 11.541824, 49.942272, 11.555643));

//                 // rounding error
//                 // assertFalse(dc.validEdgeDistance(0.001, 0.001, 0.001, 0.002, 0.00099987, 0.00099987));
//         }

//         @Test
//         public void testPrecisionBug() {
//                 DistanceCalc dist = new DistancePlaneProjection();
//                 //        DistanceCalc dist = new DistanceCalc();
//                 double queryLat = 42.56819, queryLon = 1.603231;
//                 double lat16 = 42.56674481705006, lon16 = 1.6023790821964834;
//                 double lat17 = 42.56694505140808, lon17 = 1.6020622462495173;
//                 double lat18 = 42.56715199128878, lon18 = 1.601682266630581;

//                 // segment 18
//                 assertEquals(171.487, dist.calcDist(queryLat, queryLon, lat18, lon18), 1e-3);
//                 // segment 17
//                 assertEquals(168.298, dist.calcDist(queryLat, queryLon, lat17, lon17), 1e-3);
//                 // segment 16
//                 assertEquals(175.188, dist.calcDist(queryLat, queryLon, lat16, lon16), 1e-3);

//                 assertEquals(167.385, dist.calcDenormalizedDist(
//                                 dist.calcNormalizedEdgeDistance(queryLat, queryLon, lat16, lon16, lat17, lon17)), 1e-3);

//                 assertEquals(168.213, dist.calcDenormalizedDist(
//                                 dist.calcNormalizedEdgeDistance(queryLat, queryLon, lat17, lon17, lat18, lon18)), 1e-3);

//                 // 16_17
//                 assertEquals(new GHPoint(42.567048, 1.6019),
//                                 dist.calcCrossingPointToEdge(queryLat, queryLon, lat16, lon16, lat17, lon17));
//                 // 17_18
//                 // assertEquals(new GHPoint(42.566945,1.602062), dist.calcCrossingPointToEdge(queryLat, queryLon, lat17, lon17, lat18, lon18));
//         }

//         @Test
//         public void testPrecisionBug2() {
//                 DistanceCalc distCalc = new DistancePlaneProjection();
//                 double queryLat = 55.818994, queryLon = 37.595354;
//                 double tmpLat = 55.81777239183573, tmpLon = 37.59598350366913;
//                 double wayLat = 55.818839128736535, wayLon = 37.5942968784488;
//                 assertEquals(68.25, distCalc.calcDist(wayLat, wayLon, queryLat, queryLon), .1);

//                 assertEquals(60.88,
//                                 distCalc.calcDenormalizedDist(distCalc.calcNormalizedEdgeDistance(queryLat, queryLon,
//                                                 tmpLat, tmpLon, wayLat, wayLon)),
//                                 .1);

//                 assertEquals(new GHPoint(55.81863, 37.594626), distCalc.calcCrossingPointToEdge(queryLat, queryLon,
//                                 tmpLat, tmpLon, wayLat, wayLon));
//         }

//         @Test
//         public void testDistance3dEarth() {
//                 DistanceCalc distCalc = new DistanceCalcEarth();
//                 assertEquals(1, distCalc.calcDist3D(
//                                 0, 0, 0,
//                                 0, 0, 1), 1e-6);
//         }

//         @Test
//         public void testDistance3dEarthNaN() {
//                 DistanceCalc distCalc = new DistanceCalcEarth();
//                 assertEquals(0, distCalc.calcDist3D(
//                                 0, 0, 0,
//                                 0, 0, Double.NaN), 1e-6);
//                 assertEquals(0, distCalc.calcDist3D(
//                                 0, 0, Double.NaN,
//                                 0, 0, 10), 1e-6);
//                 assertEquals(0, distCalc.calcDist3D(
//                                 0, 0, Double.NaN,
//                                 0, 0, Double.NaN), 1e-6);
//         }

//         @Test
//         public void testDistance3dPlane() {
//                 DistancePlaneProjection distCalc = new DistancePlaneProjection();
//                 assertEquals(1, distCalc.calcDist3D(
//                                 0, 0, 0,
//                                 0, 0, 1), 1e-6);
//                 assertEquals(10, distCalc.calcDist3D(
//                                 0, 0, 0,
//                                 0, 0, 10), 1e-6);
//         }

//         @Test
//         public void testDistance3dPlaneNaN() {
//                 DistancePlaneProjection distCalc = new DistancePlaneProjection();
//                 assertEquals(0, distCalc.calcDist3D(
//                                 0, 0, 0,
//                                 0, 0, Double.NaN), 1e-6);
//                 assertEquals(0, distCalc.calcDist3D(
//                                 0, 0, Double.NaN,
//                                 0, 0, 10), 1e-6);
//                 assertEquals(0, distCalc.calcDist3D(
//                                 0, 0, Double.NaN,
//                                 0, 0, Double.NaN), 1e-6);
//         }

//         @Test
//         public void testIntermediatePoint() {
//                 DistanceCalc distCalc = new DistanceCalcEarth();
//                 GHPoint point = distCalc.intermediatePoint(0, 0, 0, 0, 0);
//                 assertEquals(0, point.getLat(), 1e-5);
//                 assertEquals(0, point.getLon(), 1e-5);

//                 point = distCalc.intermediatePoint(0.5, 0, 0, 10, 0);
//                 assertEquals(5, point.getLat(), 1e-5);
//                 assertEquals(0, point.getLon(), 1e-5);

//                 point = distCalc.intermediatePoint(0.5, 0, 0, 0, 10);
//                 assertEquals(0, point.getLat(), 1e-5);
//                 assertEquals(5, point.getLon(), 1e-5);

//                 // cross international date line going west
//                 point = distCalc.intermediatePoint(0.5, 45, -179, 45, 177);
//                 assertEquals(45, point.getLat(), 1);
//                 assertEquals(179, point.getLon(), 1e-5);

//                 // cross international date line going east
//                 point = distCalc.intermediatePoint(0.5, 45, 179, 45, -177);
//                 assertEquals(45, point.getLat(), 1);
//                 assertEquals(-179, point.getLon(), 1e-5);

//                 // cross north pole
//                 point = distCalc.intermediatePoint(0.25, 45, -90, 45, 90);
//                 assertEquals(67.5, point.getLat(), 1e-1);
//                 assertEquals(-90, point.getLon(), 1e-5);
//                 point = distCalc.intermediatePoint(0.75, 45, -90, 45, 90);
//                 assertEquals(67.5, point.getLat(), 1e-1);
//                 assertEquals(90, point.getLon(), 1e-5);
//         }

//         /**
//          * Vérifie le comportement de la méthode createBBox(). Cette méthode calcule une boîte englobante (BBox) à 
//          * partir d’un point central (lat, lon) et d’un rayon en mètres.
//          * Le test couvre plusieurs cas :
//          *      - Entrées invalides (rayons nuls ou négatifs)
//          *      - Rayons très grands (cohérence et absence d’infini/NaN)
//          *      - Cas particuliers proches des pôles
//          *      - Rayons minuscules (résolution fine)
//          *      - Propriété de symétrie et monotonicité des bornes
//          * 
//          * L’objectif est de s’assurer que createBBox() renvoie toujours une boîte valide, cohérente et 
//          * mathématiquement stable, quelles que soient les valeurs d’entrée.
//          */
//         @Test
//         void testCreateBBox() {
//                 DistanceCalcEarth calc = new DistanceCalcEarth();
//                 double eps = 1e-6;

//                 // 1) Entrées invalides
//                 assertThrows(IllegalArgumentException.class, () -> calc.createBBox(0, 0, 0),
//                                 "Un rayon nul doit déclencher une IllegalArgumentException.");
//                 assertThrows(IllegalArgumentException.class, () -> calc.createBBox(25, 50, -100),
//                                 "Un rayon négatif doit déclencher une IllegalArgumentException.");

//                 // 2) Rayon extrêmement grand
//                 double largeRadius = 2000000000;
//                 BBox b = calc.createBBox(0, 0, largeRadius);

//                 // Vérifie que les bornes sont ordonnées et valides.
//                 assertTrue(b.minLat < b.maxLat, "minLat doit être inférieur à maxLat");
//                 assertTrue(b.minLon < b.maxLon, "minLon doit être inférieur à maxLon");

//                 // Aucune valeur infinie ou indéfinie ne doit être générée.
//                 assertAll("Les bornes doivent être finies et définies",
//                                 () -> assertFalse(Double.isNaN(b.minLat) || Double.isNaN(b.maxLat)),
//                                 () -> assertFalse(Double.isInfinite(b.minLat) || Double.isInfinite(b.maxLat)),
//                                 () -> assertFalse(Double.isNaN(b.minLon) || Double.isNaN(b.maxLon)),
//                                 () -> assertFalse(Double.isInfinite(b.minLon) || Double.isInfinite(b.maxLon)));

//                 // Vérifie la symétrie de la BBox par rapport à l’équateur et au méridien zéro.
//                 assertEquals(-b.minLat, b.maxLat, eps,
//                                 "La BBox doit être symétrique en latitude par rapport à l'équateur.");
//                 assertEquals(-b.minLon, b.maxLon, eps,
//                                 "La BBox doit être symétrique en longitude par rapport au méridien principal.");

//                 // 3) Valeurs extrêmes mais cohérentes
//                 BBox bPolar = calc.createBBox(90 - 1e-12, 0, 10000);
//                 assertTrue(bPolar.maxLat >= bPolar.minLat,
//                                 "Les latitudes doivent rester ordonnées même à proximité des pôles.");
//                 assertFalse(Double.isNaN(bPolar.maxLon),
//                                 "Les longitudes doivent être valides à toute latitude, y compris près des pôles.");

//                 // 4) Rayon minuscule
//                 BBox bSmall = calc.createBBox(0, 0, eps);
//                 assertTrue(bSmall.maxLat > bSmall.minLat,
//                                 "Une BBox de très petit rayon doit garder des bornes distinctes en latitude.");
//                 assertTrue(bSmall.maxLon > bSmall.minLon,
//                                 "Une BBox de très petit rayon doit garder des bornes distinctes en longitude.");

//                 // Monotonicité
//                 assertTrue((b.maxLat - b.minLat) > (bSmall.maxLat - bSmall.minLat),
//                                 "Une BBox de rayon plus grand doit couvrir une plus grande étendue latitudinale que celle d'un petit rayon.");
//         }


//         /**
//          * Vérifie la cohérence de la méthode calcDistance(). Cette méthode calcule la distance cumulée d’une liste 
//          * de points (PointList), avec ou sans prise en compte de l’élévation (2D ou 3D).
//          * Le test couvre :
//          *      - les cas limites (liste vide ou à un seul point)
//          *      - les segments horizontaux, verticaux et diagonaux
//          *      - la différence entre distance 2D et 3D
//          *      - la robustesse numérique et la cohérence géométrique (Pythagore)
//          */
//         @Test
//         void testInternCalcDistance() {
//                 double eps = 1e-6;

//                 // 1) Liste vide
//                 PointList plEmpty = new PointList(0, true);
//                 assertAll("Une liste vide doit toujours produire une distance nulle",
//                                 () -> assertEquals(0, DistanceCalcEarth.calcDistance(plEmpty, false)),
//                                 () -> assertEquals(0, DistanceCalcEarth.calcDistance(plEmpty, true)));

//                 // 2) Un seul point
//                 PointList plSingle = new PointList(1, true);
//                 plSingle.add(0.0, 0.0, 0.0);
//                 assertAll("Une liste contenant un seul point doit produire une distance nulle",
//                                 () -> assertEquals(0, DistanceCalcEarth.calcDistance(plSingle, false), eps),
//                                 () -> assertEquals(0, DistanceCalcEarth.calcDistance(plSingle, true), eps));

//                 // 3) Segments verticaux et horizontaux
//                 PointList pl = new PointList(4, true);
//                 pl.add(0, 0, 0); // P0
//                 pl.add(0, 0, 100); // P1
//                 pl.add(0, -50, 100); // P2
//                 pl.add(0, -50, 0); // P3

//                 // Calcul des distances
//                 double dist2D = DistanceCalcEarth.calcDistance(pl, false); // Ignorer l'élévation
//                 double dist3D = DistanceCalcEarth.calcDistance(pl, true); // Inclure l'élévation

//                 // Vérifie la non-négativité et la stabilité numérique
//                 assertTrue(dist2D >= 0 && dist3D >= 0, "Distances doivent être non négatives");
//                 assertAll("Les distances doivent être finies et définies",
//                                 () -> assertFalse(Double.isNaN(dist2D) || Double.isInfinite(dist2D)),
//                                 () -> assertFalse(Double.isNaN(dist3D) || Double.isInfinite(dist3D)));
//                 assertTrue(dist3D > dist2D,
//                                 "Avec de l'élévation, la distance 3D doit etre supérieur à la distance 2D");
//                 assertEquals(200, dist3D - dist2D, eps,
//                                 "L'ajout de deux segments verticaux de 100 m doit augmenter la distance totale d'environ 200 m.");

//                 // 4) Segment diagonal
//                 PointList plDiag = new PointList(2, true);
//                 plDiag.add(0, 0, 0);
//                 plDiag.add(1, -1, 100);

//                 double dist2DDiag = DistanceCalcEarth.calcDistance(plDiag, false);
//                 double dist3DDiag = DistanceCalcEarth.calcDistance(plDiag, true);

//                 assertTrue(dist3DDiag > dist2DDiag,
//                                 "Pour un segment diagonal, la distance 3D doit être supérieure à la distance 2D.");

//                 // Relation de Pythagore
//                 double expected3D = Math.sqrt(dist2DDiag * dist2DDiag + 100 * 100);
//                 assertEquals(expected3D, dist3DDiag, eps,
//                                 "La distance 3D doit respecter la relation de Pythagore entre le plan 2D et la différence d'altitude.");
//         }

        
//     /**
//      * Vérifie le comportement de la méthode projectCoordinate(double, double, 
//      * double, double), qui calcule le point d'arrivé d'un déplacement selon le 
//      * point de départ, la direction empruntée et la distance parcourue.
//      *     Les tests sont divisés en 4 familles: 
//      *         (1) Point de départ: simple. Direction: simple.
//      *             -> Vérifie le comportement lors du changement d'hémisphère.
//      *         (2) Point de départ: simple. Direction: limite.
//      *         (3) Point de départ: simple. Direction: Typique.
//      *         (4) Point de départ: limite. Direction: simple.
//      *             -> Vérifie le comportement le de la traversée d'un pôle.
//      * Inspiré de ChatGPT
//      * Source de double vérification des latitudes/longitudes: https://www.omnicalculator.com/other/latitude-longitude-distance 
//      */
//     @Test
//     void testProjectCoordinateCardinalDirections() {
//         DistanceCalcEarth calc = new DistanceCalcEarth();
//         double changedDelta = 1e-3;
//         double sameDelta = 1e-6;
//         double distKm = 1000.0; // 1 km
//         double distDegrees = .009; // Distance en degrés correpondant à 1km à l'équateur

//         // 1) POINT DE DÉPART ET DIRECTIONS SIMPLES

//         // Point de départ: équateur
//         double lat0 = 0.0;
//         double lon0 = 0.0;

//         // a) Nord : latitude augmente, longitude inchangée
//         GHPoint north = calc.projectCoordinate(lat0, lon0, distKm, 0);
//         assertEquals(distDegrees, north.lat, changedDelta, "La différence de latitude doit correspondre à la distance parcourue.");
//         assertEquals(lon0, north.lon, sameDelta, "Longitude doit rester quasi constante vers le nord");

//         // b) Sud : latitude diminue, longitude inchangée
//         GHPoint south = calc.projectCoordinate(lat0, lon0, distKm, 180);
//         assertEquals(-distDegrees, south.lat, changedDelta, "La différence de latitude doit correspondre à la distance parcourue.");
//         assertEquals(lon0, south.lon, sameDelta, "Longitude doit rester quasi constante vers le sud");

//         // c) Est : longitude augmente, latitute inchangée
//         GHPoint east = calc.projectCoordinate(lat0, lon0, distKm, 90);
//         assertEquals(distDegrees, east.lon, changedDelta, "La latitude doit rester inchangée");
//         assertEquals(lat0, east.lat, sameDelta, "La différence de longitude doit correspondre à la distance parcourue.");

//         // d) Ouest : longitude diminue, latitute inchangée
//         GHPoint west = calc.projectCoordinate(lat0, lon0, distKm, 270);
//         assertEquals(-distDegrees, west.lon, changedDelta, "La latitude doit rester inchangée");
//         assertEquals(lat0, west.lat, sameDelta, "La différence de longitude doit correspondre à la distance parcourue.");

//         // 2) POINT DE DÉPART SIMPLE, DIRECTION LIMITE
        
//         // a) Nord, angle = 360° : latitude augmente, longitude inchangée
//         GHPoint north2 = calc.projectCoordinate(lat0, lon0, distKm, 360);
//         assertEquals(distDegrees, north2.lat, changedDelta, "La différence de latitude doit correspondre à la distance parcourue.");
//         assertEquals(lon0, north2.lon, sameDelta, "La longitude doit rester inchangée");

//         // b) Est, angle = 450° > 360° : longitude diminue, latitute inchangée
//         GHPoint east2 = calc.projectCoordinate(lat0, lon0, distKm, 450);
//         assertEquals(distDegrees, east2.lon, changedDelta, "La latitude doit rester inchangée");
//         assertEquals(lat0, east2.lat, sameDelta, "La différence de longitude doit correspondre à la distance parcourue.");

//         // c) Ouest, angle = -90° < 0° : longitude diminue, latitute inchangée
//         GHPoint west2 = calc.projectCoordinate(lat0, lon0, distKm, -90);
//         assertEquals(-distDegrees, west2.lon, changedDelta, "La latitude doit rester inchangée");
//         assertEquals(lat0, west2.lat, sameDelta, "La différence de longitude doit correspondre à la distance parcourue.");

//         // 3) POINT DE DÉPART SIMPLE, DIRECTION TYPIQUE
        
//         // Nord-Est, angle non carré
//         double dist45d = .006389; // Distance sur un axe correspondant à un mouvement
//                                   // de 45° sur 1km
//         GHPoint northEast = calc.projectCoordinate(lat0, lon0, distKm, 45);
//         assertEquals(dist45d, northEast.lat, changedDelta, "La latitude est incorrecte.");
//         assertEquals(dist45d, northEast.lon, changedDelta, "La longitude est incorrecte.");

//         // 4) POINT DE DÉPART LIMITE

//         // Coordonnées de départ: pôle nord
//         lat0 = 90.0;
//         lon0 = 5.0;

//         double direction = 90.0; // Direction du mouvement

//         // 90 degrés : latitude diminue, longitude d'arrivée est opposée
//         south = calc.projectCoordinate(lat0, lon0, distKm, direction);
//         assertEquals(lat0 - distDegrees, south.lat, changedDelta, "La latitude doit diminuer autant que la distance parcourue");
//         assertEquals(lon0 + direction, south.lon, sameDelta, "Le pôle nord est franchi, la longitude d'arrivée doit être la somme de la longitude initiale et de la direction.");

//     }

// }
