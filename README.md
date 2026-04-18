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

## Règle du jeu 

Chaque joueur possède 12 cartes face caché devant lui.  
Les cartes possèdes des valeurs entre **-2** et **12**.  
Lorsque toutes les cartes d'un joueur sont retournées, il y'a un tour supplémentaire pour les autres joueur et c'est la fin de la manche.   
Le but est d'avoir la somme des nombres la plus petites possible (la somme de toutes les valeurs des 12 cartes).
Lorsqu'un joueur atteint **100 points** *( à travers une ou plusieurs manches)*, alors la partie est terminée.

Lors du premier tour de chaque manche, chaque joueur doit retourner deux cartes.
La somme des deux cartes défini le l'ordre des actions des joueurs, celui qui à la somme la plus élévé joue en premier, le deuxième qui à la plus grosse somme joue en deuxième etc..

Ensuite à chaque tour jusqu'à la fin de la manche, chaque joueur peut : 
    - Piocher une carte soit dans la pioche, soit dans la défausse *( si il y en a un )*. Et remplacer une de ses 12 cartes par la carte pioché. 
    - Piocher une carte dans la pioche mais ne pas vouloir la jouer, donc la défausser directement. Dans ce cas, il est necessaire de retourner une carte parmis les 12.
> Si on pioche dans la défausse, on est obliger de la jouer
> À noter, la pioche est face caché mais la défausse est face visible pour tous  

Pour le mode **Skyjo Action** : 


## Utilisation du jeu

### Écran de connexion 

Créer un compte en fournissant **mail**, **pseudonyme** ( vu par tous ) et **mot de passe** ( rénitialisable en contactant le support ).   
> Attention, votre mot de passe doit avoir au **minimum 6 characteres*  
Ce compte vous donne accès au **mode de jeu en réseau** ainsi qu'à la sauvegarde de vos statistiques sur notre base de données.   

Si vous avez déjà un compte, vous pouvez cliquer sur **"Se Connecter"** pour accéder à votre compte.   
> Attention, les entrées sont sensibles à la casse  
> Vérifiez bien dans que vous n'ayez pas mis d'espace au debut ou à la fin de votre saisie (pour la création ou la connexion) 

Si vous ne souhaitez pas avoir un compte, vous pouvez toujours jouer en tant qu'*invité*, vous aurez assez d'argent pour debloqué tous les skins de cartes mais vous ne pourrez pas jouer en réseau.  

### Menu principal

- **Boutique** : Vous donne accès à votre classeur de skins, votre portefeuille, à trois différents types boosters, ainsi qu'aux pourcentages de chance d'avoir une rareté de skins selons les boosters (Commun, Rare ou Légendraire).   
    Les booster vous permettent de débloquer des skins de dos de carte *(N'influence en aucun cas le Gameplayer, pûrement décoratif)*
    Pour équiper un skin, rendez vous dans votre classer et cliquer sur "Equiper".
    > Les skins que vous avez débloquer auront un fond vert

- **Règlages** : 

    - Graphique : Vous pouvez activer ou désactiver l'effet visuel des cartes flottantes, il est activé par defaut mais sur certains PC ça peut rendre le jeu lent ou provoquer des lag, il est *conseiller* de désactivé cet effet dans ce cas là.

    - Audio : Vous pouvez activé/desactivé le son global du jeu ainsi que régler le volume.
    - Autres : Vous pouvez activer/desactivé le **mode plein écran**. *(il ne fonctionne pas exactement pareil que le bouton natif de la fenêtre sur MacOS pour pouvoir laissé la barre des tâches et des applications visibles)*

- **LeaderBoard** (1er bouton en haut à droite) : Il affiche le classement *mondial* entre les joueurs, classé selon leur elo et leur Rank. *(Seuls les joueurs ayant un compte sont affichés)*

- **Statistiques** (2ème bouton en haut à droite) : Si vous êtes connecté vos statistiques personnelles s'afficheront, tels que **le pseudo**, **le rang**, **l'élo**, **l'argent** ainsi que votre **UUID**.

- **Jouer** : Affiche le menu de sélection entre le mode Local et le Multijoueur

    - Local : Vous pouvez configurer le nombres de joueurs *( jusqu'à 8 maximum )*, le noms des ces derniers, dire si se sont des bots ( et choisir le type de bot ) ainsi qu'activer ou non le mode **Skyjo Action**

    - Multijoueur : Vous pouvez créer un salon ou bien en rejoindre un déjà existant si il existe un
    > Attention, c'est un mode en Peer-To-Peer et donc vous ne pouvez que détecter que les joueurs qui sont sur le même réseau internet que vous
    **A FINIR DE REMPLIR LORSQUE MULTIJOUEUR FONCTIONNEL**

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