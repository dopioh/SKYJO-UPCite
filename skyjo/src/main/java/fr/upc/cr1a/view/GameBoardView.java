package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.NumberCardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;
import fr.upc.cr1a.utils.ImageCache;

/**
 * Vue graphique du plateau de jeu avec affichage des cartes et boutons joueurs
 */
public class GameBoardView extends JPanel {

    private static final int cardSpacing = 2; // <----------------
    private static final boolean EnableHandCardAnimations = true; // DESACTIVER SI LE JEU LAG

    // elements important :
    private ArrayList<PlayerModel> listPlayer; // jusqu'a 8 normalement

    // a transferer :
    private ArrayList<JButton> buttonlistPlayer; // jusqu'a 8 normalement

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
    private CardButton deckActionButton;
    private ActionCardOverlayPanel actionCardOverlay;
    private JLabel actionDeckCountLabel;

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

    // pour fixer le clique
    private boolean isPinned = false;

    // Panel de l'entre round
    NextRoundPanel nextRoundPanel;

    // Temps pour les tours
    private JLabel turnTimerLabel;

    // Panel de transition lors du changement de joueur
    private PlayerTransitionPanel transitionPanel;

    public GameBoardView(ArrayList<PlayerModel> listPlayerModels) {
        this.listPlayer = listPlayerModels;
        this.buttonlistPlayer = new ArrayList<JButton>();
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

        topBar.add(mancheLabel, BorderLayout.WEST);
        topBar.add(actionGuide, BorderLayout.CENTER);
        topBar.add(pointsLabel, BorderLayout.EAST);

        plateauPanel.add(topBar, BorderLayout.NORTH);

        // panel du bas(millieu en gros) qui contient zonePlayer + zonePlateau
        JPanel gameArea = new JPanel(new GridBagLayout());
        gameArea.setOpaque(false);

        // ZONE PLAYER ( a gauche de l'ecran, la ou y'a les boutons)
        this.zonePlayer = new JPanel();
        zonePlayer.setLayout(new GridBagLayout());
        zonePlayer.setOpaque(false);
        zonePlayer.setPreferredSize(new Dimension(50, 0));

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

        //panel des cartes actions dans la main du joueur
        JPanel actionDeckPanel = new JPanel(new BorderLayout());
        actionDeckPanel.setOpaque(false);

        JPanel deckButtonContainer = new JPanel(new BorderLayout());
        deckButtonContainer.setOpaque(false);
        deckActionButton = new CardButton();
        deckActionButton.setAnimationEnabled(false);
        deckActionButton.setCardSize(65, 90);
        actionDeckCountLabel = new JLabel("0");
        actionDeckCountLabel.setFont(patrickFont.deriveFont(Font.BOLD, 14f));
        actionDeckCountLabel.setForeground(new Color(255, 200, 0)); // couleur dorée
        actionDeckCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel countPanel = new JPanel();
        countPanel.setOpaque(false);
        countPanel.add(actionDeckCountLabel);

        deckButtonContainer.add(countPanel, BorderLayout.NORTH);
        deckButtonContainer.add(deckActionButton, BorderLayout.CENTER);
        actionDeckPanel.add(deckButtonContainer, BorderLayout.CENTER);

        // creer les 12 CardButton
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                gbc.gridx = col;
                gbc.gridy = row;
                CardButton btn = new CardButton();
                btn.setAnimationEnabled(EnableHandCardAnimations);
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
        cardsGrid.add(actionDeckPanel, gbcActionHand);

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

        //ajouter l'overlay des cartes action
        actionCardOverlay = new ActionCardOverlayPanel();
        actionCardOverlay.setVisible(false);
        layeredPane.add(actionCardOverlay, JLayeredPane.PALETTE_LAYER);

        this.nextRoundPanel = new NextRoundPanel(currentPlayer);
        layeredPane.add(nextRoundPanel, JLayeredPane.PALETTE_LAYER);

        transitionPanel = new PlayerTransitionPanel();
        layeredPane.add(transitionPanel, JLayeredPane.POPUP_LAYER);

        cursorCardView = new CursorCardView(this);
        cursorCardView.setOverlayBounds(getWidth(), getHeight());
        layeredPane.add(cursorCardView.getOverlay(), JLayeredPane.DRAG_LAYER);

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
                    btn.setCardImage("/cartes/carteVide.png");
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
     * Maj et affiche les cartes action du joueur
     */
    public void showActionHand(PlayerModel player) {
        ArrayList<ActionCardModel.ActionType> actions = player.getActionHand();
        int actionCount = (actions == null) ? 0 : actions.size();

        //maj le compteur du deck
        actionDeckCountLabel.setText(String.valueOf(actionCount));

        if (actionCount > 0) {
            deckActionButton.setVisible(true);
            String firstActionPath = getCardImagePath(new ActionCardModel(false, actions.get(0)));
            deckActionButton.setCardImage(firstActionPath);
        } else {
            deckActionButton.setVisible(false);
            deckActionButton.clearImage();
        }
    }

    /**
     * Getters pour que le controller puisse ajouter ses listeners
     */
    public CardButton[] getActionCardButtons() {
        return actionCardOverlay.getActionCardButtons();
    }

    public ActionType getActionCardType(int index) {
        return actionCardOverlay.getActionCardType(index);
    }

    public int getVisibleActionCount() {
        return actionCardOverlay.getVisibleActionCount();
    }

    /**
     * ouvrir l'overlay des cartes action
     */
    public void openActionCardOverlay(ArrayList<ActionCardModel.ActionType> actions) {
        actionCardOverlay.showActionHand(actions, this);
    }

    /**
     *fermer l'overlay des cartes action
     */
    public void closeActionCardOverlay() {
        actionCardOverlay.closeOverlay();
    }

    /**
     * obtenir l'index de la carte sélectionnée dans l'overlay
     */
    public int getSelectedActionCardIndex() {
        return actionCardOverlay.getSelectedCardIndex();
    }

    /**
     *définir le retour pour quand une carte action est validée
     */
    public void setActionCardSelectionCallback(Runnable callback) {
        actionCardOverlay.setOnActionSelected(callback);
    }

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
    private void playerButton() {
        GridBagConstraints gbcPlayerButton = new GridBagConstraints();
        gbcPlayerButton.gridx = 0;
        gbcPlayerButton.gridy = 0;
        gbcPlayerButton.weightx = 1;
        gbcPlayerButton.weighty = 1;
        int colorIndex = 0;
        for (PlayerModel pm : listPlayer) {
            Color playerColor = PLAYER_COLORS[colorIndex % PLAYER_COLORS.length];
            colorIndex++;

            JButton p = new JButton(pm.getName());
            p.setOpaque(true);
            p.setBorderPainted(false);
            p.setBackground(playerColor);
            p.setForeground(Color.WHITE);
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    p.setBackground(playerColor.brighter());
                    isPinned = false;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isPinned) {
                        p.setBackground(playerColor);
                    }
                }
            });
            buttonlistPlayer.add(p);
            zonePlayer.add(p, gbcPlayerButton);
            gbcPlayerButton.gridy++;
        }
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
            c.setBounds(size.width / 6, size.height / 10, size.width - size.width / 3, size.height - size.height / 5);
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

    public ArrayList<JButton> getListPlayerButton() {
        return this.buttonlistPlayer;
    }

    public CardButton[][] getCardButtons() {
        return this.cardButtons;
    }

    // animation flip : appelle 'onChange' quand on voit plus la carte
    public void flipCardAt(int row, int col, Runnable onChange) {
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

    public CardButton getDeckActionButton() {
        return deckActionButton;
    }

    /**
     * Affiche ou masque les pioches d'actions selon le mode de jeu
     * @param visible true pour afficher
     */
    public void setActionDecksVisible(boolean visible) {
        btnPiocheAction.setVisible(visible);
        btnDefausseAction.setVisible(visible);
        labPiocheAction.setVisible(visible);
        labDefausseAction.setVisible(visible);
    }

    public void setActionHandVisible(boolean visible) {
        deckActionButton.setVisible(visible);
        actionDeckCountLabel.setVisible(visible);
    }

    public void setActionGuide(String message) {
        actionGuide.setText(message);
    }

    public void setMancheLabel(int roundNumber) {
        mancheLabel.setText("Manche " + roundNumber);
    }

    public void updatePoints() {
        if (currentPlayer != null) {
            int liveScore = currentPlayer.getHand().getVisibleCardScore();
            int totalWithLive = currentPlayer.getTotalScore() + liveScore;
            pointsLabel.setText(totalWithLive + " P");
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
        int colorIndex = listPlayer.indexOf(player) % PLAYER_COLORS.length;
        transitionPanel.showTransition(player.getName(), PLAYER_COLORS[colorIndex], onDone);
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

    // a redimenssionner dynamiquement plus tard ?
    private void resizeCards() {
        int sizeH = this.getHeight();
        int sizeW = this.getWidth();

        // 95x135 Taille de base
        int largeurCarte = 95;
        int hauteurCarte = 135;

        System.out.println(sizeH);
        System.out.println(sizeW);
        if (sizeH > 720 || sizeW > 1280) { // ca veut dire qu'on est pas en mode fenetre, et donc on est en full
                                           // screen..
            largeurCarte = 95 + 30; // pour les petits ecran type 13 pouces
            hauteurCarte = 135 + 30;
            if (sizeH > 1000 || sizeW > 2000) { // pour les gros ecrans types 23/27 pouces
                largeurCarte = 95 * 2;
                hauteurCarte = 135 * 2;
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                cardButtons[i][j].setCardSize(largeurCarte, hauteurCarte);
            }
        }
        previewPanel.setCardSize(largeurCarte, hauteurCarte);

        //redimensionner le bouton deck action et les boutons de l'overlay
        int smallCardWidth = (int) (largeurCarte * 0.7);
        int smallCardHeight = (int) (hauteurCarte * 0.7);
        deckActionButton.setCardSize(smallCardWidth, smallCardHeight);

        CardButton[] overlayButtons = actionCardOverlay.getActionCardButtons();
        for (CardButton btn : overlayButtons) {
            btn.setCardSize(largeurCarte, hauteurCarte);
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
        CardButton[] overlayActionButtons = actionCardOverlay.getActionCardButtons();
        for (int i = 0; i < overlayActionButtons.length; i++) {
            if (overlayActionButtons[i] != null) {
                overlayActionButtons[i].setAnimationEnabled(false);
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
