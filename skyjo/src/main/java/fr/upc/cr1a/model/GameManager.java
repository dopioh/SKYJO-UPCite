package fr.upc.cr1a.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe principal pour initialiser une partie (et pas le menu !)
 * Et relier toutes vos features in game au jeu 
 * 
 * Faites vos propres fichiers pour vos features !!
 * Qu'on se retrouve pas encore avec 2k lignes !! // oui patronne 
 */
public class GameManager {

    private List<PlayerModel> players;
    private PiocheModel deck;
    private DefausseModel discardPile;
    private int currentPlayerIndex;
    private int firstPlayerToPlayIndex;  // joueur qui a commencé la manche
    private GameState gameState;
    private TurnPhase turnPhase;
    private CardModel currentDrawCard;
    
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
        DRAW_DECISION,              // joueur doit choisir ; pioche ou défausse
        CARD_DRAW_FROM_DECK,        // carte piochée du deck ; peut échanger ou défausser
        CARD_TAKE_FROM_DISCARD,     // carte prise de la défausse ; doit échanger
        TURN_ENDED                  // tour terminé
    }

    /**
     * Constructeur qui doit etre appeler pour lancer une partie
    */
   public GameManager(){
       
}

/*
*constructeur avec les noms des joueurs
*permet de d'initialiser les joueurs avec leurs noms
*/
    public GameManager(List<PlayerData> playersData){
        //initialiser les joueurs
        this.players = new ArrayList<>();
        for (PlayerData player : playersData) {
            //créer un BotModel si c'est un bot, sinon créer un player
            if (player.isBot()) {
                players.add(new BotModel(player));
            } else {
                players.add(new PlayerModel(player));
            }
        }
        
        currentPlayerIndex = 0;
        firstPlayerToPlayIndex = 0; // le joueur qui commence la manche
        deck = new PiocheModel();
        discardPile = new DefausseModel();
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
    
    /*
    *Initialisation de la game 
    */
   public void initializeGame(){
        //creer un deck deja melange
        deck = new PiocheModel();
        
        firstPlayerToPlayIndex = currentPlayerIndex; // faut changer ici si le premier joueur n'est pas le joueur 0
        
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
        System.out.println("Tour de : " + getCurrentPlayer().getName());
    
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
    
    /*
    *prendre la carte de la défausse
    *@return la carte prise
    */
    public CardModel takeDiscard(){
        currentDrawCard = discardPile.getTopCard();
        discardPile.removeCardAtTop();
        turnPhase = TurnPhase.CARD_TAKE_FROM_DISCARD;
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
        hand.replaceCard(row, col, currentDrawCard);
        discardPile.addCardAtTop(cardInHand);
        //montrer la nouvelle carte
        if (currentDrawCard.isFaceDown()) {
            currentDrawCard.returnCard();
        }
        currentDrawCard = null;
        
        //vérifier les colonnes/lignes identiques
        RemoveIdenticalColumns();
        RemoveIdenticalRows();
        //vérifier si fin de manche
        checkEndOfRound();
        turnPhase = TurnPhase.TURN_ENDED;
    }
    
    /*
    *défausser la carte piochée
    */
    public void discardDrawCard(){
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
    }

    /*
    *retire les colonnes avec 3 cartes identiques
    *@param curentPlayer le joueur actuelle
    *@param hand la main du joueur
    */
    private void RemoveIdenticalColumns(){
        PlayerModel currentPlayer = getCurrentPlayer();
        PlayerHandModel hand = currentPlayer.getHand();

        for (int col = 0; col < hand.getColumns(); col++) {
            CardModel firstCard = hand.getCard(0, col);
            //permet de sauter si une colonne est vide
            if (firstCard == null) continue;

            boolean sameCard = true;
            for (int row = 1; row < hand.getRows(); row++) {
                CardModel card = hand.getCard(row, col);
                if (card == null || card.getCardValue() != firstCard.getCardValue()) {
                    sameCard = false;
                    break;
                }
            }
            if (sameCard) {
                for (int row = 0; row < hand.getRows(); row++) {
                    CardModel removedCard = hand.getCard(row, col);
                    discardPile.addCardAtTop(removedCard);
                    hand.replaceCard(row, col, null);
                }
            }
        }
    }

    /*
    *retire les lignes avec 3 cartes identiques
    *@param curentPlayer le joueur actuelle
    *@param hand la main du joueur
    */
    private void RemoveIdenticalRows(){
        PlayerModel currentPlayer = getCurrentPlayer();
        PlayerHandModel hand = currentPlayer.getHand();

        for (int row = 0; row < hand.getRows(); row++) {
            CardModel firstCard = hand.getCard(row, 0);
            //permet de sauter si une ligne est vide
            if (firstCard == null) continue;

            boolean sameCard = true;
            for (int col = 1; col < hand.getColumns(); col++) {
                CardModel card = hand.getCard(row, col);
                if (card == null || card.getCardValue() != firstCard.getCardValue()) {
                    sameCard = false;
                    break;
                }
            }
            if (sameCard) {
                for (int col = 0; col < hand.getColumns(); col++) {
                    CardModel removedCard = hand.getCard(row, col);
                    discardPile.addCardAtTop(removedCard);
                    hand.replaceCard(row, col, null);
                }
            }
        }
    }
    
    /*
    *vérifie si quelqu'un a toutes ses cartes révélées et lance le dernier tour
    */
    private void checkEndOfRound() {
        for (PlayerModel player : players) {
            //regarde si un joueur a toutes ses cartes révélées
            if (player.getHand().allCardsRevealed()) {
                //mettre en mode dernier tour
                gameState = GameState.LAST_ROUND;
                return; 
            }
        }
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
        
        //calculer le score de chacun
        PlayerModel lowestPlayer = null;
        int lowestScore = 2000; //volontairement haut
        
        for (PlayerModel player : players) {
            int score = player.getHand().calculateScore();
            //cherche le joueur avec le score le plus bas
            if (score < lowestScore) {
                lowestScore = score;
                lowestPlayer = player;
            }
        }

        //cherche le joueur qui lance la fin de la manche
        PlayerModel roundEndPlayer = null;
        for (PlayerModel player : players) {
            if (player.getHand().allCardsRevealed()) {
                roundEndPlayer = player;
                break;
            }
        }
        
        //garde pas un code obsolète 

        //Nouvelle version du code d'en haut, applique le score a tout le monde
        for (PlayerModel player : players) {
            int score = player.getHand().calculateScore();
            player.addScore(score);
        }
        // Puis ici double le score du joueur qui a lancé la fin de la manche
        if (roundEndPlayer != null && roundEndPlayer != lowestPlayer) {
            int roundEndPlayerScore = roundEndPlayer.getHand().calculateScore();
            roundEndPlayer.addScore(roundEndPlayerScore); 
        }

        //affiche les scores totaux
        System.out.println("Fin de la manche");
        for (PlayerModel player : players) {
            System.out.println(player.getName() + " : " + player.getTotalScore() + " points au total");
            showHand(player, true);
        }

        //regarde si quelqu'un a 100 pts
        boolean end = false;
        for (PlayerModel player : players) {
            if (player.getTotalScore() >= 100) {
                end = true;
                break;
            }
        }

        
        //finir la partie ou commencer une nouvelle manche
        if (end) {
            gameState = GameState.GAME_ENDED;
            //regarde le gagnant
            PlayerModel winner = players.get(0);
            for (PlayerModel player : players) {
                if (player.getTotalScore() < winner.getTotalScore()) {
                    winner = player;
                }
            }
            System.out.println("WINNER: " + winner.getName() + " avec " + winner.getTotalScore() + " points !?? EZZZZZ");
        }
        else {
            gameState = GameState.ROUND_ENDED;
            System.out.println("\nFin de la manche. \n");
        }
    }

    /**
     * Si un joueur n'a pas atteint les 100 points, on peut recommencer une nouvelle manche
     */
    public void startNewGame() {
        currentPlayerIndex = firstPlayerToPlayIndex;
        
        biggestScorePlayer(); //Le joueur avec le score le plus grand commence
        System.out.println("Tour de " + getCurrentPlayer().getName());
        currentDrawCard = null;
        discardPile = new DefausseModel();
        initializeGame();
    }

    /**
     *affiche la main d'un joueur
     *si la carte est null = X; face cachée = ?; face visible = valeur
     */
    private void showHand(PlayerModel player, boolean showValues) {
        System.out.println("Main de " + player.getName() + "\n");
        System.out.println("         Col 0   Col 1   Col 2   Col 3");
        //boucle qui affiche la main
        for (int row = 0; row < 3; row++) {
            System.out.print("Ligne " + row + " ");
            for (int col = 0; col < 4; col++) {
                CardModel card = player.getHand().getCard(row, col);
                if (card == null) {
                    System.out.print("[  X  ]");
                } else if (!showValues && card.isFaceDown()) {
                    System.out.print("[  ?  ]");
                } else {
                    System.out.printf("[ %3d ]", card.getCardValue());
                }
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println("\n" + "Score de la manche: "+ player.getHand().getVisibleCardScore() + "\n");
    }

    /**
     * Joueur avec le plus grand score commence la partie suivante
     */
    private void biggestScorePlayer() {
        int maxScore = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            int score = players.get(i).getTotalScore();
            if (score > maxScore) {
                maxScore = score;
                index = i;
            }
        }
        currentPlayerIndex = index;
    }

    /**
     * Détermine le joueur qui commence la partie en regardant ses cartes visibles
     */
    public void firstPlayer() {
        int maxScore = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            int visibleScore = players.get(i).getHand().getVisibleCardScore();
            if (visibleScore > maxScore) {
                maxScore = visibleScore;
                index = i;
            }
        }
        currentPlayerIndex = index;
        System.out.println("Le joueur " + players.get(currentPlayerIndex).getName() + " commence la partie.");
    }
}
