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

## v.0.5: SPRINT 5
---

### Added
- Les cartes actions suivantes : 
    - **REPLACE_CARD_IN_HAND, PLAY_TWO_TURN, DRAW_THREE_CARD, PLAY_LAST_ACTION_IN_DEFAUSSE, ALL_PLAYERS_DROP_CARD, SPY_ONE_COLUMN**

- Defausse des actions visibles de chaque tour

- Le set de cartes est différent selon le mode de jeu (base ou action)

- Possibilité d'activité ou desactivé le mode action

- Main menu
    - Fond d'écran
    - Logo Skyjo
    - Bouton compte en haut à droite (J button)
    - Animation scale au survol du DoodleButton avec ease

- Creation de compte /connexion avec la base de donnees, dans le terminal

### Modified 
- Refacto propre dans **GameTerminalModel.useAction()**

- Couleurs par défaut du DoodleButton remplacées

- Boutons du Main Menu utilisent des images pour le fond

- Animation scribble du DoodleButton améliorée et adapte au couleurs

- Icônes des DoodleButton plus grandes et mieux centrées

### Fixed
- Reimplémentation du système de carte action qui avait disparu lors d'un ancien refactoring


## v.0.6: SPRINT 6
---

### Added

- `AnimLoop` : système centralisé d'animation
    - Un seul Timer à 60fps pour toute le jeu
    - Delta time pour que ça soit pareil sur n'importe quel ordi
    - Démarrage et arrêt automatiques selon le nombre de tâches enregistrées
    - Pas besoin de toucher au code de AnimLoop pour l'utiliser, contrairement a Gameloop de worms avec un utilisation simple:
      - `AnimLoop.add(this, animation)` / `AnimLoop.remove(this)` (voir le javadoc dans AnimLoop pour l'utilisation)

- Top bar au dessus du plateau de jeu
    - Image de fond
    - Numéro de manche à gauche (`Manche X`)
    - Instruction au centre
    - nouveau system de calcul de score du joueur courant à droite (`X P`)

- Système de couleur par joueur
    - 3 sets d'images : rouge / vert / jaune pour la top bar et le fond
    - Tourne si plus de 3 joueurs
    - Fond et top bar changent automatiquement à chaque changement de joueur
    - Boutons joueurs à gauche colorés selon la couleur du joueur (temporaire)

- Interface simple au lancement du jeu pour se connecter ou creer un compte (obliger de se connecter pour acceder au jeu)

- Lors de la creation du compte, ses statistiques sont defini automatiquement dans la database

- Apres la creation/login, les infos du joueurs sont sur la db et le cachelocal `PlayerData.java` (il faut juste le reactualiser quand on change une stat dans la db)

- Ajout d'un nouveau bot botCounting, liaision du bot simple avec le jeu action


### Modified

- Migration vers `AnimLoop` : `DoodleButton`, `NextRoundPanel`, `PlayerPreviewPanel`, `CursorCardView`, `CardButton`

- Vitesse de fade pour l'écran "prochaine joueur" accéléré pour plus de fluidité

- Suppression du label "Main de ..." au dessus des cartes

- Suppression du rectangle/bordure noire autour de la grille de cartes

- Suppression des code en double/nettoyage

### Fixed

- Fluidité globale : Le jeu ne lag plus sur Windows en passant de 12 repaint a un seul pour l'animation carte flottante ainsi que tout les autres animations qui sont maintenant geré avec `AnimLoop`

- Repaint intelligent : seules les zones nécessaires sont redessinées (ancienne et nouvelle position de la carte curseur) au lieu de tout l'écran -> carte curseur plus fluide

- Plus de lignes/artefacts visibles lors du survol des cartes ou de la preview de la main des autres joueurs

- Selection du mode de jeu (vanilla ou action) dans le menu de selection

- Affichage de la pioche et de la defausse action en jeu

- Couverture de tests pour 
    - les pioches
    - les defausses
    - les cartes numéros et les cartes actions
    - les comportements des cartes actions
    - la CursorCard
    - le BotHandModel

- debut de recode des actions pour awt/swing

- affichage de la main action du joueur

## v.0.7: SPRINT 7
---

### Added

- Fin du recode des cartes action pour le mode graphique
- Bouton invitée pour accéder au jeu sans identifiants

### Modified

- Le système de tour se passe désormais dans `GameLoop.java`
- Toutes les actions joueurs Swing sont gérées par `HumanTurnController`
- Main des cartes actions sous les cartes numéro
- Connexion par username dans l'interface

### Fixed

- Optimisation :
    - Le jeu ne lag enfin plus
    - fix de lag quand on clique sur lancer le jeu et au premier changement de joueur
    - transition changement de joueur / preview carte beaucoup plus fluide
    - Et bien plus
- PLAY_TWO_TURN joue 2 tours (contre 3 avant)

## v.0.8: SPRINT 8

### Added
- Systeme d'overlay quand on passe la souris sur une carte action

- Description des cartes action

- Les etoiles dans la main se comportent comme des jokers

- Les etoiles peuvent se faire voler par des adversaires

- Si on pioche une carte etoile :
    - soit on la joue directement et piocher une action
    - soit on peut la met dans notre main et piocher une action aux prochains tours

- Transition au changement de joueur (`PlayerTransitionPanel`)
    - Bande colorée en couleur du joueur avec le nom du joueur

- Mailmap

- Bouton multiplayer qui va ensuite faire un panel, où sont les salons et pouvoir créer un salon

- Système de Timer (Si le joueur ne joue pas avant la fin du temps 2 fois il est "remplacé par un bot")

- Toggle Timer

- PauseOverlay (appuyez sur ESC pendant la partie pour voir le menu Pause qui contient (retour + paramètres))

### Modified
- Refactor des checks lignes/colonnes

- Ajout du cas wildcard (joker etoile) dans les checks lignes/colonnes

- Quelques modif sur les fonctions de jeu pour le nouveau système d'étoiles

- Fusion des comportements du POPUP_Layer pour paneltransitionPanel et actionDescriptionOverlay

- Possibilité de Host et Join un serveur dans le terminal

- INTERFACE GRAPHIQUE POUR LES LOBBY

- Animations des cartes cleared à chaque fois qu'on quitte la partie

- Bouton retour déplacé dans le PauseOverlay

### Fixed

- Le joueur qui commence après l'init reveal est maintenant correctement déterminé par le score des cartes révélées (`firstPlayer`)

- L'index du joueur de départ n'est plus avancé en double à la fin de l'init reveal

- Linux : Si le jeu détecte qu'elle tourne sous linux, elle désactive automatiquement l'animation carte flottante qui lag, mais en laissant l'effet scale (hover) car elle ne lag pas

- Fix boutons qui ne répondaient pas au premier clic quand la souris arrivait trop vite (`mouseClicked` -> `mousePressed`)

- LAST ROUND

## v.0.9: SPRINT 9

### Added

- les Salons pour le multiplayer

### Added

- Boutique avec 3 packs de skins disponibles avec raretés differentes
    - Plusieurs skins
    - Logique d'achat
    - Système de debloquage de skins (version temporaire)

- Classeur de skins
    - Classeur permettant de voir les skins du jeu, debloqués en non debloqués
    - Possibilité d'equiper un skin debloqué dans le classeur

- Cartes dos des 5 devs pour la boutique

- BroadCast en continue du Host (marche sur toutes les cartes reseaux (VLAN y compris)) + ajouts methodes kickPlayer et Quit

### Modified

- `DoodleButton`
    - ajout du système de positionnement d'icône
        - `setIconSide(IconSide.RIGHT ou LEFT)` : icône collée au bord
    - suppression de l'ancien version sans image et nettoyage du code + remplacé les appel vers l'ancien button
    - possibilité de changer la taille de police

- Page lobby / game mode selection
    - icone, correction des doodle button, espacement, couleur, font...
    - fix double clic

- Page PlayersSetup panel
    - Fond d'écran, police, couleur, espacement...
    - Bouton Retour et Lancer en haut comme les autres panel
    - mode Skyjo Action et Boutons nb de joueurs : vert si actif, transparent sinon
    - Bouton `?` avec popup d'explication des bots

- Page connexion panel
    - font, couleur, fond, espacement, doodle button
    - JTextField/mdp plus beau

## v.0.10: SPRINT 10

### Added

- Animation flip :
    - Sur pioche : joueur et bot
    - Sur init reveal : joueur et bot
    - Sur révéler une carte après avoir défaussé la piochée : joueur et bot
    - Mais pas pour placer/échanger les cartes

- Animation press :
    - sur la défausse : joueur et bot
    - sur les cartes du plateau : bot

- Animation lift :
    - sur la défausse : quand le bot décide de prendre la défausse

### Modified

- Amélioration DA de l'écran fin de manche / fin de partie

- Application de opti partout :
    - background qui n'étais pas en cache et redimentionnais constament sur les panel autre que GameBoardView

### Fixed

- Fix de l'animation hover du `DoodleButton` qui grossisait si le dernier hover datais (reset tick de deltatime)

- Fix du `PlayerPreviewPanel` qui ne s'affichait plus en fin de manche/partie

- Décalage lorsqu'il y a trop de carte action

- Reset des cartes action après une manche

- le comptage des pts des cartes actions 

- délié entre le deck de la première manche et des suivantes

- désactivation des listener lors d'un bot vs bot 


