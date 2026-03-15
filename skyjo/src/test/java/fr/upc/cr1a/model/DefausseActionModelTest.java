package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import fr.upc.cr1a.model.ActionCardModel.ActionType;

/**
 * Couverture de DefausseActionModel par tests unitaires 
 * @author Nassim
 */
class DefausseActionModelTest {

    private DefausseActionModel defausseAction;

    @BeforeEach
    void setUp() {
        defausseAction = new DefausseActionModel();
    }

    @Test
    void testEmpty() {
        assertEquals(0, defausseAction.stackSize());
        assertTrue(defausseAction.isEmpty());
    }


    @Test
    void testAddCardAtTop() {
        ActionCardModel card = new ActionCardModel(false, ActionType.TRADE_CARD_WITH_PLAYER);
        defausseAction.addCardAtTop(card);
        assertEquals(1, defausseAction.stackSize());
        assertEquals(ActionType.TRADE_CARD_WITH_PLAYER, ((ActionCardModel) defausseAction.getTopCard()).getType());
    }


    @Test
    void testMultiAddCardAtTop() {
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.DRAW_THREE_CARD));
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.PLAY_TWO_TURN));
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.SPY_ONE_COLUMN));
        assertEquals(3, defausseAction.stackSize());
    }


    @Test
    void testGetTopCard() {
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.DRAW_THREE_CARD));
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.PLAY_TWO_TURN));
        ActionCardModel top = (ActionCardModel) defausseAction.getTopCard();
        assertEquals(ActionType.PLAY_TWO_TURN, top.getType());
    }


    @Test
    void testRemoveCardAtTop() {
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.DRAW_THREE_CARD));
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.PLAY_TWO_TURN));
        defausseAction.removeCardAtTop();
        assertEquals(1, defausseAction.stackSize());
        assertEquals(ActionType.DRAW_THREE_CARD, ((ActionCardModel) defausseAction.getTopCard()).getType());
    }


    @Test
    void testGetCardAt() {
        ActionCardModel c1 = new ActionCardModel(false, ActionType.DRAW_THREE_CARD);
        ActionCardModel c2 = new ActionCardModel(false, ActionType.SPY_ONE_COLUMN);
        defausseAction.addCardAtTop(c1);
        defausseAction.addCardAtTop(c2);

        assertEquals(ActionType.SPY_ONE_COLUMN, ((ActionCardModel) defausseAction.getCardAt(0)).getType());
        assertEquals(ActionType.DRAW_THREE_CARD, ((ActionCardModel) defausseAction.getCardAt(1)).getType());
    }


    @Test
    void testReplaceCardAt() {
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.DRAW_THREE_CARD));
        ActionCardModel remplacement = new ActionCardModel(false, ActionType.ALL_PLAYERS_DROP_CARD);
        defausseAction.replaceCardAt(remplacement, 0);
        assertEquals(ActionType.ALL_PLAYERS_DROP_CARD, ((ActionCardModel) defausseAction.getTopCard()).getType());
    }


    @Test
    void testGetUnderCard() {
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.DRAW_THREE_CARD));
        defausseAction.addCardAtTop(new ActionCardModel(false, ActionType.SPY_ONE_COLUMN));
        ActionCardModel under = (ActionCardModel) defausseAction.getUnderCard();
        assertEquals(ActionType.DRAW_THREE_CARD, under.getType());
    }
}
