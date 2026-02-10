package fr.upc.cr1a.model;

import java.util.List;
import java.util.Scanner;

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
    private int currentPlayerIndex;
    private int firstPlayerToPlayIndex;  // joueur qui a commencé la manche
    private GameState gameState;
    private TurnPhase turnPhase;
    private CardModel currentDrawCard;
    private Scanner scanner;

    private GameBoardModel gameBoardModel;
    
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
     * Dans la version swing
    */
    public GameManager(Frame frame){
        System.out.println("GameManager Called");

        this.players = new ArrayList<>(); // il faudra initialiser les joueurs a un moment
        for (int i = 1 ; i <= 7 ; i++){   // juste pour les tests avec tous les joueurs
            players.add(new PlayerModel(new PlayerData("Joueur " + i)));
        }

        // TEMPORAIRE : toutes les cartes face cachée pour tester - Ian
        for (PlayerModel player : players) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 4; col++) {
                    CardModel card = new NumberCardModel(true, 0, "blue");
                    player.getHand().placeCard(row, col, card);
                }
            }
        }

        this.gameBoardModel = new GameBoardModel(players, frame);
        GameBoardView view = this.gameBoardModel.getGameBoardView();

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
            //créer un BotModel si c'est un bot, sinon créer un player
            if (player.isBot()) {
                players.add(new BotModel(player));
            } else {
                players.add(new PlayerModel(player));
            }
        }
        
        currentPlayerIndex = 0;
        firstPlayerToPlayIndex = 0; // le joueur qui commence la manche
        scanner = new Scanner(System.in);
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
    
    /**
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
        hand.replaceCard(row, col, currentDrawCard);
        discardPile.addCardAtTop(cardInHand);
        //montrer la nouvelle carte
        if(currentDrawCard.isFaceDown()) currentDrawCard.returnCard();
        if (currentDrawCard.isFaceDown()) {
            currentDrawCard.returnCard();
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

        //affiche les scores totaux
        System.out.println("Fin de la manche");
        for (PlayerModel player : players) {
            System.out.println(player.getName() + " : " + player.getTotalScore() + " points au total");
            showHand(player, true);
        }

        //regarde si quelqu'un a 100 pts
        boolean end = GameRuleModel.isGameOver(players);

        
        //finir la partie ou commencer une nouvelle manche
        if (end) {
            gameState = GameState.GAME_ENDED;
            //regarde le gagnant
            PlayerModel winner = GameRuleModel.determineWinner(players);
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
        
        //Le joueur avec le score le plus grand commence
        currentPlayerIndex = GameRuleModel.getBiggestScorePlayerIndex(players);
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
        System.out.println("\n" + "Score de la manche: "+ player.getHand().getVisibleCardScore());
        System.out.println("Score de la partie: "+ (player.getTotalScore() + player.getHand().getVisibleCardScore()) + "\n");
    }

    /**
     * Détermine le joueur qui commence la partie en regardant ses cartes visibles
     */
    public void firstPlayer() {
        currentPlayerIndex = GameRuleModel.getFirstPlayerIndex(players);
        System.out.println("Le joueur " + players.get(currentPlayerIndex).getName() + " commence la partie.");
    }

    /**
     * Déroulement de la partie
     */
    public void playGame() {
        //init la première manche
        initializeGame();
        
        //choisir 2 cartes pour chaque joueur au début
        initializeHand();

        //Joueur avec la plus grande carte
        firstPlayer();

        //Boucle de JEU 
        while (getGameState() != GameState.GAME_ENDED) {
            
            //boucle de la partie
            while (getGameState() == GameState.IS_PLAYING || getGameState() == GameState.LAST_ROUND) {
                playTurn();
            }

            //Manche prochaine
            if (getGameState() == GameState.ROUND_ENDED) {
                System.out.println("----------------------------------------------------------------");
                System.out.println("\nNouvelle Manche\n");
                startNewGame();
                initializeHand();
            }
        }
        
        //fin de partie
        if (getGameState() == GameState.GAME_ENDED) {
            System.out.println("\nPartie terminée ");
        }
        
        scanner.close();
    }

    /**
     * Initialise la main de chaque joueur en révélant 2 cartes
     */
    public void initializeHand() {
        for (PlayerModel player : getPlayers()) {
            if (player instanceof BotModel) {
                BotHandModel hand = ((BotModel) player).getHand();
                hand.revealRandomCard(2);
                System.out.println("\n" + player.getName() + " a révelé 2 cartes\n");
                showHand(player, false);
                System.out.println("------------------------------------------------------------------");
            } else {
                PlayerHandModel hand = player.getHand();
                System.out.println("\n" + player.getName() + ", choisissez 2 cartes à révéler");
                showHand(player, false);
                
                for (int i = 0; i < 2; i++) {
                    int row = -1, col = -1;
                    
                    do {
                        try {
                            System.out.print("Carte " + (i + 1) + " - Ligne (0-2) : ");
                            row = scanner.nextInt();
                            System.out.print("Carte " + (i + 1) + " - Colonne (0-3) : ");
                            col = scanner.nextInt();
                            
                            if (row < 0 || row >= 3 || col < 0 || col >= 4) {
                                System.out.println("\nPosition invalide ! Réessayez.\n");
                            } else if (!hand.getCard(row, col).isFaceDown()) {
                                System.out.println("\nCette carte est déjà révélée !\n");
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            System.out.println("t'as pas compris quoi? t'as besoin d'un cerveau?");
                            scanner.nextLine();
                        }
                    } while (true);
                    
                    hand.revealCard(row, col);
                    System.out.println("\nCarte révélée : " + hand.getCard(row, col).getCardValue());
                    showHand(player, false);
                }
                
                System.out.println("------------------------------------------------------------------");
                scanner.nextLine();
            }
        }
    }

    /**
     * Tour complet d'un joueur
     */
    public void playTurn() {
        //début du tour
        startTurn();
        PlayerModel currentPlayer = getCurrentPlayer();
        //regarde si c'est un bot
        if (currentPlayer instanceof BotModel) {
            botTurn((BotModel) currentPlayer);
        } else {
            playerTurn(currentPlayer);
        }
    }

    /**
     * Tour d'un joueur 
     */
    public void playerTurn(PlayerModel player) {
        System.out.println("----------------------------------------------------------------");
        
        //affiche la main du joueur
        showHand(player, false);
        
        //montre la carte sur la défausse
        System.out.println("\nCarte sur la défausse : " + getDiscardPile().getTopCard().getCardValue());
        
        //choisir piocher ou prendre de la défausse
        System.out.println("----------------------------------------------------------------");
        System.out.println("\n Action possible :\n");
        System.out.println("1 = Piocher");
        System.out.println("2 = Prendre de la défausse");
        System.out.print("\nVotre choix : ");
        
        int choice = 0;
        boolean validChoice = false;
        do {
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1 || choice == 2) {
                    validChoice = true;
                } else {
                    System.out.print("T'es pas allé à l'école toi?");
                }
            } catch (Exception e) {
                System.out.print("2 choix possible et t'en donnes une troisième a quoi tu joues");
                scanner.nextLine();
            }
        } while (!validChoice);
        
        CardModel drawnCard;
        boolean mustExchange = false;
        
        //choix de prendre de la défausse
        if (choice == 2) {
            //échanger une carte
            drawnCard = takeDiscard();
            mustExchange = true;
            System.out.println("--------------------jehaislebot--------------------------------");
            System.out.println("Vous avez pris : " + drawnCard.getCardValue());
        } else { //piocher une carte
            drawnCard = drawDeck();
            System.out.println("-----------------------ilestnulenplus-----------------------------");
            System.out.println("Vous avez pioché : " + drawnCard.getCardValue());
        }
    
        //échanger
        if (mustExchange) {
            System.out.println("\nEchanger la carte avec une carte de votre main");
            exchangeCard(player);
        //quand le jouer pioche 2 actions possibles
        } else {
            System.out.println("\n Action possible :\n");
            System.out.println("1 = Echanger");
            System.out.println("2 = défausser puis révéler une carte");
            System.out.print("\nVotre choix : ");
            
            int action = 0;
            boolean validAction = false;
            do {
                try {
                    action = scanner.nextInt();
                    scanner.nextLine();
                    if (action == 1 || action == 2) {
                        validAction = true;
                    } else {
                        System.out.print("Tu te crois drole? 1 ou 2 pas autre chose");
                    }
                } catch (Exception e) {
                    System.out.print("T'es un rigolo toi, il te manque la touche 1 et 2 ou quoi?");
                    scanner.nextLine();
                }
            } while (!validAction);
            //échanger
            if (action == 1) {
                exchangeCard(player);
            } else { //défausser puis révéler une carte
                //défausser
                discardDrawCard();
                System.out.println("\nCarte défaussée");
                //reveler une carte de la main
                System.out.println("Revéler une carte de votre main\n");
                System.out.println("----------------------------------------------------------------");
                showHand(player, false);
                //choix de la carte à reveler
                int row = -1, col = -1;
                do {
                    try {
                        System.out.print("\nLigne (0-2) : ");
                        row = scanner.nextInt();
                        System.out.print("Colonne (0-3) : ");
                        col = scanner.nextInt();
                        scanner.nextLine();
                        
                        if (row < 0 || row >= 3 || col < 0 || col >= 4) {
                            System.out.println("\nPosition invalide !\n");
                        } else if (player.getHand().getCard(row, col) == null) {
                            System.out.println("\nPas de carte à cette position !\n");
                        } else if (!player.getHand().getCard(row, col).isFaceDown()) {
                            System.out.println("\nCette carte est t déjà révélée !\n");
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("Les chiffres et toi ça fait 2 apparemment");
                        scanner.nextLine();
                    }
                } while (true);
                //revéler la carte
                player.getHand().revealCard(row, col);
                System.out.println("----------------------------------------------------------");
                System.out.println("\nCarte révélée : " + player.getHand().getCard(row, col).getCardValue());
            }
        }
        
        //afficher la main
        showHand(player, false);
        
        //passe au joueur
        System.out.println("\nAppuyez sur Entrée pour continuer :)");
        scanner.nextLine();
        
        nextPlayer();
    }

    /**
     * Tour d'un bot
     */
    public void botTurn(BotModel bot) {
        showHand(bot, false);
        BotHandModel botHand = bot.getHand();
        CardModel discardCard = getDiscardPile().getTopCard();
        System.out.println("\nCarte sur la défausse : " + discardCard.getCardValue());
        //regarde si il doit la prendre ou non
        boolean takeDiscard = BotTerminalModel.takeFromDiscard(botHand, discardCard);
        CardModel drawCard;
        boolean mustExchange = false; // si il prendre de la défausse obliger d'échanger
        
        if (takeDiscard) {
            //prend de la défausse
            drawCard = takeDiscard();
            mustExchange = true;
            System.out.println(bot.getName() + " prend la carte de la défausse : " + drawCard.getCardValue());
        } else {
            //pioche
            drawCard = drawDeck();
            System.out.println(bot.getName() + " pioche une carte : " + drawCard.getCardValue());
        }
        
        //quand il pioche soit il défausse soit il échange
        if (mustExchange) {
            // obliger d'échanger
            int[] position = BotTerminalModel.cardToExchange(botHand, drawCard);
            if (position[0] != -1 && position[1] != -1) {
                CardModel oldCard = botHand.getCard(position[1], position[0]);
                exchangeCardInHand(position[0], position[1]);
                System.out.println(bot.getName() + " échange à [Ligne " + position[1] + ", Col " + position[0] + "]");
                System.out.println("Carte défaussée : " + oldCard.getCardValue());
            }
        } else {
            //choisit d'échanger ou de défausser
            boolean exchange = BotTerminalModel.exchangeDrawCard(botHand, drawCard);
            
            if (exchange) {
                //écahnger la carte piochée avec une carte de la main
                int[] position = BotTerminalModel.cardToExchange(botHand, drawCard);
                if (position[0] != -1 && position[1] != -1) {
                    CardModel oldCard = botHand.getCard(position[1], position[0]);
                    exchangeCardInHand(position[0], position[1]);
                    System.out.println(bot.getName() + " échange à [Ligne " + position[1] + ", Col " + position[0] + "]");
                    System.out.println("Carte défaussée : " + oldCard.getCardValue());
                }
            } else {
                //défausser une carte
                discardDrawCard();
                System.out.println(bot.getName() + " défausse la carte");
                
                //révéler une carte aléatoire
                botHand.revealRandomCard(1);
                System.out.println(bot.getName() + " révèle une carte");
            }
        }
        //permet que ça soit pas instantané, donne un aspect moins robotique
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n-------------------------------------------------------");
        showHand(bot, false);
        System.out.println("------------------------------------------------------------");
        System.out.println("\nAppuyez sur Entrée pour continuer <3");
        scanner.nextLine();
        
        nextPlayer();
    }

    /**
     * Fonction pour échanger une carte
     */
    public void exchangeCard(PlayerModel player) {
        System.out.println("\nDonnez l'emplacement de la carte à échanger");
        System.out.println("------------------------------------------------------------");
        showHand(player, false);
        
        int row = -1, col = -1;
        do {
            try {
                System.out.print("\nLigne (0-2) : ");
                row = scanner.nextInt();
                System.out.print("Colonne (0-3) : ");
                col = scanner.nextInt();
                scanner.nextLine();
                
                if (row < 0 || row >= 3 || col < 0 || col >= 4) {
                    System.out.println("\nPosition invalide !\n");
                } else if (player.getHand().getCard(row, col) == null) {
                    System.out.println("\nPas de carte à cette position !\n");
                } else {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Tu sais pas lire une phrase toi, quitte les cours");
                scanner.nextLine();
            }
        } while (true);
        
        CardModel oldCard = player.getHand().getCard(row, col);
        exchangeCardInHand(col, row);
        System.out.println("\nCarte échangé. Carte défaussée : " + oldCard.getCardValue() + "\n");
        System.out.println("-------------------------------------------------------------------------");
    }
}
