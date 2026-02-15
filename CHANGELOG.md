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

- Main de chaque joueur affichées dans le panel playerpreview

- Cartes actions (Joueur seulement)
    - implémentation de la premiere carte action : tradeCardWithPlayer
    - ajout des étoiles dans la pioche
    - initalisation d'une pioche & defausse pour les cartes action
