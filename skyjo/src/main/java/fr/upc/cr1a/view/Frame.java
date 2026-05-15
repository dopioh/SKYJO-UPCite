package fr.upc.cr1a.view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import fr.upc.cr1a.controller.help_ingame.HelpController;
import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GraphicsSettings;
import fr.upc.cr1a.model.TutorialModel;
import fr.upc.cr1a.utils.ImageCache;
import fr.upc.cr1a.view.help_ingame.HelpOverlay;

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
    private Leaderboard leaderboard;
    private ShopPanel shopPanel;
    private TutorialModel tutorialModel;
    private TutorialView tutorialView;
    private TutorialModeSelectionView tutorialModeSelectionView;
    private fr.upc.cr1a.controller.TutorialController tutorialController;
    private boolean fullscreen = false;
    private Rectangle windowedBounds;
    private final AudioManager audioManager;
    private PlayersSetupPanel playersSetupPanel;
    private ConnexionPanel connexionPanel;
    private GameManager currentGame;
    private HelpOverlay helpOverlay;
    private boolean handCardAnimationsEnabled = GraphicsSettings.isHighQuality();

    // Controller
    private AccountController account; // sert a rien pour le moment
    private ConnexionController connexionController;
    private LobbyRoomController lobbyRoomController;
    private LobbyBrowserController lobbyBrowserController;
    private HelpController helpController;


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

        // Overlay classement
        leaderboard = new Leaderboard();
        getLayeredPane().add(leaderboard, JLayeredPane.POPUP_LAYER);

        // Overlay tutoriel au niveau de Frame
        tutorialModel = new TutorialModel();
        tutorialModeSelectionView = new TutorialModeSelectionView();
        tutorialModeSelectionView.setVisible(false);
        getLayeredPane().add(tutorialModeSelectionView, JLayeredPane.DRAG_LAYER);
        
        tutorialView = new TutorialView(tutorialModel);
        tutorialView.setVisible(false);
        getLayeredPane().add(tutorialView, JLayeredPane.DRAG_LAYER);
        
        //initialiser TutorialController
        tutorialController = new fr.upc.cr1a.controller.TutorialController(tutorialModel, tutorialView, tutorialModeSelectionView);

        // Overlay help in game 
        this.helpOverlay = new HelpOverlay();
        //add(playersSetupPanel, "HELP_OVERLAY");
        getLayeredPane().add(helpOverlay, JLayeredPane.POPUP_LAYER);
        helpOverlay.setBounds(0, 0, getWidth(), getHeight());

        // redimentionnement
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                leaderboard.setBounds(0, 0, getWidth(), getHeight());
                tutorialModeSelectionView.setBounds(0, 0, getWidth(), getHeight());
                tutorialView.setBounds(0, 0, getWidth(), getHeight());
            }
        });

        //Contrôleurs
        audioManager = new AudioManager();
        DoodleButton.setAudioManager(audioManager);
        CardButton.setAudioManager(audioManager);
        CaseOpeningOverlayPanel.setAudioManager(audioManager);
        new SettingsOverlayController(settingsOverlay, this, audioManager);
        new MainMenuController(this, mainMenuPanel);
        new ShopController(this, shopPanel);
        new GameModeSelectionController(this, gameModeSelectionPanel);
        lobbyBrowserController = new LobbyBrowserController(this, lobbyBrowserPanel);
        lobbyRoomController = new LobbyRoomController(this, lobbyRoomPanel);
        new PlayersSetupController(this, playersSetupPanel);
        this.helpController = new HelpController(this);

        this.connexionController = new ConnexionController(this, connexionPanel);

        this.account = new AccountController(mainMenuPanel);

        // Precharge toutes les images du jeu en arriere-plan pour zero lag au lancement   // ça marchéeeeeeeeeeee enfinnnn c'etait ça la raison
        new Thread(() -> {
            // cartes
            ImageCache.preload(
                "/cartes/carteDos.png", "/cartes/cartevide.png",
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
                "/boutique/nul_card.png", "/boutique/bof.png", "/boutique/bien.png", "/boutique/GOLDGOLDGOLD.png"
            );
            // son : ouvre et ferme chaque clip pour que Java initialise le mixer audio
            try {
                String[] sfx = {
                    "/sfx/UIclick.wav", "/sfx/round.wav",
                    "/sfx/button_click.wav", "/sfx/back.wav",
                    "/sfx/card_flip.wav", "/sfx/card_click.wav",
                    "/sfx/defausse.wav", "/sfx/teleport.wav",
                    "/sfx/harp.wav", "/sfx/player_change.wav",
                    "/music/elevator_bgm.wav",
                    "/sfx/money_buy.wav", "/sfx/classeur.wav",
                    "/sfx/volume.wav",
                    "/music/main_menu.wav", "/music/game_bgm1.wav", "/music/game_bgm2.wav"
                };
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
        settingsOverlay.getHandCardAnimationsButton().setText(handCardAnimationsEnabled ? "HAUT" : "BAS");
        settingsOverlay.setVisible(true);
        settingsOverlay.requestFocusInWindow();
        settingsOverlay.revalidate();
        settingsOverlay.repaint();
    }

    public void showLeaderboard(java.util.List<fr.upc.cr1a.model.player.PlayerData> players) {
        leaderboard.setBounds(0, 0, getWidth(), getHeight());
        if (players.isEmpty()) {
            leaderboard.showLoading();
        } else {
            leaderboard.updatePlayers(players);
        }
        leaderboard.setVisible(true);
        leaderboard.revalidate();
        leaderboard.repaint();
    }

    public void hideLeaderboard() {
        leaderboard.setVisible(false);
        repaint();
    }

    public Leaderboard getLeaderboard() { return leaderboard; }

    public TutorialView getTutorialView() { return tutorialView; }
    public TutorialModeSelectionView getTutorialModeSelectionView() { return tutorialModeSelectionView; }
    public TutorialModel getTutorialModel() { return tutorialModel; }
    public fr.upc.cr1a.controller.TutorialController getTutorialController() { return tutorialController; }

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

    public LobbyBrowserController getLobbyBrowserController(){
        return this.lobbyBrowserController;
    }

    // help in game
    public HelpController getHelpController(){return this.helpController;}
    public HelpOverlay getHelpOverlay(){return this.helpOverlay;}

    public void setCurrentGame(GameManager game) {
        this.currentGame = game;
    }

    public GameManager getCurrentGame() {
        return currentGame;
    }

    public void clearCurrentGame() {
        this.currentGame = null;
    }

    public boolean isHandCardAnimationsEnabled() {
        return handCardAnimationsEnabled;
    }

    public void setHandCardAnimationsEnabled(boolean enabled) {
        this.handCardAnimationsEnabled = enabled;
    }

    public SettingsOverlay getSettingsOverlay(){
        return this.settingsOverlay;
    }
}
