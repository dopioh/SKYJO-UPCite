package fr.upc.cr1a.model;

import java.util.ArrayList;

import fr.upc.cr1a.model.interfaces.CardStackInterface;

/**
 * @author Nassim
 * @implements l'interface CardStackInterface
 * Classe permettant de créer des piles de cartes (pour la pioche & defausse)
 */
public class CardStackModel implements CardStackInterface{
    ArrayList<CardModel> Stack = new ArrayList<CardModel>();

    // constructeur de la classe
    // 150 cartes numero
    // 36 cartes actions
    public CardStackModel() {
    }

    /**
     * retourne le nombre carte dans la pile
     * @return la taille de la pile
     */
    public int stackSize() {
        return Stack.size();
    }

    /**
     * retourne la carte a l'indice en paramètre
     * @return la carte (CardModel)
     */
    public CardModel getCardAt(int indice) {
        return Stack.get(indice);
    }

    /**
     * retourne la carte au dessus de la pile
     * @return la carte (CardModel)
     */
    public CardModel getTopCard() {
        return Stack.get(0);
    }

    /**
     * retourne la carte en dessous de la pile
     * @return la carte (CardModel)
     */
    public CardModel getUnderCard () {
        return Stack.get(this.stackSize()-1);
    }

    /**
     * ajoute la carte en parametre en haut de la pile
     * @param carte la carte a placer
     */
    public void addCardAtTop(CardModel carte) {
        Stack.add(0, carte);
    }

    /**
     * supprime la carte a l'indice pris en parametre
     * @param indice l'indice de la carte a supprimer
     */
    public void removeCardAtTop() {
        Stack.remove(0);
    }

    /**
     * ajoute la carte en parametre a la place en parametre
     * @param carte la carte a placer
     * @param indice la position de la carte a remplacer
     */
    public void replaceCardAt(CardModel carte, int indice) {
        Stack.set(indice, carte);
    }

    public void afficheStack() {
        for (int i = 0; i < this.stackSize(); i++) {
            System.out.println(i + " : " + getCardAt(i).getCardValue() + " | " + getCardAt(i).getCardColor() );
        }
    }

    /**
     * @return true si la stack est vide
     */
    public boolean isEmpty() {
        return this.stackSize() == 0;
    }

}
