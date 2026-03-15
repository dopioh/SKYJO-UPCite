package fr.upc.cr1a.model;

import java.util.ArrayList;
import java.util.Scanner;

import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.interfaces.BotStrategyInterface;

/**
 * Classe qui gère toutes les interactions du jeu via le terminal
 * Séparée du GameManager pour une meilleure organisation du code
 * @author Billy & Nassim
 */

public class GameTerminalModel {

    private Scanner scanner;
    private GameManager gameManager;
    private boolean hasGameManagerActionsCard; // seulement pour pas repeter l'appel a chaque check

    /**
     * Constructeur
     * @param gameManager Le gestionnaire de jeu
     */
    public GameTerminalModel(GameManager gameManager) {
        this.gameManager = gameManager;
        this.scanner = new Scanner(System.in);
        this.hasGameManagerActionsCard = gameManager.withActionCard();
    }

    /** 
     * POURQUOI CETTE FONCTION EST ICI ???
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
                
                //regarde et supprimer les colonnes/lignes identiques
                GameRuleModel.removeIdenticalColumns(hand, gameManager.getDiscardPile());
                GameRuleModel.removeIdenticalRows(hand, gameManager.getDiscardPile());
                
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

                    // si l'etoile retournée est une carte etoile
                    NumberCardModel cardDrawn = (NumberCardModel) hand.getCard(row, col);
                    if (cardDrawn.isStar()) player.addStarsScore();
                    
                    // Vérifier et supprimer les colonnes/lignes identiques
                    GameRuleModel.removeIdenticalColumns(hand, gameManager.getDiscardPile());
                    GameRuleModel.removeIdenticalRows(hand, gameManager.getDiscardPile());
                    
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
     * Methode pour jouer une action
     * appelée dans playerTurn() pour éviter de dupliquer le code
     * @param player joueur courant
     */
    private void useAction(PlayerModel player) {
        if (hasGameManagerActionsCard) {

            if (player.getActionHand().isEmpty()) {
                System.out.println("Aucune action disponible.");
                return;
            }

            System.out.println("Cartes action disponibles :");
            for (int i = 0; i < player.getActionHand().size(); i++) {
                System.out.println((i + 1) + " = " + player.getActionHand().get(i));
            }

            int actionIndex = -1;
            do {
                try {
                    System.out.print("Choisissez une carte action (1-" + player.getActionHand().size() + ") : ");
                    actionIndex = scanner.nextInt() - 1;
                    scanner.nextLine();
                } catch (Exception e) {
                    scanner.nextLine();
                    actionIndex = -1;
                }
            } while (actionIndex < 0 || actionIndex >= player.getActionHand().size());

            ActionType selectedAction = player.getActionHand().get(actionIndex);
            ArrayList<PlayerModel> players = new ArrayList<>(gameManager.getPlayers());
            boolean actionPlayed = true;
            System.out.println("\n--------------------------------ACTION!--------------------------------\n");
            
            switch (selectedAction) {
                case TRADE_CARD_WITH_PLAYER:
                    ActionSkills.tradeCardWithPlayer(scanner, players, gameManager.getCurrentPlayerIndex());
                    break;

                case REPLACE_CARD_IN_HAND:
                    ActionSkills.replaceCardInHand(scanner, players, gameManager.getCurrentPlayerIndex());
                    break;

                case DRAW_THREE_CARD:
                    ActionSkills.drawThreeCard(scanner, players, gameManager.getCurrentPlayerIndex(), gameManager);
                    break;

                case PLAY_LAST_ACTION_IN_DEFAUSSE:
                    ActionSkills.playLastActionInDefausse(scanner, players, gameManager.getCurrentPlayerIndex(), gameManager);
                    break;

                case SPY_ONE_COLUMN:
                    ActionSkills.spyOneColumn(scanner, players, gameManager.getCurrentPlayerIndex(), gameManager);
                    break;

                case PLAY_TWO_TURN:
                    ActionSkills.playTwoTurn(scanner, players, gameManager.getCurrentPlayerIndex(), gameManager);
                    break;
                
                case ALL_PLAYERS_DROP_CARD:
                    ActionSkills.allPlayersDropCard(scanner, players, gameManager.getCurrentPlayerIndex(), gameManager, this);
                    break;
                
                default:
                    System.out.println("Action non implémentée pour le moment.");
                    actionPlayed = false;
                    break;
            }

            if (actionPlayed) {
                player.getActionHand().remove(actionIndex);
                gameManager.getDiscardActionPile().addCardAtTop(new ActionCardModel(false, selectedAction));
                System.out.println("\n--------------------------------ACTION! (fin)----------------------------\n");
            }
        }
        else {
            System.out.println("vous ne jouez pas en mode action :p");
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
        System.out.println("\nCarte numéro sur la défausse : " + gameManager.getDiscardPile().getTopCard().getCardValue());
        if (hasGameManagerActionsCard && gameManager.getDiscardActionPile().stackSize() > 0) {
            System.out.println("\nCarte action sur la défausse : " + ((ActionCardModel) gameManager.getDiscardActionPile().getTopCard()).getType());
        }

        //choisir piocher ou prendre de la défausse
        System.out.println("----------------------------------------------------------------");
        System.out.println("\n Action possible :\n");
        System.out.println("1 = Piocher");
        System.out.println("2 = Prendre de la défausse");
        if (hasGameManagerActionsCard && player.getStarsScore() > 0) System.out.println("3 = Piocher une action (" + player.getStarsScore() + ")");
        if (hasGameManagerActionsCard && !player.getActionHand().isEmpty()) System.out.println("4 = Jouer une action (" + player.showActionHand() + ")");
        System.out.print("\nVotre choix : ");
        
        int choice = 0;
        boolean validChoice = false;
        do {
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1
                    || choice == 2
                    || (choice == 3 && player.getStarsScore() > 0 && hasGameManagerActionsCard)
                    || (choice == 4 && !player.getActionHand().isEmpty() && hasGameManagerActionsCard)) {
                    validChoice = true;
                } else {
                    System.out.print("T'es pas allé à l'école toi?");
                }
            } catch (Exception e) {
                System.out.print("quelque chose s'et mal passé...");
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
        } else if (choice == 3) {
            drawnCard = gameManager.drawActionDeck();
            player.subStarsScore();
            if (drawnCard instanceof ActionCardModel actionCard) {
                player.getActionHand().add(actionCard.getType());
            }

            System.out.println("Vos cartes action - " + player.showActionHand());
            System.out.println("1 = Utiliser une action");
            System.out.println("2 = Finir le tour");

            int value = 0;
            boolean validValue = false;
            do {
                try {
                    value = scanner.nextInt();
                    scanner.nextLine();
                    validValue = (value == 1 || value == 2);
                } catch (Exception e) {
                    scanner.nextLine();
                    value = 0;
                }
            } while (!validValue);

            if (value == 1) {
                useAction(player);
            }
            gameManager.nextPlayer();
            return;
        } else if (choice == 4) {
            useAction(player);
            showHand(player, false);
            System.out.println("\nAppuyez sur Entrée pour continuer :)");
            scanner.nextLine();
            gameManager.nextPlayer();
            return;
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
                
                //regarde et supprimer les colonnes/lignes identiques
                GameRuleModel.removeIdenticalColumns(player.getHand(), gameManager.getDiscardPile());
                GameRuleModel.removeIdenticalRows(player.getHand(), gameManager.getDiscardPile());
                
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
        
        //MODE ACTION
        if (hasGameManagerActionsCard && bot.getStarsScore() > 0 && Math.random() < 0.2) {
            CardModel drawnActionCard = gameManager.drawActionDeck();
            bot.subStarsScore();
            if (drawnActionCard instanceof ActionCardModel actionCard) {
                bot.getActionHand().add(actionCard.getType());
                System.out.println(bot.getName() + " pioche une carte action : " + actionCard.getType());
                System.out.println(bot.getName() + " a maintenant " + bot.getStarsScore() + " étoiles");
            }
            
            //Le bot peut décider de jouer une action immédiatement
            botUseActionRandom(bot);
            
            System.out.println("\n-----------------------------------------------------------");
            showHand(bot, false);
            System.out.println("----------------------------------------------------------------");
            
            gameManager.nextPlayer();
            return;
        }
        
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
                
                //regarde et supprimer les colonnes/lignes identiques
                GameRuleModel.removeIdenticalColumns(botHand, gameManager.getDiscardPile());
                GameRuleModel.removeIdenticalRows(botHand, gameManager.getDiscardPile());
                
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
        
        //si le mode action est actif, le bot peut jouer une carte d'action aléatoirement
        botUseActionRandom(bot);
        
        gameManager.nextPlayer();
    }

    /**
     *permet au bot de jouer une carte action
     * @param bot Le bot qui joue l'action
     */
    private void botUseActionRandom(BotModel bot) {
        //vérifie si le mode action est actif
        if (!hasGameManagerActionsCard) {
            return;
        }
        
        //vérifie si le bot a des cartes d'action
        if (bot.getActionHand().isEmpty()) {
            return;
        }
        
        //50% de chance de jouer une action
        if (Math.random() > 0.5) {
            return;
        }
        
        //Choisit aléatoirement une action parmi celles disponibles
        int actionIndex = (int) (Math.random() * bot.getActionHand().size());
        ActionType selectedAction = bot.getActionHand().get(actionIndex);
        
        System.out.println(bot.getName() + " utilise l'action : " + selectedAction);
        
        ArrayList<PlayerModel> players = new ArrayList<>(gameManager.getPlayers());
        boolean actionPlayed = true;
        
        try {
            switch (selectedAction) {
                case TRADE_CARD_WITH_PLAYER:
                    botTradeCardWithPlayer(bot, players);
                    break;
                    
                case REPLACE_CARD_IN_HAND:
                    botReplaceCardInHand(bot);
                    break;
                    
                case DRAW_THREE_CARD:
                    botDrawThreeCard(bot);
                    break;
                    
                case PLAY_LAST_ACTION_IN_DEFAUSSE:
                    botPlayLastActionInDefausse(bot);
                    break;
                    
                case SPY_ONE_COLUMN:
                    botSpyOneColumn(bot);
                    break;
                    
                case PLAY_TWO_TURN:
                    gameManager.grantExtraTurn();
                    System.out.println(bot.getName() + " jouera deux fois !");
                    break;
                    
                case ALL_PLAYERS_DROP_CARD:
                    botAllPlayersDropCard(bot, players);
                    break;
                    
                default:
                    actionPlayed = false;
                    break;
            }
            
            if (actionPlayed) {
                bot.getActionHand().remove(actionIndex);
                gameManager.getDiscardActionPile().addCardAtTop(new ActionCardModel(false, selectedAction));
            }
        } catch (Exception e) {
            System.out.println("BUG : " + e.getMessage());
        }
    }
    
    /**
     * version bot de TRADE_CARD_WITH_PLAYER
     */
    private void botTradeCardWithPlayer(BotModel bot, ArrayList<PlayerModel> players) {
        int botIndex = players.indexOf(bot);
        
        //choisit un autre joueur aléatoirement
        int targetIndex;
        do {
            targetIndex = (int) (Math.random() * players.size());
        } while (targetIndex == botIndex);
        
        PlayerModel target = players.get(targetIndex);
        
        //choisit des positions aléatoires
        int myRow = (int) (Math.random() * 3);
        int myCol = (int) (Math.random() * 4);
        int opRow = (int) (Math.random() * 3);
        int opCol = (int) (Math.random() * 4);
        
        CardModel myCard = bot.getHand().getCard(myRow, myCol);
        CardModel opCard = target.getHand().getCard(opRow, opCol);
        
        if (myCard != null && opCard != null) {
            bot.getHand().replaceCard(myRow, myCol, opCard);
            target.getHand().replaceCard(opRow, opCol, myCard);
            System.out.println(bot.getName() + " échange sa carte avec " + target.getName());
        }
    }
    
    /**
     * version bot de REPLACE_CARD_IN_HAND
     */
    private void botReplaceCardInHand(BotModel bot) {
        //choisit deux positions aléatoires
        int row1 = (int) (Math.random() * 3);
        int col1 = (int) (Math.random() * 4);
        int row2 = (int) (Math.random() * 3);
        int col2 = (int) (Math.random() * 4);
        
        CardModel card1 = bot.getHand().getCard(row1, col1);
        CardModel card2 = bot.getHand().getCard(row2, col2);
        
        if (card1 != null && card2 != null) {
            bot.getHand().replaceCard(row1, col1, card2);
            bot.getHand().replaceCard(row2, col2, card1);
            System.out.println(bot.getName() + " échange deux cartes dans sa main");
        }
    }
    
    /**
     * version bot de DRAW_THREE_CARD
     */
    private void botDrawThreeCard(BotModel bot) {
        NumberCardModel card1 = (NumberCardModel) gameManager.drawDeck();
        NumberCardModel card2 = (NumberCardModel) gameManager.drawDeck();
        NumberCardModel card3 = (NumberCardModel) gameManager.drawDeck();
        
        card1.returnCard();
        card2.returnCard();
        card3.returnCard();
        
        System.out.println(bot.getName() + " pioche 3 cartes : " + card1.getCardValue() + ", " + 
                          card2.getCardValue() + ", " + card3.getCardValue());
        
        //choisit aléatoirement une carte ou aucune
        int choice = (int) (Math.random() * 4);
        
        NumberCardModel chosenCard = null;
        switch (choice) {
            case 0:
                chosenCard = card1;
                gameManager.getDiscardPile().addCardAtTop(card2);
                gameManager.getDiscardPile().addCardAtTop(card3);
                break;
            case 1:
                chosenCard = card2;
                gameManager.getDiscardPile().addCardAtTop(card1);
                gameManager.getDiscardPile().addCardAtTop(card3);
                break;
            case 2:
                chosenCard = card3;
                gameManager.getDiscardPile().addCardAtTop(card1);
                gameManager.getDiscardPile().addCardAtTop(card2);
                break;
            default:
                //n'en garde aucune
                gameManager.getDiscardPile().addCardAtTop(card1);
                gameManager.getDiscardPile().addCardAtTop(card2);
                gameManager.getDiscardPile().addCardAtTop(card3);
                System.out.println(bot.getName() + " ne garde aucune carte");
                return;
        }
        
        //remplace une carte aléatoire dans sa main
        int row = (int) (Math.random() * 3);
        int col = (int) (Math.random() * 4);
        CardModel oldCard = bot.getHand().getCard(row, col);
        
        if (oldCard != null) {
            gameManager.getDiscardPile().addCardAtTop(oldCard);
            bot.getHand().replaceCard(row, col, chosenCard);
            System.out.println(bot.getName() + " garde la carte " + chosenCard.getCardValue());
        }
    }
    
    /**
     *version bot de PLAY_LAST_ACTION_IN_DEFAUSSE
     */
    private void botPlayLastActionInDefausse(BotModel bot) {
        if (gameManager.getDiscardActionPile().getTopCard() != null) {
            ActionCardModel topCard = (ActionCardModel) gameManager.getDiscardActionPile().getTopCard();
            bot.getActionHand().add(topCard.getType());
            gameManager.getDiscardActionPile().removeCardAtTop();
            System.out.println(bot.getName() + " récupère l'action : " + topCard.getType());
        }
    }
    
    /**
     * version bot de SPY_ONE_COLUMN
     */
    private void botSpyOneColumn(BotModel bot) {
        int col = (int) (Math.random() * 4);
        System.out.println(bot.getName() + " espionne la colonne " + col + " de sa main");
        // Le bot regarde mais n'affiche pas c'est un bot
    }
    
    /**
     * version bot de ALL_PLAYERS_DROP_CARD
     */
    private void botAllPlayersDropCard(BotModel bot, ArrayList<PlayerModel> players) {
        for (PlayerModel player : players) {
            if (player != bot) {
                //choisit une carte aléatoire du joueur
                int row = (int) (Math.random() * 3);
                int col = (int) (Math.random() * 4);
                
                CardModel cardToDrop = player.getHand().getCard(row, col);
                if (cardToDrop != null) {
                    int droppedValue = cardToDrop.getCardValue();
                    CardModel newCard = gameManager.drawDeck();
                    newCard.returnCard();
                    player.getHand().replaceCard(row, col, newCard);
                    
                    System.out.println(player.getName() + " remplace son " + droppedValue + " par un " + newCard.getCardValue());
                    gameManager.getDiscardPile().addCardAtTop(new NumberCardModel(false, droppedValue, null, false));
                }
            }
        }
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
