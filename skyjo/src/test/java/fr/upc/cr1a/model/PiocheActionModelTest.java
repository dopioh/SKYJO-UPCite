package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Couverture de PiocheActionModel par tests unitaires 
 * @author Nassim
 */
class PiocheActionModelTest {

    private PiocheActionModel piocheAction;

    @BeforeEach
    void setUp() {
        piocheAction = new PiocheActionModel();
    }


    @Test
    void testPiocheActionFull() {
        // 6 types * 3 cartes = 18
        assertEquals(18, piocheAction.stackSize());
    }


    @Test
    void testFullFaceDown() {
        for (int i = 0; i < piocheAction.stackSize(); i++) {
            assertTrue(piocheAction.getCardAt(i).isFaceDown());
        }
    }


    @Test
    void testGetTopCard() {
        CardModel top = piocheAction.getTopCard();
        assertNotNull(top);
    }


    @Test
    void testRemoveCardAtTop() {
        int tailleAvant = piocheAction.stackSize();
        piocheAction.removeCardAtTop();
        assertEquals(tailleAvant - 1, piocheAction.stackSize());
    }


    @Test
    void testDefausseInPioche() {
        DefausseActionModel defausseAction = new DefausseActionModel();

        int taille = piocheAction.stackSize();
        for (int i = 0; i < taille; i++) {
            CardModel card = piocheAction.getTopCard();
            piocheAction.removeCardAtTop();
            defausseAction.addCardAtTop(card);
        }
        assertEquals(0, piocheAction.stackSize());

        int tailleDefausse = defausseAction.stackSize();
        piocheAction.DefausseInPioche(defausseAction);

        assertEquals(1, defausseAction.stackSize());
        assertEquals(tailleDefausse - 1, piocheAction.stackSize());
    }


    @Test
    void testIsEmpty() {
        assertFalse(piocheAction.isEmpty());
        int taille = piocheAction.stackSize();
        for (int i = 0; i < taille; i++) {
            piocheAction.removeCardAtTop();
        }
        assertTrue(piocheAction.isEmpty());
    }
}
