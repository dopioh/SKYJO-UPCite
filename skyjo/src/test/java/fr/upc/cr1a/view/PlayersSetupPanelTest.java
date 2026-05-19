package fr.upc.cr1a.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour PlayersSetupPanel (ecran de configuration des joueurs avant partie solo).
 */
public class PlayersSetupPanelTest {

    private PlayersSetupPanel panel;

    @BeforeEach
    public void setUp() {
        panel = new PlayersSetupPanel();
    }

    @Test
    public void testConstructionDoesNotCrash() {
        assertNotNull(panel);
    }

    @Test
    public void testGetBtnBackIsCreated() {
        DoodleButton btn = panel.getBtnBack();
        assertNotNull(btn, "Le bouton Retour doit etre instancie");
    }

    @Test
    public void testGetBtnLaunchIsCreated() {
        DoodleButton btn = panel.getBtnLaunch();
        assertNotNull(btn, "Le bouton Lancer doit etre instancie");
    }

    @Test
    public void testGetPlayerCountButtonPanelIsCreated() {
        JPanel pcp = panel.getPlayerCountButtonPanel();
        assertNotNull(pcp);
    }

    @Test
    public void testGetPlayersContainerIsCreated() {
        JPanel container = panel.getPlayersContainer();
        assertNotNull(container);
    }

    @Test
    public void testGetActionModeToggleIsCreated() {
        JToggleButton toggle = panel.getActionModeToggle();
        assertNotNull(toggle);
    }

    @Test
    public void testActionModeDefaultDisabled() {
        // Par defaut, le mode action Skyjo doit etre desactive
        assertFalse(panel.isSkyjoActionEnabled(),
            "Le mode Skyjo Action doit etre desactive par defaut");
    }

    @Test
    public void testSetSelectedPlayerCount() {
        panel.setSelectedPlayerCount(4);
        assertEquals(4, panel.getSelectedPlayerCount());

        panel.setSelectedPlayerCount(8);
        assertEquals(8, panel.getSelectedPlayerCount());
    }

    @Test
    public void testRebuildPlayerRowsDoesNotThrow() {
        // rebuildPlayerRows doit gerer correctement de 2 a 8 joueurs
        for (int count = 2; count <= 8; count++) {
            final int c = count;
            assertDoesNotThrow(() -> panel.rebuildPlayerRows(c),
                "rebuildPlayerRows doit gerer " + c + " joueurs sans crasher");
        }
    }

    @Test
    public void testUpdateCardPreviewDoesNotThrow() {
        assertDoesNotThrow(() -> panel.updateCardPreview(true));
        assertDoesNotThrow(() -> panel.updateCardPreview(false));
    }

    @Test
    public void testGettersIdempotent() {
        DoodleButton back1 = panel.getBtnBack();
        DoodleButton back2 = panel.getBtnBack();
        assertSame(back1, back2);

        DoodleButton launch1 = panel.getBtnLaunch();
        DoodleButton launch2 = panel.getBtnLaunch();
        assertSame(launch1, launch2);
    }
}
