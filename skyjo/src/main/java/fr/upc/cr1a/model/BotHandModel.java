package fr.upc.cr1a.model;

/**
* Modèle de la main d'un bot dans le jeu Skyjo
* Gère une grille de cartes 4x3 avec des fonctionnalités des règles du jeu

* @author Nassim, Billy
*/

public class BotHandModel extends PlayerHandModel {
    //constructeur
    public BotHandModel(){
        super();
    }

    /**
     * revele n cartes de la main du bot
     * ne pas retourner de carte déjà retournée pour éviter que la game soit interminable
     * @param n le nombre de carte a reveler
     */
    public void revealRandomCard(int n) {
        for (int i = 0; i < n; i++) {
            int randomRow, randomCol;
            CardModel card;
            //cherche une carte qui n'est pas retournée et la retourne
            do {
                randomRow = (int) (Math.random() * getRows());
                randomCol = (int) (Math.random() * getColumns());
                card = getCard(randomRow, randomCol);
            } while (card == null || !card.isFaceDown());
            
            // Révéler la carte trouvée
            revealCard(randomRow, randomCol);
        }
    }

    /**
     * @param carte la carte qu'on cherche dans la main
     * @return un tableau [x,y] des coordonées d'une carte similaire dans la main 
     */
    public int[] sameCardInHandPostition(CardModel carte) {
        int [] coord = new int[2];
        //obligé de mettre ici psq ce que je veux pas c'est un retour d'un tab et pas un break useless
        coord[0] = -1; // si on trouve rien les coordonnées sont -1 -1
        coord[1] = -1;
    
        if (carte == null) {
            return coord;
        }
        
        for (int i = 0; i < getColumns(); i++) {
            for (int j = 0; j < getRows(); j++) {
                CardModel cardInHand = getCard(j, i);
                if (cardInHand != null && !cardInHand.isFaceDown() && 
                    cardInHand.getCardValue() == carte.getCardValue()) {
                    coord[0] = i;
                    coord[1] = j;
                    return coord;
                }
            }
        }
        return coord;
    }

    /**
     * remplace une autre carte par celle en parametre si la carte se trouve deja dans la main du bot
     * @param carte la carte a ajouter dans la main
     * actuellement : remplace uniquement sur les colonnes
     */
    public void tradeInHand(CardModel carte) {
        int[] sameCardPostition = sameCardInHandPostition(carte);
        int actualCol = sameCardPostition[0];

        if (sameCardPostition[0] != -1 && sameCardPostition[1] != -1) {
            //meme carte, on échange dans la meme colonne
            for (int i = 0; i < getRows(); i++) {
                CardModel cardInCol = getCard(i, actualCol);
                //empeche de remplacer la carte semblable
                if (cardInCol == getCard(sameCardPostition[1], actualCol)) {
                    continue;
                }
                if (cardInCol != null && cardInCol.isFaceDown()) {
                    replaceCard(i, actualCol, carte);
                    return;
                }
            }
            
            //si toutes les cartes sont révéles alors faut échanger une carte diff de la carte en main
            for (int i = 0; i < getRows(); i++) {
                CardModel cardInCol = getCard(i, actualCol);
                //empeche de remplacer la carte semblable
                if (cardInCol == getCard(sameCardPostition[1], actualCol)) {
                    continue;
                }
                if (cardInCol != null && cardInCol.getCardValue() != carte.getCardValue()) {
                    replaceCard(i, actualCol, carte);
                    return;
                }
            }
        }
        
        //rien alors choisit une carte aléatoire
        int randomRow, randomCol;
        do {
            randomRow = (int) (Math.random() * getRows());
            randomCol = (int) (Math.random() * getColumns());
        } while (getCard(randomRow, randomCol) == null);
        
        replaceCard(randomRow, randomCol, carte);
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
