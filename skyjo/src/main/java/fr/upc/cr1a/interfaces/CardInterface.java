package fr.upc.cr1a.interfaces;

/**
 * @author Nassim
 * l'interface de CardModel
 * contenant les classes a implémenter obligatoirement
 */
public interface CardInterface {

    public void revealCard(boolean value);
    public void setPathImage(String path);

    public boolean isFaceDown();
    public String getPathImage();

}
