package fr.upc.cr1a.model;

import fr.upc.cr1a.model.interfaces.BotStrategyInterface;

/**
 * strat plus agressive pour finir le jeu plus vite
 * il prend des cartes hautes.
 * 
 * @author Billy
 */

public class BotHigh implements BotStrategyInterface {
    
    private int seuil = 8; // seuil plus élevé pour accepter des cartes
    
    /**
     * constructeur avec seuil 8
     */
    public BotHigh() {
        this.seuil = 8;
    }
    
    /**
     * Constructeur avec un seuil personnalisé
     * @param seuil le seuil pour accepter une carte
     */
    public BotHigh(int seuil) {
        this.seuil = seuil;
    }
    
    @Override
    public int getSeuil() {
        return seuil;
    }
    
    @Override
    public boolean TakeFromDiscard(BotHandModel hand, CardModel discardCard) {
        if (discardCard == null) return false;
        //défausse
        boolean isAcceptable = hand.isAcceptableCard(seuil, discardCard);
        int[] sameCard = hand.sameCardInHandPostition(discardCard);
        boolean gotSameCard = (sameCard[0] != -1);
        
        return isAcceptable || gotSameCard;
    }
    
    @Override
    public boolean ExchangeDrawCard(BotHandModel hand, CardModel drawCard) {
        if (drawCard == null) return false;
        
        //pioche
        boolean isAcceptable = hand.isAcceptableCard(seuil, drawCard);
        int[] sameCard = hand.sameCardInHandPostition(drawCard);
        boolean gotSameCard = (sameCard[0] != -1);
        
        return isAcceptable || gotSameCard;
    }
    
    @Override
    public int[] CardToExchange(BotHandModel hand, CardModel card) {
        int[] sameCardPosition = hand.sameCardInHandPostition(card);
        
        if (sameCardPosition[0] != -1) {
            int actualCol = sameCardPosition[0];
            int sameCardRow = sameCardPosition[1];
            
            // Révéler les cartes cachées de la colonne
            for (int i = 0; i < hand.getRows(); i++) {
                CardModel cardInCol = hand.getCard(i, actualCol);
                if (cardInCol == null) continue;
                if (i == sameCardRow) continue;
                if (cardInCol.isFaceDown()) {
                    return new int[]{actualCol, i};
                }
            }
            
            // Échanger une carte différente
            for (int i = 0; i < hand.getRows(); i++) {
                CardModel cardInCol = hand.getCard(i, actualCol);
                if (cardInCol == null) continue;
                if (i == sameCardRow) continue;
                if (!cardInCol.isFaceDown() && cardInCol.getCardValue() != card.getCardValue()) {
                    return new int[]{actualCol, i};
                }
            }
        }
        
        // Sinon, échanger une carte aléatoire face cachée
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
