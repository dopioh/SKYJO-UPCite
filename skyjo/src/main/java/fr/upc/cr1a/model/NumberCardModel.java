package fr.upc.cr1a.model;

/**
 * @author Nassim
 * @extends la classe CardModel
 * Classe permettant de créer des cartes numérotés
 */
public class NumberCardModel extends CardModel  {
    private int cardValue;
    private String cardColor; // a changé quand on aura l'ui

    public NumberCardModel(boolean faceDown, String pathImage, int cardValue, String cardColor) {
        super(faceDown, pathImage);
        this.cardValue = cardValue;
        this.cardColor = cardColor;
    }

    // constructeur NumberCardModel sans image
    public NumberCardModel(boolean faceDown, int cardValue, String cardColor) {
        super(faceDown, null);
        this.cardValue = cardValue;
        this.cardColor = cardColor;
    }


    // Getters & Setters
    public int getCardValue() { return cardValue; }
    public String getCardColor() { return cardColor; }

    public void setCardColor(String color) { cardColor = color ;}
    public void setCardValue(int x) { cardValue = x ;} 
}

