package fr.upc.cr1a.interfaces;

/**
 * @author Nassim
 * l'interface de NumberCardModel
 * contenant classes a implémenter obligatoirement
 */
public interface NumberCardInterface {
    public void revealCard(boolean value);
    public void setCardColor(String color);
    public void setCardValue(int x);

    public boolean isFaceDown();
    public int getCardValue();
    public String getCardColor();

}
