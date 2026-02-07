package fr.upc.cr1a.model;

/**
* Modèle de la main d'un bot dans le jeu Skyjo
* Gère une grille de cartes 4x3 avec des fonctionnalités des règles du jeu

* @author Nassim
*/

public class BotHandModel extends PlayerHandModel {
    //constructeur
    public BotHandModel(){
        super();
    }

    /**
     * revele n cartes de la main du bot
     * @param n le nombre de carte a reveler
     */
    public void revealRandomCard(int n) {
        for (int i = 0; i < n; i++) {
            int randomRow = (int) (Math.random() * getRows()); // cast pour convertir le double en integer
            int randomCol = (int) (Math.random() * getColumns());
            revealCard(randomRow, randomCol);
        }
    }

    /**
     * @param carte la carte qu'on cherche dans la main
     * @return un tableau [x,y] des coordonées d'une carte similaire dans la main 
     */
    public int[] sameCardInHand(CardModel carte) {
        int [] coord = new int[2];
        for (int i = 0; i < getColumns(); i++) {
            for (int j = 0; j < getRows(); j++) {
                if (getCard(i, j) == carte) {
                    coord[0] = i;
                    coord[1] = j;
                    break;
                }
            }
        }
        coord[0] = -1; // si on trouve rien les coordonnées sont -1 -1
        coord[1] = -1;
        return coord;
    }

    /**
     * remplace une autre carte par celle en parametre si la carte se trouve deja dans la main du bot
     * @param carte la carte a ajouter dans la main
     * actuellement : remplace uniquement sur les colonnes
     */
    public void tradeInHand(CardModel carte) {
        int[] sameCardInHandPostition = sameCardInHand(carte);
        int actualCol = sameCardInHandPostition[0];

        if (sameCardInHandPostition[0] != -1 && sameCardInHandPostition[1] != -1) {
            for (int i = 0; i < getRows(); i++) {
                if (isAvailable(actualCol, i) && getCard(actualCol, i) != carte) {
                    replaceCard(actualCol, i, carte);
                    break;

                }
            }
        }
    }

    /**
     * regarde si la carte est au dessus du seuil arbitraire n du bot
     * @param n le seuil n
     * @param carte la carte a regarder
     */
    public boolean isAcceptableCard(int n, CardModel carte) {
        return carte.getCardValue() <= n;
    }
}
