package fr.upc.cr1a.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import fr.upc.cr1a.model.ActionCardModel.ActionType;

/**
 * Couverture de ActionCardModel par tests unitaires 
 * @author Nassim
 */
class ActionCardModelTest {

    @Test
    void testConstructeurAvecImage() {
        ActionCardModel card = new ActionCardModel(true, "/test.png", ActionType.TRADE_CARD_WITH_PLAYER);
        assertTrue(card.isFaceDown());
        assertEquals("/test.png", card.getPathImage());
        assertEquals(ActionType.TRADE_CARD_WITH_PLAYER, card.getType());
    }

    @Test
    void testConstructeurSansImage() {
        ActionCardModel card = new ActionCardModel(false, ActionType.PLAY_TWO_TURN);
        assertFalse(card.isFaceDown());
        assertNull(card.getPathImage());
        assertEquals(ActionType.PLAY_TWO_TURN, card.getType());
    }


    @Test
    void testReturnCard() {
        ActionCardModel card = new ActionCardModel(true, ActionType.PLAY_TWO_TURN);
        assertTrue(card.isFaceDown());
        card.returnCard();
        assertFalse(card.isFaceDown());
    }
}
