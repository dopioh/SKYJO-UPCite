# Architecture du Projet Skyjo

Ce document montre la structure qu'on respectera au cours du projet, elle va évolué et elle doit évoluer au cours du projet.

## 1.Base

Des classes qui permette d'éxécuter le jeu.

* **GameLauncher** : Permet de lancer le jeu. Ne pas mettre de méthode a l'intérieur(elles seront pluoto dans le gameManager)
* **GameManager** : Gère tous ce qui entoure le foncitonnement du jeu
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
        * Méthode : getter et setter
    * `ActionCardModel`: Cartes spéciales (hérite de CardModel)
        * Méthode : **a faire**

* **Joueur** :
    * `PlayerModel`: Représente un joueur
        * Attribut : name, hand (PlayerHandModel), totalScore
        * Méthode : **getName()**, **getHand()**, **getTotalScore()**, **addScore()**, **resetHand()**
    * `PlayerHandModel`: Grille 4x3 de cartes du joueur
        * Attribut : grid (CardModel[][]), rows=3, columns=4
        * Méthode : **placeCard()**, **getCard()**, **isAvailable()**, **révéler cartes**, **détecter colonnes complètes**
    * `PlayerData`: Données persistantes du joueur
        * Attribut : id, rank (Rank), elo, xp, money, skins (ArrayList)
        * Méthode : **getters/setters pour toutes les données**

* **Règles et logique** :
    * `GameRuleModel`: Définit les règles du jeu
        * Constantes : **INITIAL_REVEALED_CARD=2**, **WINNING_SCORE=100**, **COLUMN_SIZE=4**, **ROWS_SIZE=3**
        * Méthode : **canTakeFromDiscard()**, **canDrawFromDeck()**, **mustExchangeCard()**
    * `ScoreCalculator`: Calcule les scores
        * Méthode : **calculateHandScore()**, **détection colonnes identiques**
    * `Rank`: Enum des rangs (IRON, BRONZE, SILVER, GOLD, PLATINUM, EMERALD, DIAMOND, MASTER, GRANDMASTER, CHALLENGER)
        * Méthode : **findRankByElo()**

* **Audio** :
    * `AudioManager`: Gère la musique et les effets sonores
        * Attribut : bgmClip (Clip), enabled (boolean), volume (int)
        * Méthode : **playBgm()**, **playSfx()**, **stopBgm()**, **setEnabled()**, **setVolume()**

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

### CONTROLLER

Gère toutes les entrées du joueur avec l'ordinateur

* **MainMenuController** : Contrôleur du menu principal
    * Attribut : frame (Frame), view (MainMenuPanel), audio (AudioManager)
    * Méthode : **ActionListener pour playButton, settingsButton, shopButton**

* **SettingsOverlayController** : Contrôleur de l'overlay paramètres
    * Attribut : frame (Frame), audioManager (AudioManager)
    * Méthode : **ActionListener pour backButton, tabJeu, tabAudio, tabAutres, fullscreenButton, soundButton, volumeButtons**

## UTILS

Classes utilitaires

* **ImageCache** : Cache pour les images du jeu
    * Attribut : cache (Map<String, BufferedImage>)
    * Méthode : **get(String path)**, **preload(String... paths)**, **clear()**
