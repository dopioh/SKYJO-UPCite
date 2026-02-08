package fr.upc.cr1a.model;

/**
 *Classe qui permet au bot de jouer dans le terminal 
 * 
 * @author Billy
 */
public class BotTerminalModel {
    
    private static int n = 5; // seuil pour accepter une carte
    
    /**
     * regarede si le bot doit prendre la carte de la défausse
     * @param hand la main du bot
     * @param discardCard la carte sur la défausse
     * @return true si le bot prend, false sinon
     */
    public static boolean takeFromDiscard(BotHandModel hand, CardModel discardCard) {
        if (discardCard == null) return false;
        //prendre si la carte est en dessus de n 
        boolean isAcceptable = hand.isAcceptableCard(n, discardCard);
        //regarde si la carte est dans la main 
        int[] sameCard = hand.sameCardInHandPostition(discardCard);
        boolean gotSameCard = (sameCard[0] != -1);
        return isAcceptable || gotSameCard;
    }
    
    /**
     *regarde si le bot doit échanger la carte piochée ou la défausser
     * @param hand la main du bot
     * @param drawCard la carte piochée
     * @return true si échange, false si défausse
     */
    public static boolean exchangeDrawCard(BotHandModel hand, CardModel drawCard) {
        if (drawCard == null) return false;
        //échanger si la carte est en dessus de n
        boolean isAcceptable = hand.isAcceptableCard(n, drawCard);
        int[] sameCard = hand.sameCardInHandPostition(drawCard);
        boolean gotSameCard = (sameCard[0] != -1);
        return isAcceptable || gotSameCard;
    }
    
    /**
     * regarde quelle carte remplacer dans la main
     * @param hand la main du bot
     * @param card la carte à placer
     * @return tab[row][col]
     */
    public static int[] cardToExchange(BotHandModel hand, CardModel card) {
        //regarde si la carte est dans la main
        int[] sameCardPosition = hand.sameCardInHandPostition(card);
        
        if (sameCardPosition[0] != -1) {
            //meme carte en main
            int actualCol = sameCardPosition[0];
            int sameCardRow = sameCardPosition[1];
            
            //d'abord réveler les cartes caches de la colonne
            for (int i = 0; i < hand.getRows(); i++) {
                CardModel cardInCol = hand.getCard(i, actualCol);
                if (cardInCol == null) continue;
                //ne pas suppriemr le carte qu'o nvient de trouver
                if (i == sameCardRow) continue;
                if (cardInCol.isFaceDown()) {
                    return new int[]{actualCol, i};
                }
            }
            
            //si toutes les cartes sont découverts, échanger une carte retoruné différente de la carte
            for (int i = 0; i < hand.getRows(); i++) {
                CardModel cardInCol = hand.getCard(i, actualCol);
                if (cardInCol == null) continue;
                //ne pas suppriemr le carte qu'o nvient de trouver 
                if (i == sameCardRow) continue;
                if (!cardInCol.isFaceDown() && cardInCol.getCardValue() != card.getCardValue()) {
                    return new int[]{actualCol, i};
                }
            }
        }
        
        //sinon, échanger une carte aléatoire face caché
        for (int col = 0; col < hand.getColumns(); col++) {
            for (int row = 0; row < hand.getRows(); row++) {
                CardModel cardAt = hand.getCard(row, col);
                if (cardAt == null) continue;
                if (cardAt.isFaceDown()) {
                    return new int[]{col, row};
                }
            }
        }
        return new int[]{-1, -1};
    }
}
