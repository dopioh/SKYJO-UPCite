# Changelog

## v.0.1 : SPRINT 1
---

### Added

- Création de la base Maven

- Structure MVC

- Pipeline CI/CD

- .gitignore

- Documentations
    - contact.csv
    - CONTRIBUTING
    - LICENSE (MIT)
    - README
    - AUTHORS
    - DESIGN
    - CHANGELOG

- Template de Merge Request avec checklist

- Base de Main menu

- Systeme de carte numéro & carte spéciales

- Systeme de pile de carte (Pioche & Defausse)

- Observateur/facade données joueur

- Systeme de Rank

- Système de lecture des sons (Background et SFX)

- Page de paramètres (Plein Ecran et Gestion de son)

- Systeme d'image dans le cache

- Système pour la main du joueur avec toutes ses fonctions et les regles du jeu

- Ajout de fonctions dans le gameManager 

- Jouabilité dans le terminal 


## v.0.2: SPRINT 2
---

### Added

- Boutons doodle
    - box wiggle
    - Animation scribble au survol
    - Support d'icônes à côté du texte
    - 3 couleurs prédéfinies (TURQUOISE, GREEN, RED)
    
- Cartes Basique pour la vue graphique

- Plateau de jeu avec joueurs cliquable

- Score et main récapitulatif fin de partie

- Premier joueur de manche

- Possibilité d'enchainer 2 manches

- Affichage des cartes sur le plateau
    - CardButton : JButton custom pour afficher les cartes
    - Crop les images et met dans imageCache
    - Rendu d'image en haut qualité
    - Système de switching entre les mains des différents joueurs
    - affichage du main actuelle en haut
    - affiche "Joueur X - Carte [row][col]" en terminal
    - boutton retour

- Ajout du bot dans le Terminal

### Fixed

- Colonnes et lignes supprimées uniquement si elles sont retournées

- Carte de la défausse faceDown dans la main du joueur

- Tout les joueurs jouent pendant la derniere manche


## v.0.3: SPRINT 3
---

### Added

- Liaison view avec `PiocheModel` pour les cartes

- Retournement des cartes au clic
    - Clic sur une carte face cachée pour la révéler
    - Blocage des clics sur cartes déjà révélées

- Rotation automatique des joueurs via GameManager
    - Attente de 2 secondes après révélation d'une carte
    - Blocage des clics pendant le délai de transition

- Ajout Pioche et Défausse

- Ajout d'un texte guideAction pour indiquer les actions que le joueur doit faire

- Panel temporaire pour séléction du nombre de joueur dans le terminal

- Main de chaque joueur affichées dans le panel playerpreview

- Cartes actions (Joueur seulement)
    - implémentation de la premiere carte action : tradeCardWithPlayer
    - ajout des étoiles dans la pioche
    - initalisation d'une pioche & defausse pour les cartes action

## v.0.4: SPRINT 4
---

### Added

- BotStatsManager : Fichier qui enregistre les victoires des joueurs

- Ajout de 2 bots botRow et botLow

- Affichage du score en temps réel pendant la partie
    - Liaison de `ScoreCalculator` et `PlayerModel` avec la `GameBoardView`

- Écran de fin de manche avec classement des joueurs et leur points actuel
    - Survol d'un joueur pour voir sa main (`PlayerPreviewPanel`)
    - Bouton Continuer ou retour au menu selon fin de manche ou fin de partie (`DoodleButton`)

- Partie complète jouable : enchaînement des manches jusqu'à la fin de partie!

- Ajout de GameBoardBotController permet de gérer les bots dans le view

- Nouvelle carte 'vide'

- Ajout de GameBoardBotController permet de gérer les bots dans le view

- Animation flottante des cartes

- Petite carte à jouer sous le curseur

- Panel de transition entre les rounds

### Modified 

- gameTerminalModel pour séparer et alléger le gameManger

- L'écran de transition est maintenant un ecran de controle pour choisir le nombre de joueur/bots etc.. (contient aussi une option)

- Effet de fondu pour le PreviewPanel


### Fixed

- Fix on pouvait toujours jouer avec les emplacements vides

- Fix Manche 2 (Le premier tour commencait au joueur 0 et pas au joueuractuel + le bot ne jouait pas)

- Fix Taille des cartes en fullScreen sur grand ecran
