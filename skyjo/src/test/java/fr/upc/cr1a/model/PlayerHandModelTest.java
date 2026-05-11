package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour PlayerHandModel
 */
public class PlayerHandModelTest {
    
    private PlayerHandModel hand;
    private CardModel testCard;

    @BeforeEach
    public void setUp() {
        hand = new PlayerHandModel();
        testCard = new CardModel(true, "/images/card.png");
    }

    @Test
    public void testHandInitialization() {
        assertEquals(3, hand.getRows(), "Hand should have 3 rows");
        assertEquals(4, hand.getColumns(), "Hand should have 4 columns");
    }

    @Test
    public void testPlaceCard() {
        hand.placeCard(0, 0, testCard);
        assertEquals(testCard, hand.getCard(0, 0), "Card should be placed at position");
    }

    @Test
    public void testReplaceCard() {
        hand.placeCard(0, 0, testCard);
        CardModel newCard = new CardModel(false, "/images/newcard.png");
        hand.replaceCard(0, 0, newCard);
        assertEquals(newCard, hand.getCard(0, 0), "Card should be replaced");
        assertNotEquals(testCard, hand.getCard(0, 0), "Old card should be gone");
    }

    @Test
    public void testRevealCard() {
        hand.placeCard(1, 1, testCard);
        assertTrue(testCard.isFaceDown(), "Card should be face down initially");
        hand.revealCard(1, 1);
        assertFalse(testCard.isFaceDown(), "Card should be face up after reveal");
    }

    @Test
    public void testAllCardsRevealed() {
        // Ajouter des cartes et les révéler
        for (int i = 0; i < hand.getRows(); i++) {
            for (int j = 0; j < hand.getColumns(); j++) {
                CardModel card = new CardModel(true);
                hand.placeCard(i, j, card);
            }
        }
        
        assertFalse(hand.allCardsRevealed(), "Not all cards should be revealed");
        
        // Révéler toutes les cartes
        for (int i = 0; i < hand.getRows(); i++) {
            for (int j = 0; j < hand.getColumns(); j++) {
                hand.revealCard(i, j);
            }
        }
        
        assertTrue(hand.allCardsRevealed(), "All cards should be revealed");
    }

    @Test
    public void testCountRevealedCards() {
        // Placer 3 cartes
        CardModel card1 = new CardModel(true);
        CardModel card2 = new CardModel(true);
        CardModel card3 = new CardModel(false);  // Déjà révélée
        
        hand.placeCard(0, 0, card1);
        hand.placeCard(0, 1, card2);
        hand.placeCard(0, 2, card3);
        
        assertEquals(1, hand.countRevealedCards(), "Should count 1 revealed card");
        
        hand.revealCard(0, 0);
        assertEquals(2, hand.countRevealedCards(), "Should count 2 revealed cards");
    }

    @Test
    public void testEmptyGridCountRevealed() {
        assertEquals(0, hand.countRevealedCards(), "Empty hand should have 0 revealed cards");
    }

    @Test
    public void testMultiplePlacement() {
        // Placer plusieurs cartes à différentes positions
        for (int i = 0; i < 5; i++) {
            CardModel card = new CardModel(i % 2 == 0);  // Alternately face up/down
            hand.placeCard(i / 4, i % 4, card);
        }
        
        assertNotNull(hand.getCard(0, 0), "Card at (0,0) should not be null");
        assertNotNull(hand.getCard(0, 1), "Card at (0,1) should not be null");
        assertNotNull(hand.getCard(1, 0), "Card at (1,0) should not be null");
    }
}
