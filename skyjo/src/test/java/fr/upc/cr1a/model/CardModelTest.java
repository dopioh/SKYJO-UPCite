package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour CardModel
 */
public class CardModelTest {
    
    private CardModel cardFaceDown;
    private CardModel cardFaceUp;
    private CardModel cardWithPath;

    @BeforeEach
    public void setUp() {
        cardFaceDown = new CardModel(true);
        cardFaceUp = new CardModel(false);
        cardWithPath = new CardModel(true, "/images/card.png");
    }

    @Test
    public void testCardInitializationFaceDown() {
        assertTrue(cardFaceDown.isFaceDown(), "Card should be face down");
        assertNull(cardFaceDown.getPathImage(), "Card should have no path initially");
    }

    @Test
    public void testCardInitializationFaceUp() {
        assertFalse(cardFaceUp.isFaceDown(), "Card should be face up");
    }

    @Test
    public void testCardWithPath() {
        assertEquals("/images/card.png", cardWithPath.getPathImage(), "Card should have correct path");
        assertTrue(cardWithPath.isFaceDown(), "Card should be face down");
    }

    @Test
    public void testReturnCard() {
        assertTrue(cardFaceDown.isFaceDown(), "Card should start face down");
        cardFaceDown.returnCard();
        assertFalse(cardFaceDown.isFaceDown(), "Card should be face up after return");
        cardFaceDown.returnCard();
        assertTrue(cardFaceDown.isFaceDown(), "Card should be face down after second return");
    }

    @Test
    public void testSetPathImage() {
        assertNull(cardFaceDown.getPathImage());
        cardFaceDown.setPathImage("/new/path.png");
        assertEquals("/new/path.png", cardFaceDown.getPathImage());
    }

    @Test
    public void testGetCardValue() {
        assertEquals(0, cardFaceDown.getCardValue(), "Default card value should be 0");
    }

    @Test
    public void testGetCardColor() {
        assertNull(cardFaceDown.getCardColor(), "Default card color should be null");
    }
}
