package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Couverture de BotHandModel par tests unitaires 
 * @author Nassim
 */
class BotHandModelTest {

    private BotHandModel hand;

    @BeforeEach
    void setUp() {
        hand = new BotHandModel();
        for (int r = 0; r < hand.getRows(); r++) {
            for (int c = 0; c < hand.getColumns(); c++) {
                hand.placeCard(r, c, new NumberCardModel(true, r+c, "vert", false));
            }
        }
    }


    @Test
    void testRevealRandomCard() {
        int reveleesAvant = hand.countRevealedCards();
        hand.revealRandomCard(1);
        assertEquals(reveleesAvant + 1, hand.countRevealedCards());
    }


    @Test
    void testRevealRandomCard2() {
        hand.revealRandomCard(3);
        assertEquals(3, hand.countRevealedCards());
    }


    @Test
    void testRevealRandomCard3() {
        hand.revealRandomCard(12); // retourne toutes les cartes
        assertTrue(hand.allCardsRevealed());
    }


    @Test
    void testNotSameCard() {
        // toutes face cachée, aucune ne sera trouvée
        CardModel carte = new NumberCardModel(false, 99, "vert", false);
        int[] coord = hand.sameCardInHandPostition(carte);
        assertEquals(-1, coord[0]);
        assertEquals(-1, coord[1]);
    }

    @Test
    void testSameCard() {
        hand.revealCard(0, 0);
        CardModel recherche = new NumberCardModel(false, 0, "vert", false);
        int[] coord = hand.sameCardInHandPostition(recherche);
        assertEquals(0, coord[0]); // col
        assertEquals(0, coord[1]); // row
    }


    @Test
    void testTradeInHand() {
        // (0,1) avec valeur 1 et (1,1) est face cachée avec valeur 2
        hand.revealCard(0, 1);
        NumberCardModel nouvelleCarteValeur1 = new NumberCardModel(false, 1, "vert", false);
        hand.tradeInHand(nouvelleCarteValeur1);
        // la nouvelle carte doit être dans la colonne 1
        boolean trouvee = false;
        for (int r = 0; r < hand.getRows(); r++) {
            CardModel card = hand.getCard(r, 1);
            if (card == nouvelleCarteValeur1) {
                trouvee = true;
                break;
            }
        }
        assertTrue(trouvee);
    }


    @Test
    void testIsAcceptableCard() {
        NumberCardModel carte = new NumberCardModel(false, 3, "vert", false);
        assertTrue(hand.isAcceptableCard(5, carte));
        assertTrue(hand.isAcceptableCard(3, carte));
        assertFalse(hand.isAcceptableCard(2, carte));
    }


    @Test
    void testGetRowsEtColumns() {
        assertEquals(3, hand.getRows());
        assertEquals(4, hand.getColumns());
    }
}
