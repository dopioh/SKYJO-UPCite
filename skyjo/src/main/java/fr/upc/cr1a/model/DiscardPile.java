package fr.upc.cr1a.model;

import java.util.Stack;

/*
*Temporaire
 */

public class DiscardPile implements CardContainer {
    private Stack<Card> cards;

    @Override
    public void addCard(Card card) {
        cards.add(card);
    }

    @Override
    public void removeCard(Card card) {
        cards.remove(card);
    }

    @Override
    public int sizeOfPile() {
        return cards.size();
    }

    @Override
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}