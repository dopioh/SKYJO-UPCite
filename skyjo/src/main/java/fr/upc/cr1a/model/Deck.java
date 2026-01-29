package fr.upc.cr1a.model;

import java.util.ArrayList;
import java.util.List;

/*
*Temporaire
 */

public class Deck implements CardContainer {
    private List<Card> cards;

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