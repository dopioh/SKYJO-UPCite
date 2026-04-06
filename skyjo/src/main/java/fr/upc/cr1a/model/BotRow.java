package fr.upc.cr1a.model;

import fr.upc.cr1a.model.interfaces.BotStrategyInterface;

/**
 * bot qui focus les colonnes
 * prend la carte que si elle est une carte déjà dans sa main.
 * sinon retourne une carte d'une autre colonne
 * 
 * @author Billy
 */
public class BotRow implements BotStrategyInterface {
    
    /**
     * constructeur par défaut
     */
    public BotRow() {
    }
    
    @Override
    public int getSeuil() {
        return 0; 
    }
    
    @Override
    public boolean TakeFromDiscard(BotHandModel hand, CardModel discardCard) {
        if (discardCard == null) return false;
        //prendre que si meme carte
        int[] sameCard = hand.sameCardInHandPostition(discardCard);
        return (sameCard[0] != -1);
    }
    
    @Override
    public boolean ExchangeDrawCard(BotHandModel hand, CardModel drawCard) {
        if (drawCard == null) return false;
        //prendre que si meme carte
        int[] sameCard = hand.sameCardInHandPostition(drawCard);
        return (sameCard[0] != -1);
    }
    
    @Override
    public int[] CardToExchange(BotHandModel hand, CardModel card) {
        int[] sameCardPosition = hand.sameCardInHandPostition(card);
        
        if (sameCardPosition[0] != -1) {
            //carte identique mettre dans la même colonne
            int actualCol = sameCardPosition[0];
            int sameCardRow = sameCardPosition[1];
            
            //essayer de révéler une carte cachée dans la même colonne
            for (int i = 0; i < hand.getRows(); i++) {
                CardModel cardInCol = hand.getCard(i, actualCol);
                if (cardInCol == null) continue;
                //ne pas remplacer la carte identique qu'on a trouvée
                if (i == sameCardRow) continue;
                if (cardInCol.isFaceDown()) {
                    return new int[]{actualCol, i};
                }
            }
            
            //remplacer une carte différente dans la même colonne
            for (int i = 0; i < hand.getRows(); i++) {
                CardModel cardInCol = hand.getCard(i, actualCol);
                if (cardInCol == null) continue;
                if (i == sameCardRow) continue;
                if (!cardInCol.isFaceDown() && cardInCol.getCardValue() != card.getCardValue()) {
                    return new int[]{actualCol, i};
                }
            }
        }
        
        //révéler une carte d'une autre colonne
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

    /**
     * décide si le bot doit jouer une carte action 
     * @return true si le bot doit jouer une action
     */
    public boolean shouldPlayAction() {
        return Math.random() < 0.5;
    }
}
