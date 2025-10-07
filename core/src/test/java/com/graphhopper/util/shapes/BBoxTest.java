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
package com.graphhopper.util.shapes;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

/**
 * @author Peter Karich
 */
public class BBoxTest {
    @Test
    public void testCreate() {
        DistanceCalc c = new DistanceCalcEarth();
        BBox b = c.createBBox(52, 10, 100000);

        // The calculated bounding box has no negative values (also for southern hemisphere and negative meridians)
        // and the ordering is always the same (top to bottom and left to right)
        assertEquals(52.8993, b.maxLat, 1e-4);
        assertEquals(8.5393, b.minLon, 1e-4);

        assertEquals(51.1007, b.minLat, 1e-4);
        assertEquals(11.4607, b.maxLon, 1e-4);
    }

    @Test
    public void testContains() {
        assertTrue(new BBox(1, 2, 0, 1).contains(new BBox(1, 2, 0, 1)));
        assertTrue(new BBox(1, 2, 0, 1).contains(new BBox(1.5, 2, 0.5, 1)));
        assertFalse(new BBox(1, 2, 0, 0.5).contains(new BBox(1.5, 2, 0.5, 1)));
    }

    @Test
    public void testIntersect() {
        //    ---
        //    | |
        // ---------
        // |  | |  |
        // --------
        //    |_|
        //

        // use ISO 19115 standard (minLon, maxLon followed by minLat(south!),maxLat)
        assertTrue(new BBox(12, 15, 12, 15).intersects(new BBox(13, 14, 11, 16)));
        // assertFalse(new BBox(15, 12, 12, 15).intersects(new BBox(16, 15, 11, 14)));

        // DOES NOT WORK: use bottom to top coord for lat
        // assertFalse(new BBox(6, 2, 11, 6).intersects(new BBox(5, 3, 12, 5)));
        // so, use bottom-left and top-right corner!
        assertTrue(new BBox(2, 6, 6, 11).intersects(new BBox(3, 5, 5, 12)));

        // DOES NOT WORK: use bottom to top coord for lat and right to left for lon
        // assertFalse(new BBox(6, 11, 11, 6).intersects(new BBox(5, 10, 12, 7)));
        // so, use bottom-right and top-left corner
        assertTrue(new BBox(6, 11, 6, 11).intersects(new BBox(7, 10, 5, 12)));
    }

    @Test
    public void testPointListIntersect() {
        BBox bbox = new BBox(-0.5, 1, 1, 2);
        PointList pointList = new PointList();
        pointList.add(5, 5);
        pointList.add(5, 0);
        assertFalse(bbox.intersects(pointList));

        pointList.add(-5, 0);
        assertTrue(bbox.intersects(pointList));

        pointList = new PointList();
        pointList.add(5, 1);
        pointList.add(-1, 0);
        assertTrue(bbox.intersects(pointList));

        pointList = new PointList();
        pointList.add(5, 0);
        pointList.add(-1, 3);
        assertFalse(bbox.intersects(pointList));

        pointList = new PointList();
        pointList.add(5, 0);
        pointList.add(-1, 2);
        assertTrue(bbox.intersects(pointList));

        pointList = new PointList();
        pointList.add(1.5, -2);
        pointList.add(1.5, 2);
        assertTrue(bbox.intersects(pointList));
    }

    @Test
    public void testCalculateIntersection() {
        BBox b1 = new BBox(0, 2, 0, 1);
        BBox b2 = new BBox(-1, 1, -1, 2);
        BBox expected = new BBox(0, 1, 0, 1);

        assertEquals(expected, b1.calculateIntersection(b2));

        //No intersection
        b2 = new BBox(100, 200, 100, 200);
        assertNull(b1.calculateIntersection(b2));

        //Real Example
        b1 = new BBox(8.8591,9.9111,48.3145,48.8518);
        b2 = new BBox(5.8524,17.1483,46.3786,55.0653);

        assertEquals(b1, b1.calculateIntersection(b2));
    }

    @Test
    public void testParseTwoPoints() {
        assertEquals(new BBox(2, 4, 1, 3), BBox.parseTwoPoints("1,2,3,4"));
        // stable parsing, i.e. if first point is in north or south it does not matter:
        assertEquals(new BBox(2, 4, 1, 3), BBox.parseTwoPoints("3,2,1,4"));
    }

    @Test
    public void testParseBBoxString() {
        assertEquals(new BBox(2, 4, 1, 3), BBox.parseBBoxString("2,4,1,3"));
    }

    
    @Test
    void testDegenerateContact() {
        // Ligne verticale : lon fixe = 10, lat ∈ [-1, 1]
        BBox verticalLine = new BBox(10, 10, -1, 1);

        // 1) Contact (lon touche sans chevauchement) → false
        BBox horizontalContact = new BBox(10, 10, 0, 0);
        assertFalse(verticalLine.intersects(horizontalContact));

        // 2) Chevauchement minimal en lon, lat à l’intérieur → true
        BBox horizontalOverlapLon = new BBox(9.9999, 10.0001, 0, 0);
        assertTrue(verticalLine.intersects(horizontalOverlapLon));

        // Chevauchement en lon, lat à l’extérieur de [-1,1] → false
        BBox horizontalOutsideLat = new BBox(9.9999, 10.0001, 2, 2);
        assertFalse(verticalLine.intersects(horizontalOutsideLat));

        // Pas de chevauchement en lon, lat à l’intérieur
        BBox horizontalOutsideLon = new BBox(9.5, 9.9, 0, 0);
        assertFalse(verticalLine.intersects(horizontalOutsideLon));

        // Symétrie d’appel (débusque certaines suppressions de conditions)
        assertFalse(horizontalContact.intersects(verticalLine));
        assertTrue(horizontalOverlapLon.intersects(verticalLine));
        assertFalse(horizontalOutsideLat.intersects(verticalLine));
        assertFalse(horizontalOutsideLon.intersects(verticalLine));

        // Vérifs de dégénérescence
        assertEquals(verticalLine.minLon, verticalLine.maxLon);
        assertEquals(horizontalContact.minLat, horizontalContact.maxLat);
    }

    @Test
    void testIsValid() {
        double eps = 1e-12;

        // Bornes égales
        assertFalse(new BBox(10.0, 10.0, -5, 0).isValid());
        assertFalse(new BBox(1, 2, -2, -4).isValid());
        assertTrue(new BBox(-3, 0, 0, 7, 11, 11).isValid());

        // Bornes inversées classiques
        assertFalse(new BBox(10, -10, 0, 5).isValid());
        assertFalse(new BBox(-2, 8, -4, -6).isValid());
        assertFalse(new BBox(-7, 0, 0, 14, 21, -3).isValid());

        // Bornes proches
        assertTrue(new BBox(0, 0 + eps, -10, -10 + eps).isValid());
        assertTrue(new BBox(-10.000001, -10, 0, 0.000001).isValid());

        // Bornes extrêmes
        assertTrue(new BBox(-180, 180, -90, 90).isValid());
        assertTrue(new BBox(-500, 500, -250, 250).isValid());
        assertFalse(new BBox(0, 10, -10, 0, 0, -Double.MAX_VALUE).isValid());
        assertFalse(new BBox(0, 10, -10, 0, Double.MAX_VALUE, Double.MAX_VALUE).isValid());
        assertTrue(new BBox(0, 10, -10, 0, -Double.MAX_VALUE, Double.MAX_VALUE).isValid());
    }
    
    @Test
    void testUpdate() {
        BBox b = new BBox(0, 0, 0, 0, 0, 0);
        double eps = 1e-12;

        // Points au-delà des bornes
        b.update(11, 12, 13);
        assertEquals(0, b.minLat);
        assertEquals(11, b.maxLat);
        assertEquals(0, b.minLon);
        assertEquals(12, b.maxLon);
        assertEquals(0, b.minEle);
        assertEquals(13, b.maxEle);

        // Points intérieurs
        b.update(5, 5, 5);
        assertEquals(0, b.minLat);
        assertEquals(11, b.maxLat);
        assertEquals(0, b.minLon);
        assertEquals(12, b.maxLon);
        assertEquals(0, b.minEle);
        assertEquals(13, b.maxEle);

        // Points egaux
        b.update(11, 12, 13);
        assertEquals(0, b.minLat);
        assertEquals(11, b.maxLat);
        assertEquals(0, b.minLon);
        assertEquals(12, b.maxLon);
        assertEquals(0, b.minEle);
        assertEquals(13, b.maxEle);

        b.update(0, 0, 0);
        assertEquals(0, b.minLat);
        assertEquals(11, b.maxLat);
        assertEquals(0, b.minLon);
        assertEquals(12, b.maxLon);
        assertEquals(0, b.minEle);
        assertEquals(13, b.maxEle);

        // Points sous les bornes
        b.update(-3, -6, -9);
        assertEquals(-3, b.minLat);
        assertEquals(11, b.maxLat);
        assertEquals(-6, b.minLon);
        assertEquals(12, b.maxLon);
        assertEquals(-9, b.minEle);
        assertEquals(13, b.maxEle);

        // Points avec valeurs extrêmes
        b.update(Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE);
        assertEquals(-3, b.minLat);
        assertEquals(Double.MAX_VALUE, b.maxLat);
        assertEquals(-Double.MAX_VALUE, b.minLon);
        assertEquals(12, b.maxLon);
        assertEquals(-9, b.minEle);
        assertEquals(Double.MAX_VALUE, b.maxEle);

        // Point proches
        b.update(-3 - eps, 12 + eps, -9 + eps);
        assertEquals(-3 - eps, b.minLat, 0);
        assertEquals(Double.MAX_VALUE, b.maxLat);
        assertEquals(-Double.MAX_VALUE, b.minLon);
        assertEquals(12 + eps, b.maxLon);
        assertEquals(-9, b.minEle);
        assertEquals(Double.MAX_VALUE, b.maxEle);

        BBox bNoEle = new BBox(0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> bNoEle.update(1, 1, 1));

        bNoEle.update(1, 1);
        assertEquals(0, bNoEle.minLat);
        assertEquals(1, bNoEle.maxLat);
        assertEquals(0, bNoEle.minLon);
        assertEquals(1, bNoEle.maxLon);
    }

    @Test
    void testClone() {
        BBox b = new BBox(-10, 10, -5, 5, 15, 25);
        BBox c = b.clone();

        assertNotSame(b, c);
        assertEquals(b.minLon, c.minLon);
        assertEquals(b.maxLon, c.maxLon);
        assertEquals(b.minLat, c.minLat);
        assertEquals(b.maxLat, c.maxLat);
        assertEquals(b.minEle, c.minEle);
        assertEquals(b.maxEle, c.maxEle);
        assertEquals(b.hasElevation(), c.hasElevation());

        // Indépendance des deux objets
        c.minLat = -99;
        c.maxEle = 999;
        assertNotEquals(b.minLat, c.minLat);
        assertNotEquals(b.maxEle, c.maxEle);

        // Sans élévation
        BBox bNoElev = new BBox(-1, 1, -2, 2);
        BBox cNoElev = bNoElev.clone();
        assertFalse(cNoElev.hasElevation());

        assertNotNull(b.clone(), "clone() ne doit jamais retourner null");
        assertTrue(b.equals(c));
        assertEquals(b.hashCode(), c.hashCode());
    }
}
