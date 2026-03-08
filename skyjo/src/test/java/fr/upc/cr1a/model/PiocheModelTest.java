package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Couverture de PiocheModel par tests unitaires 
 * @author Nassim
 */
class PiocheModelTest {

    private PiocheModel pioche;

    @BeforeEach
    void setUp() {
        pioche = new PiocheModel(false);
    }


    @Test
    void testPiocheFull() {
        assertEquals(150, pioche.stackSize());
    }


    @Test
    void testFullFaceDown() {
        for (int i = 0; i < pioche.stackSize(); i++) {
            assertTrue(pioche.getCardAt(i).isFaceDown());
        }
    }


    @Test
    void testPiocheWithAction() {
        PiocheModel piocheAction = new PiocheModel(true);
        int starCount = 0;
        for (int i = 0; i < piocheAction.stackSize(); i++) {
            NumberCardModel card = (NumberCardModel) piocheAction.getCardAt(i);
            if (card.isStar()){
                starCount++;
            }
        }
        assertEquals(15, starCount);
    }


    @Test
    void testPiocheWithActionFull() {
        PiocheModel piocheAction = new PiocheModel(true);
        int total = 3 + 7 + 11 + 12 * 7 + 15;
        assertEquals(total, piocheAction.stackSize());
    }


    @Test
    void testDefausseInPioche() {
        DefausseModel defausse = new DefausseModel();
        int taillePioche = pioche.stackSize();
        for (int i = 0; i < taillePioche; i++) {
            CardModel card = pioche.getTopCard();
            pioche.removeCardAtTop();
            defausse.addCardAtTop(card);
        }
        assertEquals(0, pioche.stackSize());

        int tailleDefausse = defausse.stackSize();
        pioche.DefausseInPioche(defausse);

        assertEquals(1, defausse.stackSize());
        assertEquals(tailleDefausse - 1, pioche.stackSize());
    }


    @Test
    void testDefausseInPioche2() {
        DefausseModel defausse = new DefausseModel();
        defausse.addCardAtTop(new NumberCardModel(false, 5, "vert", false));
        int tailleAvant = pioche.stackSize();
        pioche.DefausseInPioche(defausse);
        assertEquals(tailleAvant, pioche.stackSize());
    }


    @Test
    void testGetTopCard() {
        CardModel top = pioche.getTopCard();
        assertNotNull(top);
        assertInstanceOf(NumberCardModel.class, top);
    }


    @Test
    void testRemoveCardAtTop() {
        int tailleAvant = pioche.stackSize();
        pioche.removeCardAtTop();
        assertEquals(tailleAvant - 1, pioche.stackSize());
    }
}
