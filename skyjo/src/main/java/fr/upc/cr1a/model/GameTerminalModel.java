package fr.upc.cr1a.model;

import java.util.ArrayList;
import java.util.Scanner;

import fr.upc.cr1a.model.interfaces.BotStrategyInterface;

/**
 * Classe qui gère toutes les interactions du jeu via le terminal
 * Séparée du GameManager pour une meilleure organisation du code
 */
public class GameTerminalModel {

    private Scanner scanner;
    private GameManager gameManager;

    /**
     * Constructeur
     * @param gameManager Le gestionnaire de jeu
     */
    public GameTerminalModel(GameManager gameManager) {
        this.gameManager = gameManager;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Affiche la main d'un joueur dans le terminal
     * Si la carte est null = X; face cachée = ?; face visible = valeur
     * @param player Le joueur dont on affiche la main
     * @param showValues Si true, affiche toutes les valeurs (même les cartes face cachée)
     */
    public void showHand(PlayerModel player, boolean showValues) {
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
     * Déroulement de la partie en mode terminal
     */
    public void playGame() {
        //init la première manche
        gameManager.initializeGame();
        
        //choisir 2 cartes pour chaque joueur au début
        initializeHand();

        //Joueur avec la plus grande carte
        gameManager.firstPlayer();
        firstPlayer();

        //Boucle de JEU 
        while (gameManager.getGameState() != GameManager.GameState.GAME_ENDED) {
            
            //boucle de la partie
            while (gameManager.getGameState() == GameManager.GameState.IS_PLAYING 
                   || gameManager.getGameState() == GameManager.GameState.LAST_ROUND) {
                playTurn();
            }

            //Afficher les résultats de la manche
            if (gameManager.getGameState() == GameManager.GameState.ROUND_ENDED 
                || gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
                displayRoundResults();
            }

            //Afficher le gagnant
            if (gameManager.getGameState() == GameManager.GameState.GAME_ENDED) {
                displayWinner();
            }

            //Manche prochaine
            if (gameManager.getGameState() == GameManager.GameState.ROUND_ENDED) {
                displayNewRound();
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
     * Détermine le joueur qui commence la partie en regardant ses cartes visibles
     */
    private void firstPlayer() {
        System.out.println("Le joueur " + gameManager.getCurrentPlayer().getName() + " commence la partie.");
    }

    /**
     * Initialise la main de chaque joueur en révélant 2 cartes
     */
    public void initializeHand() {
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
     * Tour complet d'un joueur
     */
    public void playTurn() {
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
     * Tour d'un joueur 
     */
    public void playerTurn(PlayerModel player) {
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
        
        gameManager.nextPlayer();
    }

    /**
     * Tour d'un bot
     */
    public void botTurn(BotModel bot) {
        showHand(bot, false);
        BotHandModel botHand = bot.getHand();
        BotStrategyInterface strategy = bot.getStrategy();
        CardModel discardCard = gameManager.getDiscardPile().getTopCard();
        System.out.println("\nCarte sur la défausse : " + discardCard.getCardValue());
        //regarde si il doit la prendre ou non
        boolean takeDiscard = strategy.TakeFromDiscard(botHand, discardCard);
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
            int[] position = strategy.CardToExchange(botHand, drawCard);
            if (position[0] != -1 && position[1] != -1) {
                CardModel oldCard = botHand.getCard(position[1], position[0]);
                gameManager.exchangeCardInHand(position[0], position[1]);
                System.out.println(bot.getName() + " échange à [Ligne " + position[1] + ", Col " + position[0] + "]");
                System.out.println("Carte défaussée : " + oldCard.getCardValue());
            }
        } else {
            //choisit d'échanger ou de défausser
            boolean exchange = strategy.ExchangeDrawCard(botHand, drawCard);
            
            if (exchange) {
                //écahnger la carte piochée avec une carte de la main
                int[] position = strategy.CardToExchange(botHand, drawCard);
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
        //try {
            //Thread.sleep(1200);
        //} catch (InterruptedException e) {
            //Thread.currentThread().interrupt();
        //}
        
        System.out.println("\n-------------------------------------------------------");
        showHand(bot, false);
        System.out.println("------------------------------------------------------------");
        //System.out.println("\nAppuyez sur Entrée pour continuer <3");
        //scanner.nextLine();
        
        gameManager.nextPlayer();
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
        gameManager.exchangeCardInHand(col, row);
        System.out.println("\nCarte échangé. Carte défaussée : " + oldCard.getCardValue() + "\n");
        System.out.println("-------------------------------------------------------------------------");
    }

    /**
     * Ferme le scanner
     */
    public void closeScanner() {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Affiche les résultats de fin de manche
     */
    public void displayRoundResults() {
        System.out.println("Fin de la manche");
        for (PlayerModel player : gameManager.getPlayers()) {
            System.out.println(player.getName() + " : " + player.getTotalScore() + " points au total");
            showHand(player, true);
        }
    }

    /**
     * Affiche le gagnant de la partie
     */
    public void displayWinner() {
        ArrayList<PlayerModel> players = new ArrayList<>(gameManager.getPlayers());
        PlayerModel winner = GameRuleModel.determineWinner(players);
        System.out.println("WINNER: " + winner.getName() + " avec " + winner.getTotalScore() + " points !?? EZZZZZ");
    }

    /**
     * Affiche un message de nouvelle manche
     */
    public void displayNewRound() {
        System.out.println("----------------------------------------------------------------");
        System.out.println("\nNouvelle Manche\n");
    }
}
