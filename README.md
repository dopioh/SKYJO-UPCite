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

### Mode Classique

Chaque joueur possède 12 cartes face cachée devant lui.   
Les cartes possèdent des valeurs entre **-2** et **12**.   

Lorsque toutes les cartes d'un joueur sont retournées, il y a un tour supplémentaire pour les autres joueurs et c'est la fin de la manche.   

Le but est d'obtenir la somme la plus faible possible (la somme de toutes les valeurs des 12 cartes).  
Lorsqu'un joueur atteint **100 points** *( à travers une ou plusieurs manches)*, alors la partie est terminée.  

Lors du premier tour de chaque manche, chaque joueur doit retourner deux cartes.  

La somme des deux cartes défini l'ordre des actions des joueurs, celui qui  la somme la plus élévée joue en premier, le deuxième qui à la plus grosse somme joue en deuxième etc..

Ensuite à chaque tour jusqu'à la fin de la manche, chaque joueur peut :  

- Piocher une carte soit dans la pioche, soit dans la défausse *( si il y en a une )*.   
    Et remplacer une de ses 12 cartes par la carte pioché.   

- Piocher une carte dans la pioche mais ne pas vouloir la jouer, donc la défausser directement.   
    Dans ce cas, il est necessaire de retourner une carte parmis les 12.  

> Si on pioche dans la défausse, on est obligé de la jouer.  
> **À noter**f : la pioche est face caché mais la défausse est face visible pour tous.    

### Mode **Skyjo Action** : 

Il existe 6 cartes spéciales dans le jeu *( disponible que dans le **Mode Action**)* :  

- **ECHANGE** : Échange une carte de ta main avec une carte d'un autre joueur.  

- **REPLACE** : Echange la position de 2 cartes dans ta main.

- **JOUE 2 FOIS** : Tu gagnes un tour supplémentaire à la fin de ce tour.

- **REJOUE** : Rejoue la dernière action de la défausse action. 

- **CHAQUE JOUEUR DEFAUSSE** : Pour chaque autre joueur, choisis une carte à défausser: elle est remplacée par la première carte de la pioche.    

- **REGARDE** : Révèle pendant 3s toutes les cartes d'une colonne de ta main.  

Chaque tour, le joueur peut décider de ... *( en plus des actions de bases )* :

- Piocher une carte action, mais son tour se finit instantanément.

- Jouer une carte action, si une carte action est jouée alors le tour est fini, on ne peut pas piocher une carte normale ensuite

> Il existe une pioche et défausse pour les cartes actions.   
> Mais contrairement à la défausse classique, on peut pas piocher dedans.  



## Utilisation du jeu

### Écran de connexion 

Créer un compte en fournissant **mail**, **pseudonyme** ( vu par tous ) et **mot de passe** ( réinitialisable en contactant le support ).   
> Attention, votre mot de passe doit avoir au **minimum 6 caractères*   
Ce compte vous donne accès au **mode de jeu en réseau** ainsi qu'à la sauvegarde de vos statistiques sur notre base de données.


Si vous avez déjà un compte, vous pouvez cliquer sur **"Se Connecter"** pour accéder à votre compte.     
> Attention, les entrées sont sensibles à la casse.     
> Vérifiez bien dans que vous n'ayez pas mis d'espace au début ou à la fin de votre saisie (pour la création ou la connexion).   


Si vous ne souhaitez pas avoir un compte, vous pouvez toujours jouer en tant qu'*invité*, vous aurez assez d'argent pour débloquer tous les skins de cartes mais vous ne pourrez pas jouer en réseau.  

### Menu principal

- **Boutique** : Vous donne accès à votre classeur de skins, votre portefeuille, à trois différents types boosters, ainsi qu'aux pourcentages de chance d'avoir une rareté de skins selon les boosters (Commun, Rare ou Légendaire).   
    Les booster vous permettent de débloquer des skins de dos de carte *(N'influence en aucun cas le Gameplayer, pûrement décoratif)*
    Pour équiper un skin, rendez vous dans votre classeur et cliquer sur "Equiper".
    > Les skins que vous avez débloqués auront un fond vert.  

- **Réglages** : 

    - Graphique : Vous pouvez activer ou désactiver **l'effet visuel des cartes flottantes**, il est activé par **défaut** mais sur certains PC cela peut rendre le jeu **lent** ou provoquer des **lags**, il est *conseiller* de désactivé cet effet dans ce cas là.

    - Audio : Vous pouvez activer/desactiver le son global du jeu ainsi que régler le volume.
    - Autres : Vous pouvez activer/desactivé le **mode plein écran**. *(il ne fonctionne pas exactement pareil que le bouton natif de la fenêtre sur MacOS pour pouvoir laisser la barre des tâches et des applications visibles)*

- **LeaderBoard** (1er bouton en haut à droite) : Il affiche le classement *mondial* entre les joueurs, classé selon leur elo et leur Rank. *(Seuls les joueurs ayant un compte sont affichés)*

- **Statistiques** (2ème bouton en haut à droite) : Si vous êtes connecté vos statistiques personnelles s'afficheront, tels que **le pseudo**, **le rang**, **l'élo**, **l'argent** ainsi que votre **UUID**.

- **Jouer** : Affiche le menu de sélection entre le mode Local et le Multijoueur

    - **Local** : Vous pouvez configurer le nombres de joueurs *( jusqu'à 8 maximum )*, le noms des ces derniers, dire si se sont des bots ( et choisir le type de bot ) ainsi qu'activer ou non le mode **Skyjo Action**

    - **Multijoueur** : Vous pouvez créer un salon ou bien en rejoindre un déjà existant si il existe un  
    > Attention, c'est un mode en Peer-To-Peer et donc vous ne pouvez que détecter que les joueurs qui sont sur le même réseau internet que vous.    

    **A FINIR DE REMPLIR LORSQUE MULTIJOUEUR FONCTIONNEL**

### Déroulement d'une partie

Dans une partie, les pseudos des joueurs s'affichent à gauche, lorsqu'on passe notre curseur dessus, on peut voir **les 12 cartes du joueur survolé**.  

En haut se trouve des **indications** sur l'action qui doit être faite pour que la partie puisse continuer.

En haut à gauche se trouve **le nombre de manche** qu'on a fait ( ou si nous somme en **Pre-manche**, donc le retournement de 2 cartes pour chacun). 

En haut à droite on retrouve **le nombre de points du joueur actuel dans cette manche**.   
Ainsi que un `?` pour expliquer les **règles de toutes les cartes actions** *( ce bouton ne s'affiche que dans le mode action)*. 

Le jeu suit exactement les règles mentionnée **un peu avant**.

À la fin d'un manche se trouve le **classement des joueurs** de la manche qui vient de se terminer, on peut voir leur cartes en survolant leurs pseudos.  

La partie se **finit** lorsqu'un joueur a atteint **100 points**.

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