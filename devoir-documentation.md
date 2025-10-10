# Tâche #2 - IFT3913

### **Équipe**
**Emmanuel Chicoine, 20248681**
**Maxime Belotti, 20251262**

**Github:** https://github.com/maximebelotti/graphhopper


## **Classes testées**
Les tests réalisés suivent une approche globale, consistant à regrouper plusieurs scénarios dans une même cas de test afin de valider l’ensemble du comportement d’une methode. Ce choix s’inspire directement de la structure des tests préexistants du projet, qui ne distinguaient pas les cas isolés, et s’explique également par l’absence de consignes précises quant au découpage attendu.
La documentation produite repose donc sur cette logique: chaque test est conçu pour évaluer la méthode dans son ensemble, à travers tout ses aspects.
PS : Nous avons préféré prendre le risque de faire des tests trop grands plutôt que pas assez complets.

## Classe 1: `BBoxTest.java`
La classe BBox a été choisie en raison de son rôle central dans l’architecture de GraphHopper. Elle intervient dans la quasi-totalité des calculs spatiaux, que ce soit pour définir les zones de recherche, les intersections de chemins ou les limites géographiques des cartes. Malgré cette importance, plusieurs de ses méthodes fondamentales, telles que isValid(), update(), clone() ou encore calculateIntersection(), n’étaient pas couvertes par des tests unitaires ou pas sufisament.
Sa structure relativement simple, en fait une classe à la fois critique et facile à tester. En la ciblant, il est possible d’obtenir un gain significatif en couverture et en robustesse globale du code.  

Afin d’évaluer l’impact concret de l’ajout des nouveaux tests, une comparaison a été effectuée avant et après leur intégration. Les résultats mettent en évidence une amélioration significative de la couverture et de la détection des mutations au sein de la classe BBoxTest.  

Avant l'ajout des tests:
- Line Coverage: 45% (56/132)
- Mutation Coverage: 25% (47/187)
- Test Strength: 67% (47/70)

Après l’ajout des tests:
- Line Coverage: 71% (94/132)
- Mutation Coverage: 51% (95/187)
- Test Strength: 69% (95/137)

### Test1: `testIsValid()`

**🧭 Intention**  
Le test `testIsValid()` vérifie que la méthode `isValid()` distingue correctement les bounding box cohérentes de celles qui sont impossibles ou non initialisées.  
Cette vérification est essentielle, car tout l’écosystème de GraphHopper repose sur des objets `BBox` valides pour ses calculs spatiaux. Une erreur dans `isValid()` pourrait entraîner des résultats incorrects dans le routage, la génération de tuiles ou la détection d’intersections.  
Le choix de cette méthode s’explique aussi par sa complexité logique, qui la rend sensible aux imprécisions numériques et aux valeurs extrêmes, ainsi que par le fait qu’elle n’était jusque-là jamais testée, malgré son rôle critique dans la validation des coordonnées.

**🧩 Motivation des données utiliseées**  
Les valurs choisies couvrent des situations significatives pour évaluer la robustesse logique de la méthode:
- Une boîte parfaitement valide, où toutes les bornes minimales sont inférieures aux maximales, pour verfier le comportement de la méthode dans un contexte simple et cohérent.
- Des bornes égales pour la latitude, longitude et altitude, afin de tester la logique de comparaison.
- Des bornes inversées, simulant des erreurs d’initialisation où le minimum dépasse le maximum.
- Des bornes très proches, séparées d’un écart minimal, afin de vérifier la stabilité numérique face aux imprécisions des flottants.
- Des bornes extrêmes, utilisant des valeurs égales ou supérieur aux dimensions réelles du globe pour véfier le comportement du programme. Le test inclue également l’utilisation de `Double.MAX_VALUE` et `-Double.MAX_VALUE`, qui peuvent indiquer une élévation non initialisée.

**🔮 Oracle**  
Une boîte est valide si chaque borne minimale est inférieure à sa borne maximale, sauf pour les l'élévation où l’égalité est aussi tolérée.  
Les bornes très proches doivent rester valides et les bornes extrêmement grande sont tolérées si la cohérence numérique est respectée.  
Quant-aux bornes inversées ou non initialisées (`maxEle == -Double.MAX_VALUE || minEle == Double.MAX_VALUE`), elles donnent `false`.

**📊 Score de mutation**  
Mutations liés à `testIsValid()`: 29
- Tuées: 22
- Surivante: 6
- Non couverte: 1  

**Test strength** = 22/28 ≈ 79%; **Mutation coverage** = 22/29 ≈ 76%; **Temps d’exécution**: 0ms

**🔬 Explication des mutations**  
Les mutations observées couvrent plusieurs familles représentatives:
- Removed conditional: suppression ou inversion de comparaisons, détectées grâce aux tests sur les boîtes valides, égales et inversées.
- Changed conditional boundary: modification des comparateurs, révélée par les scénarios de bornes très proches ou égales.
- Replaced boolean return: inversion du résultat global, mise en évidence par la confrontation entre boîtes valides et invalides.

La majorité des mutants ont été tués, confirmant que `testIsValid()` couvre la logique essentielle de validation des bornes.  
Les mutants survivants concernent uniquement des comparaisons d’égalité très spécifiques liées à la gestion de l’élévation. Leur couverture nécessiterait probablement un test distinct.  
Le mutant non couvert provient d’un retour de méthode non atteint. 


### Test 2: `testUpdate()`
**🧭 Intention**  
Le test `testUpdate()` vérifie la capacité des deux méthodes `update()` à ajuster les bornes d’une bounding box en conservant sa validité, y compris lorsqu’elle reçoit des valeurs extrêmes. La version à trois paramètres (lat, lon, elev) doit pouvoir mettre à jour l’élévation avant de délèguer la mise à jour des coordonnées horizontales à la version à deux paramètres.  
Cette relation hiérarchique entre les deux surcharges rend la méthode centrale pour GraphHopper, car elle définit comment une bounding box s’étend dynamiquement lors de l’ajout de nouveaux points.  
Aucun test ne couvrait auparavant ces méthodes combinées, justifiant ainsi la nécessité d’une validation complète.

**🧩 Motivation des données utiliseées**  
Les valeurs choisies couvrent les situations de mise à jour les plus pertinants:
- Des points au-delà des bornes initiales, pour vérifier que `update()` élargit correctement les limites existantes. Ils permettent de vérifier le fonctionnement de la méthode dans un contexte simple et cohérent.
- Des points intérieurs ou égaux, afin de confirmer le bon fonctionnement de la logique de comparaison. 
- Des points très proches, garantissant la stabilité numérique face aux variations infimes.
- Enfin, des valeurs extrêmes (`-Double.MAX_VALUE`, `Double.MAX_VALUE`), testant la robustesse de la méthode face aux limites de représentation et la cohérence des bornes.

**🔮 Oracle**  
Après chaque mise à jour, les bornes minimales doivent être inférieures ou égales aux maximales.  
Les bornes ne doivent s’élargir que lorsqu’un point dépasse les limites existantes.    
Toute tentative de mise à jour d’une boîte sans élévation avec un point 3D doit provoquer une exception.  
L’ajout de points proches ou extrêmes doit préserver la validité globale de la boîte.

**📊 Score de mutation**  
Mutations liées à `testUpdate()` : 21
- Tuées: 15
- Surivante: 6
- Non couverte: 0

**Test strength** = 15/21 ≈ 71%; **Mutation coverage** = 15/21 ≈ 71%; **Temps d’exécution**: 4ms

**🔬 Explication des mutations**  
Les mutations observées se répartissent en trois grandes catégories : 
- Removed conditional: suppression ou inversion de comparaisons, détectée grâce aux tests sur les points hors bornes et extrêmes.
- Changed conditional boundary: modification des comparateurs, partiellement couverte par les cas de points très proches.
- Removed call: suppression de l’appel à `update()`, détectée par les vérifications finales des bornes et de la validité de la boîte et de la validité globale.

La majorité des mutants ont été tués, démontrant que `testUpdate()` couvre efficacement la logique d’expansion et de stabilité des bornes.  
Les mutants survivants, limités aux changements de seuils numériques, représentent des écarts insignifiants sans impact fonctionnel notable et ne nécessitent pas de test supplémentaire.


### Test 3: `testEquals()`
**🧭 Intention**  
Le test `testEquals()` vérifie la conformité de la méthode `equals()` avec les propriétés fondamentales de l’égalité (réflexivité, symétrie et transitivité), tout en évaluant sa robustesse face à certaines erreurs et valeurs extrêmes.
Cette méthode est tres utile pour GraphHopper, car elle permet de comparer deux `BBox` et de déterminer si elles représentent la même zone géographique.
De plus, aucun test ne validait auparavant la cohérence de cette logique d’égalité, qui repose sur la comparaison approchée des coordonnées via `NumHelper.equalsEps()`.

**🧩 Motivation des données utiliseées**  
Les valeurs sélectionnées couvrent les scénarios nécessaires à la validation de la méthode:
- Des bounding box identiques, pour tester la réflexivité, la symétrie et la transitivité de la relation d’égalité.
- Des `BBox` différentes sur la latitude ou la longitude, pour vérifier que la méthode puisse faire la différance.
- Des bounding box sans élévation, afin de confirmer que la comparaison reste valide même sans composante verticale.
- Des `BBox` proches, différant de très faibles marges (`1e-9`), pour vérifier la tolérance numérique offerte par `equalsEps()`.
- Des `BBox` au-delà de cette tolérance (`1e-3`), pour confirmer la tolérance numérique de `equalsEps()`.
- Enfin, des cas d’objets non comparables (`null` ou `String`), pour vérifier la robustesse de la méthode face aux entrées invalides.

**🔮 Oracle**  
Deux boîtes sont considérées égales (`true`) si leurs coordonnées minimales et maximales de latitude et de longitude sont identiques ou très proches selon la tolérance définie par `equalsEps()`.
Toute différence significative de coordonnées doit produire `false`. 
La comparaison avec un objet `null` doit toujours retourner `false`, et celle avec un objet d’un autre type doit lever une exception (`ClassCastException`).

**📊 Score de mutation**  
Mutations liées à `testUpdate()` : 13
- Tuées: 11
- Surivante: 2
- Non couverte: 0

**Test strength** = 11/13 ≈ 85%; **Mutation coverage** = 11/13 ≈ 85%; **Temps d’exécution**: 1ms

**🔬 Explication des mutations**  
Les mutations observées appartiennent à deux catégories principales:  
- Removed conditional : suppression ou inversion de comparaisons, détectée par les tests comparant des boîtes identiques et différentes, qui vérifient directement la cohérence logique de la méthode.  
- Replaced boolean return : inversion du résultat global, révélée par les vérifications de réflexivité, de symétrie et de transitivité.  

La majorité des mutants ont été tués, démontrant que `testEquals()` valide efficacement la logique d’égalité et la robustesse de la méthode.  
Les mutants survivants de type `replaced boolean return` proviennent d’un retour forcé à `true` situé dans une branche non atteinte par le test.


### Test 4: `testClone()`
**🧭 Intention**  
Le test `testClone()` vérifie la méthode `clone()` de la classe `BBox`, qui doit produire une copie exacte et indépendante d’un objet existant.  
Cette méthode a été choisie car elle n’était pas encore couverte par les tests existants, malgré sa simplicité. Tester cette méthode permet donc, à faible coût, d’éviter de futurs problèmes importants de corruption de données ou de comportement inattendu dans GraphHopper.

**🧩 Motivation des données utiliseées**  
Les valeurs sélectionnées couvrent les deux configurations principales de `BBox` : 
- Une boîte avec élévation, pour vérifier que toutes les bornes (`minLat`, `maxLat`, `minLon`, `maxLon`, `minEle`, `maxEle`) et l’état du drapeau `hasElevation()` sont copiés correctement.  
- Une boîte sans élévation, afin de s’assurer que le clonage conserve correctement l’absence de dimension verticale. 

**🔮 Oracle**  
- Le clone doit être une instance distincte (`assertNotSame(b, c)`).  
- Toutes les valeurs numériques correspondantes doivent être égales.
- Les modifications appliquées au clone ne doivent pas affecter l’original.
- Le champ `hasElevation()` doit être identique au modèle copié.

**📊 Score de mutation**  
Mutations liées à `testCreateBBox()` : 3
- Tuées: 2
- Surivante: 1
- Non couverte: 0  

**Test strength** = 2/3 ≈ 67%; **Mutation coverage** = 2/3 ≈ 67%; **Temps d’exécution**: 14ms

**🔬 Explication des mutations**  
Les mutations détectées concernent deux méthodes : `clone()` et `hasElevation()`.
- Replaced return value with null (`clone()`): mutation forçant la méthode à retourner `null`, détectée immédiatement par les assertions de comparaison qui échoueraient sur un objet inexistant. 
- Replaced boolean return with true/false (`hasElevation()`): mutations inversant la valeur retournée, testée à travers les cas avec et sans élévation.

Le mutant survivant correspond donc à une inversion logique non couverte sur le cas d’une boîte avec élévation. Cette mutation met en évidence une amélioration possible.


### Test 5: `testCalculateIntersectionConsistencyWithFaker()`
**🧭 Intention**  
Le test `testCalculateIntersectionConsistencyWithFaker()` vérifie la cohérence du calcul d’intersection entre deux boîtes géographiques (`BBox`) en utilisant des coordonnées générées aléatoirement via la bibliothèque JavaFaker. Il permet d’évaluer la robustesse de la méthode `calculateIntersection()` sur un large ensemble de configurations réalistes et variées, difficiles à reproduire manuellement. 
**Pourquoi utiliser JavaFaker**: L’utilisation de **JavaFaker** permet de générer automatiquement des coordonnées plausibles et diversifiées, tout en maintenant la grâce à une graine fixe. Cette approche agit comme une forme de fuzz testing contrôlé, capable de révéler des erreurs rares ou dépendantes de la distribution des valeurs, renforçant ainsi la fiabilité globale de la méthode. Elle est particulièrement appropriée ici, car `calculateIntersection()` traite des données continues et fortement dépendantes du positionnement spatial, rendant les tests déterministes classiques moins représentatifs des usages réels.

**🧩 Motivation des données utilisées**  
Les valeurs sont générées de manière pseudo-aléatoire pour couvrir un large éventail de situations :  
- Des coordonnées réalistes de latitude et longitude, fournies par JavaFaker, assurent la cohérence géographique des tests.  
- Des tailles de boîtes variées (entre 0.1° et 5°) permettent de tester la méthode à la fois sur de petites zones locales et sur des régions plus vastes.  
- Les décalages aléatoires entre les deux boîtes (plus ou moins 5°) créent aussi bien des cas d’intersection que de séparation totale, garantissant la couverture des interséctions et de l'absence d'interséction.  

**🔮 Oracle**  
- Si une intersection existe (`intersection != null`), celle-ci doit être incluse dans les deux boîtes, c’est-à-dire que le point d’intersection doit être inclus (`contains()`) ou se superposer (`intersects()`) avec b1 et b2.
- Les bornes de l’intersection doivent être cohérentes (`minLat ≤ maxLat`, `minLon ≤ maxLon`).  
- Toutes les coordonnées de l’intersection doivent être finies et non `NaN`.  
- Si aucune intersection n’existe (`intersection == null`), les deux boîtes doivent être*disjointes, ce que la méthode `intersects()` doit confirmer.  

**📊 Score de mutation**  
Mutations liées à `testCalculateIntersectionConsistencyWithFaker()` : 14
- Tuées: 5
- Surivante: 9
- Non couverte: 0  

**Test strength** = 5/9 ≈ 56%; **Mutation coverage** = 5/9 ≈ 56%; **Temps d’exécution**: 33ms

**🔬 Explication des mutations**  
Les mutations générées concernent se répartissent en quatre grandes catégories .  
- Removed conditional: suppression ou inversion de comparaisons sur les bornes géographiques. Plusieurs ont été tuées par les assertions de cohérence géométrique (`min ≤ max`) et les validations d’intersection.
- Changed conditional boundary: modification des opérateurs de comparaison, restée partiellement survivante car les valeurs aléatoires générées par *JavaFaker* ne couvrent pas systématiquement les cas limites où ces changements influencent le résultat.  
- Replaced boolean return with true/false: inversion du résultat global de la méthode `intersects()`, détectée dans les cas où une intersection aurait été faussement considérée comme valide ou absente.

Les mutants survivants correspondent à des scénarios limites de contact ou d’égalité parfaite entre deux boîtes, que la génération aléatoire ne reproduit pas toujours. Cependant, d’autres méthodes de test préexistantes comme testCalculateIntersection() et testIntersects() sont parfaitement complémentaires et permettent de tuer la plupart des mutations restantes.  


## Classe2: `DistanceCalcEarth`
La classe DistanceCalcEarth a été sélectionnée en raison de son importance fondamentale dans le calcul des distances et des zones géographiques au sein de GraphHopper. Elle constitue un élément central des opérations de routage, puisque la précision de ses calculs influe directement sur la qualité des itinéraires générés et des estimations de distance affichées à l’utilisateur.  
Bien que cette classe disposât déjà d’un certain nombre de tests préexistants, sa taille importante et la présence de plusieurs méthodes non vérifiées justifiaient un approfondissement de la couverture. Certaines portions critiques comme internCalcDistance() ou createBBox(), restaient en effet peu ou pas testées malgré leur rôle déterminant dans la cohérence des résultats spatiaux.  

Afin d’évaluer l’impact concret de l’ajout des nouveaux tests, une comparaison a été effectuée avant et après leur intégration. Les résultats mettent en évidence une amélioration significative de la couverture et de la détection des mutations au sein de la classe DistanceCalcEarth, confirmant la pertinence des scénarios ajoutés:

Avant l'ajout des tests:
- Line Coverage: 74% (110/149)
- Mutation Coverage: 66% (162/246)
- Test Strength: 86% (162/189)

Après l’ajout des tests :
- Line Coverage: 95% (142/149)
- Mutation Coverage: 81% (199/246)
- Test Strength: 87% (199/229)

### Test 5: `testCreateBBox()`
**🧭 Intention** 
Le test `testCreateBBox()` vérifie la robustesse et la cohérence de la méthode `createBBox()`, qui doit généré des bounding box valides nuériquement, symétriques et monotones, tout en rejetant les entrées incohérentes.  
Cette méthode joue un rôle essentiel dans GraphHopper, car elle définit les zones de recherche géographique. De plus, avant ce test, aucun scénario ne validait le comportement de `createBBox()`.

**🧩 Motivation des données utiliseées** 
Les valeurs choisies couvrent un large spectre de scénarios représentatifs:
- Des entrées invalides (rayon nul ou négatif), pour vérifier que la méthode lève bien une erreur.
- Un rayon extrêmement grand, afin d’évaluer la stabilité numérique et la cohérence des bornes.
- Des valeurs très proche de proche de l'equateur pour s’assurer que la méthode reste valide près des pôles et ne produit pas d’erreurs d’arrondi.  
- Un rayon minuscule, pour tester la précision et la cohérence de la méthode dans des cas limites.  

**🔮 Oracle**  
- Toute entrée invalide (rayon nul, négatif ou incohérent) lève une exception `IllegalArgumentException`.
- Aucune valeur infinie ou non définie n’est produite.
- Les coordonnées respectent la symétrie du point central. 
- Les rayons croissants génèrent des boîtes plus grandes.

**📊 Score de mutation**
Mutations liées à `testCreateBBox()` : 12
- Tuées: 10
- Surivante: 2
- Non couverte: 0  

**Test strength** = 10/12 ≈ 83%; **Mutation coverage** = 10/12 ≈ 83%; **Temps d’exécution**: 6ms

**🔬 Explication des mutations**  
Les mutations observées se répartissent en cinq catégories:
- Changed conditional: modification des opérateurs de comparaison, détectée par les cas limites avec des rayons très petits et des latitudes proches des pôles. 
- Remove conditional: suppression de vérifications sur la validité des entrées, tuées par les tests d’entrées invalides.
- Replace double addition with subtraction: inversion des opérations d’addition et de soustraction dans les calculs de coordonnées, détectée par les tests de symétrie et de cohérence géométrique.  
- Replaced return value with null: mutation forçant à retourner `null`, tuée par les assertions de validité (`assertTrue(b.minLat < b.maxLat)`).
- Replaced double division with multiplication: modifie certains calculs trigonométriques internes, détectée en partie grace aux coordonnées proches des pôles.

La majorité des mutants ont été tués, confirmant que `testCreateBBox()` vérifie efficacement la validité, la symétrie et la stabilité numérique des boîtes générées.  
Les deux mutants survivants correspondent à des inversions de la division avec la multiplication, dont les effets sur les bornes restent proportionnellement identiques. Ces mutants peuvent être considérés comme équivalents du point de vue fonctionnel.


### Test 6: `testInternCalcDistance()`
**🧭 Intention**
Le test `testInternCalcDistance()` vérifie indirectement la méthode statique `internCalcDistance()` en passant par la méthode publique `calcDistance()`. Ce test assure donc la validité des calculs de la distance entre une série de points géographiques.  
Cette méthode est essentielle pour GraphHopper, car elle est utilisée pour calculer la longueur cumulée des segments d’itinéraires. Elle a également été choisie car, jusque-là, seule la méthode élémentaire calcDist(double fromLat, double fromLon, double toLat, double toLon) faisait l’objet de tests unitaires, laissant internCalcDistance(), plus généraliste, non couverte.

**🧩 Motivation des données utiliseées** 
Les données testées couvrent plusieurs situations concretes: 
- Une liste vide et une liste avec un unique point, pour vérifier que la méthode retourne une distance nulle.
- Une séquence verticale et horizontale, pour valider le cumul correct des distances sur des segments disjoints et tester la différence entre 2D et 3D.  
- Un segment diagonal, destiné à vérifier la cohérence géométrique et la conformité à la formule de Pythagore.

**🔮 Oracle**
- Une liste vide ou un seul point doit produire une distance `0`.  
- Les distances calculées doivent toujours être positives et finies.  
- La distance 3D doit être strictement supérieure à la distance 2D si il y a de l'élévation.  
- La différence entre la distance 3D et 2D doit correspondre au dénivelé cumulé.
- Pour un segment diagonal, la relation de Pythagore doit être respectée

**📊 Score de mutation**
Mutations liées à `testInternCalcDistance()` : 14
- Tuées: 13
- Surivante: 1
- Non couverte: 0  

**Test strength** = 13/14 ≈ 93%; **Mutation coverage** = 13/14 ≈ 93%; **Temps d’exécution**: 7ms

**🔬 Explication des mutations**  
Les mutations observées se répartissent en quatre catégories principales: 
- Replaced double return: mutation forçant à retourner `null`, détecté par les tests vérifiant des distances strictement positives sur plusieurs points.
- Remove conditional: suppression ou inversion de conditions de comparaison sur la taille de la liste ou les indices des points, détectée grâce aux cas de liste vide et à un seul point.  
- Changed conditional boundary: modification des opérateurs de comparaison, tuée par les scénarios de distances cumulées.
- Replaced double addition with substraction: inversion d’opérations arithmétiques, détectée par les différences entre les distances 2D et 3D et par la vérification de la relation de Pythagore.  

La majorité des mutants ont été éliminés, confirmant la couverture complète de la logique de calcul cumulatif et la cohérence entre les distances 2D et 3D.  
Le mutant survivant correspond à une suppression conditionnelle sur une égalité logique non exercée, car toute les test incluent un `PointList` en 3D. Ce mutant est ainsi équivalent, car il n’impacte pas la logique fonctionnelle. Mais un cas avec `PointList` purement 2D est envisageable.


### Test 7: `testProjectCoordinateCardinalDirections()`
**🧭 Intention**  
Le test `testProjectCoordinateCardinalDirections()` vérifie indirectement la méthode `projectCoordinate(double, double, double, double)`, qui prend en paramètres une latitude et une longitude de départ, une distance et une direction et qui retourne le point d'arrivé sous forme de GHPoint. Elle est utilisée par la méthode RoundTripRouting.generateValidPoint(...), qui crée un route quasi circulaire.

**🧩 Motivation des données utilisées**  
Les données testées couvrent trois ensembles de scénarios: les simples; ceux ayant un point de départ simple mais une direction limite; et ceux ayant un point de départ limite mais une direction simple.
- Les scénarios simples ont comme point de départ la coordonnée (0°, 0°), c'est à dire le point de croisement de l'équateur et du Méridien de Greenwich. Les directions utilisées sont les quatres points cardinaux: 0°, 90°, 180° et 270°. 
- Les scénarios ayant un point de départ de simple mais des directions limites commencent à (0°, 0°) comme le premier groupe, et leur directions sont hors de l'intervalle [0°, 360°) attendu. Ces directions sont: -90°, 360° et 450°. 
- Les scénarios au point de départ limite partent du pôle nord (90°, 0°) et testent un direction simple de 5°. 

**🔮 Oracle**  
- La distance parcourue étant petite, les scénarios partant de l'équateurs ne franchissent pas les pôles. Dans le cas des angles « verticaux », 0° et 90°, la longitude ne change pas, mais la latitude change exactement de la distance totale parcourue. Les mêmes résultats sont attendus quand on part de l'équateur et qu'on utilise l'angle limite vertical de 360°.
- Pour la même raison, dans les cas où on part de l'équateur et qu'on fait un mouvement « horizontal », de 90° ou de 270°, on ne fait pas un tour complet de la Terre et la longitude d'arrivée diffère de la longitude de départ d'excatement la distance parcourue. La latitude, elle, ne change pas. Les mêmes résultats sont attendus quand on utilise les angles limites horizontaux de -180° et de 450°.
- En partant du pôle nord (90°, 5°), la longitude d'arrivée correspond à la somme de la longitude de départ et de la direction empruntée, tandisque la latitude diminue toujours d'exactement la distance parcourue.

**📊 Score de mutation**  
Mutations liées à `testProjectCoordinateCardinalDirections()`: 14
- Tuées: 13
- Survivantes: 1
- Non couverte: 0  

**Test strength** = 13/14 ≈ 93%; **Mutation coverage** = 13/14 ≈ 93%

**🔬 Explication des mutations**  
Les mutations observées dans projectCoordinate() se répartissent en six catégories principales :

- Replaced double division with multiplication : certaines divisions dans le calcul de l’angle ou des coordonnées projetées ont été remplacées par des multiplications. Ces mutants ont été tués par les tests vérifiant la latitude et la longitude d’arrivée sur les directions cardinales et diagonales.
- Replaced double addition with subtraction : les additions dans le calcul de la longitude projetée ont été inversées en soustraction. Ces mutants ont été détectés par les assertions sur les coordonnées finales, notamment dans les cas de directions horizontales et diagonales.
- Replaced double multiplication with division : les multiplications dans les formules trigonométriques ont été remplacées par des divisions. La plupart de ces mutants ont été tués par les vérifications de distances exactes sur les directions simples et diagonales. Un mutant a survécu, correspondant à une multiplication dans le calcul de longitude qui n’a pas impacté les tests actuels de directions cardinaux simples.
- Replaced double subtraction with addition : inversion des soustractions dans le calcul de longitude, tuée par les tests de directions cardinales.
- Replaced double modulus with multiplication : la normalisation de longitude a été modifiée, tuée par les assertions de longitude d’arrivée.
- Replaced return value with null : mutation forçant le retour de null pour projectCoordinate(), tuée par toutes les assertions vérifiant la validité des coordonnées finales.

La majorité des mutants ont été tués, démontrant que testProjectCoordinateCardinalDirections() couvre efficacement la logique de projection des coordonnées pour différentes directions et points de départ.

Le mutant survivant correspond à une modification de multiplication en division dans le calcul de longitude. Il n’a pas été tué car le scénario testé n’exerce pas la combinaison exacte de trigonométries affectées par cette mutation. Ce mutant pourrait nécessiter un test complémentaire pour des angles particuliers ou des distances plus grandes pour être couvert.

