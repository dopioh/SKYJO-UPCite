package fr.upc.cr1a.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CaseOpeningOverlayPanelTest {

    private CaseOpeningOverlayPanel panel;

    @BeforeEach
    public void setUp() {
        panel = new CaseOpeningOverlayPanel();
    }

    @Test
    public void testOpenClasseurVisible() {
        panel.openClasseur();
        assertTrue(panel.isOpen(), "L'overlay doit etre visible apres ouverture du classeur");
    }

    @Test
    public void testUnlockSkinNull_ReturnsFalse() {
        assertFalse(CaseOpeningOverlayPanel.unlockSkin(null), "unlockSkin(null) doit renvoyer false");
    }

    @Test
    public void testSelectedSkinPath_NotBlank() {
        String selected = CaseOpeningOverlayPanel.getSelectedSkinPath();
        assertNotNull(selected, "Le skin selectionne ne doit pas etre null");
        assertFalse(selected.isBlank(), "Le skin selectionne ne doit pas etre vide");
    }

    @Test
    public void testUnlockSkinThenIsUnlocked() {
        String path = "/test/skin.png";
        CaseOpeningOverlayPanel.unlockSkin(path);
        assertTrue(CaseOpeningOverlayPanel.isSkinUnlocked(path), "Le skin doit etre marque comme debloque");
    }
}
