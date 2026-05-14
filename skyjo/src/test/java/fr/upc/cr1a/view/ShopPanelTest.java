package fr.upc.cr1a.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShopPanelTest {

    private ShopPanel panel;

    @BeforeEach
    public void setUp() {
        panel = new ShopPanel();
    }

    @Test
    public void testButtons() {
        assertNotNull(panel.getBackButton(), "Le bouton Retour ne doit pas etre null");
        assertNotNull(panel.getClasseurButton(), "Le bouton Classeur ne doit pas etre null");

        DoodleButton[] buyButtons = panel.getBuyButtons();
        assertNotNull(buyButtons, "Le tableau des boutons d'achat ne doit pas etre null");
        for (DoodleButton button : buyButtons) {
            assertNotNull(button, "Chaque bouton d'achat doit etre non null");
        }
    }

    @Test
    public void testOpenClasseur() {
        panel.openClasseur();
        assertTrue(panel.isCaseOpeningVisible(), "L'overlay du classeur doit etre visible apres son ouverture");
    }

    @Test
    public void testGetSetMoney() {
        panel.setMoney(-5);
        assertEquals(0, panel.getMoney(), "Le montant ne doit pas etre negatif");

        panel.setMoney(123);
        assertEquals(123, panel.getMoney(), "Le montant doit etre mis a jour");
    }
}
