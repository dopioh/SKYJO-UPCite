package fr.upc.cr1a.model;

import java.util.List;

import fr.upc.cr1a.model.interfaces.BotStrategyInterface;
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
    private GameState gameState;
    private TurnPhase turnPhase;
    private CardModel currentDrawCard;

    private GameBoardModel gameBoardModel;
    private int initRevealCount = 0;
    
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
        CARD_TAKE_FROM_DISCARD,     // carte prise de la défausse ; doit échanger
        TURN_ENDED                  // tour terminé
    }

    /**
     * Constructeur qui doit etre appeler pour lancer une partie
     * Dans la version swing
    */
    public GameManager(Frame frame){
        System.out.println("GameManager Called");

        this.players = new ArrayList<>(); // il faudra initialiser les joueurs a un moment
        for (int i = 1 ; i <= 3 ; i++){   // juste pour les tests avec tous les joueurs
            players.add(new PlayerModel(new PlayerData("Joueur " + i)));
        }

        // Distribution depuis la pioche, cartes face cachée
        this.deck = new PiocheModel();
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

        this.gameBoardModel = new GameBoardModel(players, frame, this);
        GameBoardView view = this.gameBoardModel.getGameBoardView();
        view.updateCardDisplay(discardPile.getTopCard(), deck.stackSize() > 0);
        frame.add(view, "GAME");
        frame.showPanel("GAME");
    }

    public GameManager(Frame frame, List<PlayerData> playersData){
        //Creation des joueurs 
        this.players = new ArrayList<>();
        for (PlayerData player : playersData) {
            //créer un BotModel si c'est un bot, sinon créer un player
            if (player.isBot()) {
                players.add(new BotModel(player));
            } else {
                players.add(new PlayerModel(player));
            }
        }

        this.currentPlayerIndex = 0;
        this.firstPlayerToPlayIndex = 0; // le joueur qui commence la manche
        this.deck = new PiocheModel();
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
        frame.add(view, "GAME");
        frame.showPanel("GAME");
    }


    public GameManager(){
       
    }

    /**
     * constructeur avec les noms des joueurs
     * permet de d'initialiser les joueurs avec leurs noms
     * @param playersData
     */
    public GameManager(List<PlayerData> playersData){
        //initialiser les joueurs
        this.players = new ArrayList<>();
        for (PlayerData player : playersData) {
            //créer un BotModel si c'est un bot, sinon créer un playdéfatuter
            if (player.isBot()) {
                //strat du bot appelé
                BotStrategyInterface strategy;
                String botType = player.getBotType();
                
                if ("botrow".equals(botType)) {
                    strategy = new BotRow(); // BotRow
                } else if ("botlow".equals(botType)) {
                    strategy = new BotHigh(8); // BotHigh)
                } else {
                    strategy = new BotTerminalModel(); // défaut
                }
                
                players.add(new BotModel(player, strategy));
            } else {
                players.add(new PlayerModel(player));
            }
        }
        
        currentPlayerIndex = 0;
        firstPlayerToPlayIndex = 0; // le joueur qui commence la manche
        deck = new PiocheModel();
        discardPile = new DefausseModel();
        actionDeck = new PiocheActionModel();
        discardActionPile = new DefausseActionModel();

        currentDrawCard = null;
        gameState = GameState.WAITING_FOR_PLAYERS;
        turnPhase = TurnPhase.DRAW_DECISION;
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
    
    /**
    *Initialisation de la game 
    */
   public void initializeGame(){
        //creer un deck deja melange
        deck = new PiocheModel();
        
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

    /*
    *passe au joueur suivant
    */
    public void nextPlayer() {
        currentPlayerIndex++;
        //revenir au premier si le dernier joueur a joué
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
        //regarde si on est sur le dernier tour et si on est revenu au premier joueur
        if (gameState == GameState.LAST_ROUND && currentPlayerIndex == firstPlayerToPlayIndex) {
            endRound();
        }
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
        // si la carte est une etoile on +1 le starsScore
        if (currentDrawCard instanceof NumberCardModel numberCard) {
            if (numberCard.isStar()) {
                currentPlayer.addStarsScore();
            }        
        }
        currentDrawCard = null;
        
        //vérifier les colonnes/lignes identiques
        GameRuleModel.removeIdenticalColumns(hand, discardPile);
        GameRuleModel.removeIdenticalRows(hand, discardPile);
        //vérifier si fin de manche
        if (GameRuleModel.checkEndOfRound(players)) {
            gameState = GameState.LAST_ROUND;
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
        turnPhase = TurnPhase.TURN_ENDED;
    }
    
    /*
    *montre la carte après avoir défaussé
    */
    public void revealCardInHand(int col, int row){
        currentDrawCard.returnCard();
        getCurrentPlayer().getHand().revealCard(row, col);

        // check
        GameRuleModel.removeIdenticalColumns(getCurrentPlayer().getHand(), discardPile);
        GameRuleModel.removeIdenticalRows(getCurrentPlayer().getHand(), discardPile);

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
        
        //cherche le joueur qui lance la fin de la manche
        PlayerModel roundEndPlayer = GameRuleModel.findRoundEndPlayer(players);
        
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
     * Si un joueur n'a pas atteint les 100 points, on peut recommencer une nouvelle manche
     */
    public void startNewGame() {
        currentPlayerIndex = firstPlayerToPlayIndex;
        
        //Le joueur avec le score le plus grand commence
        currentPlayerIndex = GameRuleModel.getBiggestScorePlayerIndex(players);
        currentDrawCard = null;
        discardPile = new DefausseModel();
        initializeGame();
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

    public boolean revealInitCard(int col, int row){
        if (turnPhase != TurnPhase.INIT_REVEAL) return false;
        CardModel card = getCurrentPlayer().getHand().getCard(row, col);
        if (card == null || !card.isFaceDown()) return false;
        getCurrentPlayer().getHand().revealCard(row, col);
        initRevealCount++;
        if (initRevealCount >= 2) {
            initRevealCount = 0;
            currentPlayerIndex++;
            gameBoardModel.getGameBoardController().nextPlayerSound();
            if (currentPlayerIndex >= players.size()) {
                currentPlayerIndex = 0;
                startTurn(); // met DRAW_DECISION
            }
        }
        return true;
    }
}
