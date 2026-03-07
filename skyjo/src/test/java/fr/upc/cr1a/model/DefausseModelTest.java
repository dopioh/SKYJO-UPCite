package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Couverture de DefausseModel par tests unitaires 
 * @author Nassim
 */
class DefausseModelTest {

    private DefausseModel defausse;

    @BeforeEach
    void setUp() {
        defausse = new DefausseModel();
    }

    @Test
    void testEmpty() {
        assertEquals(0, defausse.stackSize());
        assertTrue(defausse.isEmpty());
    }

    @Test
    void testAddCardAtTop() {
        CardModel carte = new NumberCardModel(true, 5, "vert", false);
        assertTrue(carte.isFaceDown());
        defausse.addCardAtTop(carte);
        assertFalse(carte.isFaceDown());
    }


    @Test
    void testGetTopCard() {
        NumberCardModel c1 = new NumberCardModel(false, 3, "vert", false);
        NumberCardModel c2 = new NumberCardModel(false, 7, "vert", false);
        defausse.addCardAtTop(c1);
        defausse.addCardAtTop(c2);
        assertEquals(7, defausse.getTopCard().getCardValue());
    }


    @Test
    void testRemoveCardAtTop() {
        defausse.addCardAtTop(new NumberCardModel(false, 1, "vert", false));
        defausse.addCardAtTop(new NumberCardModel(false, 2, "vert", false));
        assertEquals(2, defausse.stackSize());
        defausse.removeCardAtTop();
        assertEquals(1, defausse.stackSize());
        assertEquals(1, defausse.getTopCard().getCardValue());
    }


    @Test
    void testStackSize() {
        defausse.addCardAtTop(new NumberCardModel(false, 1, "vert", false));
        defausse.addCardAtTop(new NumberCardModel(false, 2, "vert", false));
        defausse.addCardAtTop(new NumberCardModel(false, 3, "vert", false));
        assertEquals(3, defausse.stackSize());
    }

    
    @Test
    void testGetCardAt() {
        NumberCardModel c1 = new NumberCardModel(false, 10, "vert", false);
        NumberCardModel c2 = new NumberCardModel(false, 5, "vert", false);
        defausse.addCardAtTop(c1);
        defausse.addCardAtTop(c2);
        assertEquals(5, defausse.getCardAt(0).getCardValue());
        assertEquals(10, defausse.getCardAt(1).getCardValue());
    }


    @Test
    void testGetUnderCard() {
        defausse.addCardAtTop(new NumberCardModel(false, 1, "vert", false));
        defausse.addCardAtTop(new NumberCardModel(false, 9, "vert", false));
        assertEquals(1, defausse.getUnderCard().getCardValue());
    }
}
