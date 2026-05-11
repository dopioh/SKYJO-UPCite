package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour CardStackModel
 */
public class CardStackModelTest {
    
    private CardStackModel stack;

    @BeforeEach
    public void setUp() {
        stack = new CardStackModel();
    }

    @Test
    public void testInitialStack() {
        assertEquals(0, stack.stackSize(), "Stack should be empty initially");
    }

    @Test
    public void testAddCardAtTop() {
        CardModel card = new CardModel(true);
        stack.addCardAtTop(card);
        
        assertEquals(1, stack.stackSize());
        assertEquals(card, stack.getTopCard());
    }

    @Test
    public void testAddMultipleCardsAtTop() {
        CardModel card1 = new CardModel(true);
        CardModel card2 = new CardModel(false);
        CardModel card3 = new CardModel(true);
        
        stack.addCardAtTop(card1);
        stack.addCardAtTop(card2);
        stack.addCardAtTop(card3);
        
        assertEquals(3, stack.stackSize());
        assertEquals(card3, stack.getTopCard());
        assertEquals(card1, stack.getUnderCard());
    }

    @Test
    public void testGetCardAt() {
        CardModel card1 = new CardModel(true);
        CardModel card2 = new CardModel(false);
        
        stack.addCardAtTop(card1);
        stack.addCardAtTop(card2);
        
        assertEquals(card2, stack.getCardAt(0));
        assertEquals(card1, stack.getCardAt(1));
    }

    @Test
    public void testRemoveCardAtTop() {
        CardModel card1 = new CardModel(true);
        CardModel card2 = new CardModel(false);
        
        stack.addCardAtTop(card1);
        stack.addCardAtTop(card2);
        assertEquals(2, stack.stackSize());
        
        stack.removeCardAtTop();
        assertEquals(1, stack.stackSize());
        assertEquals(card1, stack.getTopCard());
    }

    @Test
    public void testReplaceCardAt() {
        CardModel card1 = new CardModel(true);
        CardModel card2 = new CardModel(false);
        CardModel replacement = new NumberCardModel(true, 5, "vert", false);
        
        stack.addCardAtTop(card1);
        stack.addCardAtTop(card2);
        
        stack.replaceCardAt(replacement, 0);
        assertEquals(replacement, stack.getTopCard());
    }

    @Test
    public void testGetStack() {
        assertNotNull(stack.getStack());
        assertEquals(0, stack.getStack().size());
    }

    @Test
    public void testEmptyStackOperations() {
        assertEquals(0, stack.stackSize());
        assertTrue(stack.getStack().isEmpty());
    }

    @Test
    public void testOrderOfCards() {
        CardModel c1 = new CardModel(true, "/1");
        CardModel c2 = new CardModel(true, "/2");
        CardModel c3 = new CardModel(true, "/3");
        
        stack.addCardAtTop(c1);
        stack.addCardAtTop(c2);
        stack.addCardAtTop(c3);
        
        assertEquals(c3, stack.getCardAt(0));
        assertEquals(c2, stack.getCardAt(1));
        assertEquals(c1, stack.getCardAt(2));
    }
}
