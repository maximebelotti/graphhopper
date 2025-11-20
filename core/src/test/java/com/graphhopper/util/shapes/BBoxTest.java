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

import com.github.javafaker.Faker;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

import java.util.Locale;
import java.util.Random;

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
        b1 = new BBox(8.8591, 9.9111, 48.3145, 48.8518);
        b2 = new BBox(5.8524, 17.1483, 46.3786, 55.0653);

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


    /**
     * Vérifie le comportement de la méthode isValid() pour différents scénarios de validité.
     * Le test couvre plusieurs familles de cas : bornes correctement ordonnées, bornes égales, bornes inversées, 
     * bornes très proches et bornes extrêmes.
     * L’objectif est de s’assurer que la méthode distingue correctement une BBox cohérente d’une BBox incohérente ou 
     * dégénérée, même dans des situations limites.
     */
    @Test
    void testIsValid() {
        double eps = 1e-12;

        // 1) Bornes justes
        assertTrue(new BBox(1, 2, 3, 4).isValid(),
                "BBox classique avec bornes bien ordonnées doit être valide.");

        // 2) Bornes égales
        assertFalse(new BBox(10, 10, -5, 0).isValid(),
                "BBox avec longitudes égales doit être invalide.");
        assertFalse(new BBox(1, 2, -4, -4).isValid(),
                "BBox avec latitudes égales doit être invalide.");
        assertTrue(new BBox(-3, 0, 0, 7, 11, 11).isValid(),
                "BBox avec des bornes valides et une élévation égale doit rester valide.");

        // 3) Bornes inversées classiques
        assertFalse(new BBox(10, -10, 0, 5).isValid(),
                "BBox avec longitudes inversées doit être invalide.");
        assertFalse(new BBox(-2, 8, -4, -6).isValid(),
                "BBox avec latitudes inversées doit être invalide.");
        assertFalse(new BBox(-7, 0, 0, 14, 21, -3).isValid(),
                "BBox avec élévations inversées doit être invalide.");

        // 4) Bornes proches
        assertTrue(new BBox(0, 0 + eps, -10, -10 + eps).isValid(),
                "BBox avec bornes très proches doit rester valide.");
        assertTrue(new BBox(-10 - eps, -10, 0 - eps, 0).isValid(),
                "BBox avec bornes très proches doit rester valide.");

        // 5) Bornes extrêmes
        assertTrue(new BBox(-180, 180, -90, 90).isValid(),
                "BBox mondiale doit être valide.");
        assertTrue(new BBox(-500, 500, -250, 250).isValid(),
                "BBox étendue au-delà des limites géographiques reste valide.");
        assertFalse(new BBox(0, 10, -10, 0, 0, -Double.MAX_VALUE).isValid(),
                "BBox avec maxEle négative infinie doit être invalide.");
        assertFalse(new BBox(0, 10, -10, 0, Double.MAX_VALUE, Double.MAX_VALUE).isValid(),
                "BBox avec minEle infinies doit être invalide.");
        assertTrue(new BBox(0, 10, -10, 0, -Double.MAX_VALUE, Double.MAX_VALUE).isValid(),
                "BBox extrême mais avec des bornes cohérentes reste valide.");
    }

    
    /**
     * Vérifie le bon fonctionnement de la méthode update() de la classe BBox. Cette méthode met à jour les bornes min 
     * et max d’une boîte englobante en fonction de nouveaux points (latitude, longitude et éventuellement élévation).
     * Le test couvre plusieurs cas : mise à jour avec des valeurs hors bornes, points intérieurs, gestion de 
     * l’absence d’élévation, tolérance numérique et comportement face à des valeurs extrêmes.
     */
    @Test
    void testUpdate() {
        BBox b = new BBox(0, 0, 0, 0, 0, 0);
        double eps = 1e-12;

        // 1) Points au-delà des bornes
        b.update(11, 12, 13);
        b.update(-3, -6, -9);
        assertAll("Les bornes doivent s'étendre pour inclure les points en dehors de la BBox initiale",
                () -> assertEquals(-3, b.minLat),
                () -> assertEquals(11, b.maxLat),
                () -> assertEquals(-6, b.minLon),
                () -> assertEquals(12, b.maxLon),
                () -> assertEquals(-9, b.minEle),
                () -> assertEquals(13, b.maxEle));

        // 2) Points intérieurs et égaux
        b.update(5, 5, 5);
        b.update(-3, -6, -9);
        b.update(11, 12, 13);
        assertAll("Les bornes doivent rester stables lorsque les points sont à l'intérieur des limites existantes",
                () -> assertEquals(-3, b.minLat),
                () -> assertEquals(11, b.maxLat),
                () -> assertEquals(-6, b.minLon),
                () -> assertEquals(12, b.maxLon),
                () -> assertEquals(-9, b.minEle),
                () -> assertEquals(13, b.maxEle));

        // 3) BBox sans élévation
        BBox bNoEle = new BBox(0, 0, 0, 0);
        assertThrows(IllegalStateException.class, () -> bNoEle.update(1, 1, 1),
                "La mise à jour avec une élévation sur une BBox sans altitude doit lever une exception.");

        bNoEle.update(1, 1);
        assertAll("La BBox sans élévation doit s'ajuster correctement avec des points 2D",
                () -> assertEquals(0, bNoEle.minLat),
                () -> assertEquals(1, bNoEle.maxLat),
                () -> assertEquals(0, bNoEle.minLon),
                () -> assertEquals(1, bNoEle.maxLon));

        // 4) Point proches
        b.update(-3 - eps, 12 + eps, -9 + eps);
        assertAll("Les bornes doivent s’ajuster légèrement sans dégradation numérique notable",
                () -> assertEquals(-3 - eps, b.minLat, 0),
                () -> assertEquals(11, b.maxLat),
                () -> assertEquals(-6, b.minLon),
                () -> assertEquals(12 + eps, b.maxLon),
                () -> assertEquals(-9, b.minEle),
                () -> assertEquals(13, b.maxEle));

        // 5) Points avec valeurs extrêmes
        b.update(Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE);
        assertAll("La BBox doit rester cohérente après des mises à jour avec des valeurs extrêmes",
                () -> assertEquals(-3 - eps, b.minLat),
                () -> assertEquals(Double.MAX_VALUE, b.maxLat),
                () -> assertEquals(-Double.MAX_VALUE, b.minLon),
                () -> assertEquals(12 + eps, b.maxLon),
                () -> assertEquals(-9, b.minEle),
                () -> assertEquals(Double.MAX_VALUE, b.maxEle));

        // Vérifie que la BBox finale reste valide après toutes les mises à jour.
        assertTrue(b.isValid(), "Après plusieurs mises à jour extrêmes, la BBox doit rester valide.");
    }


    /**
     * Vérifie en profondeur le comportement de la méthode equals() de la classe BBox.
     * Ce test couvre différents scénarios d’égalité : réflexivité, symétrie, transitivité, différences sur les 
     * bornes, gestion de l’élévation, tolérance numérique et vérification des comportements anormaux.
     */
    @Test
    void testEquals() {
        // 1) BBox identiques
        BBox a = new BBox(1, 2, 3, 4, 5, 6);
        BBox b = new BBox(1, 2, 3, 4, -5, -6);
        BBox c = new BBox(1, 2, 3, 4, 10, 25);

        // Propriétés fondamentales de l’égalité
        assertTrue(a.equals(a),
                "Une BBox doit toujours être égale à elle-même (réflexivité)."); // Réflexivité
        assertTrue(a.equals(b) && b.equals(a),
                "L'égalité doit être symétrique entre deux BBox équivalentes."); // Symétrie
        assertTrue(a.equals(b) && b.equals(c) && a.equals(c),
                "L'égalité doit être transitive entre BBox partageant les mêmes bornes géographiques."); // Transitivité

        // 2) BBox différentes
        BBox diffLon = new BBox(0, 2, 3, 4, 5, 6);
        BBox diffLat = new BBox(1, 2, 0, 4, 5, 6);

        assertFalse(a.equals(diffLon),
                "Deux BBox avec des longitudes différentes ne doivent pas être égales.");
        assertFalse(a.equals(diffLat),
                "Deux BBox avec des latitudes différentes ne doivent pas être égales.");
        assertTrue(!a.equals(diffLon) && !diffLon.equals(a),
                "L'inégalité doit être symétrique entre deux BBox différentes."); // Symétrie

        // 3) BBox sans élévation
        BBox noEleA = new BBox(1, 2, 3, 4);
        BBox noEleB = new BBox(1, 2, 3, 4);
        assertTrue(noEleA.equals(noEleB),
                "Deux BBox purement 2D identiques doivent être considérées égales.");
        assertTrue(a.equals(noEleA),
                "Une BBox 3D doit pouvoir être égale à une BBox 2D si leurs latitude et longitude correspondent.");

        // 4) Tolérance numérique
        BBox withinTolerance = new BBox(1 + 1e-9, 2, 3, 4);
        assertTrue(a.equals(withinTolerance),
                "De faibles écarts (flottants) doivent être considérés comme égaux.");
        BBox outsideTolerance = new BBox(1 + 1e-3, 2, 3, 4);
        assertFalse(a.equals(outsideTolerance),
                "Des écarts au-delà du seuil de tolérance doivent être considérés comme différents.");

        // 5) Cas limites et erreurs
        assertFalse(a.equals(null),
                "Une BBox ne doit jamais être égale à null.");
        assertThrows(ClassCastException.class, () -> a.equals("BBox"),
                "Une comparaison avec un autre type 'objet doit lever une exception.");
    }


    /**
     * Vérifie que la méthode clone() crée une copie fidèle et indépendante d’une BBox.
     * Le test s’assure que les valeurs sont identiques entre l’original et le clone, que les deux objets ne 
     * partagent pas la même référence en mémoire, et que le clonage préserve correctement l’absence d’élévation pour 
     * les BBox 2D.
     */
    @Test
    void testClone() {
        // 1) Clonage d’une BBox avec élévation
        BBox b = new BBox(-10, 10, -5, 5, 15, 25);
        BBox c = b.clone();

        assertAll("Le clone doit contenir les mêmes bornes et attributs que l'objet original",
                () -> assertNotSame(b, c),
                () -> assertEquals(b.minLon, c.minLon),
                () -> assertEquals(b.maxLon, c.maxLon),
                () -> assertEquals(b.minLat, c.minLat),
                () -> assertEquals(b.maxLat, c.maxLat),
                () -> assertEquals(b.minEle, c.minEle),
                () -> assertEquals(b.maxEle, c.maxEle),
                () -> assertEquals(b.hasElevation(), c.hasElevation()));

        // Verification de l'indépendance des deux objets
        c.minLat = -99;
        c.maxEle = 999;
        assertAll("La modification du clone ne doit pas impacter l'objet original",
                () -> assertNotEquals(b.minLat, c.minLat),
                () -> assertNotEquals(b.maxEle, c.maxEle));

        // 2) Clonage d’une BBox sans élévation
        BBox bNoElev = new BBox(-1, 1, -2, 2);
        BBox cNoElev = bNoElev.clone();
        assertFalse(cNoElev.hasElevation(),
                "Le clonage d'une BBox sans élévation doit préserver son état d'elevation");
    }


    /**
     * Vérifie la cohérence des intersections calculées entre deux BBox générées aléatoirement.
     * Ce test utilise JavaFaker pour produire des coordonnées réalistes, couvrant un large éventail de situations : 
     * boîtes se chevauchant partiellement, totalement ou disjointes.
     * L’objectif est de valider que calculateIntersection() renvoie un résultat logique, soit une intersection 
     * cohérente ou rien.
     */
    @Test
    void testCalculateIntersectionConsistencyWithFaker() {
        Faker faker = new Faker(new Random(42));

        for (int i = 0; i < 200; i++) {
            // Génération aléatoire de deux BBox
            double lat1 = Double.parseDouble(faker.address().latitude().replace(",", "."));
            double lon1 = Double.parseDouble(faker.address().longitude().replace(",", "."));
            double size1 = faker.random().nextDouble() * 5 + 0.1; // 0.1° à 5°
            BBox b1 = new BBox(lon1, lon1 + size1, lat1, lat1 + size1);

            // La seconde BBox est générée avec un léger décalage aléatoire
            double lat2 = lat1 + faker.random().nextDouble() * 10 - 5; // décalage de plus ou moins 5°
            double lon2 = lon1 + faker.random().nextDouble() * 10 - 5;
            double size2 = faker.random().nextDouble() * 5 + 0.1;
            BBox b2 = new BBox(lon2, lon2 + size2, lat2, lat2 + size2);

            BBox intersection = b1.calculateIntersection(b2);

            if (intersection != null) {
                // L’intersection doit se trouver à l’intérieur ou en chevauchement des deux BBox
                assertAll("L'intersection calculée doit être cohérente et contenue dans les deux BBox",
                        () -> assertTrue(b1.contains(intersection.minLat, intersection.minLon)
                                || b1.intersects(intersection)),
                        () -> assertTrue(b2.contains(intersection.minLat, intersection.minLon)
                                || b2.intersects(intersection)),
                        () -> assertTrue(intersection.minLat <= intersection.maxLat),
                        () -> assertTrue(intersection.minLon <= intersection.maxLon),
                        () -> assertFalse(Double.isNaN(intersection.minLat)),
                        () -> assertFalse(Double.isNaN(intersection.maxLat)));
            } else {
                // Si aucune intersection n’est trouvée, les deux BBox doivent être disjointes
                assertFalse(b1.intersects(b2),
                        "Si l'intersection est nulle, les BBox ne devraient pas se croiser");
            }
        }
    }

}
