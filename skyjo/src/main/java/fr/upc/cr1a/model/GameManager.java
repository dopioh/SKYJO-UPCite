package fr.upc.cr1a.model;

import java.util.List;
import java.util.HashMap;
//import javax.swing.Timer;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.controller.network.connexion.GameClient;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.interfaces.BotStrategyInterface;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameBoardView;

import java.util.ArrayList;

/**
 * Classe principal pour initialiser une partie (et pas le menu !)
 * Et relier toutes vos features in game au jeu 
 * 
 * Faites vos propres fichiers pour vos features !!
 * Qu'on se retrouve pas encore avec 2k lignes !! // oui patronne 
 */
public class GameManager {

    private ArrayList<PlayerModel> players;
    private PiocheModel deck;
    private DefausseModel discardPile;
    private PiocheActionModel actionDeck;
    private DefausseActionModel discardActionPile;
    private int currentPlayerIndex;
    private int firstPlayerToPlayIndex;  // joueur qui a commencé la manche
    private int lastRoundPlayer = -1;  // joueur qui a déclenché le LAST_ROUND
    private GameState gameState;
    private TurnPhase turnPhase;
    private CardModel currentDrawCard;
    private int extraTurnsRemaining = 0;  // nombre de tours supplémentaires pour PLAY_TWO_TURN

    private GameBoardModel gameBoardModel;
    private int initRevealCount = 0;
    private int roundNumber = 1; // numero de la manche actuelle
    private boolean initRevealStarted = false;
    private boolean initRevealJustFinished = false;

    private boolean gameWithAction = false; // true si on utilise les actions
    private GameClient onlineClient; // si on est en ligne alors on en associe un, sinon ça reste a null
    
    private HashMap<PlayerModel, Integer> afkCounts = new HashMap<>();
    private HashMap<PlayerModel, Boolean> autoControlledPlayers = new HashMap<>();

    /*
    *classe local enum pour gérer les états du jeu
    */
    public enum GameState {
        WAITING_FOR_PLAYERS,    // attende des joueurs
        IS_PLAYING,             // en train de jouer
        LAST_ROUND,             // dernier tour (un joueur a révélé toutes ses cartes)
        ROUND_ENDED,            // manche terminée
        GAME_ENDED              // jeu terminé (un joueur atteint 100 points)
    }

    public enum TurnPhase {
        INIT_REVEAL,
        DRAW_DECISION,              // joueur doit choisir ; pioche ou défausse
        CARD_DRAW_FROM_DECK,        // carte piochée du deck ; peut échanger ou défausser
        CARD_DISCARD_FROM_DRAW,
        CARD_TAKE_FROM_DISCARD,     // carte prise de la défausse ; doit échanger
        TURN_ENDED                  // tour terminé
    }

    /**
     * Constructeur qui doit etre appeler pour lancer une partie
     * Dans la version swing
    */
    public GameManager(Frame frame, boolean action){
        // On l'utilise quand ce gamemanager ??
        this.gameWithAction = action;
        if (this.gameWithAction) {
            actionDeck = new PiocheActionModel();
            discardActionPile = new DefausseActionModel();
            ActionCardModel initActionCard = (ActionCardModel) actionDeck.getTopCard();
            actionDeck.removeCardAtTop();
            discardActionPile.addCardAtTop(initActionCard);
            ensureFaceUp(discardActionPile.getTopCard());
        }

        System.out.println("GameManager Called");

        this.players = new ArrayList<>(); // il faudra initialiser les joueurs a un moment
        for (int i = 1 ; i <= 3 ; i++){   // juste pour les tests avec tous les joueurs
            players.add(new PlayerModel(new PlayerData("Joueur " + i)));
        }

        // Distribution depuis la pioche, cartes face cachée
        this.deck = new PiocheModel(this.gameWithAction);
        for (PlayerModel player : players) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 4; col++) {
                    CardModel card = deck.getTopCard();
                    deck.removeCardAtTop();
                    player.getHand().placeCard(row, col, card);
                }
            }
        }

        this.discardPile = new DefausseModel();
        CardModel firstDiscard = deck.getTopCard();
        deck.removeCardAtTop();
        if (firstDiscard.isFaceDown()) firstDiscard.returnCard();
        discardPile.addCardAtTop(firstDiscard);

        this.initRevealStarted = false;
        this.initRevealJustFinished = false;
        this.gameBoardModel = new GameBoardModel(players, frame, this);
        GameBoardView view = this.gameBoardModel.getGameBoardView();
        view.updateCardDisplay(discardPile.getTopCard(), deck.stackSize() > 0);
        frame.add(view, "GAME");
        frame.showPanel("GAME");
    }

    public GameManager(Frame frame, List<PlayerData> playersData, boolean action){
        this.gameWithAction = action;
        if (this.gameWithAction) {
            actionDeck = new PiocheActionModel();
            discardActionPile = new DefausseActionModel();
            ActionCardModel initActionCard = (ActionCardModel) actionDeck.getTopCard();
            actionDeck.removeCardAtTop();
            discardActionPile.addCardAtTop(initActionCard);
            ensureFaceUp(discardActionPile.getTopCard());
        }
        //Creation des joueurs 
        this.players = new ArrayList<>();
        ArrayList<BotCounting> botCountingList = new ArrayList<>();
        
        for (PlayerData player : playersData) {
            //créer un BotModel si c'est un bot, sinon créer un player
            if (player.isBot()) {
                //strat du bot appelé
                BotStrategyInterface strategy;
                String botType = player.getBotType();
                
                if ("botrow".equals(botType)) {
                    strategy = new BotRow(); // BotRow
                } else if ("botlow".equals(botType)) {
                    strategy = new BotLow(3); // BotLow
                } else if ("botcounting".equals(botType)) {
                    strategy = new BotCounting(); // BotCounting
                    botCountingList.add((BotCounting) strategy);
                } else {
                    strategy = new BotTerminalModel(); // défaut
                }
                
                players.add(new BotModel(player, strategy));
            } else {
                players.add(new PlayerModel(player));
            }
        }
        
        // Définir le GameManager pour les bots BotCounting
        for (BotCounting bot : botCountingList) {
            bot.setGameManager(this);
        }

        this.currentPlayerIndex = 0;
        this.firstPlayerToPlayIndex = 0; // le joueur qui commence la manche
        this.lastRoundPlayer = -1;
        this.deck = new PiocheModel(this.gameWithAction);
        this.discardPile = new DefausseModel();
        this.currentDrawCard = null;
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.turnPhase = TurnPhase.DRAW_DECISION;

        //Distribution des cartes
        for (PlayerModel player : players) {
            player.resetHand();
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 4; col++) {
                    CardModel card = deck.getTopCard();
                    deck.removeCardAtTop();
                    player.getHand().placeCard(row, col, card);
                }
            }
        }

        CardModel firstDiscard = deck.getTopCard();
        deck.removeCardAtTop();
        ensureFaceUp(firstDiscard);
        discardPile.addCardAtTop(firstDiscard);

        this.turnPhase = TurnPhase.INIT_REVEAL;
        this.currentPlayerIndex = 0;    

        this.gameState = GameState.IS_PLAYING;

        this.gameBoardModel = new GameBoardModel(players, frame, this);
        GameBoardView view = this.gameBoardModel.getGameBoardView();
        view.showPlayerCards(players.get(currentPlayerIndex));
        view.updateCardDisplay(
            discardPile.isEmpty() ? null : discardPile.getTopCard(),
            deck.stackSize() > 0
        );
        view.setMancheLabel("Pré-Manche");
        frame.add(view, "GAME");
        frame.showPanel("GAME");
    }

    public GameManager(){ // Jle laisse pour pas casser vos tests
        
    }

    /**
     * constructeur avec les noms des joueurs
     * permet de d'initialiser les joueurs avec leurs noms
     * @param playersData
     */
    public GameManager(List<PlayerData> playersData, boolean action){
        //initialiser les joueurs
        this.players = new ArrayList<>();
        this.gameWithAction = action;
        ArrayList<BotCounting> botCountingList = new ArrayList<>();
        
        for (PlayerData player : playersData) {
            //créer un BotModel si c'est un bot, sinon créer un playdéfatuter
            if (player.isBot()) {
                //strat du bot appelé
                BotStrategyInterface strategy;
                String botType = player.getBotType();
                
                if ("botrow".equals(botType)) {
                    strategy = new BotRow(); // BotRow
                } else if ("botlow".equals(botType)) {
                    strategy = new BotLow(3); // BotLow 
                } else if ("botcounting".equals(botType)) {
                    strategy = new BotCounting(); // BotCounting
                    botCountingList.add((BotCounting) strategy);
                } else {
                    strategy = new BotTerminalModel(); // défaut
                }
                
                players.add(new BotModel(player, strategy));
            } else {
                players.add(new PlayerModel(player));
            }
        }
        
        // Définir le GameManager pour les bots BotCounting
        for (BotCounting bot : botCountingList) {
            bot.setGameManager(this);
        }
        
        currentPlayerIndex = 0;
        firstPlayerToPlayIndex = 0; // le joueur qui commence la manche
        lastRoundPlayer = -1;
        deck = new PiocheModel(this.gameWithAction);
        discardPile = new DefausseModel();
        initRevealStarted = false;
        initRevealJustFinished = false;

        if (this.gameWithAction) {
            actionDeck = new PiocheActionModel();
            discardActionPile = new DefausseActionModel();
            ActionCardModel initActionCard = (ActionCardModel) actionDeck.getTopCard();
            actionDeck.removeCardAtTop();
            ensureFaceUp(discardActionPile.getTopCard());
            discardActionPile.addCardAtTop(initActionCard);
        }

        currentDrawCard = null;
        gameState = GameState.WAITING_FOR_PLAYERS;
        turnPhase = TurnPhase.DRAW_DECISION;
        initRevealStarted = false;
        initRevealJustFinished = false;
    }
    
    //getters
    public GameState getGameState(){
        return gameState;
    }

    public TurnPhase getTurnPhase(){
        return turnPhase;
    }

    public List<PlayerModel> getPlayers(){
        return players;
    }

    public DefausseModel getDiscardPile(){
        return discardPile;
    }

    public PiocheModel getDeck(){
        return deck;
    }

    public DefausseActionModel getDiscardActionPile(){
        return discardActionPile;
    }

    public PiocheActionModel getActionDeck(){
        return actionDeck;
    }
    
    public CardModel getCurrentDrawCard(){
        return currentDrawCard;
    }

    public static boolean isStarCard(CardModel card) {
        return (card instanceof NumberCardModel numberCard && numberCard.isStar());
    }

    public boolean withActionCard() {
        return this.gameWithAction;
    } 
    
    /**
    *Initialisation de la game 
    */
   public void initializeGame(){
        //creer un deck deja melange
        //deck = new PiocheModel(); // on a deja un deck initalisé dans le constructeur !
        
        firstPlayerToPlayIndex = currentPlayerIndex;

        for (PlayerModel player : players) {
            player.resetHand();
            //distribue les cartes
            for (int i = 0; i < 3; i++){
                for (int j = 0; j < 4; j++){
                    CardModel card = deck.getTopCard();
                    deck.removeCardAtTop();
                    player.getHand().placeCard(i, j, card);
                }
            }
        }
        
        //mettre une carte dans la défausse
        CardModel firstDiscard = deck.getTopCard();
        deck.removeCardAtTop();
        discardPile.addCardAtTop(firstDiscard);
        
        // Changer l'état du jeu
        gameState = GameState.IS_PLAYING;
    }
    
    /*
    *démarre le tour du joueur actuelle
    */
    public void startTurn(){
        currentDrawCard = null;
        turnPhase = TurnPhase.DRAW_DECISION;
    }
    
    /*
    *savoir quel est le joueur actuelle qui joue
    *@return le joueur actuelle
    */
    public PlayerModel getCurrentPlayer(){
        return players.get(currentPlayerIndex);
    }

    /**
     * 
     * @return
     */
    public int getCurrentPlayerIndex(){
        return this.currentPlayerIndex;
    }

    /**
     * Retourne le joueur qui a déclenché le dernier round
     */
    public PlayerModel getLastRoundPlayer(){
        return (lastRoundPlayer >= 0) ? players.get(lastRoundPlayer) : null;
    }

    /*
    *passe au joueur suivant
    */
    public void nextPlayer() {
        currentPlayerIndex++;
        //revenir au premier si le dernier joueur a joué
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
        //regarde si on est sur le dernier tour et si on est revenu au joueur qui a déclenché la fin
        if (gameState == GameState.LAST_ROUND && currentPlayerIndex == lastRoundPlayer) {
            endRound();
            return;
        }

        // si le joueur a des tours supplémentaires (mais pas pour le joueur qui a déclenché LAST_ROUND)
        if (extraTurnsRemaining > 0) {
            extraTurnsRemaining--;
            return;
        }
    }

    public void grantExtraTurn() {
        // PLAY_TWO_TURN = 2 tours au total pour le joueur courant, donc 1 tour supplémentaire
        this.extraTurnsRemaining += 1;
    }

    /*
    *pioche une carte
    *@return la carte piochée
    */
    public CardModel drawDeck(){
        //regarde si la pioche est vide
        if (deck.stackSize() == 0) {
            deck.DefausseInPioche(discardPile);
        }
        
        currentDrawCard = deck.getTopCard();
        deck.removeCardAtTop();
        turnPhase = TurnPhase.CARD_DRAW_FROM_DECK;
        return currentDrawCard;
    }

    /**
    *pioche une carte dans la pioche action
    *@return la carte piochée
    */
    public CardModel drawActionDeck(){
        //regarde si la pioche est vide
        if (actionDeck.stackSize() == 0) {
            actionDeck.DefausseInPioche(discardActionPile);
        }
        
        currentDrawCard = actionDeck.getTopCard();
        actionDeck.removeCardAtTop();
        turnPhase = TurnPhase.CARD_DRAW_FROM_DECK;
        return currentDrawCard;
    }

    /**
     * pioche une action recompense automatiquement apres reveal d'une etoile
     * sans modif les etats ingame
     */
    public ActionCardModel drawActionRewardCard(){
        if (actionDeck == null) {
            return null;
        }
        if (actionDeck.stackSize() == 0) {
            actionDeck.DefausseInPioche(discardActionPile);
        }
        if (actionDeck.stackSize() == 0) {
            return null;
        }
        CardModel card = actionDeck.getTopCard();
        actionDeck.removeCardAtTop();
        ensureFaceUp(card);
        if (card instanceof ActionCardModel actionCard) {
            return actionCard;
        }
        return null;
    }

    /**
     * applique l'effet etoile sur un joueur : gagne une carte action
     * @param player joueur qui gagne l'action
     * @return la carte action gagnée ou null si indisponible
     */
    public ActionCardModel grantActionForStar(PlayerModel player) {
        if (player == null || !gameWithAction) {
            return null;
        }
        ActionCardModel actionCard = drawActionRewardCard();
        if (actionCard == null) {
            return null;
        }
        player.getActionHand().add(actionCard.getType());
        return actionCard;
    }

    /*
    *prendre la carte de la défausse
    *@return la carte prise
    */
    public CardModel takeDiscard(){
        currentDrawCard = discardPile.getTopCard();
        discardPile.removeCardAtTop();
        turnPhase = TurnPhase.CARD_TAKE_FROM_DISCARD;
        if (currentDrawCard.isFaceDown()) currentDrawCard.returnCard();
        return currentDrawCard;
    }

    /**
    *prendre la carte de la défausse action
    *@return la carte prise
    */
    public CardModel takeActionDiscard(){
        currentDrawCard = discardActionPile.getTopCard();
        discardActionPile.removeCardAtTop();
        turnPhase = TurnPhase.CARD_TAKE_FROM_DISCARD;
        if (currentDrawCard.isFaceDown()) currentDrawCard.returnCard();
        return currentDrawCard;
    }
    
    /*
    *échanger la carte piochée avec une carte de la main
    */
    public void exchangeCardInHand(int col, int row){
        PlayerModel currentPlayer = getCurrentPlayer();
        PlayerHandModel hand = currentPlayer.getHand();
        CardModel cardInHand = hand.getCard(row, col);
        
        //echange de cartes
        ensureFaceUp(cardInHand);
        hand.replaceCard(row, col, currentDrawCard);
        discardPile.addCardAtTop(cardInHand);
        //montrer la nouvelle carte
        if(currentDrawCard.isFaceDown()) currentDrawCard.returnCard();
        if (currentDrawCard.isFaceDown()) {
            currentDrawCard.returnCard();
        }
        currentDrawCard = null;
        
        //vérifier les colonnes/lignes identiques
        GameRuleModel.removeIdenticalColumns(hand, discardPile, currentPlayer);
        GameRuleModel.removeIdenticalRows(hand, discardPile, currentPlayer);

        //vérifier si fin de manche (ne déclencher qu'une seule fois, un seul joueur peut déclencher)
        if (gameState != GameState.LAST_ROUND && lastRoundPlayer == -1 && GameRuleModel.checkEndOfRound(players)) {
            gameState = GameState.LAST_ROUND;
            lastRoundPlayer = currentPlayerIndex;  // stocker le joueur qui déclenche le dernier tour
        }
        turnPhase = TurnPhase.TURN_ENDED;
    }
    
    /*
    *défausser la carte piochée // en theorie ça marche aussi pour les cartes action
    */
    public void discardDrawCard(){
        ensureFaceUp(currentDrawCard);
        discardPile.addCardAtTop(currentDrawCard);
        currentDrawCard = null;
        turnPhase = TurnPhase.CARD_DISCARD_FROM_DRAW;
    }
    
    /*
    *montre la carte après avoir défaussé
    */
    public void revealCardInHand(int col, int row){
        PlayerHandModel hand = getCurrentPlayer().getHand();
        CardModel card = hand.getCard(row, col);
        if (card == null || !card.isFaceDown()) {
            return;
        }
        hand.revealCard(row, col);

        // check
        GameRuleModel.removeIdenticalColumns(getCurrentPlayer().getHand(), discardPile, getCurrentPlayer());
        GameRuleModel.removeIdenticalRows(getCurrentPlayer().getHand(), discardPile, getCurrentPlayer());

        //vérifier si fin de manche (ne déclencher qu'une seule fois, un seul joueur peut déclencher)
        if (gameState != GameState.LAST_ROUND && lastRoundPlayer == -1 && GameRuleModel.checkEndOfRound(players)) {
            gameState = GameState.LAST_ROUND;
            lastRoundPlayer = currentPlayerIndex;  // stocker le joueur qui déclenche le dernier tour
        }
        turnPhase = TurnPhase.TURN_ENDED;
    }


    
    /*
    *fonction qui gère la fin de la manche
    *regarde si après chaque manche on rejour ou bien on termine la partie
    */
    private void endRound() {
        //montrer les cartes de tous les joueurs
        for (PlayerModel player : players) {
            PlayerHandModel hand = player.getHand();
            for (int row = 0; row < hand.getRows(); row++) {
                for (int col = 0; col < hand.getColumns(); col++) {
                    CardModel card = hand.getCard(row, col);
                    if (card != null && card.isFaceDown()) {
                        card.returnCard();
                    }
                }
            }
        }
        
        //calculer le score de chacun et trouver le joueur avec le score le plus bas
        PlayerModel lowestPlayer = GameRuleModel.findLowestScorePlayer(players);
        
        //cherche le joueur qui a déclenché la fin de la manche
        PlayerModel roundEndPlayer = (lastRoundPlayer >= 0) ? players.get(lastRoundPlayer) : null;
        
        //Applique les scores selon les règles Skyjo
        GameRuleModel.applyRoundScores(players, roundEndPlayer, lowestPlayer);

        //regarde si quelqu'un a 100 pts
        boolean end = GameRuleModel.isGameOver(players);

        
        //finir la partie ou commencer une nouvelle manche
        if (end) {
            gameState = GameState.GAME_ENDED;
            
            //enregistrer la victoire
            //try {
            //    BotStatsManager.recordVictory(winner, players);
            //} catch (Exception e) {
            //    System.out.println("[ERREUR] Exception lors de recordVictory: " + e.getMessage());
            //    e.printStackTrace();
            //}
        }
        else {
            gameState = GameState.ROUND_ENDED;

        }
    }

    /**
     * Recommence une nouvelle manche, le joueur avec la plus haute somme de cartes visibles après INIT_REVEAL commence
     */
    public void startNewGame() {
        roundNumber++;
        currentPlayerIndex = 0;
        currentDrawCard = null;
        lastRoundPlayer = -1;  // réinitialiser le déclencheur du dernier tour
        extraTurnsRemaining = 0;  // réinitialiser les tours supplémentaires
        discardPile = new DefausseModel();

        //vider les cartes actions de tous les joueurs
        for (PlayerModel player : players) {
            player.getActionHand().clear();
        }

        //vider les cartes actions des decks
        if (this.gameWithAction) {
            actionDeck = new PiocheActionModel();
            discardActionPile = new DefausseActionModel();
            ActionCardModel initActionCard = (ActionCardModel) actionDeck.getTopCard();
            actionDeck.removeCardAtTop();
            discardActionPile.addCardAtTop(initActionCard);
            ensureFaceUp(discardActionPile.getTopCard());
        }
        
        initializeGame();
        ensureFaceUp(discardPile.getTopCard());
        turnPhase = TurnPhase.INIT_REVEAL;
        initRevealCount = 0;
        initRevealStarted = false; //reset
        initRevealJustFinished = false;
        firstPlayerToPlayIndex = 0;
        
        //maj du deck et de la défausse
        if (gameBoardModel != null && gameBoardModel.getGameBoardView() != null) {
            GameBoardView view = gameBoardModel.getGameBoardView();
            view.updateCardDisplay(discardPile.getTopCard(), deck.stackSize() > 0);
            if (gameWithAction) {
                view.updateActionCardDisplay(discardActionPile.getTopCard(), actionDeck.stackSize() > 0);
            }
        }
    }

    /**
     * Détermine le joueur qui commence la partie en regardant ses cartes visibles
     */
    public void firstPlayer() {
        currentPlayerIndex = GameRuleModel.getFirstPlayerIndex(players);
    }

    // Etre sur que la carte est viisble
    private void ensureFaceUp(CardModel card) {
        if (card != null && card.isFaceDown()) {
            card.returnCard();
        }
    }
    
    public int getInitRevealCount(){
        return initRevealCount;
    }
    
    public boolean isInitRevealJustFinished() {
        return initRevealJustFinished;
    }
    
    public void setInitRevealJustFinished(boolean value) {
        this.initRevealJustFinished = value;
    }

    public GameClient getNetwork(){
        return this.onlineClient;
    }

    public void setNetwork(GameClient gameClient){
        this.onlineClient = gameClient;
    }

    public void setCurrentDrawCard(CardModel card) {
        this.currentDrawCard = card;
    }

    public int getRoundNumber(){
        return roundNumber;
    }

    public boolean revealInitCard(int col, int row){
        if (turnPhase != TurnPhase.INIT_REVEAL) return false;
        CardModel card = getCurrentPlayer().getHand().getCard(row, col);
        if (card == null || !card.isFaceDown()) return false;
        getCurrentPlayer().getHand().revealCard(row, col);
        
        //regarde et supprimer les colonnes/lignes identiques
        GameRuleModel.removeIdenticalColumns(getCurrentPlayer().getHand(), discardPile, getCurrentPlayer());
        GameRuleModel.removeIdenticalRows(getCurrentPlayer().getHand(), discardPile, getCurrentPlayer());

        // vérifie si après suppression de colonnes/lignes, toutes les cartes sont révélées (ne déclencher qu'une seule fois, un seul joueur peut déclencher)
        if (gameState != GameState.LAST_ROUND && lastRoundPlayer == -1 && GameRuleModel.checkEndOfRound(players)) {
            gameState = GameState.LAST_ROUND;
            lastRoundPlayer = currentPlayerIndex;
        }

        initRevealCount++;
        if (initRevealCount >= 2) {
            initRevealCount = 0;
            
            //marquer l'init
            if (!initRevealStarted) {
                initRevealStarted = true;
                firstPlayerToPlayIndex = currentPlayerIndex;
            }
            
            currentPlayerIndex++;
            if (currentPlayerIndex >= players.size()) {
                currentPlayerIndex = GameRuleModel.getFirstPlayerIndex(players);
                firstPlayerToPlayIndex = currentPlayerIndex;
                startTurn(); // passe à DRAW_DECISION
            }
        }
        return true;
    }

    public void setTurnPhase(TurnPhase phase){
        this.turnPhase = phase;
    }

    public void stopGame() {
        if (gameBoardModel != null) {
            GameBoardController controller = gameBoardModel.getGameBoardController();
            if (controller != null) {
                controller.stopGame();
            }
        }
    }   

    public GameBoardModel getGameBoardModel(){
        return this.gameBoardModel;
    }

    /**
     * Methode exclusive au multijoueur (Cote Client)
     * Force la revelation d'une carte sur le plateau local selon la request du serveur
     */
    public void forceRevealCardFromNetwork(String playerId, int col, int row, int cardValue) {
        
        PlayerModel targetPlayer = null;
        for (PlayerModel p : players) {
            if (p.getPlayerData().getID().equals(playerId)) {
                targetPlayer = p;
                break;
            }
        }
        
        if (targetPlayer != null) {

            NumberCardModel vraieCarte = new NumberCardModel(false, cardValue);
            targetPlayer.getHand().replaceCard(row, col, vraieCarte);
            
            if (gameBoardModel != null && gameBoardModel.getGameBoardView() != null) {
                gameBoardModel.getGameBoardView().showPlayerCards(getCurrentPlayer());
            }
        }
    }

    /**
     * Force le changement de tour d'apres une request Host
     */
    public void forceTurnFromNetwork(String playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getPlayerData().getID().equals(playerId)) {
                
                this.currentPlayerIndex = i;
                
                this.turnPhase = TurnPhase.DRAW_DECISION;
                
                if (gameBoardModel != null && gameBoardModel.getGameBoardController() != null) {
                    gameBoardModel.getGameBoardController().refreshGuide(); // pour afficher les bonnes indications pour DRAW DECISION
                    

                    if (gameBoardModel.getGameBoardView() != null) {
                        gameBoardModel.getGameBoardView().showPlayerTransition(players.get(i), () -> {
                            // pour lancer l'animation du changement de round
                            // besoin d'attendre l'implementation de l'interface graphique
                        });
                    }
                }
                break;
            }
        }
    }

    public int getAfkCount(PlayerModel player) {
        return afkCounts.getOrDefault(player, 0);
    }

    public void registerAfkTurn(PlayerModel player) {
        afkCounts.put(player, getAfkCount(player) + 1);
    }

    public void resetAfkCount(PlayerModel player) {
        afkCounts.put(player, 0);
    }

    public boolean isAutoControlled(PlayerModel player) {
        return autoControlledPlayers.getOrDefault(player, false);
    }

    public void setAutoControlled(PlayerModel player, boolean value) {
        autoControlledPlayers.put(player, value);
    }
}
