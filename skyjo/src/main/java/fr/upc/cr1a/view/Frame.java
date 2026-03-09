package fr.upc.cr1a.view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.upc.cr1a.controller.AccountController;
import fr.upc.cr1a.controller.ConnexionController;
import fr.upc.cr1a.controller.MainMenuController;
import fr.upc.cr1a.controller.PlayersSetupController;
import fr.upc.cr1a.controller.SettingsOverlayController;
import fr.upc.cr1a.model.AudioManager;

import java.awt.CardLayout;
import java.awt.Rectangle;

/**
 * Classe qui sert a instancier la fenetre de jeu (JFrame)
 * Dans le projet Worms elle etait fusionner avec GameLauncher mais ca faisait que GameLauncher faisaient 2k lignes
 * Et aussi que on avait de la view dans model
 */
public class Frame extends JFrame{
    
    private CardLayout cardLayout;
    private MainMenuPanel mainMenuPanel;
    private SettingsOverlay settingsOverlay;
    private boolean fullscreen = false;
    private Rectangle windowedBounds;
    private final AudioManager audioManager;
    private PlayersSetupPanel playersSetupPanel;
    private ConnexionPanel connexionPanel;

    // Controller 
    private AccountController account; // sert a rien pour le moment
    private ConnexionController connexionController;


    public Frame() {

        setTitle("SKYJO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        connexionPanel = new ConnexionPanel();
        add(connexionPanel, "CONNEXION");

        mainMenuPanel = new MainMenuPanel();
        add(mainMenuPanel, "MENU");
        
        playersSetupPanel = new PlayersSetupPanel();
        add(playersSetupPanel, "PLAYERS");

        //Overlay paramètre
        settingsOverlay = new SettingsOverlay();
        setGlassPane(settingsOverlay);

        //Contrôleurs
        audioManager = new AudioManager();
        new SettingsOverlayController(settingsOverlay, this, audioManager);
        new MainMenuController(this, mainMenuPanel);
        new PlayersSetupController(this, playersSetupPanel);
        this.connexionController = new ConnexionController(this, connexionPanel);
        this.account = new AccountController(mainMenuPanel);

        setVisible(true);
    }
    
    public MainMenuPanel getMainMenuPanel() {
        return mainMenuPanel;
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(getContentPane(), panelName);
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void showSettingsOverlay() {
        settingsOverlay.setVisible(true);
        settingsOverlay.requestFocusInWindow();
        settingsOverlay.revalidate();
        settingsOverlay.repaint();
    }

    public void hideSettingsOverlay() {
        settingsOverlay.setVisible(false);
        repaint();
    }

    public boolean isSettingsOverlayVisible() {
        return settingsOverlay.isVisible();
    }

    public void enterFullscreen() {
        if (fullscreen) return;
        windowedBounds = getBounds();  
        dispose(); 
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        fullscreen = true;
    }

    public void exitFullscreen() {
        if (!fullscreen) return;
        dispose();
        setUndecorated(false);
        setExtendedState(JFrame.NORMAL);
        setBounds(windowedBounds);
        setVisible(true);
        fullscreen = false;
    }

    public void toggleFullscreen() {
        if (fullscreen) exitFullscreen();
        else enterFullscreen();
    }

    public boolean isFullscreen(){
        return fullscreen;
    }

    public PlayersSetupPanel getPlayersSetupPanel(){
        return playersSetupPanel;
    }

    public AccountController getAccountController(){
        return this.account;
    }
}
