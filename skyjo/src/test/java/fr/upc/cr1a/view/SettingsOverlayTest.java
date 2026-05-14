package fr.upc.cr1a.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SettingsOverlayTest {

    private SettingsOverlay overlay;

    @BeforeEach
    public void setUp() {
        overlay = new SettingsOverlay();
    }

    @Test
    public void testTabGraphiqueButton() {
        assertNotNull(overlay.getTabGraphique(), "La section Reglage graphique ne doit pas etre null");
        assertNotNull(overlay.getTabGraphiquePanel(), "Le panneau de reglage graphique ne doit pas etre null");
    }
}
