# DESIGN.MD

## Gestion du projet : [Maven](https://github.com/apache/maven)
Nous avons utiliser Maven pour de standardiser la structure du projet, pour gérer les dépendances, et pour automatiser le build et les tests sur nos machines et sur l'intégration continue.

## Gestion des données : [Gson](https://github.com/google/gson)
Afin d'implémenter notre système de sauvegarde et d'inventaire, nous avons utilisés la bibliothèque Gson de Google qui permet de convertir des objets Java en Json et inversement.  

## Bibliothèques de Tests : [JUnit](https://github.com/junit-team/junit-framework) [Mockito](https://github.com/mockito/mockito)
JUnit nous a permis de tester notre code avec les tests unitaires. En compagnie de Mockito pour faire du mocking d'objets pour nos tests quand nous n'avions pas le code de l'un de nos collégues. 

## Couverture des tests : [Jacoco](https://github.com/jacoco/jacoco)
Nous avons utiliser la bibliothèque Jacoco pour faire la couverture de l'ensemble de nos tests.