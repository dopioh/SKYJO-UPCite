package fr.upc.cr1a.model;

/*
*Temporaire
 */

public interface CardContainer {
    
    void addCard(Card card); // ajoute une carte au conteneur
    void removeCard(Card card); // retire une carte du conteneur
    int sizeOfPile(); // retourne le nombre de cartes dans le conteneur
    boolean isEmpty(); // vérifie si le conteneur est vide
}
