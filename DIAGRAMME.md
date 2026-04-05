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
        * firstPlayerToPlayIndex, lastRoundPlayer, extraTurnsRemaining, roundNumber
        * afkCounts, autoControlledPlayers (gestion AFK)
    * Méthode :
        * **initializeGame()**, **startTurn()**, **nextPlayer()**
        * **drawDeck()**, **takeDiscard()**, **drawActionDeck()**, **takeActionDiscard()**
        * **drawActionRewardCard()**, **grantActionForStar()**
        * **exchangeCardInHand()**, **discardDrawCard()**, **revealCardInHand()**
        * **firstPlayer()**, **startNewGame()**, **endRound()**
        * **getGameState()**, **getTurnPhase()**, **getPlayers()**, **getDiscardPile()**, **getDeck()**, **getCurrentPlayer()**, **getCurrentDrawCard()**
        * **revealInitCard()**
        * **grantExtraTurn()**, **getCurrentPlayerIndex()**, **getInitRevealCount()**, **getRoundNumber()**
        * **setTurnPhase()**, **stopGame()**
        * **getAfkCount()**, **registerAfkTurn()**, **resetAfkCount()**
        * **isAutoControlled()**, **setAutoControlled()**
        * **isStarCard()** (static), **withActionCard()**, **getDiscardActionPile()**, **getActionDeck()**
* **GameTerminalModel** : Gère toutes les interactions du jeu via le terminal (séparé du GameManager)
    * Attribut :
        * scanner (Scanner)
        * gameManager (GameManager)
        * hasGameManagerActionsCard (boolean) - Indique si le mode action est actif
    * Méthode :
        * **playGame()** - Déroulement complet de la partie en mode terminal
        * **initializeHand()** - Révélation des 2 cartes initiales avec interaction utilisateur
        * **playTurn()**, **playerTurn()**, **botTurn()** - Gestion des tours de jeu
        * **exchangeCard()** - Échange de cartes avec interaction utilisateur
        * **showHand()** - Affichage de la main d'un joueur dans le terminal
        * **displayRoundResults()**, **displayWinner()**, **displayNewRound()** - Affichages des résultats
        * **useAction()** - Permet aux joueurs d'utiliser une carte d'action
        * **grantActionForStar()** - Applique l'effet étoile (gagne une action)
        * **revealRandomFaceDownCard()** - Révèle une carte cachée aléatoire (utilisé pour les bots)
        * **closeScanner()** - Fermeture du scanner
        * **Support du mode action pour les bots** :
            * **botUseActionRandom()** - Permet au bot de jouer une action aléatoirement
            * **botTradeCardWithPlayer()** - Version bot de TRADE_CARD_WITH_PLAYER
            * **botReplaceCardInHand()** - Version bot de REPLACE_CARD_IN_HAND
            * **botDrawThreeCard()** - Version bot de DRAW_THREE_CARD
            * **botPlayLastActionInDefausse()** - Version bot de PLAY_LAST_ACTION_IN_DEFAUSSE
            * **botSpyOneColumn()** - Version bot de SPY_ONE_COLUMN
            * **botAllPlayersDropCard()** - Version bot de ALL_PLAYERS_DROP_CARD
* **GameLoop** : Gère le foncitonnement des cycles du jeu; les updates() et les repaint()
    * **proceedAfterAction()**, **continueFlow()**, **continueInitRevealFlow()**, **finishNormalTurnFlow()**, **startCurrentFlow()**
    * **startTurnCountdown()**, **stopTurnCountdown()**, **isGameActive()**, **stopGame()**, **handleTurnTimeout()**
    * **setPaused()**, **setTimeoutController()**, **setTimerEnabled()**

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
            * **getStack()**, **afficheStack()**


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
        * Attribut : name, hand (PlayerHandModel), totalScore
        * Méthode : **getName()**, **getHand()**, **getTotalScore()**, **addScore()**, **resetHand()**, **showActionHand()**, **getActionHand()**
    * `PlayerHandModel`: Grille 4x3 de cartes du joueur
        * Attribut : grid (CardModel[][]), rows=3, columns=4
        * Méthode : **placeCard()**, **getCard()**, **isAvailable()**, **getVisibleCardScore()**, **révéler cartes**, **détecter colonnes complètes**
        * **replaceCard()**, **revealCard()**, **allCardsRevealed()**, **countRevealedCards()**, **checkRows()**, **removeRow()**, **calculateScore()**, **revealCardsTemp()**
    * `PlayerData`: Données persistantes du joueur
        * Attribut : id, rank (Rank), elo, xp, money, skins (ArrayList)
        * Méthode : **getters/setters pour toutes les données**

* **Bot** :
    * `BotModel`: Représente un bot (héritage de bot)
        * Attribut : meme que PlayerModel + strategy (BotStrategyInterface)
        * Méthode : meme que PlayerModel + **getStrategy()**
    * `BotHandModel`: Grille 4x3 de cartes du bot
        * Super-classe : **PlayerHandModel**
        * Méthodess supplémentaires : **revealRandomCard()**, **sameCardInHandPosition()**, **tradeInHand()**, **isAcceptableCard()**
        * **countVisibleCardsInColumn()**
    * `BotStrategyInterface`: Interface définissant les stratégies de bot
        * Méthode : **TakeFromDiscard()**, **ExchangeDrawCard()**, **CardToExchange()**, **getSeuil()**
    * `BotTerminalModel`: Stratégie de bot basique (implémente BotStrategyInterface)
        * Stratégie par défaut pour les bots avec seuil fixe
    * `BotRow`: Stratégie de bot optimisant les lignes (implémente BotStrategyInterface)
        * Se concentre sur l'élimination de lignes complètes
    * `BotLow`: Stratégie de bot favorisant les cartes basses (implémente BotStrategyInterface)
        * Privilégie les cartes de faible valeur
    * `BotCounting`: Stratégie de bot avancée qui compte les cartes (implémente BotStrategyInterface)
        * Attribut : gameManager (GameManager), cardDeck (Map statique des cartes dans le deck)
        * Méthode : 
            * **getSeuil()** - Calcule dynamiquement le seuil basé sur les probabilités
            * **countVisibleCards()** - Compte toutes les cartes visibles (mains + défausse)
            * **calculateRemainingCards()** - Calcule les cartes restantes dans la pioche
            * **calculateAverage()** - Calcule la moyenne pondérée des cartes restantes
            * **TakeFromDiscard()** - Décision basée sur les probabilités
            * **ExchangeDrawCard()** - Décision d'échange basée sur les statistiques
            * **CardToExchange()** - Choix intelligent de la carte à échanger
    * `BotStatsManager`: Gère les statistiques des bots
        * Méthode : **recordVictory()**, gestion des stats de parties

* **Règles et logique** :
    * `GameRuleModel`: Définit les règles du jeu
        * Constantes : **INITIAL_REVEALED_CARD=2**, **WINNING_SCORE=100**, **COLUMN_SIZE=4**, **ROWS_SIZE=3**
        * Méthode : 
            * **canTakeFromDiscard()**, **canDrawFromDeck()**, **mustExchangeCard()**
            * **checkEndOfRound()** - Vérifie si la manche est terminée
            * **removeIdenticalColumns()**, **removeIdenticalRows()** - Supprime les colonnes/lignes identiques
            * **isWildcardColumn** retourne Vrai si une colonne doit etre supprimée (avec joker)
            * **isWildcardRow** retourne Vrai si une ligne doit etre supprimée (avec joker)
            * **findLowestScorePlayer()**, **findRoundEndPlayer()** - Trouve les joueurs spécifiques
            * **applyRoundScores()** - Applique les scores de la manche selon les règles
            * **isGameOver()** - Vérifie si un joueur a atteint 100 points
            * **determineWinner()** - Détermine le gagnant
            * **getFirstPlayerIndex()**, **getBiggestScorePlayerIndex()** - Gestion des indices joueurs
            * **canDiscardCard()**
    * `ScoreCalculator`: Calcule les scores
        * Méthode : **calculateHandScore()**, **détection colonnes identiques**
        * **calculateRoundScores()**, **getLowestScorePlayer()**, **applyEndGamePenalty()**
    * `Rank`: Enum des rangs (OQTF, NOPAPER, A4, NEWSPAPAER, PLASTIC, PAPYRUS, PARCHEMIN, BULLETIN, CESTLACARTE, DEGREES, BLACKCARD, WORMS)
        * Méthode : **findRankByElo()**
    * `ActionSkills` Implémente les capacités des cartes actions
        - **Methode** : tradeCardWithPlayer(), replaceCardInHand(), drawThreeCard(), playLastActionInDefausse(), spyOneColumn(), allPlayersDropCard(), playTwoTurn(), askCardPos(), grantActionForStar()

* **Audio** :
    * `AudioManager`: Gère la musique et les effets sonores
        * Attribut : bgmClip (Clip), enabled (boolean), volume (int)
        * Méthode : **playBgm()**, **playSfx()**, **stopBgm()**, **setEnabled()**, **setVolume()**
        * **applyToClip()**, **percentToDb()**

* **Plateau** :
    * `GameBoardModel` Gere la logique du plateau
        * Elle relie `GameBoardView` et `GameBoardController`
        * Methode : **buttonPlayerCalled()** pour gerer l'affichage du `PlayerPreviewPanel`, **retourMenuCalled()**
        * **getGameBoardController()**, **getFrame()**

* **Multijoueur / Lobby** :
    * `LobbyModel`: Représente un salon de jeu multijoueur P2P
        * Attribut : lobbyId, lobbyName, hostName, currentPlayers, maxPlayers, actionMode, status
        * Enum : **LobbyStatus** (WAITING, PLAYING, FULL)
        * Méthode : **canJoin()**, **updateStatus()**, **getters/setters**
         **setCurrentPlayers()**, **setStatus()**, **toString()**
    * `LobbyPlayerModel`: Représente un joueur dans un salon d'attente multijoueur
        * Attribut : playerId, playerName, isHost, isReady, isBot
        * Méthode : **getters/setters**, les bots sont automatiquement prêts



### VIEW

Gère l'affichage 

* **Frame** : Fenêtre principale du jeu (JFrame)
    * Attribut : cardLayout (CardLayout), mainMenuPanel, gameModeSelectionPanel, lobbyBrowserPanel, settingsOverlay, fullscreen, windowedBounds, audioManager
    * Méthode : **showSettingsOverlay()**, **hideSettingsOverlay()**, **toggleFullscreen()**, **refreshGuide()**, **getAudioManager()**

* **Panels** :
    * `MainMenuPanel`: Panel du menu principal
        * Attribut : shopButton, playButton, settingsButton
        * Méthode : **getShopButton()**, **getPlayButton()**, **getSettingsButton()**
    * `ShopPanel`: Panel de la boutique
        * Attribut : backBtn, classeurBtn, buyButtons, packImages, topBar, chanceContainer, chanceLabel, cardsRow, caseOpeningOverlay
        * Méthode : **getBackButton()**, **getBuyButtons()**, **getClasseurButton()**, **startCaseOpening()**, **openClasseur()**, **isCaseOpeningVisible()**
    * `GameModeSelectionPanel`: Panel de sélection du mode de jeu (Local vs Multi P2P)
        * Attribut : localBtn, multiBtn, backBtn, backgroundImage
        * Méthode : **getLocalButton()**, **getMultiButton()**, **getBackButton()**
    * `LobbyBrowserPanel`: Panel pour afficher la liste des salons multijoueur disponibles
        * Attribut : backBtn, createLobbyBtn, refreshBtn, lobbiesListPanel, scrollPane, backgroundImage, lobbyItems
        * Méthode : **addLobby()**, **clearLobbies()**, **refreshLobbies()**, **showNoLobbiesMessage()**, **getBackButton()**, **getCreateLobbyButton()**, **getRefreshButton()**
    * `LobbyItemPanel`: Composant représentant un salon dans la liste
        * Attribut : lobby (LobbyModel), joinBtn, nameLabel, hostLabel, playersLabel, modeLabel
        * Méthode : **getJoinButton()**, **getLobby()**, **updateLobbyInfo()**, **getPlayerCountColor()**
    * `LobbyRoomPanel`: Panel de la salle d'attente d'un salon multijoueur (hôte peut configurer, tous voient les joueurs)
        * Attribut : lobbyTitleLabel, lobbyInfoLabel, playersTitleLabel, backBtn, readyBtn, startBtn, playersListPanel, leftPanel, configPanel, maxPlayersSpinner, actionModeCheckbox, addBotButton, playerSlots, isHost
        * Méthode : **setIsHost()**, **setLobbyTitle()**, **setLobbyInfo()**, **addPlayer()**, **removePlayer()**, **clearPlayers()**, **updatePlayersCount()**, **refreshPlayerSlots()**, **getters**
    * `PlayerLobbySlot`: Composant représentant un slot de joueur dans le salon d'attente
        * Attribut : player (LobbyPlayerModel), nameLabel, statusLabel, roleLabel, kickButton, isLocalPlayerHost
        * Méthode : **updatePlayerStatus()**, **getPlayer()**, **getKickButton()**
    * `CreateLobbyDialog`: Dialogue pour créer un nouveau salon multijoueur
        * Attribut : lobbyNameField, maxPlayersSpinner, actionModeCheckbox, createBtn, cancelBtn, confirmed
        * Méthode : **getLobbyName()**, **getMaxPlayers()**, **isActionMode()**, **isConfirmed()**, **setConfirmed()**, **validateInput()**, **getCreateButton()**, **getCancelButton()**
    * `SettingsOverlay`: Overlay des paramètres (JComponent)
        * Attribut : card (JPanel), backButton, contentPanel, tabJeu, tabAudio, tabAutres
        * Méthode : **getBackButton()**, **getTabJeu()**, **getTabAudio()**, **getTabAutres()**, **createTabButton()**, **createCardPanel()**
    * `CaseOpeningOverlayPanel`: Overlay boutique & classeur
        * Attributs (tempo) : unlockedSkinPaths (ArrayList<String>), selectedSkinPath (String)
        * Méthodes : **startOpening()**, **openClasseur()**, **drawClasseur()**, **drawSkinBox()**, **handleClasseurClick()**, **getEquipButtonBounds()**, **drawEquipButton()**
        * Méthodes tempo : **getUnlockedSkinPaths()**, **unlockSkin()**, **isSkinUnlocked()**, **getSelectedSkinPath()**, **setSelectedSkinPath()**

* **GameBoardView** : Panel pour afficher le plateau de jeu
    * Méthode : **initComponents()**, **showPlayerscards()**, **getCardImagePath()**, **resizeLayers()**, **showCursorCard()**, **hideCursorCard()**, **updateCardDisplay()**, **updateDiscardOnly()**, **updateActionCardDisplay()**, **updateActionDiscardOnly()**
    * **showActionDescription**
    * **hideActionDescription**
    * **getActionTitle**
    * **getActionDescription**

* **PlayerPreviewPanel** : Panel pour afficher les cartes des autres joueurs
    * Méthode : **showOtherPlayerHand**, **getCardImagePath()**

* **ActionDescriptionOverlayPanel** : système d'overlay qui affiche les détails d'une carte action quand le curseur se trouve au dessus (hover)

* **CursorCardView** : Vue pour la petite carte sous le curseur

* **NextRoundPanel** : Panel de transitions entre les rounds
    * Methode : **fade()**,**paintComponent()**,**paintChildren()**, **paint()**

* **ConnextionPannel** : Panel qui va demander au joueur de se connecter ou créer un compte avant d'entrer dans le jeu.
    * Methode : **selection()**,**connexion()**,**createAccount()**

### CONTROLLER

Gère toutes les entrées du joueur avec l'ordinateur

* **MainMenuController** : Contrôleur du menu principal
    * Attribut : frame (Frame), view (MainMenuPanel), audio (AudioManager)
    * Méthode : **ActionListener pour playButton** → redirige vers GameModeSelectionPanel, **settingsButton, shopButton**
    * **clicShop()**, **clicSettings()**, **clicAccount()**

* **GameModeSelectionController** : Contrôleur du panel de sélection de mode de jeu
    * Attribut : frame (Frame), view (GameModeSelectionPanel)
    * Méthode : **ajouterListeners()**, **clicLocal()** → redirige vers PlayersSetupPanel, **clicMulti()** → redirige vers LobbyBrowserPanel, **clicRetour()** → retour au menu

* **PlayersSetupController** : Contrôleur pour la configuration des joueurs
    * Méthode : **addRowHoverEffect()**, **updatePlayerCountButtonsStyle()**, **lancerPartie()**

* **LobbyBrowserController** : Contrôleur du navigateur de salons multijoueur
    * Attribut : frame (Frame), view (LobbyBrowserPanel), availableLobbies (List<LobbyModel>)
    * Méthode : **ajouterListeners()**, **clicRetour()**, **clicCreerSalon()**, **clicRafraichir()**, **createLobby()**, **loadLobbies()**, **refreshView()**, **joinLobby()**, **generateTestLobbies()**
    * Note : Les méthodes createLobby(), loadLobbies() et joinLobby() sont à connecter au système P2P

* **LobbyRoomController** : Contrôleur de la salle d'attente d'un salon multijoueur
    * Attribut : frame (Frame), view (LobbyRoomPanel), players (List<LobbyPlayerModel>), lobbyId, lobbyName, isHost, isReady
    * Méthode :
        * **initializeLobby()** - initialise le salon avec les paramètres (host/joueur)
        * **addPlayer()**, **removePlayer()** - gestion des joueurs
        * **updatePlayerStatus()** - met à jour le statut prêt d'un joueur
        * **clicQuitter()** - quitter le salon avec confirmation
        * **clicReady()** - basculer entre prêt/pas prêt (joueurs non-host)
        * **clicStart()** - lancer la partie (host uniquement, vérifie que tous sont prêts)
        * **clicKick()** - retirer un joueur/bot du salon (host uniquement)
        * **clicAjouterBot()** - ajouter un bot avec sélection de stratégie
        * **onMaxPlayersChanged()**, **onActionModeChanged()** - gestion des paramètres
        * **updateLobbyInfo()** - met à jour l'affichage des infos (mode, prêts)
    * Note : À connecter au système P2P pour synchronisation réseau

* **SettingsOverlayController** : Contrôleur de l'overlay paramètres
    * Attribut : frame (Frame), audioManager (AudioManager)
    * Méthode : **ActionListener pour backButton, tabJeu, tabAudio, tabAutres, fullscreenButton, soundButton, volumeButtons**

* **GameBoardController** : Controller pour le Plateau de jeu (la partie)
    * Méthode : **listener()**, **nextPlayerSound()**, **showRoundEndScreen()**, **ContinueRound()**, **refreshGuide()**, **updateAllDecksDisplay()** , **toggleTimer()**
    * Méthode (boucle de jeu) : **requestEndTurnFromBot()**, **showRoundEndScreenFromLoop()**
    * **setWaitingNextTurn()**, **isWaitingNextTurn()**, **togglePauseOverlay()**

* **GameBoardBotController** : controller pour les bots
    * Attribut :
        * gameBoardView (GameBoardView)
        * gameManager (GameManager)
        * mainController (GameBoardController)
    * Méthode :
        * **handleBotTurn()**
        * **handleBotInit()**
        * **handleBotDrawDecision()**
        * **handleBotAfterDraw()**
        * **handleBotAfterTakeDiscard()**
        * **revealRandomFaceDownCard()**
        * **grantActionForStar()**

* **TimeoutTurnController** : Gère les joueurs AFK et les timeouts
    * Méthode :
        * **handleTimeoutForCurrentPlayer()**
        * **copyToBotHand()**
        * **handleTimeoutInitReveal()**
        * **handleTimeoutDrawDecision()**
        * **handleTimeoutAfterDraw()**
        * **handleTimeoutAfterTakeDiscard()**
        * **handleTimeoutRevealAfterDiscard()**
        * **findRandomHiddenCard()**
        * **runLater()**

* **GameLoop** : Boucle du jeu (Les actions des autres controllers seront acceptés par ce fichier)

* **HumanTurnController** : Toutes les actions joueurs sur le gameBoard
    * Méthode : **installListeners()**, **handlePlayerButtonClicked()**, listeners des pioches/mains/actions
    * Méthodes (actions) : 
        * **onActionCardClicked()** - Gestion du clic sur une carte action
        * **handleActionModeClick()** - Gestion des clics en mode action
        * **selectTradeTargetPlayer()** - Sélection du joueur cible pour l'échange
        * **completeTrade()** - Finalisation de l'échange
        * **startAllPlayersDropAction()** - Initiation de l'action "drop" pour tous
        * **selectAllPlayersDropTarget()** - Sélection de la cible pour le drop
        * **applyAllPlayersDropOnSelectedCard()** - Application du drop sur la carte sélectionnée
        * **grantActionForStar()** - Applique l'effet étoile côté
        * **resetTurnState()** - Réinitialisation de l'état du tour
        * **Listeners** pour savoir si le curseur est sur une carte action 
    * **removeAllListeners()**, **getLastReplayableActionInDiscard()** 

* **AccountController** : Contrôleur pour la gestion des comptes
    * Méthode : Principales méthodes de gestion des comptes

**Architecture du système de lobby** :
- `LobbyBrowserPanel` : liste des salons disponibles (créer/rejoindre)
- `LobbyRoomPanel` : salle d'attente avec deux modes :
  - **Mode Hôte** : configuration visible (max joueurs, actions, ajout bots), bouton "Lancer la partie", kick joueurs
  - **Mode Joueur** : configuration cachée, bouton "Prêt"/"Annuler" pour indiquer son statut
- `PlayerLobbySlot` : affichage d'un joueur avec badges (HOST/BOT), statut prêt, bouton kick (hôte uniquement)

## 4. Corrections et Améliorations Récentes

### Problème 1 : Affichage du bouton Kick
**Symptôme** : Le bouton affichait "..." au lieu de "X"
**Cause** : Le caractère Unicode "✕" (U+2715) n'était pas supporté par la police Arial
**Solution apportée** (PlayerLobbySlot.java:86-112) :
- Changement du caractère "✕" vers "X" simple
- Changement de police de "Arial" vers "SansSerif" (cross-platform)
- Ajustement des dimensions : 45x45 → 50x50 pixels
- Ajout d'effet hover : changement de couleur rouge au survol
```
Avant : kickButton = new JButton("✕");
Après  : kickButton = new JButton(); + setText("X")
         Police: SansSerif Font.BOLD 18
         Taille: 50x50 avec border vide
```

### Problème 2 : Double affichage du compteur de joueurs
**Symptôme** : Le titre "Joueurs (X/Y)" avait un affichage en double et se chevauchait avec la zone centrale
**Cause** : Deux informations similaires s'affichaient au même endroit + pas de rafraîchissement correct
**Solution apportée** :
- a) **Séparation des informations** (LobbyRoomController.java:289-299) :
  - Avant : `"Joueurs (X/Y) • Mode Actions • Prêts (Z)"`
  - Après : `"Mode Actions • Prêts (Z)"` (le nombre de joueurs est maintenant seulement au-dessus de la liste)

- b) **Rafraîchissement correct** (LobbyRoomPanel.java:26,104,330-335) :
  - Ajout du champ de classe `leftPanel` pour pouvoir l'invalider
  - `updatePlayersCount()` appelle maintenant `leftPanel.revalidate()` et `leftPanel.repaint()`

- c) **Appel au bon moment** (LobbyRoomController.java:280) :
  - Quand `onMaxPlayersChanged()` est appelé, on appelle maintenant :
    ```java
    view.updatePlayersCount();  // Met à jour le titre "Joueurs (X/Y)"
    updateLobbyInfo();           // Met à jour "Mode X • Y prêts"
    ```

### Modifications aux commentaires
**Standardisation des commentaires** (PlayerLobbySlot.java + LobbyRoomPanel.java) :
- Retiré les espaces après "//"
- Retiré la majuscule au début
- Format : `//texte minuscule` au lieu de `// Texte majuscule`

## 5. Points d'Intégration P2P Restants

Marqués avec `// MEI` ou `// TODO` dans le code :
- **LobbyBrowserController.java:75,89,105,135** : Appels réseau P2P pour créer/rejoindre/rafraîchir salons
- **LobbyRoomController.java:69,70,147,148** : Synchronisation P2P des états joueurs
- **LobbyRoomPanel.java:215** : Note d'aide pour les hôtes (bouton kick avec "X" désormais affiché correctement)

## 6. Modifications Affichage des Cartes Action (Avril 2026)

### Problème résolu
**Symptôme** : Quand trop de cartes action s'acumulaient, il y avait un décalage avec les cartes de main affichées en FlowLayout qui prenait trop de place horizontalement.

**Solution implémentée** :

#### Architecture avant
- `GameBoardView` : utilisait `actionHandPanel` (FlowLayout) qui affichait toutes les cartes action visibles en ligne
- Problème : prenait beaucoup d'espace et créait un décalage visuel

#### Architecture après
1. **Nouveau composant `ActionCardOverlayPanel`** (fr.upc.cr1a.view.ActionCardOverlayPanel)
   - Panel d'overlay affichant les cartes action comme un modal semi-transparent
   - Gère la sélection des cartes avec un contour vert
   - Affiche un bouton "Valider" pour confirmer l'action
   - Se ferme au clic en dehors ou avec le bouton Fermer
   - Inclut le hover avec description des cartes action
   - Attributs :
     * `actionCardButtons[]` : 8 boutons pour les cartes action
     * `cardWrappers[]` : panels wrapper pour gérer les contours de sélection
     * `selectedCardIndex` : index de la carte sélectionnée
     * `validateButton` : bouton de validation
   - Méthodes principales :
     * **showActionHand()** : affiche l'overlay avec les cartes du joueur
     * **selectCard()** : sélectionne une carte et affiche le contour
     * **playSelectedCard()** : valide et joue la carte sélectionnée
     * **closeOverlay()** : ferme l'overlay avec animation fade

2. **Modification de `GameBoardView`**
   - Remplacement de `actionHandPanel` (FlowLayout avec 8 CardButtons) par :
     * `deckActionButton` : un seul bouton affichant le premier type de carte action
     * `actionDeckCountLabel` : étiquette affichant le nombre total de cartes action
   - Placement : 4e ligne de la grille (entre les cartes main et les pioches)
   - Nouvelles méthodes :
     * **openActionCardOverlay()** : ouvre l'overlay avec la main d'action
     * **closeActionCardOverlay()** : ferme l'overlay
     * **getSelectedActionCardIndex()** : récupère l'index de la carte sélectionnée
     * **setActionCardSelectionCallback()** : définit le callback de validation

3. **Layout du Plateau**
   ```
   Ligne 1-3 : Grille 3x4 des cartes normales
   Ligne 4   : Deck Action Button + Label compteur (remplace actionHandPanel)
   Droite    : Pioches et Défausses (normales et actions)
   ```

4. **Intégration Overlay**
   - L'overlay est ajouté au `JLayeredPane` en couche `PALETTE_LAYER`
   - Au clic sur le deck button → `openActionCardOverlay()` affiche toutes les cartes
   - L'utilisateur sélectionne une carte (contour vert) et valide
   - Le callback exécute l'action via `HumanTurnController`

5. **Avantages de cette approche**
   - Plus compact : 1 bouton au lieu de 8
   - Pas de décalage des cartes de main
   - Interface modale et claire
   - Sélection explicite avec validation
   - Overlay réutilisable pour d'autres contextes

#### DATA :



* **SendAPI** : Classe qui regroupes des methodes static pour envoyer des donnees a l'API
    * Variables gloabales : **urlAPI (String)**, **client (HttpClient)**
    * Methodes : **WinGameSolo()**, **getStats()**

* **SupabaseAuth** : Classe qui gere l'authentification du joueur a la base de donnees
    * Variables gloabales : **SUPABASE_URL (String)**, **SUPABASE_PUBLIC_KEY (String)**
    * Methodes : **connectPlayerByEmail()**, **connectPlayerByUsername()**, **createAccount()**,**getIUD()**, **getAccesToken()**

## UTILS

Classes utilitaires

* **ImageCache** : Cache pour les images du jeu
    * Attribut : cache (Map<String, BufferedImage>)
    * Méthode : **get(String path)**, **preload(String... paths)**, **clear()**
