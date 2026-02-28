package fr.upc.cr1a.model;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Nassim
 * Classe qui execute les des cartes action
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
     * fonction qui demande la position d'une carte
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
}
