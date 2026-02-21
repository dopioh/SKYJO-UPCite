# Architecture du Projet Skyjo

Ce document montre la structure qu'on respectera au cours du projet, elle va évolué et elle doit évoluer au cours du projet.

## 1.Base

Des classes qui permette d'éxécuter le jeu.


* **GameLauncher** : Permet de lancer le jeu. Ne pas mettre de méthode a l'intérieur(elles seront pluoto dans le gameManager)
* **GameManager** : Gère tous ce qui entoure le foncitonnement du jeu (logique pure sans affichage)
    *   Attribut :
        * players (ArrayList<PlayerModel>)
        * deck / discardPile (pioche & défausse normales)
        * actionDeck / discardActionPile (pioche & défausse actions)
        * currentPlayerIndex, gameState, turnPhase, currentDrawCard
        * gameBoardModel (pour la version graphique)
    * Méthode :
        * **initializeGame()**, **startTurn()**, **nextPlayer()**
        * **drawDeck()**, **takeDiscard()**, **drawActionDeck()**, **takeActionDiscard()**
        * **exchangeCardInHand()**, **discardDrawCard()**, **revealCardInHand()**
        * **firstPlayer()**, **startNewGame()**, **endRound()**
        * **getGameState()**, **getTurnPhase()**, **getPlayers()**, **getDiscardPile()**, **getDeck()**, **getCurrentPlayer()**, **getCurrentDrawCard()**
        * **revealInitCard()** - Gère la révélation des cartes initiales
    * Notes :
        * Supporte différents types de bots (bot, botRow, botLow) via PlayerData
        * Initialise les bots avec les bonnes stratégies (BotTerminalModel par défaut, BotRow pour botRow, BotHigh(3) pour botLow)
* **GameTerminalModel** : Gère toutes les interactions du jeu via le terminal (séparé du GameManager)
    * Attribut :
        * scanner (Scanner)
        * gameManager (GameManager)
    * Méthode :
        * **playGame()** - Déroulement complet de la partie en mode terminal
        * **initializeHand()** - Révélation des 2 cartes initiales avec interaction utilisateur
        * **playTurn()**, **playerTurn()**, **botTurn()** - Gestion des tours de jeu
        * **exchangeCard()** - Échange de cartes avec interaction utilisateur
        * **showHand()** - Affichage de la main d'un joueur dans le terminal
        * **displayRoundResults()**, **displayWinner()**, **displayNewRound()** - Affichages des résultats
        * **closeScanner()** - Fermeture du scanner
* **GameLoop** : Gère le foncitonnement des cycles du jeu; les updates() et les repaint()

## 2. MVC 

On travaille sur un model MVC 

### MODEL


Gère la logique du jeu.

* **Interface** :
    * `CardInterface`: Interface pour les cartes
    * `CardStackInterface`: Interface pour les piles de cartes

* **Card** :
    * `CardModel`: Classe permettant de créer des cartes vient de l'interface
    *CardInterface*
        * Attribut : value, facedown, pathimage
        * Méthode : getter et setter
    * `CardStackModel` : Classe qui créer les piles de cartes vient de l'interface *CardStackInterface*
        * Méthode : 
            * **stackSize()**, **getCardAt(int indice)**,**getTopCard()**, **getUnderCard()**,**addCardAtTop()**, **removeCardAtTop()**, **replaceCardAt()**, **isEmpty()**


* **Piles de cartes** :
    * `PiocheModel`: Classe de la pioche principale (hérite de CardStackModel)
        * Méthode : **mélange et initialise les 150 cartes du jeu**
    * `DefausseModel`: Classe de la défausse (hérite de CardStackModel)
        * Méthode : **pile vide à l'initialisation**

* **Types de cartes** :
    * `NumberCardModel`: Cartes numérotées (hérite de CardModel)
        * Attribut : cardValue, cardColor
        * Méthode : getter et setter, **isStar()**
    * `ActionCardModel`: Cartes spéciales (hérite de CardModel)
        * Attribut : **ActionType**
        * Méthode : **getType()**

* **Joueur** :
    * `PlayerModel`: Représente un joueur
        * Attribut : name, hand (PlayerHandModel), totalScore, starsScore
        * Méthode : **getName()**, **getHand()**, **getTotalScore()**, **addScore()**, **resetHand()**, **showActionHand()**, **getStarsScore**, **addStarsScore**, **subStarsScore**
    * `PlayerHandModel`: Grille 4x3 de cartes du joueur
        * Attribut : grid (CardModel[][]), rows=3, columns=4
        * Méthode : **placeCard()**, **getCard()**, **isAvailable()**, **getVisibleCardScore()**, **révéler cartes**, **détecter colonnes complètes**
    * `PlayerData`: Données persistantes du joueur
        * Attribut : id, rank (Rank), elo, xp, money, skins (ArrayList), botType (String)
        * Méthode : **getters/setters pour toutes les données**
        * Notes :
            * botType peut être null (joueur humain), "bot", "botRow", ou "botLow"
            * Utilisé par GameManager pour initialiser les bots avec la bonne stratégie

* **Bot** :
    * `BotModel`: Représente un bot (héritage de bot)
        * Attribut : meme que PlayerModel + strategy (BotStrategyInterface)
        * Méthode : meme que PlayerModel + **getStrategy()**
    * `BotHandModel`: Grille 4x3 de cartes du bot
        * Super-classe : **PlayerHandModel**
        * Méthodess supplémentaires : **revealRandomCard()**, **sameCardInHandPosition()**, **tradeInHand()**, **isAcceptableCard()**
    * `BotStrategyInterface`: Interface définissant les stratégies de bot
        * Méthode : **TakeFromDiscard()**, **ExchangeDrawCard()**, **CardToExchange()**
    * `BotTerminalModel`: Stratégie de bot basique (implémente BotStrategyInterface)
        * Stratégie par défaut pour les bots
    * `BotRow`: Stratégie de bot optimisant les lignes (implémente BotStrategyInterface)
        * Se concentre sur l'élimination de lignes complètes
    * `BotHigh`: Stratégie de bot évitant les cartes hautes (implémente BotStrategyInterface)
        * Évite les cartes au-dessus d'un certain seuil
    * `BotStatsManager`: Gère les statistiques des bots
        * Méthode : **recordVictory()**, gestion des stats de parties

* **Règles et logique** :
    * `GameRuleModel`: Définit les règles du jeu
        * Constantes : **INITIAL_REVEALED_CARD=2**, **WINNING_SCORE=100**, **COLUMN_SIZE=4**, **ROWS_SIZE=3**
        * Méthode : 
            * **canTakeFromDiscard()**, **canDrawFromDeck()**, **mustExchangeCard()**
            * **checkEndOfRound()** - Vérifie si la manche est terminée
            * **removeIdenticalColumns()**, **removeIdenticalRows()** - Supprime les colonnes/lignes identiques
            * **findLowestScorePlayer()**, **findRoundEndPlayer()** - Trouve les joueurs spécifiques
            * **applyRoundScores()** - Applique les scores de la manche selon les règles
            * **isGameOver()** - Vérifie si un joueur a atteint 100 points
            * **determineWinner()** - Détermine le gagnant
            * **getFirstPlayerIndex()**, **getBiggestScorePlayerIndex()** - Gestion des indices joueurs
    * `ScoreCalculator`: Calcule les scores
        * Méthode : **calculateHandScore()**, **détection colonnes identiques**
    * `Rank`: Enum des rangs (OQTF, NOPAPER, A4, NEWSPAPAER, PLASTIC, PAPYRUS, PARCHEMIN, BULLETIN, CESTLACARTE, DEGREES, BLACKCARD, WORMS)
        * Méthode : **findRankByElo()**
    * `ActionSkills` Implémente les capacités des cartes actions
        - **Methode** : tradeCardWithPlayer()

* **Audio** :
    * `AudioManager`: Gère la musique et les effets sonores
        * Attribut : bgmClip (Clip), enabled (boolean), volume (int)
        * Méthode : **playBgm()**, **playSfx()**, **stopBgm()**, **setEnabled()**, **setVolume()**

* **Plateau** :
    * `GameBoardModel` Gere la logique du plateau  
        * Elle relie `GameBoardView` et `GameBoardController`
        * Methode : **buttonPlayerCalled()** pour gerer l'affichage du `PlayerPreviewPanel`, **retourMenuCalled()**


### VIEW

Gère l'affichage 

* **Frame** : Fenêtre principale du jeu (JFrame)
    * Attribut : cardLayout (CardLayout), mainMenuPanel, settingsOverlay, fullscreen, windowedBounds, audioManager
    * Méthode : **showSettingsOverlay()**, **hideSettingsOverlay()**, **toggleFullscreen()**, **getAudioManager()**

* **Panels** :
    * `MainMenuPanel`: Panel du menu principal
        * Attribut : shopButton, playButton, settingsButton
        * Méthode : **getShopButton()**, **getPlayButton()**, **getSettingsButton()**
    * `SettingsOverlay`: Overlay des paramètres (JComponent)
        * Attribut : card (JPanel), backButton, contentPanel, tabJeu, tabAudio, tabAutres
        * Méthode : **getBackButton()**, **getTabJeu()**, **getTabAudio()**, **getTabAutres()**, **createTabButton()**, **createCardPanel()**

* **GameBoardView** : Panel pour afficher le plateau de jeu
    * Méthode : **initComponents()**, **showPlayerscards()**, **getCardImagePath()**, **resizeLayers()**
    * Notes :
        * Supporte l'affichage des bots automatiquement grâce au polymorphisme (BotModel extends PlayerModel)
        * Affiche tous les types de joueurs (humains et bots) de manière uniforme

* **PlayerPreviewPanel** : Panel pour afficher les cartes des autres joueurs
    * Méthode : **showOtherPlayerHand**, **getCardImagePath()**

### CONTROLLER

Gère toutes les entrées du joueur avec l'ordinateur

* **MainMenuController** : Contrôleur du menu principal
    * Attribut : frame (Frame), view (MainMenuPanel), audio (AudioManager)
    * Méthode : **ActionListener pour playButton, settingsButton, shopButton**
    * Notes :
        * Accepte les entrées "bot", "botRow", "botLow" pour créer différents types de bots
        * Crée des PlayerData avec le type de bot approprié pour initialisation par GameManager

* **SettingsOverlayController** : Contrôleur de l'overlay paramètres
    * Attribut : frame (Frame), audioManager (AudioManager)
    * Méthode : **ActionListener pour backButton, tabJeu, tabAudio, tabAutres, fullscreenButton, soundButton, volumeButtons**

* **GameBoardController** : Contrôleur principal du plateau de jeu
    * Attribut : 
        * gameBoardView (GameBoardView)
        * gameManager (GameManager)
        * frame (Frame)
        * botController (GameBoardBotController)
        * waitingNextTurn (boolean)
    * Méthode :
        * **listener()** - Gère les clics sur les boutons (pioche, défausse, cartes)
        * **switchToNextPlayer()** - Gère la transition entre joueurs avec délai de 800ms
        * **refreshGuide()** - Met à jour le guide d'action selon la phase du jeu
        * **setWaitingNextTurn()**, **isWaitingNextTurn()** - Gestion de l'état d'attente
    * Notes :
        * Délègue toute la logique des bots au GameBoardBotController
        * Utilise Timer (javax.swing) pour les transitions fluides entre joueurs

* **GameBoardBotController** : Contrôleur dédié au jeu automatique des bots
    * Attribut :
        * gameBoardView (GameBoardView)
        * gameManager (GameManager)
        * mainController (GameBoardController)
    * Méthode :
        * **handleBotTurn()** - Dispatcher principal, appelle les méthodes selon la phase actuelle
        * **handleBotInit()** - Révèle 2 cartes aléatoires en phase INIT_REVEAL avec délais
        * **handleBotDrawDecision()** - Phase DRAW_DECISION : décide pioche ou défausse selon stratégie
        * **handleBotAfterDraw()** - Phase CARD_DRAW_FROM_DECK : échange ou défausse+révèle
        * **handleBotAfterTakeDiscard()** - Phase CARD_TAKE_FROM_DISCARD : échange obligatoire
    * Notes :
        * Utilise Timer (javax.swing) avec délais de 800-1500ms pour visibilité
        * Réutilise les fonctions de base du GameManager (drawDeck, takeDiscard, exchangeCardInHand, etc.)
        * Respect du système de phases existant (pas de flux parallèle)
        * Affiche les cartes piochées via getBtnPioche().setCardImage() pour feedback visuel
        * Gère les transitions bot→humain avec showPlayerCards()

## UTILS

Classes utilitaires

* **ImageCache** : Cache pour les images du jeu
    * Attribut : cache (Map<String, BufferedImage>)
    * Méthode : **get(String path)**, **preload(String... paths)**, **clear()**
