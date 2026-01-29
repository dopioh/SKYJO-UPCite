package fr.upc.cr1a.interfaces;

import fr.upc.cr1a.model.CardModel;

/**
 * @author Nassim
 * l'interface de CardStackModel
 * contenant les classes a implémenter obligatoirement
 */
public interface CardStackInterface {
    public int stackSize();
    public CardModel getCardAt(int indice);
    public CardModel getTopCard();
    public CardModel getUnderCard();
    public void afficheStack();
    public boolean isEmpty();
    
    public void addCardAtTop(CardModel carte);
    public void removeCardAtTop();
    public void replaceCardAt(CardModel carte, int indice);

}