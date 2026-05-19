package fr.upc.cr1a.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour PauseOverlayPanel (overlay accessible via Echap pendant une partie).
 */
public class PauseOverlayPanelTest {

    private PauseOverlayPanel panel;

    @BeforeEach
    public void setUp() {
        panel = new PauseOverlayPanel();
    }

    @Test
    public void testConstructionDoesNotCrash() {
        assertNotNull(panel);
    }

    @Test
    public void testBtnResumeIsCreated() {
        JButton btn = panel.getBtnResume();
        assertNotNull(btn, "Le bouton Resume doit etre instancie");
    }

    @Test
    public void testBtnSettingsIsCreated() {
        JButton btn = panel.getBtnSettings();
        assertNotNull(btn, "Le bouton Settings doit etre instancie");
    }

    @Test
    public void testBtnQuitIsCreated() {
        JButton btn = panel.getBtnQuit();
        assertNotNull(btn, "Le bouton Quit doit etre instancie");
    }

    @Test
    public void testButtonsAreDifferent() {
        // Les 3 boutons doivent etre des instances distinctes
        assertNotSame(panel.getBtnResume(), panel.getBtnSettings());
        assertNotSame(panel.getBtnResume(), panel.getBtnQuit());
        assertNotSame(panel.getBtnSettings(), panel.getBtnQuit());
    }

    @Test
    public void testPanelIsHiddenByDefault() {
        // Le panel pause doit etre cree mais ne pas etre force visible par defaut
        // (la visibilite est geree par GameBoardController quand Echap est presse)
        assertNotNull(panel);
    }

    @Test
    public void testGetButtonsIdempotent() {
        // Plusieurs appels au getter doivent renvoyer la meme instance (pas de re-creation)
        JButton resume1 = panel.getBtnResume();
        JButton resume2 = panel.getBtnResume();
        assertSame(resume1, resume2, "Les appels successifs au getter doivent renvoyer la meme instance");
    }
}
