package fr.upc.cr1a.model.interfaces;

/**
 * @author Nassim
 * l'interface de CardModel
 * contenant les classes a implémenter obligatoirement
 */
public interface CardInterface {

    public void returnCard();
    public void setPathImage(String path);

    public boolean isFaceDown();
    public String getPathImage();

}
