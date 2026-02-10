package fr.upc.cr1a.model;

import fr.upc.cr1a.view.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe pour lancer le jeu 
 * Elle doit servir que pour ca et pas pour autre chose
 */
public class GameLauncher {

    private GameManager gameManager;
    private Scanner scanner;
    private int botCounter = 0; // compteur bot
    
    public GameLauncher() {
        // Fenetre du jeu
        new Frame();

        // Jeu dams le Terminal 
        //scanner = new Scanner(System.in);
        //startGame();
    }
    
    /**
     * Démarre le jeu et gère le menu principal
     */
    private void startGame() {
        List<PlayerData> playerNames = new ArrayList<>();
        //demande le nombre de joueurs
        int nbJoueurs = 0;
        boolean valid = false;
        do {
            try {
                System.out.print("Nombre de joueurs (2-8) : ");
                nbJoueurs = scanner.nextInt();
                scanner.nextLine();
                
                if (nbJoueurs < 2 || nbJoueurs > 8) {
                    System.out.println("Le nombre de joueurs doit être entre 2 et 8");
                } else {
                    valid = true;
                }
            } catch (Exception e) {
                System.out.println("Il te manque une case a toi, on t'as dis un chiffre");
                scanner.nextLine();
            }
        } while (!valid);
        
        //Donnez le nom des joueurs
        for (int i = 1; i <= nbJoueurs; i++) {
            System.out.print("Nom du joueur " + i + " ou 'bot' : ");
            String inputName = scanner.nextLine();
            
            PlayerData tmp;
            if (inputName.equalsIgnoreCase("bot")) {
                botCounter++;
                String botName = "Bot" + botCounter;
                tmp = new PlayerData(botName, true);
            } else {
                tmp = new PlayerData(inputName);
            }
            playerNames.add(tmp);
        }
        System.out.println("------------------------------------------------------------------");
        
        //créer le game manager
        gameManager = new GameManager(playerNames);
        
        // Lancer la partie
        playGame();
    }
    
    /**
     * déroulement de la partie
     */
    private void playGame() {
        //init la première manche
        gameManager.initializeGame();
        
        //choisir 2 cartes pour chaque joueur au début
        initializeHand();

        //Joueur avec la plus grande carte
        gameManager.firstPlayer();

        //Boucle de JEU 
        while (gameManager.getGameState() != GameManager.GameState.GAME_ENDED) {
            
            //boucle de la partie (A bouger plus tard dans la gameLoop peut etre jsp faut ce concerté les amis)
            while (gameManager.getGameState() == GameManager.GameState.IS_PLAYING || gameManager.getGameState() == GameManager.GameState.LAST_ROUND) {
                playTurn();
            }

            //Manche prochaine
            if (gameManager.getGameState() == GameManager.GameState.ROUND_ENDED) {
                System.out.println("----------------------------------------------------------------");
                System.out.println("\nNouvelle Manche\n");
                gameManager.startNewGame();
                initializeHand();
            }
        }
        
        //fin de partie
        if (gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
            System.out.println("\nPartie terminée ");
        }
        
        scanner.close();
    }
    
    /**
     * Initialise la main de chaque joueur en révélant 2 cartes
     */
    private void initializeHand() {
        for (PlayerModel player : gameManager.getPlayers()) {
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
     *tour complet d'un joueur
     */
    private void playTurn() {
        //début du tour
        gameManager.startTurn();
        PlayerModel currentPlayer = gameManager.getCurrentPlayer();
        //regarde si c'est un bot
        if (currentPlayer instanceof BotModel) {
            botTurn((BotModel) currentPlayer);
        } else {
            playerTurn(currentPlayer);
        }
    }
    
    /**
     *Tour d'un joueur 
     */
    private void playerTurn(PlayerModel player) {
        System.out.println("----------------------------------------------------------------");
        
        //affiche la main du joueur
        showHand(player, false);
        
        //montre la carte sur la défausse
        System.out.println("\nCarte sur la défausse : " + gameManager.getDiscardPile().getTopCard().getCardValue());
        
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
            drawnCard = gameManager.takeDiscard();
            mustExchange = true;
            System.out.println("--------------------jehaislebot--------------------------------");
            System.out.println("Vous avez pris : " + drawnCard.getCardValue());
        } else { //piocher une carte
            drawnCard = gameManager.drawDeck();
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
                gameManager.discardDrawCard();
                System.out.println("\nCarte défaussée");
                //reveler une carte de la main
                System.out.println("Revéler une carte de votre main\n");
                System.out.println("----------------------------------------------------------------");
                showHand(player, false);
                //choest videisit la carte à reveler
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
        
        gameManager.nextPlayer();
    }
    
    /**
     *fonction pour échanger une carte
     */
    private void exchangeCard(PlayerModel player) {
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
        gameManager.exchangeCardInHand(col, row);
        System.out.println("\nCarte échangé. Carte défaussée : " + oldCard.getCardValue() + "\n");
        System.out.println("-------------------------------------------------------------------------");
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
                } else if (card.isFaceDown()) {
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
     * tour d'un bot
     */
    private void botTurn(BotModel bot) {
        showHand(bot, false);
        BotHandModel botHand = bot.getHand();
        CardModel discardCard = gameManager.getDiscardPile().getTopCard();
        System.out.println("\nCarte sur la défausse : " + discardCard.getCardValue());
        //regarde si il doit la prendre ou non
        boolean takeDiscard = BotTerminalModel.takeFromDiscard(botHand, discardCard);
        CardModel drawCard;
        boolean mustExchange = false; // si il prendre de la défausse obliger d'échanger
        
        if (takeDiscard) {
            //prend de la défausse
            drawCard = gameManager.takeDiscard();
            mustExchange = true;
            System.out.println(bot.getName() + " prend la carte de la défausse : " + drawCard.getCardValue());
        } else {
            //pioche
            drawCard = gameManager.drawDeck();
            System.out.println(bot.getName() + " pioche une carte : " + drawCard.getCardValue());
        }
        
        //quand il pioche soit il défausse soit il échange
        if (mustExchange) {
            // obliger d'échanger
            int[] position = BotTerminalModel.cardToExchange(botHand, drawCard);
            if (position[0] != -1 && position[1] != -1) {
                CardModel oldCard = botHand.getCard(position[1], position[0]);
                gameManager.exchangeCardInHand(position[0], position[1]);
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
                    gameManager.exchangeCardInHand(position[0], position[1]);
                    System.out.println(bot.getName() + " échange à [Ligne " + position[1] + ", Col " + position[0] + "]");
                    System.out.println("Carte défaussée : " + oldCard.getCardValue());
                }
            } else {
                //défausser une carte
                gameManager.discardDrawCard();
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
        
        gameManager.nextPlayer();
    }

    public static void main(String[] args) throws Exception {
        new GameLauncher();
    }
}
