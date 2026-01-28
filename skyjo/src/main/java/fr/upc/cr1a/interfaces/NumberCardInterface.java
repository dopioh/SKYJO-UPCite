package fr.upc.cr1a.interfaces;
import fr.upc.cr1a.model.NumberCardModel;

public interface NumberCardInterface {
    public void CardReveal(NumberCardModel carte);
    public void setCardColor(NumberCardModel carte);
    public void setCardValue(NumberCardModel carte);

    public boolean isFaceDown(NumberCardModel carte);
    public int getCardValue(NumberCardModel carte);
    public String getCardColor(NumberCardModel carte);

}
