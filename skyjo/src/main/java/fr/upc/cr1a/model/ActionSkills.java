package fr.upc.cr1a.model;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Nassim
 * Classe qui execute les des cartes action
 */
public class ActionSkills {

    /**
     * implémentation de TRADE_CARD_WITH_PLAYER (remplacer une carte avec un autre joueur)
     * @param players liste de tous les joueurs 
     * @param currentPlayerIndex index du joueur qui fait l'actio,
     */
    public static void tradeCardWithPlayer( Scanner scanner, ArrayList<PlayerModel> players, int currentPlayerIndex) {
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
        boolean myCardStar = ( myCard instanceof NumberCardModel carte) && carte.isStar();
        boolean opCardStar = ( opCard instanceof NumberCardModel carte) && carte.isStar();

        if (myCard == null || opCard == null) {
            System.out.println("Echange annulé: case vide.");
            return;
        }

        // echange
        player.getHand().replaceCard(myRow, myCol, opCard);
        target.getHand().replaceCard(opRow, opCol, myCard);

        // update du compteur d'etoile
        // pour le joueur qui fait l'action
        if (!myCardStar && opCardStar) {
            player.addStarsScore();      // gagne une étoile
        } else if (myCardStar && !opCardStar) {
            player.subStarsScore();      // perd une étoile
        }

        // pour le joueur opposé
        if (!opCardStar && myCardStar) {
            target.addStarsScore();
        } else if (opCardStar && !myCardStar) {
            target.subStarsScore();
        }

        System.out.println("TRADE_CARD_WITH_PLAYER: échange effectué.");
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
