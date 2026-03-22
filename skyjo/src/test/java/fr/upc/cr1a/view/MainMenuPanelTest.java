package fr.upc.cr1a.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;

import static org.junit.jupiter.api.Assertions.*;

public class MainMenuPanelTest {

    private MainMenuPanel panel;

    @BeforeEach
    public void setUp() {
        panel = new MainMenuPanel();
    }

    @Test
    public void testCreation_SansException() {
        boolean executed = false;
        assertDoesNotThrow(() -> new MainMenuPanel());
        executed = true;
        assertTrue(executed, "La creation du MainMenuPanel ne doit pas lancer d'exception");
    }

    @Test
    public void testGetShopButton_NonNull() {
        assertNotNull(panel.getShopButton(), "Le bouton Boutique ne doit pas etre null");
    }

    @Test
    public void testGetPlayButton_NonNull() {
        assertNotNull(panel.getPlayButton(), "Le bouton Jouer ne doit pas etre null");
    }

    @Test
    public void testGetSettingsButton_NonNull() {
        assertNotNull(panel.getSettingsButton(), "Le bouton Reglages ne doit pas etre null");
    }

    @Test
    public void testGetAccountButton_NonNull() {
        JButton accountBtn = panel.getAccountButton();
        assertNotNull(accountBtn, "Le bouton de compte ne doit pas etre null");
    }

    @Test
    public void testShopButton_OnMouseEnter_SansException() {
        boolean executed = false;
        panel.getShopButton().onMouseEnter();
        executed = true;
        assertTrue(executed, "onMouseEnter sur le bouton Boutique ne doit pas lancer d'exception");
    }

    @Test
    public void testShopButton_OnMouseExit_SansException() {
        boolean executed = false;
        panel.getShopButton().onMouseExit();
        executed = true;
        assertTrue(executed, "onMouseExit sur le bouton Boutique ne doit pas lancer d'exception");
    }

    @Test
    public void testPlayButton_OnMouseEnter_SansException() {
        boolean executed = false;
        panel.getPlayButton().onMouseEnter();
        executed = true;
        assertTrue(executed, "onMouseEnter sur le bouton Jouer ne doit pas lancer d'exception");
    }

    @Test
    public void testPlayButton_OnMouseExit_SansException() {
        boolean executed = false;
        panel.getPlayButton().onMouseExit();
        executed = true;
        assertTrue(executed, "onMouseExit sur le bouton Jouer ne doit pas lancer d'exception");
    }

    @Test
    public void testSettingsButton_OnMouseEnter_SansException() {
        boolean executed = false;
        panel.getSettingsButton().onMouseEnter();
        executed = true;
        assertTrue(executed, "onMouseEnter sur le bouton Reglages ne doit pas lancer d'exception");
    }

    @Test
    public void testSettingsButton_OnMouseExit_SansException() {
        boolean executed = false;
        panel.getSettingsButton().onMouseExit();
        executed = true;
        assertTrue(executed, "onMouseExit sur le bouton Reglages ne doit pas lancer d'exception");
    }

    @Test
    public void testHoverAllerRetourTousBoutons_SansException() {
        boolean executed = false;
        panel.getShopButton().onMouseEnter();
        panel.getShopButton().onMouseExit();
        panel.getPlayButton().onMouseEnter();
        panel.getPlayButton().onMouseExit();
        panel.getSettingsButton().onMouseEnter();
        panel.getSettingsButton().onMouseExit();
        executed = true;
        assertTrue(executed, "Survol de tous les boutons du menu ne doit pas lancer d'exception");
    }
}
