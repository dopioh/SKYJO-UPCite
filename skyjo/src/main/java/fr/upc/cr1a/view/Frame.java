package fr.upc.cr1a.view;

import javax.swing.JFrame;

import fr.upc.cr1a.controller.MainMenuController;
import fr.upc.cr1a.controller.SettingsOverlayController;

import java.awt.CardLayout;

/**
 * Classe qui sert a instancier la fenetre de jeu (JFrame)
 * Dans le projet Worms elle etait fusionner avec GameLauncher mais ca faisait que GameLauncher faisaient 2k lignes
 * Et aussi que on avait de la view dans model
 */
public class Frame extends JFrame{
    
    private CardLayout cardLayout;
    private MainMenuPanel mainMenuPanel;
    private SettingsOverlay settingsOverlay;

    public Frame() {
        setTitle("SKYJO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        
        mainMenuPanel = new MainMenuPanel();
        add(mainMenuPanel, "MENU");

        //Overlay paramètre
        settingsOverlay = new SettingsOverlay();
        setGlassPane(settingsOverlay);

        //Contrôleurs
        new SettingsOverlayController(settingsOverlay);
        new MainMenuController(this, mainMenuPanel);

        setVisible(true);
    }
    
    public MainMenuPanel getMainMenuPanel() {
        return mainMenuPanel;
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(getContentPane(), panelName);
    }

    public void showSettingsOverlay() {
        settingsOverlay.setVisible(true);
        settingsOverlay.requestFocusInWindow();
        repaint();
    }

    public void hideSettingsOverlay() {
        settingsOverlay.setVisible(false);
        repaint();
    }

    public boolean isSettingsOverlayVisible() {
        return settingsOverlay.isVisible();
    }
}
