package fr.upc.cr1a.model;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Nassim
 * Classe qui compile tous les comportements des cartes action
 * Se réferer a ActionCardModel pour la description des actions
 */
public class ActionSkills {

    /**
     * implémentation de TRADE_CARD_WITH_PLAYER 
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'action
     */
    public static void tradeCardWithPlayer(Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex) {
        PlayerModel player = players.get(currentPlayerIndex);

        int targetPlayerIndex = -1;
        do {
            try {
                System.out.print("Index joueur cible (0-" + (players.size() - 1) + ", sauf " + currentPlayerIndex + ") : ");
                targetPlayerIndex = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine();
                targetPlayerIndex = -1;
            }
        } while (targetPlayerIndex < 0 || targetPlayerIndex >= players.size() || targetPlayerIndex == currentPlayerIndex);

        PlayerModel target = players.get(targetPlayerIndex);

        int myRow = askCardPos(scanner, "Votre ligne (0-2) : ", 0, 2);
        int myCol = askCardPos(scanner, "Votre colonne (0-3) : ", 0, 3);
        int opRow = askCardPos(scanner, "Ligne adverse (0-2) : ", 0, 2);
        int opCol = askCardPos(scanner, "Colonne adverse (0-3) : ", 0, 3);

        CardModel myCard = player.getHand().getCard(myRow, myCol);
        CardModel opCard = target.getHand().getCard(opRow, opCol);

        if (myCard == null || opCard == null) {
            System.out.println("Echange annulé: case vide.");
            return;
        }

        player.getHand().replaceCard(myRow, myCol, opCard);
        target.getHand().replaceCard(opRow, opCol, myCard);

        System.out.println("TRADE_CARD_WITH_PLAYER: succès");
    }
    
    /**
     * implémentation de REPLACE_CARD_IN_HAND
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'action
     */
    public static void replaceCardInHand(Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex) {
        PlayerModel player = players.get(currentPlayerIndex);

        int firstCardRow = askCardPos(scanner, "ligne 1e carte (0-2) : ", 0, 2);
        int firstCardCol = askCardPos(scanner, "colonne 1e carte (0-3) : ", 0, 3);
        int secondCardRow = askCardPos(scanner, "ligne 2e carte (0-2) : ", 0, 2);
        int secondCardCol = askCardPos(scanner, "colonne 2e carte (0-3) : ", 0, 3);

        CardModel firstCard = player.getHand().getCard(firstCardRow, firstCardCol);
        CardModel secondCard = player.getHand().getCard(secondCardRow, secondCardCol);

        if (firstCard == null || secondCard == null) {
            System.out.println("Echange annulé: case vide.");
            return;
        }

        player.getHand().replaceCard(secondCardRow, secondCardCol, firstCard);
        player.getHand().replaceCard(firstCardRow, firstCardCol, secondCard);
        System.out.println("REPLACE_CARD_IN_HAND: succès");
        
    }

    /**
     * implémentation de DRAW_THREE_CARD
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'action
     * @param gameManager le gestionnaire de jeu
     */
    public static void drawThreeCard(Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex, GameManager gameManager) {
        PlayerModel player = players.get(currentPlayerIndex);
        NumberCardModel firstDrawnCard = (NumberCardModel) gameManager.drawDeck();
        NumberCardModel secondDrawnCard = (NumberCardModel) gameManager.drawDeck();
        NumberCardModel thirdDrawnCard = (NumberCardModel) gameManager.drawDeck();

        firstDrawnCard.returnCard();
        secondDrawnCard.returnCard();
        thirdDrawnCard.returnCard();

        grantActionForStar(gameManager, player, firstDrawnCard);
        grantActionForStar(gameManager, player, secondDrawnCard);
        grantActionForStar(gameManager, player, thirdDrawnCard);

        System.out.println("Cartes disponibles : " + firstDrawnCard.getCardValue() + " | " + secondDrawnCard.getCardValue() + " | " +  thirdDrawnCard.getCardValue());

        int chosedCard = -1;
        do {
            try {
                System.out.println("choisir une carte (1-3) a remplacer ou (4) pour annuler");
                chosedCard = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine();
                chosedCard = -1;
            }
        } while (chosedCard < 0 || chosedCard > 4);

        switch (chosedCard) {
            case 1:
                gameManager.getDiscardPile().addCardAtTop(secondDrawnCard);
                gameManager.getDiscardPile().addCardAtTop(thirdDrawnCard);
                int firstCardRow = askCardPos(scanner, "ligne carte (0-2) : ", 0, 2);
                int firstCardCol = askCardPos(scanner, "colonne carte (0-3) : ", 0, 3);
                CardModel oldCard = player.getHand().getCard(firstCardRow, firstCardCol);

                if (firstDrawnCard == null || oldCard == null) {
                    System.out.println("Echange annulé: case vide.");
                    return;
                }

                gameManager.getDiscardPile().addCardAtTop(oldCard);
                player.getHand().replaceCard(firstCardRow, firstCardCol, firstDrawnCard);
                break;
            case 2:
                gameManager.getDiscardPile().addCardAtTop(firstDrawnCard);
                gameManager.getDiscardPile().addCardAtTop(thirdDrawnCard);
                int secondCardRow = askCardPos(scanner, "ligne carte (0-2) : ", 0, 2);
                int secondCardCol = askCardPos(scanner, "colonne carte (0-3) : ", 0, 3);
                CardModel oldCard2 = player.getHand().getCard(secondCardRow, secondCardCol);

                if (secondDrawnCard == null || oldCard2 == null) {
                    System.out.println("Echange annulé: case vide.");
                    return;
                }

                gameManager.getDiscardPile().addCardAtTop(oldCard2);
                player.getHand().replaceCard(secondCardRow, secondCardCol, secondDrawnCard);
                break;
            case 3:
                gameManager.getDiscardPile().addCardAtTop(firstDrawnCard);
                gameManager.getDiscardPile().addCardAtTop(secondDrawnCard);
                int thirdCardRow = askCardPos(scanner, "ligne carte (0-2) : ", 0, 2);
                int thirdCardCol = askCardPos(scanner, "colonne carte (0-3) : ", 0, 3);
                CardModel oldCard3 = player.getHand().getCard(thirdCardRow, thirdCardCol);

                if (thirdDrawnCard == null || oldCard3 == null) {
                    System.out.println("Echange annulé: case vide.");
                    return;
                }

                gameManager.getDiscardPile().addCardAtTop(oldCard3);
                player.getHand().replaceCard(thirdCardRow, thirdCardCol, thirdDrawnCard);
                break;
            default:
                break;
        }

        System.out.println("DRAW_THREE_CARD: succès");
        
    }

    /**
     * implémentation de PLAY_LAST_ACTION_IN_DEFAUSSE
     * IMPORTANT -> Pour le moment cette fonction a le comportement suivant: donne au joueur la carte en haut de la defausse action mais il ne la joue pas
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'action
     * @param gameManager le gestionnaire de jeu
     */
    public static void playLastActionInDefausse(Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex, GameManager gameManager) {
        if (gameManager.getDiscardActionPile().getTopCard() != null) {
            ActionCardModel topCard = (ActionCardModel) gameManager.getDiscardActionPile().getTopCard();
            PlayerModel player = players.get(currentPlayerIndex);
            player.getActionHand().add(topCard.getType());
            gameManager.getDiscardActionPile().removeCardAtTop();
            System.out.println("PLAY_LAST_ACTION_IN_DEFAUSSE: succès");
        }
        else {
            System.out.println("PLAY_LAST_ACTION_IN_DEFAUSSE: aucune carte action dans la defausse");
        }        
    }

    /**
     * implémentation de SPY_ON_COLUMN
     * IMPORTANT -> je n'ai pas mis de timer donc le joueur a autant de temps qu'il veut pour regarder sa colonne, il fait "entrer" pour continuer la manche
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'action
     * @param gameManager le gestionnaire de jeu
     */
    public static void spyOneColumn(Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex, GameManager gameManager) {
        int targetedCol = askCardPos(scanner, "entrer le numero de la colonne a reveler temporairement : ", 0, 3);
        PlayerModel player = players.get(currentPlayerIndex);

        System.out.println("\n--------------------------------zieuzieut--------------------------------\n");

        System.out.println("         Col 0   Col 1   Col 2   Col 3");
        for (int row = 0; row < 3; row++) {
            System.out.print("Ligne " + row + " ");
            for (int col = 0; col < 4; col++) {
                CardModel card = player.getHand().getCard(row, col);
                if (card == null) {
                    System.out.print("[  X  ]");
                } else if ((col != targetedCol) && (card.isFaceDown())) {
                    System.out.print("[  ?  ]");
                } else {
                    System.out.printf("[ %3d ]", card.getCardValue());
                }
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println("\n--------------------------------zieuzieut--------------------------------\n");
        System.out.println("SPY_ON_COLUMN: succès | presser ENTRER pour continuer...");   
        scanner.nextLine();   
    }

    /**
     * implémentation de ALL_PLAYERS_DROP_CARD
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'action
     * @param gameManager le gestionnaire de jeu
     * @param gmt le gameterminalmodel qui sert juste a montrer les mains des joueurs
     */
    public static void allPlayersDropCard(Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex, GameManager gameManager, GameTerminalModel gtm) {
        PlayerModel currentplayer = players.get(currentPlayerIndex);
        for (PlayerModel joueur : players) {
            if (joueur != currentplayer) {
                gtm.showHand(joueur, false);
                System.out.println("---------------------------------------------");
                int cardRow = askCardPos(scanner, "ligne de la carte a jeter (0-2) : ", 0, 2);
                int cardCol = askCardPos(scanner, "colonne de la carte a jeter (0-3) : ", 0, 3);

                int cardToDropValue = joueur.getHand().getCard(cardRow, cardCol).getCardValue();
                CardModel drawnCardFromPioche = gameManager.drawDeck();
                drawnCardFromPioche.returnCard();
                joueur.getHand().replaceCard(cardRow, cardCol, drawnCardFromPioche);
                grantActionForStar(gameManager, joueur, drawnCardFromPioche);

                System.out.println(joueur.getName() + " remplace son " + cardToDropValue + " par un " + drawnCardFromPioche.getCardValue());
                gameManager.getDiscardPile().addCardAtTop(new NumberCardModel(false, cardToDropValue, null, false));
                System.out.println("---------------------------------------------");

            }
        }
    }

    /**
     * implémentation de PLAY_TWO_TURN
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'action
     * @param gameManager le gestionnaire de jeu
     */
    public static void playTwoTurn(Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex, GameManager gameManager) {
        gameManager.grantExtraTurn();
        System.out.println("PLAY_TWO_TURN: succès");
    }


    /**
     * fonction qui demande la position d'une carte
     * la meilleure fonction du projet
     * @return la coordonnée de la carte
     */
    private static int askCardPos(Scanner scanner, String msg, int min, int max) {
        int value;
        do {
            try {
                System.out.print(msg);
                value = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine();
                value = -1;
            }
        } while (value < min || value > max);
        return value;
    }

    private static void grantActionForStar(GameManager gameManager, PlayerModel player, CardModel card) {
        if (player == null || card == null || !gameManager.withActionCard() || !GameManager.isStarCard(card)) {
            return;
        }
        ActionCardModel actionCard = gameManager.grantActionForStar(player);
        if (actionCard == null) {
            return;
        }
    }
}
