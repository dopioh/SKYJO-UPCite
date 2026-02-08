package fr.upc.cr1a.model;

/**
 *Classe qui permet au bot de jouer dans le terminal 
 * 
 * @author Billy
 */
public class BotTerminalModel {
    
    private static int n; // seuil pour accepter une carte
    
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
    public static boolean shouldExchangeDrawnCard(BotHandModel hand, CardModel drawCard) {
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
        //échanger la carte dans la main
        hand.tradeInHand(card);
        int[] sameCard = hand.sameCardInHandPostition(card);
        return sameCard;
    }
}
