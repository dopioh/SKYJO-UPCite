package fr.upc.cr1a.model;
import fr.upc.cr1a.model.interfaces.CardInterface;

/**
 * @author Nassim
 * @implements l'interface CardInterface
 * Classe permettant de créer des cartes
 */

public class CardModel implements CardInterface  {
    private boolean faceDown; // true : face cachée
    private String pathImage;

    public CardModel(boolean faceDown, String pathImage) {
        this.faceDown = faceDown;
        this.pathImage = pathImage;
    }

    // constructeur CardModel mais sans carte
    public CardModel(boolean faceDown) {
        this.faceDown = faceDown;
        this.pathImage = null;
    }

    // Getters & Setters
    public boolean isFaceDown() { return this.faceDown; }
    public String getPathImage() { return this.pathImage; }

    public void returnCard() { this.faceDown = !this.faceDown; } 
    public void setPathImage(String path) { this.pathImage = path ;}

    public int getCardValue() { return 0 ; }
    public String getCardColor() { return null ; }
}