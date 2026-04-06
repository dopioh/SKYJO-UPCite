# Skyjo

Projet Skyjo | Groupe CR1a

Jeu de cartes [Skyjo](https://fr.wikipedia.org/wiki/Skyjo) développé en Java, où les joueurs tentent d'obtenir le score le plus bas possible en révélant, échangeant et collectant des cartes au fil de la partie.

## Prérequis

- **Java 21 ou supérieur**
- **Apache Maven** 3.8+

Recommandé : Mac OS

## Compilation

Depuis le répertoire racine du projet, naviguez vers le dossier `skyjo` et compilez avec Maven :

```bash
cd skyjo
mvn clean package
```

Cette commande va :
1. Télécharger toutes les dépendances nécessaires
2. Compiler le code source
3. Exécuter les tests
4. Générer un fichier JAR exécutable dans `target/`

Le fichier JAR généré sera nommé `skyjo-app.jar` et contiendra toutes les dépendances nécessaires.

## Exécution

Après la compilation, **depuis le dossier racine du projet** :

```bash
java -jar skyjo/target/skyjo-app.jar
```

## Utilisation du jeu

### Menu principal


### Déroulement d'une partie


### Contrôles en jeu


## Tests

Pour exécuter les tests unitaires :

```bash
cd skyjo
mvn test
```

Pour générer un rapport de couverture de code avec JaCoCo :

```bash
cd skyjo
mvn clean verify
```

Le rapport sera disponible dans `target/site/jacoco/index.html`

## Documentation

- [DESIGN.md](DESIGN.md) : Décisions de conception
- [CHANGELOG.md](CHANGELOG.md) : Historique des modifications
- [CONTRIBUTING.md](CONTRIBUTING.md) : Guide de contribution
- [AUTHORS.md](AUTHORS.md) : Liste des contributeurs et leur principaux contributions
- [contacts.csv](contacts.csv) : Les contacts des contributerus

## Licence

Voir le fichier [LICENSE](LICENSE) pour les détails.