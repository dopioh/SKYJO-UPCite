package fr.upc.cr1a.view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.upc.cr1a.controller.AccountController;
import fr.upc.cr1a.controller.ConnexionController;
import fr.upc.cr1a.controller.GameModeSelectionController;
import fr.upc.cr1a.controller.LobbyBrowserController;
import fr.upc.cr1a.controller.LobbyRoomController;
import fr.upc.cr1a.controller.MainMenuController;
import fr.upc.cr1a.controller.PlayersSetupController;
import fr.upc.cr1a.controller.SettingsOverlayController;
import fr.upc.cr1a.controller.ShopController;
import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.utils.ImageCache;

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
    private GameModeSelectionPanel gameModeSelectionPanel;
    private LobbyBrowserPanel lobbyBrowserPanel;
    private LobbyRoomPanel lobbyRoomPanel;
    private SettingsOverlay settingsOverlay;
    private ShopPanel shopPanel;
    private boolean fullscreen = false;
    private Rectangle windowedBounds;
    private final AudioManager audioManager;
    private PlayersSetupPanel playersSetupPanel;
    private ConnexionPanel connexionPanel;
    private GameManager currentGame;

    // Controller
    private AccountController account; // sert a rien pour le moment
    private ConnexionController connexionController;
    private LobbyRoomController lobbyRoomController;


    public Frame() {
        // TEMPORAIRE: passe a false pour remettre l'ecran de connexion.
        //final boolean hideLoginTemporarily = false; // y'en a plus besoin mtn y'a une session inviter (c'est un peu la meme chose que ca pour le moment..)

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

        shopPanel = new ShopPanel();
        add(shopPanel, "SHOP");

        gameModeSelectionPanel = new GameModeSelectionPanel();
        add(gameModeSelectionPanel, "GAME_MODE");

        lobbyBrowserPanel = new LobbyBrowserPanel();
        add(lobbyBrowserPanel, "LOBBY_BROWSER");

        lobbyRoomPanel = new LobbyRoomPanel();
        add(lobbyRoomPanel, "LOBBY_ROOM");

        playersSetupPanel = new PlayersSetupPanel();
        add(playersSetupPanel, "PLAYERS");

        //Overlay paramètre
        settingsOverlay = new SettingsOverlay();
        setGlassPane(settingsOverlay);

        //Contrôleurs
        audioManager = new AudioManager();
        new SettingsOverlayController(settingsOverlay, this, audioManager);
        new MainMenuController(this, mainMenuPanel);
        new ShopController(this, shopPanel);
        new GameModeSelectionController(this, gameModeSelectionPanel);
        new LobbyBrowserController(this, lobbyBrowserPanel);
        lobbyRoomController = new LobbyRoomController(this, lobbyRoomPanel);
        new PlayersSetupController(this, playersSetupPanel);

        this.connexionController = new ConnexionController(this, connexionPanel);

        this.account = new AccountController(mainMenuPanel);

        // Precharge toutes les images du jeu en arriere-plan pour zero lag au lancement   // ça marchéeeeeeeeeeee enfinnnn c'etait ça la raison
        new Thread(() -> {
            // cartes
            ImageCache.preload(
                "/cartes/carteDos.png", "/cartes/carteVide.png",
                "/cartes/carteMoins2.png", "/cartes/carteMoins1.png",
                "/cartes/carte0.png", "/cartes/carte1.png", "/cartes/carte2.png",
                "/cartes/carte3.png", "/cartes/carte4.png", "/cartes/carte5.png",
                "/cartes/carte6.png", "/cartes/carte7.png", "/cartes/carte8.png",
                "/cartes/carte9.png", "/cartes/carte10.png", "/cartes/carte11.png",
                "/cartes/carte12.png", "/cartes/carteEtoile.png",
                "/cartes/carteActionTrade.png", "/cartes/carteActionReplace.png",
                "/cartes/carteActionTwoTurn.png", "/cartes/carteActionDraw3.png",
                "/cartes/carteActionReplay.png", /*"/cartes/carteActionDefense.png",*/
                "/cartes/carteActionDrop.png", "/cartes/carteActionSpy.png",
                "/cartes/carteActionDos.png"
            );
            // fonds et topbar
            ImageCache.preload(
                "/plateau/red_bg.png", "/plateau/green_bg.png", "/plateau/yellow_bg.png",
                "/plateau/red_topbar.png", "/plateau/green_topbar.png", "/plateau/yellow_topbar.png"
            );
            ImageCache.preload(
<<<<<<< HEAD
                "/boutique/nul_card.png", "/boutique/bof.png", "/boutique/bien.png", "/boutique/GOLDGOLDGOLD.png"
=======
                "/boutique/nulCard.png", "/boutique/bof.png", "/boutique/bien.png", "/boutique/GOLDGOLDGOLD.png"
>>>>>>> dev
            );
            // son : ouvre et ferme chaque clip pour que Java initialise le mixer audio
            try {
                String[] sfx = { "/sfx/UIclick.wav", "/sfx/round.wav" };
                for (String path : sfx) {
                    var url = Frame.class.getResource(path);
                    if (url == null) continue;
                    var ais = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
                    var clip = javax.sound.sampled.AudioSystem.getClip();
                    clip.open(ais);
                    clip.close();
                    ais.close();
                }
            } catch (Exception e) {
                System.err.println("[Frame] Erreur preload son : " + e.getMessage());
            }
        }, "ImagePreload").start();

        showPanel("CONNEXION");
        setVisible(true);
    }
    
    public MainMenuPanel getMainMenuPanel() {
        return mainMenuPanel;
    }

    public ShopPanel getShopPanel() {
        return shopPanel;
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

    public LobbyRoomController getLobbyRoomController(){
        return this.lobbyRoomController;

    }

    public void setCurrentGame(GameManager game) {
        this.currentGame = game;
    }

    public GameManager getCurrentGame() {
        return currentGame;
    }

    public void clearCurrentGame() {
        this.currentGame = null;
    }
}
