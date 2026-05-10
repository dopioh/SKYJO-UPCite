package fr.upc.cr1a.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;
import fr.upc.cr1a.model.GraphicsSettings;
import fr.upc.cr1a.model.NumberCardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.utils.ImageCache;
import fr.upc.cr1a.view.help_ingame.HelpOverlay;

/**
 * Vue graphique du plateau de jeu avec affichage des cartes et boutons joueurs
 */
public class GameBoardView extends JPanel {

    private static final int cardSpacing = 2; // <----------------
    private boolean handCardAnimationsEnabled = GraphicsSettings.isHighQuality();

    // elements important :
    private ArrayList<PlayerModel> listPlayer; // jusqu'a 8 normalement

    // a transferer :
    private ArrayList<DoodleButton> buttonlistPlayer; // jusqu'a 8 normalement

    // panel :
    private JPanel zonePlayer;
    private JPanel zonePlateau;
    private PauseOverlayPanel pauseOverlay;

    // grille de boutons cartes
    private CardButton[][] cardButtons;

    // joueur actuellement affiche
    private PlayerModel currentPlayer;

    private PlayerPreviewPanel previewPanel; // Le nouveau panel
    private JLayeredPane layeredPane; // gestion des couches

    private CursorCardView cursorCardView;
    private ActionDescriptionOverlayPanel actionDescriptionOverlay;
    private ActionCardTutorialOverlayPanel actionCardTutorialOverlay;

    private Frame frame;

    // 3 couleurs pour l'instant peut etre 8 un jour mais la pas le temps
    private static final Color[] PLAYER_COLORS = {
        new Color(218, 113, 72),   // rouge
        new Color(103, 187, 93),   // vert
        new Color(241, 192, 31),  // jaune
    };

    private static final String[] TOPBAR_IMAGES = {
        "/plateau/red_topbar.png",
        "/plateau/green_topbar.png",
        "/plateau/yellow_topbar.png",
    };

    private static final String[] BG_IMAGES = {
        "/plateau/red_bg.png",
        "/plateau/green_bg.png",
        "/plateau/yellow_bg.png",
    };

    private Image backgroundImage;
    private Image topBarImage;
    private Font patrickFont;

    // cache du fond redimensionné pour éviter le rescale BICUBIC à chaque repaint
    private BufferedImage cachedBackground;
    private int cachedBgWidth;
    private int cachedBgHeight;

    // Affichage de la main des cartes action du joueur
    private JPanel actionHandPanel;
    private JPanel actionHandCardsPanel;
    private final ArrayList<CardButton> actionHandButtons = new ArrayList<>();
    private int actionHandCardWidth = 65;
    private int actionHandCardHeight = 90;
    private static final int actionHandCardsParRow = 5;
    private static final int actionHandCardGap = 6;
    private static final double actionHandPetitScale = 0.8;
    private boolean actionHandVisible = true;
    private ActionCardClickListener actionCardClickListener;

    // Affichage des cartes pilées
    private CardButton btnPioche;
    private CardButton btnDefausse;

    // Top bar label
    private JLabel mancheLabel;
    private JLabel pointsLabel;

    private CardButton btnPiocheAction;
    private CardButton btnDefausseAction;
    JLabel labPiocheAction; // J'ai besoin de créer les labels ici pour setActionDecksVisible()
    JLabel labDefausseAction;

    // a qui appartient chaque deck
    private JLabel playerName;

    // Que dois faire le joueur?
    private JLabel actionGuide;

    //bouton d'aide pour les cartes action
    private JPanel helpBtn;

    // pour fixer le clique
    private boolean isPinned = false;

    // Panel de l'entre round
    NextRoundPanel nextRoundPanel;

    // Temps pour les tours
    private JLabel turnTimerLabel;

    // Panel de transition lors du changement de joueur
    private PlayerTransitionPanel transitionPanel;

    private HelpOverlay helpOverlay;

    @FunctionalInterface
    public interface ActionCardClickListener {
        void onActionCardClicked(ActionType type, int indexInHand);
    }

    public GameBoardView(ArrayList<PlayerModel> listPlayerModels, Frame frame) {
        this.frame = frame;
        this.listPlayer = listPlayerModels;
        this.buttonlistPlayer = new ArrayList<>();
        this.cardButtons = new CardButton[3][4];
        setLayout(new BorderLayout());
        setOpaque(true);
        setVisible(true);

        // Charger la police
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            if (stream != null) {
                patrickFont = Font.createFont(Font.TRUETYPE_FONT, stream);
            } else {
                patrickFont = new Font("Arial", Font.BOLD, 1);
            }
        } catch (Exception e) {
            patrickFont = new Font("Arial", Font.BOLD, 1);
        }

        layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false); // gestionnaire de couche (doc :
                                          // https://docs.oracle.com/javase/8/docs/api/javax/swing/JLayeredPane.html)
        add(layeredPane, BorderLayout.CENTER);

        // pour resize des que on change de taille de fenetre
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeLayers();
                resizeCards();
            }
        });

        initComponents();
        updateCardDisplay(null, true);

        // afficher les cartes du premier joueur par defaut
        if (!listPlayer.isEmpty()) {
            showPlayerCards(listPlayer.get(0));
        }

        revalidate();
        repaint();
    }

    public void initComponents() {
        // System.out.println("initComponents Called");

        // PANEL PLATEAU:
        JPanel plateauPanel = new JPanel();
        plateauPanel.setOpaque(false);
        plateauPanel.setLayout(new BorderLayout());

        layeredPane.add(plateauPanel, JLayeredPane.DEFAULT_LAYER);

        pauseOverlay = new PauseOverlayPanel();
        pauseOverlay.setVisible(false);
        layeredPane.add(pauseOverlay, JLayeredPane.POPUP_LAYER);

        // ============================ TOP BAR ========================== (<- Pas IA c'est juste plus lisible)
        JPanel topBar = new JPanel(new BorderLayout()) {

            //fond topBar en image
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (topBarImage != null) {
                    g.drawImage(topBarImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 70));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        mancheLabel = new JLabel("Manche 0");
        mancheLabel.setFont(patrickFont.deriveFont(Font.BOLD, 34f));
        mancheLabel.setForeground(Color.WHITE);
        mancheLabel.setHorizontalAlignment(SwingConstants.LEFT);

        actionGuide = new JLabel("Instruction ici", SwingConstants.CENTER);
        actionGuide.setFont(patrickFont.deriveFont(Font.BOLD, 34f));
        actionGuide.setForeground(Color.WHITE);

        pointsLabel = new JLabel("0 P");
        pointsLabel.setFont(patrickFont.deriveFont(Font.BOLD, 34f));
        pointsLabel.setForeground(Color.WHITE);
        pointsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        //bouton astuce pour les cartes actions
        JLabel helpLabel = new JLabel("?", SwingConstants.CENTER);
        helpLabel.setFont(patrickFont.deriveFont(Font.BOLD, 32f));
        helpLabel.setForeground(Color.WHITE);

        helpBtn = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        helpBtn.setOpaque(false);
        helpBtn.setPreferredSize(new Dimension(40, 40));
        helpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpBtn.add(helpLabel, BorderLayout.CENTER);
        helpBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                actionCardTutorialOverlay.openOverlay();
            }
        });

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        topRightPanel.setOpaque(false);
        topRightPanel.add(helpBtn);
        topRightPanel.add(pointsLabel);

        topBar.add(mancheLabel, BorderLayout.WEST);
        topBar.add(actionGuide, BorderLayout.CENTER);
        topBar.add(topRightPanel, BorderLayout.EAST);

        plateauPanel.add(topBar, BorderLayout.NORTH);

        // panel du bas(millieu en gros) qui contient zonePlayer + zonePlateau
        JPanel gameArea = new JPanel(new GridBagLayout());
        gameArea.setOpaque(false);

        // ZONE PLAYER ( a gauche de l'ecran, la ou y'a les boutons)
        this.zonePlayer = new JPanel();
        zonePlayer.setLayout(new GridBagLayout());
        zonePlayer.setOpaque(false);
        zonePlayer.setPreferredSize(new Dimension(220, 0));

        GridBagConstraints gbcZonePlayer = new GridBagConstraints();
        gbcZonePlayer.gridx = 0;
        gbcZonePlayer.gridy = 0;
        gbcZonePlayer.gridwidth = 1;
        gbcZonePlayer.gridheight = 1;
        gbcZonePlayer.weightx = 1;
        gbcZonePlayer.weighty = 1;
        gbcZonePlayer.anchor = GridBagConstraints.WEST;
        gbcZonePlayer.fill = GridBagConstraints.BOTH;

        gameArea.add(zonePlayer, gbcZonePlayer);

        // ZONE PLATEAU ( 3/4 de l'ecran a partir de la droite, les
        // cartes,pioches,defausses et score seront ici)
        this.zonePlateau = new JPanel();
        zonePlateau.setLayout(new BorderLayout());
        zonePlateau.setOpaque(false);

        GridBagConstraints gbcZonePlateau = new GridBagConstraints();
        gbcZonePlateau.gridx = 2;
        gbcZonePlateau.gridy = 0;
        gbcZonePlateau.gridwidth = 2;
        gbcZonePlateau.gridheight = 2;
        gbcZonePlateau.weightx = 5;
        gbcZonePlateau.weighty = 1;
        gbcZonePlateau.anchor = GridBagConstraints.EAST;
        gbcZonePlateau.fill = GridBagConstraints.BOTH;

        gameArea.add(zonePlateau, gbcZonePlateau);

        plateauPanel.add(gameArea, BorderLayout.CENTER);

        // panel grille pour les cartes
        JPanel cardsGrid = new JPanel(new GridBagLayout());
        cardsGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(cardSpacing, cardSpacing, cardSpacing, cardSpacing);
        gbc.fill = GridBagConstraints.NONE;

        // Container vertical pour les cartes
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BorderLayout());
        cardsContainer.setOpaque(false);

        cardsContainer.add(cardsGrid, BorderLayout.CENTER);

        // panel des cartes actions dans la main du joueur (nouvelle version : liste de cartes)
        actionHandPanel = new JPanel(new BorderLayout(0, 6));
        actionHandPanel.setOpaque(false);

        // JLabel actionHandLabel = new JLabel("Cartes actions", SwingConstants.CENTER);
        // actionHandLabel.setForeground(new Color(255, 224, 120));
        // actionHandLabel.setFont(patrickFont.deriveFont(Font.BOLD, 20f));
        // actionHandPanel.add(actionHandLabel, BorderLayout.NORTH);

        actionHandCardsPanel = new JPanel(new GridBagLayout());
        actionHandCardsPanel.setOpaque(false);
        actionHandPanel.add(actionHandCardsPanel, BorderLayout.CENTER);

        /*
         * ANCIENNE VERSION: overlay de sélection ?
         * JPanel actionDeckPanel = new JPanel(new BorderLayout());
         * JPanel deckButtonContainer = new JPanel(new BorderLayout());
         * deckActionButton = new CardButton();
         * actionDeckCountLabel = new JLabel("0");
         * deckButtonContainer.add(actionDeckCountLabel, BorderLayout.NORTH);
         * deckButtonContainer.add(deckActionButton, BorderLayout.CENTER);
         * actionDeckPanel.add(deckButtonContainer, BorderLayout.CENTER);
         */

        // creer les 12 CardButton
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                gbc.gridx = col;
                gbc.gridy = row;
                CardButton btn = new CardButton();
                btn.setAnimationEnabled(handCardAnimationsEnabled);
                cardButtons[row][col] = btn;
                cardsGrid.add(btn, gbc);
            }
        }

        GridBagConstraints gbcActionHand = new GridBagConstraints();
        gbcActionHand.gridx = 0;
        gbcActionHand.gridy = 4;
        gbcActionHand.gridwidth = 4;
        gbcActionHand.insets = new Insets(8, cardSpacing, 6, cardSpacing);
        gbcActionHand.anchor = GridBagConstraints.CENTER;
        cardsGrid.add(actionHandPanel, gbcActionHand);

        zonePlateau.add(cardsContainer, BorderLayout.CENTER);

        // Partie droite (EAST) pour les stack de pioche et défausse
        JPanel cardStack = new JPanel();
        cardStack.setOpaque(false);
        cardStack.setLayout(new GridBagLayout());
        // cardStack.setPreferredSize(new Dimension(240, 0));

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.fill = GridBagConstraints.NONE;
        gbcRight.insets = new Insets(5, 5, 5, 5);

        btnPioche = new CardButton();
        btnDefausse = new CardButton();
        btnPiocheAction = new CardButton();
        btnDefausseAction = new CardButton();
        btnPioche.setAnimationEnabled(false);
        btnDefausse.setAnimationEnabled(false);
        btnPiocheAction.setAnimationEnabled(false);
        btnDefausseAction.setAnimationEnabled(false);

        Dimension bigCardSize = new Dimension(100, 140); // AJUSTER ICI SI CA VOUS PLAIT PAS // oui ça me plait pas

        btnPioche.setPreferredSize(bigCardSize);
        btnDefausse.setPreferredSize(bigCardSize);
        btnPiocheAction.setPreferredSize(bigCardSize);
        btnDefausseAction.setPreferredSize(bigCardSize);

        btnPioche.setMinimumSize(bigCardSize);
        btnDefausse.setMinimumSize(bigCardSize);
        btnPiocheAction.setMinimumSize(bigCardSize);
        btnDefausseAction.setMinimumSize(bigCardSize);


        JLabel labPioche = new JLabel("Pioche", SwingConstants.CENTER);
        JLabel labDefausse = new JLabel("Défausse", SwingConstants.CENTER);
        labPiocheAction = new JLabel("Pioche A", SwingConstants.CENTER);
        labDefausseAction = new JLabel("Défausse A", SwingConstants.CENTER);


        // premiere ligne -> defausse et pioche 
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        cardStack.add(labPioche, gbcRight);
        
        gbcRight.gridx = 1;
        gbcRight.gridy = 0;
        cardStack.add(labDefausse, gbcRight);
        
        gbcRight.gridx = 0;
        gbcRight.gridy = 1;
        cardStack.add(btnPioche, gbcRight);
        
        gbcRight.gridx = 1;
        gbcRight.gridy = 1;
        cardStack.add(btnDefausse, gbcRight);

        // deuxieme ligne -> defausse et pioche action
        gbcRight.gridx = 0;
        gbcRight.gridy = 2;
        cardStack.add(labPiocheAction, gbcRight);
        
        gbcRight.gridx = 1;
        gbcRight.gridy = 2;
        cardStack.add(labDefausseAction, gbcRight);
        
        gbcRight.gridx = 0;
        gbcRight.gridy = 3;
        cardStack.add(btnPiocheAction, gbcRight);
        
        gbcRight.gridx = 1;
        gbcRight.gridy = 3;
        cardStack.add(btnDefausseAction, gbcRight);

        // Initialiser les boutons d'action comme "vide" par défaut
        btnPiocheAction.setText("vide");
        btnDefausseAction.setText("vide");
        
        // Masquer les pioches d'actions par défaut (visibles seulement en mode action)
        btnPiocheAction.setVisible(false);
        btnDefausseAction.setVisible(false);
        labPiocheAction.setVisible(false);
        labDefausseAction.setVisible(false);


        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        turnTimerLabel = new JLabel("Temps restant : -- s", SwingConstants.CENTER);
        turnTimerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        turnTimerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rightPanel.add(turnTimerLabel, BorderLayout.NORTH);
        rightPanel.add(cardStack, BorderLayout.CENTER);

        zonePlateau.add(rightPanel, BorderLayout.EAST);

        // ZONE PREVIEW OTHER PLAYERS ( apparait au dessus de tous les autres panels
        // quand on hover ou clique sur un button)
        previewPanel = new PlayerPreviewPanel();
        previewPanel.setVisible(false);
        // previewPanel.setBounds(400, 200, 500, 500); // taille fix pour le test
        layeredPane.add(previewPanel, JLayeredPane.MODAL_LAYER);

        actionDescriptionOverlay = new ActionDescriptionOverlayPanel();
        actionDescriptionOverlay.setVisible(false);
        layeredPane.add(actionDescriptionOverlay, JLayeredPane.POPUP_LAYER);

        this.nextRoundPanel = new NextRoundPanel(currentPlayer);
        layeredPane.add(nextRoundPanel, JLayeredPane.PALETTE_LAYER);

        transitionPanel = new PlayerTransitionPanel();
        layeredPane.add(transitionPanel, JLayeredPane.POPUP_LAYER);

        // ANCIENNE VERSION: overlay de sélection des cartes action
        // actionCardOverlay = new ActionCardOverlayPanel();
        // actionCardOverlay.setVisible(false);
        // layeredPane.add(actionCardOverlay, JLayeredPane.PALETTE_LAYER);

        //ajouter l'astuce des cartes action'
        actionCardTutorialOverlay = new ActionCardTutorialOverlayPanel();
        actionCardTutorialOverlay.setGameBoardView(this);
        actionCardTutorialOverlay.setVisible(false);
        layeredPane.add(actionCardTutorialOverlay, JLayeredPane.PALETTE_LAYER);

        cursorCardView = new CursorCardView(this);
        cursorCardView.setOverlayBounds(getWidth(), getHeight());
        layeredPane.add(cursorCardView.getOverlay(), JLayeredPane.DRAG_LAYER);

        helpOverlay = frame.getHelpOverlay();
        layeredPane.add(helpOverlay, JLayeredPane.PALETTE_LAYER);

        // BOUTTONS PLAYERS ( a gauche )
        playerButton();

        SwingUtilities.invokeLater(() -> resizeLayers()); // doit s'executer apres que tous les components soit creer
        SwingUtilities.invokeLater(() -> resizeCards()); // doit s'executer apres que tous les components soit creer
    }

    /**
     * Affiche les cartes d'un joueur qu'on veut à droite
     */
    public void showPlayerCards(PlayerModel player) {
        this.currentPlayer = player;
        int colorIndex = listPlayer.indexOf(player) % PLAYER_COLORS.length; // parce que on a que 3 couleurs pour l'instant
        topBarImage = ImageCache.get(TOPBAR_IMAGES[colorIndex]);
        backgroundImage = ImageCache.get(BG_IMAGES[colorIndex]);
        cachedBackground = null; // rendre invalide le cache quand fond change
        updatePoints();
        PlayerHandModel hand = player.getHand();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                CardButton btn = cardButtons[row][col];
                CardModel card = hand.getCard(row, col);

                if (card == null) {
                    btn.setText("");
                    btn.setCardImage("/cartes/cartevide.png");
                    btn.setEnabled(false);
                } else {
                    btn.setEnabled(true);
                    btn.setText("");
                    String imgPath = getCardImagePath(card);
                    btn.setCardImage(imgPath);
                }
                btn.setDisabledIcon(btn.getIcon());// Ca évite de griser la carte désactivée
            }
        }

        showActionHand(player);

        zonePlateau.revalidate();
        zonePlateau.repaint();
        repaint(); // met a jour la topbar et le fond
    }

    /**
     * Met à jour les boutons carte action visibles sans recréer de composants
     * utilisation de gridbag pour positionner dynamiquement les actions
     * 5 par lignes pour le moment et les cartes sont plus petites si a partir de 6 car on saute une ligne
     */
    public void showActionHand(PlayerModel player) {
        hideActionDescription();
        ArrayList<ActionCardModel.ActionType> actions = player.getActionHand();
        int actionCount = (actions == null) ? 0 : actions.size();
        int displayedCardWidth = getDisplayedActionHandCardWidth(actionCount);
        int displayedCardHeight = getDisplayedActionHandCardHeight(actionCount);
        int lastRowIndex = (actionCount - 1) / actionHandCardsParRow;

        actionHandButtons.clear();
        actionHandCardsPanel.removeAll();

        GridBagConstraints actionCardGbc = new GridBagConstraints();
        actionCardGbc.anchor = GridBagConstraints.CENTER;
        actionCardGbc.fill = GridBagConstraints.NONE;

        if (actions != null) {
            for (int i = 0; i < actions.size(); i++) {
                ActionType actionType = actions.get(i);
                int indexInHand = i;
                int row = i / actionHandCardsParRow;

                CardButton actionBtn = new CardButton();
                actionBtn.setAnimationEnabled(false);
                actionBtn.setCardSize(displayedCardWidth, displayedCardHeight);
                actionBtn.setCardImage(getCardImagePath(new ActionCardModel(false, actionType)));
                actionBtn.setToolTipText(getActionTitle(actionType));

                actionBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        showActionDescription(actionType);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hideActionDescription();
                    }
                });

                actionBtn.addActionListener(e -> {
                    if (actionCardClickListener != null) {
                        actionCardClickListener.onActionCardClicked(actionType, indexInHand);
                    }
                });

                actionHandButtons.add(actionBtn);
                actionCardGbc.gridx = i % actionHandCardsParRow;
                actionCardGbc.gridy = row;
                actionCardGbc.insets = new Insets(0, actionHandCardGap / 2, row < lastRowIndex ? actionHandCardGap : 0, actionHandCardGap / 2);
                actionHandCardsPanel.add(actionBtn, actionCardGbc);
            }
        }

        actionHandCardsPanel.setVisible(actionHandVisible && actionCount > 0);
        actionHandPanel.setVisible(actionHandVisible);
        actionHandCardsPanel.revalidate();
        actionHandCardsPanel.repaint();
    }

    public CardButton[] getActionHandButtons() {
        return actionHandButtons.toArray(new CardButton[0]);
    }

    public void setActionCardClickListener(ActionCardClickListener listener) {
        this.actionCardClickListener = listener;
    }

    /*
     * ANCIENNE VERSION: sélection via overlay
     * public CardButton[] getActionCardButtons() { return actionCardOverlay.getActionCardButtons(); }
     * public void openActionCardOverlay(ArrayList<ActionType> actions) { actionCardOverlay.showActionHand(actions, this); }
     * public void setActionCardSelectionCallback(Runnable callback) { actionCardOverlay.setOnActionSelected(callback); }
     */

    /**
     * Retourne le chemin de l'image pour une carte
     */
    public String getCardImagePath(CardModel card) {
        if (card.isFaceDown()) {
            return CaseOpeningOverlayPanel.getSelectedSkinPath(); // temporaire
        }

        if (card instanceof NumberCardModel numberCard && numberCard.isStar()) {
            return "/cartes/carteEtoile.png";
        }

        if (card instanceof ActionCardModel) {
            ActionCardModel actionCard = (ActionCardModel) card;
            switch (actionCard.getType()) {
                case TRADE_CARD_WITH_PLAYER:
                    return "/cartes/carteActionTrade.png";
                case REPLACE_CARD_IN_HAND:
                    return "/cartes/carteActionReplace.png";
                case PLAY_TWO_TURN:
                    return "/cartes/carteActionTwoTurn.png";
                case DRAW_THREE_CARD:
                    return "/cartes/carteActionDraw3.png";
                case PLAY_LAST_ACTION_IN_DEFAUSSE:
                    return "/cartes/carteActionReplay.png";
                case ALL_PLAYERS_DROP_CARD:
                    return "/cartes/carteActionDrop.png";
                case SPY_ONE_COLUMN:
                    return "/cartes/carteActionSpy.png";
                default:
                    return CaseOpeningOverlayPanel.getSelectedSkinPath(); // temporaire
            }
        }
        
        // Si c'est une carte nombre normale
        int val = card.getCardValue();
        if (val == -2)
            return "/cartes/carteMoins2.png";
        if (val == -1)
            return "/cartes/carteMoins1.png";
        return "/cartes/carte" + val + ".png";
    }

    /**
     * Initialise tous les boutons selon le nombre de joueur dans la partie
     * Recuperer l'info de la list de PlayerModel
     * Creer dans le GameManager (pour le moment)
     */
    private static final ButtonColor[] BTN_COLORS = { ButtonColor.RED, ButtonColor.GREEN, ButtonColor.YELLOW };
    private static final String[] PROFIL_ICONS = {
        "/plateau/players/red_profil.png",
        "/plateau/players/green_profil.png",
        "/plateau/players/yellow_profil.png"
    };

    private void playerButton() {
        GridBagConstraints gbcPlayerButton = new GridBagConstraints();
        gbcPlayerButton.gridx = 0;
        gbcPlayerButton.gridy = 0;
        gbcPlayerButton.weightx = 1;
        gbcPlayerButton.weighty = 1;
        int colorIndex = 0;
        for (PlayerModel pm : listPlayer) {
            int i = colorIndex % BTN_COLORS.length;
            colorIndex++;

            String nom = pm.getName();
            if (nom.length() > 8) nom = nom.substring(0, 8) + "..";
            DoodleButton p = new DoodleButton(nom, BTN_COLORS[i], 180, 55, PROFIL_ICONS[i]);
            p.setIconSide(DoodleButton.IconSide.LEFT);
            p.setFontSize(28f);
            buttonlistPlayer.add(p);
            zonePlayer.add(p, gbcPlayerButton);
            gbcPlayerButton.gridy++;
        }
        setActivePlayerButton(0);
    }

    /**
     * Affiche, selon le boolean, la preview
     * 
     * @param isVisible
     */
    public void showOtherPlayerMain(boolean isVisible) {
        previewPanel.fade(isVisible);
    }

    /**
     * Methode interne pour resize selon la taille de la fenetre swing actuelle
     */
    private void resizeLayers() {
        Dimension size = getSize(); // size de la fenetre ?
        Component[] compsD = layeredPane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER); // recupere les elements de
                                                                                           // la couche DEFAULT
        for (Component c : compsD) {
            c.setBounds(0, 0, size.width, size.height);
        }

        Component[] compsP = layeredPane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER); // round changement
        for (Component c : compsP) {
            c.setBounds(0, 0, size.width, size.height);
        }

        Component[] compsM = layeredPane.getComponentsInLayer(JLayeredPane.MODAL_LAYER); // previewPlayer
        for (Component c : compsM) {
            c.setBounds(size.width / 6, size.height / 8, size.width * 2 / 3, size.height * 7 / 8 - 20);
        }

        Component[] compsPopup = layeredPane.getComponentsInLayer(JLayeredPane.POPUP_LAYER);
        int popupW = Math.min((int) (size.width * 0.50), 520);
        int popupH = Math.min((int) (size.height * 0.26), 220);
        int popupX = (size.width - popupW) / 2;
        int popupY = (size.height - popupH) / 2;
        for (Component c : compsPopup) {
            if (c == actionDescriptionOverlay) { // le transitionPanel et le actionDescriptionOverlay sont dans le meme layer (POPUP)
                c.setBounds(popupX, popupY, popupW, popupH);
            } else {
                c.setBounds(0, 0, size.width, size.height);
            }
        }

        if (cursorCardView != null) {
            cursorCardView.setOverlayBounds(size.width, size.height);
        }

    }

    // getter
    public ArrayList<PlayerModel> getListPlayer() {
        return this.listPlayer;
    }

    public ArrayList<DoodleButton> getListPlayerButton() {
        return this.buttonlistPlayer;
    }

    public CardButton[][] getCardButtons() {
        return this.cardButtons;
    }

    // animation flip : appelle 'onChange' quand on voit plus la carte
    public void flipCardAt(int row, int col, Runnable onChange) { // wtf is runnable
        if (!handCardAnimationsEnabled) {
            if (onChange != null) {
                onChange.run();
            }
            return;
        }
        cardButtons[row][col].flip(onChange);
    }

    // simule un clic
    public void animatePressAt(int row, int col) {
        cardButtons[row][col].animatePress();
    }

    public PlayerModel getCurrentPlayer() {
        return this.currentPlayer;
    }

    public CardButton getBtnPioche() {
        return btnPioche;
    }

    public CardButton getBtnDefausse() {
        return btnDefausse;
    }

    public CardButton getBtnPiocheAction() {
        return btnPiocheAction;
    }

    public CardButton getBtnDefausseAction() {
        return btnDefausseAction;
    }

    // ANCIENNE VERSION
    // public CardButton getDeckActionButton() { return deckActionButton; }

    /**
     * Affiche ou masque les pioches d'actions selon le mode de jeu
     * @param visible true pour afficher
     */
    public void setActionDecksVisible(boolean visible) {
        btnPiocheAction.setVisible(visible);
        btnDefausseAction.setVisible(visible);
        labPiocheAction.setVisible(visible);
        labDefausseAction.setVisible(visible);
        helpBtn.setVisible(visible);
        setActionHandVisible(visible);
        if (!visible) {
            hideActionDescription();
        }
    }

    public void setActionHandVisible(boolean visible) {
        actionHandVisible = visible;
        if (actionHandPanel != null) {
            actionHandPanel.setVisible(visible);
        }
        if (actionHandCardsPanel != null) {
            actionHandCardsPanel.setVisible(visible && !actionHandButtons.isEmpty());
        }
    }

    public void setActionGuide(String message) {
        actionGuide.setText(message);
    }

    public void setMancheLabel(int roundNumber) {
        mancheLabel.setText("Manche " + roundNumber);
    }

    public void setMancheLabel(String roundNumber) {
        mancheLabel.setText(roundNumber);
    }

    public void updatePoints() {
        if (currentPlayer != null) {
            // Affiche le score total accumulé + le score des cartes visibles de la manche actuelle
            int totalScore = currentPlayer.getTotalScore();
            int liveScore = currentPlayer.getHand().getVisibleCardScore();
            int displayScore = totalScore + liveScore;
            pointsLabel.setText(displayScore + " P");
        }
    }

    public NextRoundPanel getNextRoundPanel() {
        return this.nextRoundPanel;
    }

    /**
     * Lance l'animation de transition entre les tours de joueurs
     * @param player  Le joueur dont c'est le tour
     * @param onDone  appelé en fin d'animation
     */
    public void showPlayerTransition(PlayerModel player, Runnable onDone) {
        previewPanel.fade(false); // cache la preview si elle est visible pendant la transition
        int colorIndex = listPlayer.indexOf(player) % PLAYER_COLORS.length;
        setActivePlayerButton(listPlayer.indexOf(player));
        transitionPanel.showTransition(player.getName(), PLAYER_COLORS[colorIndex], onDone);
    }

    public void setActivePlayerButton(int activeIndex) {
        for (int i = 0; i < buttonlistPlayer.size(); i++) {
            buttonlistPlayer.get(i).setActive(i == activeIndex);
        }
    }

    /**
     * Affiche la petite carte sous le curseur pendant une action
     * 
     * @param imgPath chemin d'image de la carte
     */
    public void showCursorCard(String imgPath) {
        cursorCardView.showCard(imgPath);
    }

    /**
     * Cache la petite carte sous le curseur
     */
    public void hideCursorCard() {
        cursorCardView.hideCard();
    }

    /**
     * Overlay Popup d'une carte action
     * @param actionType type de la carte action
     */
    public void showActionDescription(ActionType actionType) {
        if (actionType == null) {
            hideActionDescription();
            return;
        }
        actionDescriptionOverlay.showDescription(
            getActionTitle(actionType),
            getActionDescription(actionType)
        );
    }

    public void hideActionDescription() {
        actionDescriptionOverlay.hideDescription();
    }

    private String getActionTitle(ActionType actionType) {
        switch (actionType) {
            case TRADE_CARD_WITH_PLAYER: return "ECHANGE";
            case REPLACE_CARD_IN_HAND: return "REPLACE";
            case PLAY_TWO_TURN: return "JOUE 2 FOIS";
            case DRAW_THREE_CARD: return "DRAW_THREE_CARD"; //jamais utilisé
            case PLAY_LAST_ACTION_IN_DEFAUSSE: return "REJOUE";
            case ALL_PLAYERS_DROP_CARD: return "CHAQUE JOUEUR DEFAUSSE";
            case SPY_ONE_COLUMN: return "REGARDE";
            default: return "";
        }
    }

    private String getActionDescription(ActionType actionType) {
        switch (actionType) {
            case TRADE_CARD_WITH_PLAYER:
                return "Echange une carte de ta main avec une carte d'un autre joueur.";
            case REPLACE_CARD_IN_HAND:
                return "Echange la position de 2 cartes dans ta main.";
            case PLAY_TWO_TURN:
                return "Tu gagnes un tour supplémentaire à la fin de ce tour.";
            case DRAW_THREE_CARD:
                return "hein? t'es pas censé voir ça";
            case PLAY_LAST_ACTION_IN_DEFAUSSE:
                return "Rejoue la dernière action de la défausse action.";
            case ALL_PLAYERS_DROP_CARD:
                return "Pour chaque autre joueur, choisis une carte à défausser: elle est remplacée par la première carte de la pioche.";
            case SPY_ONE_COLUMN:
                return "Révèle pendant 3s toutes les cartes d'une colonne de ta main.";
            default: return "";
        }
    }

    /**
     * méthode publique pour accéder à la description d'une action
     */
    public String getActionDescriptionPublic(ActionType actionType) {
        return getActionDescription(actionType);
    }

    /**
     * méthode publique pour accéder au titre d'une action
     */
    public String getActionTitlePublic(ActionType actionType) {
        return getActionTitle(actionType);
    }

    /**
     * Mets à jour l'affichage des cartes de la pioche et de la défausse
     * 
     * @param topDiscard   Carte du haut de la défausse
     * @param drawHasCards
     */
    public void updateCardDisplay(CardModel topDiscard, boolean drawHasCards) {
        // Pioche
        if (drawHasCards) {
            btnPioche.setText("");
            btnPioche.setCardImage(CaseOpeningOverlayPanel.getSelectedSkinPath()); // temporaire
        } else {
            btnPioche.clearImage();
            btnPioche.setText("vide");
        }

        // Défausse
        if (topDiscard == null) {
            btnDefausse.clearImage();
            btnDefausse.setText("vide");
        } else {
            btnDefausse.setText("");
            btnDefausse.setCardImage(getCardImagePath(topDiscard));
        }
        zonePlateau.revalidate();
        zonePlateau.repaint();
    }

    public void updateDiscardOnly(CardModel topDiscard) {
        if (topDiscard == null) {
            btnDefausse.clearImage();
            btnDefausse.setText("vide");
        } else {
            btnDefausse.setText("");
            btnDefausse.setCardImage(getCardImagePath(topDiscard));
        }
        zonePlateau.revalidate();
        zonePlateau.repaint();
    }

    /**
     * Mets à jour l'affichage des cartes de la pioche et de la défausse d'actions
     * 
     * @param topActionDiscard   Carte du haut de la défausse d'actions
     * @param drawActionHasCards
     */
    public void updateActionCardDisplay(CardModel topActionDiscard, boolean drawActionHasCards) {
        // Pioche Action
        if (drawActionHasCards) {
            btnPiocheAction.setText("");
            btnPiocheAction.setCardImage(CaseOpeningOverlayPanel.getSelectedSkinPath());
        } else {
            btnPiocheAction.clearImage();
            btnPiocheAction.setText("vide");
        }

        // Défausse Action
        if (topActionDiscard == null) {
            btnDefausseAction.clearImage();
            btnDefausseAction.setText("vide");
        } else {
            btnDefausseAction.setText("");
            btnDefausseAction.setCardImage(getCardImagePath(topActionDiscard));
        }
        zonePlateau.revalidate();
        zonePlateau.repaint();
    }

    public void updateActionDiscardOnly(CardModel topActionDiscard) {
        if (topActionDiscard == null) {
            btnDefausseAction.clearImage();
            btnDefausseAction.setText("vide");
        } else {
            btnDefausseAction.setText("");
            btnDefausseAction.setCardImage(getCardImagePath(topActionDiscard));
        }
        zonePlateau.revalidate();
        zonePlateau.repaint();
    }

    private void resizeCards() {
        int sizeH = this.getHeight();
        int sizeW = this.getWidth();

        // de combien la fenetre a grandi par rapport a la taille de base
        double scaleW = (double) sizeW / 1280;
        double scaleH = (double) sizeH / 720;
        double scale = scaleW < scaleH ? scaleW : scaleH; // on prend le plus petit scale pour que ça depasse pas
        int largeurCarte = (int) (95 * scale);
        int hauteurCarte = (int) (135 * scale);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                cardButtons[i][j].setCardSize(largeurCarte, hauteurCarte);
            }
        }
        previewPanel.setCardSize(largeurCarte, hauteurCarte);

        // redimensionner les cartes action sous la main
        int smallCardWidth = (int) (largeurCarte * 0.7);
        int smallCardHeight = (int) (hauteurCarte * 0.7);
        actionHandCardWidth = smallCardWidth;
        actionHandCardHeight = smallCardHeight;
        int displayedCardWidth = getDisplayedActionHandCardWidth(actionHandButtons.size());
        int displayedCardHeight = getDisplayedActionHandCardHeight(actionHandButtons.size());
        for (CardButton btn : actionHandButtons) {
            btn.setCardSize(displayedCardWidth, displayedCardHeight);
        }


        // Pioche et Défausse sont plus grands
        int largeurGrandeCarte = (int) (largeurCarte * 1.7);
        int hauteurGrandeCarte = (int) (hauteurCarte * 1.7);

        btnPioche.setCardSize(largeurGrandeCarte, hauteurGrandeCarte);
        btnDefausse.setCardSize(largeurGrandeCarte, hauteurGrandeCarte);
        btnPiocheAction.setCardSize(largeurGrandeCarte, hauteurGrandeCarte);
        btnDefausseAction.setCardSize(largeurGrandeCarte, hauteurGrandeCarte);

        zonePlateau.revalidate();
        zonePlateau.repaint();
    }

    /**
     * getDisplayedActionHandCardWidth et getDisplayedActionHandCardHeight renvoient la taille d'une carte action
     * en fonction du nombre de cartes action deja affiché
     * @param actionCount le nombre de carte action
     * @return
     */
    private int getDisplayedActionHandCardWidth(int actionCount) {
        if (actionCount > actionHandCardsParRow) {
            return Math.max(40, (int)Math.round(actionHandCardWidth * actionHandPetitScale));
        }
        return actionHandCardWidth;
    }

    private int getDisplayedActionHandCardHeight(int actionCount) {
        if (actionCount > actionHandCardsParRow) {
            return Math.max(56, (int)Math.round(actionHandCardHeight * actionHandPetitScale));
        }
        return actionHandCardHeight;
    }

    public PlayerPreviewPanel getPlayerPreviewPanel() {
        return this.previewPanel;
    }

    public void setTurnTimerText(String text) {
        if (turnTimerLabel != null) {
            turnTimerLabel.setText("Temps restant : " + text + " s");
        }
    }

    /**
     * Sert à entierement supprimer les instances des cartes animées
     */
    public void cleanup() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (cardButtons[row][col] != null) {
                    cardButtons[row][col].setAnimationEnabled(false);
                }
            }
        }
        for (CardButton actionButton : actionHandButtons) {
            if (actionButton != null) {
                actionButton.setAnimationEnabled(false);
            }
        }
        AnimLoop.remove(nextRoundPanel);
        AnimLoop.remove(transitionPanel);
        AnimLoop.remove(actionDescriptionOverlay);
        AnimLoop.remove(cursorCardView);
        transitionPanel.setVisible(false);
        nextRoundPanel.setVisible(false);
        actionDescriptionOverlay.setVisible(false);
    }

    public PauseOverlayPanel getPauseOverlay() {
        return pauseOverlay;
    }

    public void showPauseOverlay(boolean visible) {
        pauseOverlay.setVisible(visible);
        pauseOverlay.repaint();
    }

    public void setTimerVisible(boolean visible) {
        if (turnTimerLabel != null) {
            turnTimerLabel.setVisible(visible);
        }
    }

    public void setHandCardAnimations(boolean enabled) {
        handCardAnimationsEnabled = enabled;
        if (cardButtons == null) {
            return;
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (cardButtons[row][col] != null) {
                    cardButtons[row][col].setAnimationEnabled(enabled);
                }
            }
        }
    }

    public boolean isHandCardAnimationsEnabled() {
        return handCardAnimationsEnabled;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int w = getWidth();
            int h = getHeight();
            if (cachedBackground == null || cachedBgWidth != w || cachedBgHeight != h) {
                cachedBackground = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D bg = cachedBackground.createGraphics();
                bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                bg.drawImage(backgroundImage, 0, 0, w, h, null);
                bg.dispose();
                cachedBgWidth = w;
                cachedBgHeight = h;
            }
            g.drawImage(cachedBackground, 0, 0, null);
        }
    }
}
