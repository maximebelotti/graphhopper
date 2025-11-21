# Tâche #2 - IFT3913

### **Équipe**
**Emmanuel Chicoine, 20248681**
**Maxime Belotti, 20251262**

**Github:** https://github.com/maximebelotti/graphhopper

## GitHub Action
Nous avons intégré la vérifications automatique du score de mutation directement dans le fichier [`.github/workflows/build.yml`](.github/workflows/build.yml), car c'est le workflow central utilisé lors de chaque push pour construire et tester le projet. Modifier ce fichier permet donc de faire de la vérification du score pitest (PIT) une partie intégrante du processus d'intégration continue (CI), au même titre que les tests unitaires et la compilation.

La première étape ajoutée sert à exécuter les tests de mutations PIT. Elle commence par cibler le module `core` en changeant de dossier. C'est le cœur de graphHopper, c'est là que se trouvent la dépendance PIT et tous les tests que nous avons ajoutés. Une fois dans le bon module, nous lançons l'exécution des tests de mutation avec Maven en mode batch: `mvn -B org.pitest:pitest-maven:mutationCoverage`.  
A noter que nous avons dû modifier le `pom.xml` du module `core` afin d'y restreindre les classes et les tests cibles. Nous avons choisi de cibler uniquement les classes concernées par nos deux projets, afin de nous concentrer sur nos changements tout en évitant une exécution excessive. En effet, lancer  PIT sur l'ensemble du module `core` peut être très long et provoquer quelques erreurs.

Apres l'exécution de PIT, le workflow extrait le score de mutation du fichier XML généré. Nous avons choisi d'utiliser le fichier XML plutôt que le rapport HTML, car il est plus facile à analyser automatiquement dans un script.
Pour sécuriser cette étape, nous avons ajouté deux conditions préventives. La première vérifie que le fichier `mutations.xml` existe réellement, et la seconde s'assure que le fichier contient au moins un mutant. Si l'une de ces conditions échoue, le workflow échoue immédiatement. Cela évite un faux positif dans le CI.  
Lorsque le rapport est valide, nous comptons le nombre total de mutants ainsi que le nombre de mutant tués, ce qui permet de calculer le score de mutation (100 × mutants_tués / mutants_totaux), arrondi à deux décimales. Nous exportons ensuite ce score a l'étape suivante grâce a la variable de sortie GitHub Actions.

Ensuite, on compare le score courant à une baseline stockée dans `.github/mutation-baseline.txt` et on met à jour ce fichier si le score courant est supérieur à la baseline enregistrée. Cette étape gère plusieurs scénarios.  
Dans le premier scénario, le fichier de baseline n'existe pas encore. Cela correspond à la toute première exécution du workflow ou à une réinitialisation volontaire. Dans ce cas, le score calculé devient automatiquement la nouvelle baseline. Le fichier est donc créé et le build réussit.
Un second scénario survient lorsque le fichier existe mais qu'il est vide ou contient une valeur invalide. Le workflow déclenche immédiatement un `exit 1`. Cela permet de signaler une corruption ou une mauvaise manipulation du fichier de baseline.  
Dans le troisième scénario, la baseline est valide. Le workflow compare alors le score courant au score enregistré. Si le score courant est significativement inférieur (en tenant compte d'un epsilon pour compenser les effets d'arrondi liés aux calculs en virgule flottante), alors le build échoue, car cela indique une baisse de la qualité des tests.  
Si, au contraire, le score courant est supérieur à la baseline, cela signifie que la qualité des tests a été améliorée. Le workflow met alors à jour la baseline avec ce nouveau score.  
Enfin, si le score courant est exactement égal à la baseline ou suffisamment proche selon l'epsilon, aucune mise à jour n'est effectuée. Le build réussit, car la qualité des tests s'est maintenue et le fichier de baseline reste inchangé.  
Pour permettre la mise à jour automatique de la baseline quand elle doit l'être, nous avons configuré un bot GitHub Actions. Ce bot peut effectuer des opérations `git add`, `git commit` et `git push` directement depuis le workflow. Grâce à lui, la baseline est correctement maintenue dans le dépôt sans intervention manuelle.

**Validation des modifications:**  
Une fois le code ajoute et de dubugage fini, nous avons valide le comportement du workflow dans plusieurs scenarios.:
- Premier lancement (absence de baseline)
    - Démarche: Nous avons supprimé manuellement le fichier `.github/mutation-baseline.txt`, puis déclenché un nouveau push pour observer le comportement du workflow en situation d’initialisation.
    - Résultat attendu (obtenu): Le workflow doit exécuter correctement PIT et calculer un score de mutation, puis en détectant l’absence du fichier de baseline doit créer automatiquement un nouveau fichier contenant le score courant. Dans ce scénario, le build ne devait pas échouer puisqu’il s’agissait de la toute première référence.
<img width="599" height="353" alt="image" src="https://github.com/user-attachments/assets/12fb8835-2311-4860-90d5-436bb50eae94" />  

- Régression du score de mutation
    - Démarche: Nous avons volontairement affaibli certains tests afin de provoquer une baisse réelle du score de mutation, puis poussé ces modifications.
    - Résultat attendu (obtenu): Le workflow doit exécuter PIT, obtenir un score inférieur à la baseline existante, détecter cette différence comme significative (au-delà de l’epsilon) et faire échouer la CI en signalant clairement la régression.
<img width="386" height="147" alt="image" src="https://github.com/user-attachments/assets/236a5066-7486-4340-9dc5-5696846ac790" />  

    - Démarche: Nous avons restaures les tests précédemment affaiblis, ce qui augmente le score de mutation, puis effectué un push pour observer la réaction du workflow.
    - Résultat attendu (obtenu): Le workflow doit reconnaître que le score courant est supérieur à la baseline, mettre automatiquement à jour cette baseline et terminer le build sans erreur.
- Score identique ou variation négligeable
    - Démarche: Nous avons effectué des modifications sans impact sur la suite de tests, de manière à ce que le score PIT reste identique ou légèrement différent.
    - Résultat attendu (obtenu): Le workflow doit considérer que le score est inchangé tant que la différence reste inférieure à l’epsilon, ne pas mettre à jour la baseline et terminer le build normalement.
<img width="767" height="129" alt="image" src="https://github.com/user-attachments/assets/511086d3-9606-4216-98ba-db5cfd808916" />  

- Baseline corrompue ou non numérique
    - Démarche: Nous éditons manuellement le fichier `.github/mutation-baseline.txt` pour y inscrire une valeur invalide (par exemple `Je suis le plus beau, le plus fort, le plus inteligent!`), puis nous poussons un commit sans toucher aux tests.
    - Résultat attendu (obtenu): Le script doit lire le contenu du fichier, détecter qu’il ne correspond pas à un nombre valide via la regex, afficher un message indiquant que le fichier existe mais ne contient pas un score valide, montrer le contenu lu, inviter à corriger ou supprimer le fichier, puis échouer la CI.
<img width="607" height="173" alt="image" src="https://github.com/user-attachments/assets/af5cf142-7d36-41ee-98b4-80a5f6c16a60" />  


Nous n’avons pas réalisé de tests pratiques pour les scénarios où le fichier `mutations.xml` est vide ou totalement manquant, car ces situations ne sont pas censées se produire et ne relèvent plus du comportement fonctionnel attendu de notre pipeline, mais plutôt d’un dysfonctionnement interne de PIT .
    

## Testes
Les classes testées ont été sélectionnées parce qu’elles ne disposaient d’aucune couverture dans le projet original et parce qu’elles constituent des points critiques dans le pipeline de routage. Elles offrent des scénarios riches en interactions avec les dépendances, ce qui se prête particulièrement bien à des tests basés sur des mocks.  
Comme il s’agit des tout premiers tests écrits pour ces deux classes, notre objectif n’est pas d’explorer des scénarios très spécifiques ou avancés. Nous cherchons plutôt à valider la logique principale des méthodes testées et à établir un point de référence clair et reproductible pour le code existant ainsi que pour les futurs tests.

### [Test de `PathMerger.doWork()`](core/src/test/java/com/graphhopper/util/PathMergerTest.java)
Nous avons choisi de tester la methode `doWork()` de la classe `PathMerger`, car elle joue un rôle central dans le processus de routage de GarphHopper. Cette classe fusionne plusieurs objets `Path` en un seul `ResponsePath` cohérent. La diversité des opérations effectuées et ses nombreuses dépendances rendent cette classe suffisamment complexe pour être pertinent a tester. Comme `doWork()` concentre l'essentiel de cette logique, elle constitue a elle seule un candidat idéal pour un test unitaire utilisant des mocks.  
De plus `PathMerger` n'avait encore jamais été testée dans le projet. L'absence totale de couverture, combinée et le rôle stratégique de cette classe, nous a encourage a concevoir un test normatif.

**🧩 Choix des classes mockées:**  
Pour isoler efficacement `PathMerger` et éviter tout dépendance a la logique de routage réelle, nous avons simulé toutes les classes externes utilisées comme dépendances directes. Ce choix est cohérent avec la nature fonctionnelle de la classe, qui ne calcule pas les itinéraires elle-même, mais utilise des objets déjà produits par d'autres modules. La simulation permet ainsi de contrôler précisément le contenu nécessaire a `doWork()` et de créer des scenarios extrêmes. Voici la liste des classes mockées:
- **`Graph`**: Mockée pour permettre l’appel à `wrapWeighting()` ainsi que la création d’un objet `PathMerger`. Un mock suffit donc pour satisfaire ces exigences, sans charger un véritable graphe.
- **`Weighting`**: Mockée pour satisfaire la signature de `wrapWeighting()` et le constructeur de `PathMerger`. Un mock suffit donc a faire fonctionner la classe sans declancher la logique interne de calcul du cout.
- **`EncodedValueLookup`**: Mockée pour satisfaire la signature de `doWork()`. Simuler cette classe évite de déclencher inutilement l'implémentation réelle tout en permettant a la méthode tester de s'exécuter normalement.
- **`Translation`**: Mockée car elle intervient dans la signature de `doWork()`. Un mock permet d'éviter toute dépendance aux traductions internes de GraphHopper.
- **`Path`**: Les deux objets `Path` sont quasiment entièrement mockés car ils sont déterminants pour la fusion réalisée par `PathMerger`. Leur simulation intégrale permet de créer facilement des scenarios extrêmes, pour tester en profondeur le comportement de `doWork()`. 

**🛠️ Définition des mocks:**  
Chaque mock a été conçu pour fournir uniquement les informations nécessaires au scenario teste, avec des valeurs choisies pour maximiser la pertinence et la robustesse du test:
- **`Graph`**: On simule uniquement l'appel a la méthode `wrapWeighting()` pour qu'il retourne notre mock de `Wighting`. Cette simulation est suffisante car `PathMerger` n'exploite pas la structure réelle du graphe, mais attend simplement que l'appel a `wrapWighting()` retourne un `Weighting` valide.
- **`Weighting`**: On ne simule aucun comportement particulier car la methode `doWork()` ne lit aucune valeur spécifique du `Weighting`. Cette simulation suffit au test a valider la logique interne de `PathMerger`.
- **`EncodedValueLookUp`**: Cette classe a été mocke sans comportement particulier. Le test ne nécessitant pas d'accéder a de véritables valeurs encodées, un mock minimal est suffisant.
- **`Translation`**: On simule uniquement la méthode `tr()`, qui renvoie une simple chaine de caractère. `PathMerger` utilise cet objet pour construire une `InstructionList` lorsqu'il fusionne les chemins. Même si notre test n'utilise pas les instructions, il requiert tout de même un objet `Translation` valide. Une chaine quelconque suffit à garantir la cohérence du test, même si une instruction venait à être générée..
- **`Path`**: Les deux objets `Path` utilises dans le test on été quasiment entièrement simulés afin de contrôler leurs attributs essentiels (`weight`, `distance`, `time`, `description`, `found`) et surtout la valeur renvoyée par `calcPoints()`. Nous avons choisi pour les attributs des valeurs simples a vérifier, de manière a ce que le test puisse servir d'exemple standard. Les seuls éléments extrêmes du test concernent les points géographiques, car ils permettent d'éprouver la méthode de fusion tout en maintenant la clarté globale du scenario. 
    - `isFound()` renvoie true pour les deux chemins, car le test examine la logique de fusion dans une situation nominale ou toutes les connexions entre les waypoints existent.
    - `getTime()` renvoie 1000ms pour le premier chemin et 2000ms pour le second. C'est des valeurs simples qui facilitent la vérification de la somme finale.
    - `getDistance()` renvoie 10000m pour le premier chemin et 5000m pour le second. C'est des valeurs simples qui facilitent la vérification de la somme finale.
    - `getWeight()` renvoie respectivement 10 et 20. La encore, la simplicité des valeur facilite la validation.
    - `gestDescription()` renvoie `["path1"]` pour le premier chemin et `["path2"]` pour le second. Cela permet de vérifier simplement que la description finale est la concaténation exacte des descriptions. 
    - `calcPoints()` renvoie les points une liste constitue de P0, P1 pour path1 et de P1, P2 pour path2. Le point P1 est volontairement dupliqué, car `PathMerger` doit supprimer le dernier point d'un path lorsqu'il est identique au premier point du suivant. Ce choix teste un comportement critique de la classe.
    - Les points utilises possèdent des coordonnées extrêmes comme de latitude de plus ou moins 90°, des longitudes de plus ou moins 180° et des altitudes allant de -800m à 10000m. Ces valeurs extrêmes permettent de tester la stabilité numérique et le calcul d'ascension/descente dans des scenarios atypiques.

**🔮 Oracle:**  
Le `ResponsePath` généré doit être valide et ne contenir aucune erreur.  
Le temps total doit être exactement la somme des temps des deux chemins simulés, et la distance totale doit être la somme exacte des distances correspondantes.  
La description finale doit être la concaténation exacte des deux descriptions individuelles, sans modification ni réordonnancement.  
Les points fusionnés doivent former une séquence cohérente de trois points, incluant la suppression correcte de la duplication du point P1.  
Les waypoints renvoyés doivent être strictement identiques à ceux fournis en entrée.  
Le calcul de l’ascension doit correspondre à l’élévation entre P0 et P1, et le dénivelé négatif doit correspondre à la descente entre P1 et P2.


### [Test de `ViaRouting.calcPahts()`](core/src/test/java/com/graphhopper/routing/ViaRoutingTest.java)
Nous avons choisi de tester la méthode `calcPahts()` de la classe `ViaRouting`, car elle joue un rôle central dans la construction d'un itinéraire compose de plusieurs points intermédiaires. Cette méthode organise l'enchainement des segments d'un trajet et peut appliquer des restrictions liées aux curbsides, aux headings et au mode pass-thought. La méthode orchestre donc l'ensemble du processus, de la validation des paramètre d'entrée jusqu'à l'agrégation finale des segments d'itinéraire. Son rôle impose d'utiliser pas mal de dépendance. C'est donc donc une méthode idéale pour un cas de test base sur des mocks.  
Il est également important de noter que `ViaRouting`, et donc `calcPaths()` n'avaient encore jamais été testées. Pour combler ce manque de couverture, nous avons choisi de se concentrer sur un scenario nominal et deux cas d'erreur.

**🧩 Choix des classes mockées:**  
Pour isoler efficacement `calcPaths()` et éviter de dépendre d'une implémentation réelle de GrapheHopper, nous avons simules toutes les dépendances externes qui ne sont pas l'objet direct du test:
- **`QueryGraph`**: Mocké uniquement pour satisfaire la signature de `calcPaths()`. Un mock sans comportement suffit, car notre scénario n’utilise aucune logique interne de cette classe.
- **`DirectEdgeFilter`**: Mockee pour satisfaire la signature de `calcPaths()`. Un simple mock sans comportement permet d'éviter toute dépendance a la logique de filtrage réelle.
- **`PathCalculator`**: C'est la dépendance simulée centrale dans ce test. `calcPaths()` lui délègue le calcul des segments du trajet en lui passant les identifiants des nœuds successifs ainsi que les `EdgeRestrictions`. En le mockant, on contrôle précisément les chemins retournés pour chaque segment, ainsi que le nombre de nœuds visités, ce qui permet de valider correctement l’agrégation finale.
- **`Snap`**: Les trois objet `Snap` sont simules pour permettre d'utiliser `getClosestNode()`. En les simulant, nous fixons des identifiants simples et vérifions facilement que `ViaRouting` enchaîne les nœuds dans le bon ordre.
- **`Path`**: Les deux `Path` retournes par `PathCalculator` sont également mockes. `calcPaths()` les récupère pour les ajouter au résultat final. Nous contrôlons ici leur temps de parcours et leur chaîne de debug, ce qui permet de vérifier la somme des nœuds visités et la composition de la liste `paths`.
Les autres paramètres sont de simples données et ne nécessitent pas de mocks. 

**🛠️ Définition des mocks:**  
Chaque mock a été configuré pour fournir uniquement les informations nécessaires au scénario testé, avec des valeurs choisies pour rester lisibles tout en validant la logique de `calcPaths()`:
- **`QueryGraph`**: On simule uniquement sa présence, sans comportement particulier. `calcPath()` ne lit aucune information interne du graphe dans notre scenario, un mock vide est suffisant pour satisfaire la signature de la methode.
- **`DirectedEdgeFilter`**: On ne simule aucun comportement particulier car la méthode n'utilise pas le filtrage dans le cas de test choisi. Un simple mock permet de poursuivre l'exécution sans appels inutiles.
- **`PathCalculator`**: Cette classe est centrale dans le test. Elle est entièrement simule afin de configurer et utiliser ces trois méthodes. 
    - `calcPaths()` renvoie pour le premier segment une liste contenant le `Path` simulé (pathLeg0), puis pour le second segment, une autre liste contenant le second `Path` simulé (pathLeg1). On veut vérifier que `calcPaths()` restitue la liste finale dans le bon ordre.
    - `getVisitedNotdes()` renvoie des valeurs simple comme 3 et 7. Cela permet de vérifier que la méthode additionne correctement les nœuds visités et produit un total cohérent.
    - `getDebugString()` renvoie une chaîne simple, utilise uniquement pour s'assurer que `ViaRouting` intègre bien les informations de débogage renvoyées par le calculateur, même si le contenu exact n'est pas important dans ce test.
- **`Snap`**: Pour les trois objets `Snap` on simule uniquement la méthode `getClosestNode()` pour déterminer les identifiants des nœuds a relier. Chaque `Snap` renvoie des identifiant différents (10, 20, 30). Ces valeurs simples permettent de vérifier facilement que `ViaRouting` relie les bons nœuds dans le bon ordre.
- **`Path`**: Pour les deux objets `Path` retournés par le `PathCalculator`, on simule deux méthodes nécessaires au cas de test. Les valeurs choisies sont simples à vérifier, de manière à ce que le test puisse servir d’exemple standard.
    - `getTime()` renvoie 1000ms pour le premier `Path` et 4000 ms pour le second. Cela permet de vérifier facilement que `ViaRouting` additionne correctement les durées.
    - `getDebugInfo()` renvoie une chaîne courte et distincte pour chaque `Path`, comme "leg0" et "leg1". Ces valeurs permettent de confirmer que `calcPaths()` récupère bien les informations de débogage associées à chaque segment et les place dans le résultat final dans le bon ordre.

**🔮 Oracle:**  
Le résultat de `calcPaths()` n’est jamais nul.  
Les objets contenus dans la liste paths sont exactement ceux fournis par le `PathCalculator`, dans l’ordre attendu (pathLeg0 puis pathLeg1).  
Le compteur `visitedNodes` est égal à la somme des valeurs renvoyées par `PathCalculator.getVisitedNodes()` pour chaque segment.  
`PathCalculator.calcPaths()` est appelé une fois par segment, avec les bons identifiants de nœuds (10 et 20, puis 20 et 30) ainsi qu’un objet EdgeRestrictions.  
Si `curbsides` n’est pas vide et que sa taille diffère du nombre total de points, `calcPaths()` doit lancer une `IllegalArgumentException`.  

Si `curbsides` n’est pas vide et que `headings` n’est pas vide, `calcPaths()` doit également lancer une `IllegalArgumentException`.




