package fr.upc.cr1a.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Couverture de NumberCardModel par tests unitaires 
 * @author Nassim
 */
class NumberCardModelTest {

    @Test
    void testConstructeurAvecImage() {
        NumberCardModel c = new NumberCardModel(true, "/test.png", 5, "vert", false);
        assertTrue(c.isFaceDown());
        assertEquals("/test.png", c.getPathImage());
        assertEquals(5, c.getCardValue());
        assertEquals("vert", c.getCardColor());
        assertFalse(c.isStar());
    }

    @Test
    void testConstructeurSansImage() {
        NumberCardModel c = new NumberCardModel(false, 12, "vert", false);
        assertFalse(c.isFaceDown());
        assertNull(c.getPathImage());
        assertEquals(12, c.getCardValue());
        assertEquals("vert", c.getCardColor());
    }

    @Test
    void testCarteStar() {
        NumberCardModel star = new NumberCardModel(true, 0, "starcolor", true);
        assertTrue(star.isStar());
        assertEquals(0, star.getCardValue());
        assertEquals("starcolor", star.getCardColor());
    }

    @Test
    void testSetCardValue() {
        NumberCardModel c = new NumberCardModel(true, 3, "vert", false);
        c.setCardValue(7);
        assertEquals(7, c.getCardValue());
    }

    @Test
    void testSetCardColor() {
        NumberCardModel c = new NumberCardModel(true, 3, "vert", false);
        c.setCardColor("rouge");
        assertEquals("rouge", c.getCardColor());
    }

    @Test
    void testReturnCard() {
        NumberCardModel c = new NumberCardModel(true, 1, "vert", false);
        assertTrue(c.isFaceDown());
        c.returnCard();
        assertFalse(c.isFaceDown());
        c.returnCard();
        assertTrue(c.isFaceDown());
    }
}
