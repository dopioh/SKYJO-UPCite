package fr.upc.cr1a.model;
import fr.upc.cr1a.interfaces.NumberCardInterface;

/**
 * @author Nassim
 * @implements l'interface NumberCardInterface
 * Classe permettant de créer des cartes numérotés
 */
public class NumberCardModel implements NumberCardInterface  {
    private int cardValue;
    private String cardColor; // a changé quand on aura l'ui
    private boolean faceDown; // true : face cachée

    public NumberCardModel(int cardValue, String cardColor, boolean faceDown) {
        this.cardValue = cardValue;
        this.cardColor = cardColor;
        this.faceDown = faceDown;
    }

    // Getters & Setters
    public boolean isFaceDown() { return faceDown; }
    public int getCardValue() { return cardValue; }
    public String getCardColor() { return cardColor; }

    public void revealCard(boolean value) { faceDown = value; }
    public void setCardColor(String color) { cardColor = color ;}
    public void setCardValue(int x) { cardValue = x ;} 

}

