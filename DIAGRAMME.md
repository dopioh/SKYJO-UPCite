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
        * Attribut : id, username, rank_id, elo, xp, money, skins (ArrayList), selectedSkinPath
        * Méthode : **getID()**, **getRankId()**, **getElo()**, **getXp()**, **getMoney()**, **getListSkinPlayer()**, **getSelectedSkinPath()**, **getUsername()**

* **Bot** :
    * `BotModel`: Représente un bot (héritage de bot)
        * Attribut : meme que PlayerModel + strategy (BotStrategyInterface)
        * Méthode : meme que PlayerModel + **getStrategy()**
    * `BotHandModel`: Grille 4x3 de cartes du bot
        * Super-classe : **PlayerHandModel**
        * Méthodess supplémentaires : **revealRandomCard()**, **sameCardInHandPosition()**, **tradeInHand()**, **isAcceptableCard()**
        * **countVisibleCardsInColumn()**
    * `BotStrategyInterface`: Interface définissant les stratégies de bot
        * Méthode : **TakeFromDiscard()**, **ExchangeDrawCard()**, **CardToExchange()**, **getSeuil()**, **shouldPlayAction()**
    * `BotTerminalModel`: Stratégie de bot basique (implémente BotStrategyInterface)
        * Stratégie par défaut pour les bots avec seuil fixe
        * Méthode : **TakeFromDiscard()**, **ExchangeDrawCard()**, **CardToExchange()**, **shouldPlayAction()**
    * `BotRow`: Stratégie de bot optimisant les lignes (implémente BotStrategyInterface)
        * Se concentre sur l'élimination de lignes complètes
        * Méthode : **TakeFromDiscard()**, **ExchangeDrawCard()**, **CardToExchange()**, **shouldPlayAction()**
    * `BotLow`: Stratégie de bot favorisant les cartes basses (implémente BotStrategyInterface)
        * Privilégie les cartes de faible valeur
        * Méthode : **TakeFromDiscard()**, **ExchangeDrawCard()**, **CardToExchange()**, **shouldPlayAction()**
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
            * **shouldPlayAction()** - Décide si le bot doit jouer une action (50% de chance)
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
        * Attribut : shopButton, playButton, settingsButton, accountButton, accountStatsOverlay
        * Méthode : **getShopButton()**, **getPlayButton()**, **getSettingsButton()**, **getAccountButton()**, **openAccountStats()**, **isAccountStatsVisible()**
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
    * `AccountStatsOverlayPanel`: Overlay des stats du compte (menu principal)
        * Attribut : closeButton, fonts (title/label/value/info), constantes couleurs
        * Méthodes : **openStats()**, **isOpen()**, **drawOverlayBackground()**, **drawCard()**, **drawTitle()**, **drawStats()**, **drawNoAccountMessage()**

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
    * **clicShop()**, **clicSettings()**, **clicAccount()** (ouvre l'overlay stats)
    * Les clics/hover sur les boutons principaux sont bloqués si l'overlay stats est visible

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
        * **Support des actions pour les bots en mode graphique** :
            * **shouldBotPlayAction()** - Vérifie si le bot doit jouer une action
            * **handleBotPlayAction()** - Gère le déroulement du jeu d'une action
            * **botReplaceCardInHand()** - REPLACE_CARD_IN_HAND : échange 2 cartes aléatoires
            * **botTradeCardWithPlayer()** - TRADE_CARD_WITH_PLAYER : échange une carte avec un autre joueur
            * **botSpyOneColumn()** - SPY_ONE_COLUMN : révèle temporairement une colonne
            * **botDrawThreeCard()** - DRAW_THREE_CARD : pioche 3 cartes et choisit d'en garder une
            * **botPlayLastActionInDefausse()** - PLAY_LAST_ACTION_IN_DEFAUSSE : rejette la dernière action
            * **botAllPlayersDropCard()** - ALL_PLAYERS_DROP_CARD : force les autres joueurs à défausser

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

#### DATA :

* **SendAPI** : Classe qui regroupes des methodes static pour envoyer des donnees a l'API
    * Variables gloabales : **urlAPI (String)**, **client (HttpClient)**
    * Methodes : **WinGameSolo()**, **buyPack()**, **applyGameResult()**, **unlockSkin()**, **selectSkin()**, **getStats()**

* **SupabaseAuth** : Classe qui gere l'authentification du joueur a la base de donnees
    * Variables gloabales : **SUPABASE_URL (String)**, **SUPABASE_PUBLIC_KEY (String)**
    * Methodes : **connectPlayerByEmail()**, **connectPlayerByUsername()**, **createAccount()**,**getIUD()**, **getAccesToken()**
    * **Intégration Erreurs** : Tous les appels peuplent `ApiErrorManager` en cas d'erreur HTTP (401, 403, 500, etc.)

### Système de Gestion des Erreurs API

**Architecture** : Système centralisé de capture, mapping et affichage des erreurs API/réseau

* **ApiErrorManager** : Gestionnaire centralisé des erreurs (classe statique)
    * Attribut (static volatile) :
        * `lastUserMessage` (String) - Message utilisateur français actuel
        * `lastStatusCode` (int) - Code HTTP ou code erreur réseau
        * `lastContext` (String) - Contexte de l'erreur (ex: "connexion", "boutique")
    * Méthode :
        * **mapHttpStatusToMessage()** - Traduit code HTTP → message français Skyjo-style :
            * 400 → "Mauvaise requête, vérifies tes données"
            * 401 → "Mauvaise combinaison identifiant/mot de passe. Essaie encore."
            * 403 → "Tu n'as pas accès à cette action"
            * 404 → "La ressource demandée n'existe pas"
            * 409 → "Cet email ou ce pseudo est déjà pris."
            * 422 → "Les données envoyées sont invalides"
            * 429 → "Trop de requêtes. Attends un peu."
            * 500-504 → "Erreur serveur. L'équipe Skyjo est là-dessus!"
        * **setHttpError(String context, int statusCode, String responseBody)** - Enregistre une erreur HTTP
        * **setNetworkError(String context, Exception exception)** - Enregistre une erreur réseau (ConnectException, HttpTimeoutException)
        * **getLastUserMessageOrDefault()** - Retourne le message utilisateur ou un message par défaut
        * **clear()** - Efface le dernier message d'erreur
        * **getLastStatusCode()**, **getLastContext()** - Accesseurs pour debugging

* **Intégration SupabaseAuth** :
    * Chaque méthode (`connectPlayerByEmail`, `connectPlayerByUsername`, `createAccount`, `getEmailFromUsername`) :
        * Appelle `ApiErrorManager.clear()` au début
        * Sur réponse non-200 : appelle `ApiErrorManager.setHttpError(context, statusCode, responseBody)`
        * Sur exception réseau : appelle `ApiErrorManager.setNetworkError(context, exception)`
    * Codes gérés : 401 (mauvaise auth), 403 (accès refusé), 404 (utilisateur non trouvé), 409 (email/pseudo déjà pris), 500 (erreur serveur)

* **Intégration SendAPI** :
    * Méthode helper `isHttpSuccess(HttpResponse<String> response, String context)` :
        * Retourne true si statusCode == 200
        * Sinon : `ApiErrorManager.setHttpError()` et retourne false
    * Tous les endpoints utilisent ce pattern :
        * `buyPack()` - Achat d'un paquet en boutique
        * `applyGameResult()` - Synchronisation résultats partie
        * `unlockSkin()` - Déverrouiller un skin
        * `selectSkin()` - Équiper un skin
        * `getStats()` - Récupération stats joueur
    * Codes gérés : 400, 401, 403, 404, 409, 422, 500

* **ErrorDialog** : Composant Swing personnalisé pour affichage Skyjo
    * Construction graphique :
        * Background : Semi-transparent noir (0,0,0,180)
        * Carré : Beige Skyjo (248,243,226), bordure marron (54,45,34), coins arrondis 20px
        * Fonts : PatrickHandSC bold pour titre (28f), GoogleSans regular pour message (16f)
        * Bouton : DoodleButton vert avec états hover
    * Signature : `ErrorDialog(Frame owner, String title, String message)`
    * Méthode statique : `ErrorDialog.showError(Frame owner, String title, String message)`
    * Utilisé dans :
        * `ConnexionController` → sendLogin(), sendCreate() (titres: "Connexion", "Inscription")
        * `ShopController` → clicAcheter() (titre: "Boutique")
        * `GameBoardController` → rewardEndGame() (titre: "Resultat de partie")

* **CaseOpeningOverlayPanel** : Limitation statique
    * Méthodes statiques `unlockSkin()` et `setSelectedSkinPath()` ne peuvent pas accéder au contexte instance
    * Solution : Utilisation de JOptionPane (système) au lieu de ErrorDialog personnalisé
    * À améliorer : Refactorisation en méthodes instances si styling critique pour cette section

**Flux d'erreurs global** :
1. API call dans SupabaseAuth/SendAPI
2. Si erreur : population ApiErrorManager (via setHttpError/setNetworkError)
3. Controller réceptionne réponse nulle/ErrorDialog trigger
4. ErrorDialog.showError() affiche message utilisateur français + design Skyjo
5. ApiErrorManager.clear() sur prochaine tentative (ou au début de méthode)

**Codes d'erreur gérés** : 400, 401, 403, 404, 409, 422, 429, 500, 502, 503, 504, ConnectException, HttpTimeoutException

## UTILS

Classes utilitaires

* **ImageCache** : Cache pour les images du jeu
    * Attribut : cache (Map<String, BufferedImage>)
    * Méthode : **get(String path)**, **preload(String... paths)**, **clear()**
