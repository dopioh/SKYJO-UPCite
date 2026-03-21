package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;

/**
 * Vue graphique du plateau de jeu avec affichage des cartes et boutons joueurs
 * 
 * @author Mei, Ian, Hugo, Nassim
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

    // grille de boutons cartes
    private CardButton[][] cardButtons;

    // bouton retour vers le menu
    private JButton btnRetour;

    // joueur actuellement affiche
    private PlayerModel currentPlayer;

    private PlayerPreviewPanel previewPanel; // Le nouveau panel
    private JLayeredPane layeredPane; // gestion des couches

    private CursorCardView cursorCardView;

    // Affichage de la main des cartes action du joueur
    private static final int MAX_ACTION_CARDS = 8;
    private JPanel actionHandPanel;
    private CardButton[] actionCardButtons;
    private ActionType[] actionCardTypes;
    private int visibleActionCount = 0;   // nombre de boutons actuellement visibles

    // Affichage des cartes pilées
    private CardButton btnPioche;
    private CardButton btnDefausse;

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

    public GameBoardView(ArrayList<PlayerModel> listPlayerModels) {
        this.listPlayer = listPlayerModels;
        this.buttonlistPlayer = new ArrayList<JButton>();
        this.cardButtons = new CardButton[3][4];
        this.actionCardButtons = new CardButton[MAX_ACTION_CARDS];
        this.actionCardTypes = new ActionType[MAX_ACTION_CARDS];
        setLayout(new BorderLayout());
        setOpaque(true);
        setVisible(true);

        layeredPane = new JLayeredPane(); // gestionnaire de couche (doc :
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

        // PANEL PLATEAU :
        JPanel plateauPanel = new JPanel();
        plateauPanel.setOpaque(true);
        plateauPanel.setLayout(new GridBagLayout());

        layeredPane.add(plateauPanel, JLayeredPane.DEFAULT_LAYER);

        // ZONE PLAYER ( a gauche de l'ecran, la ou y'a les boutons)
        this.zonePlayer = new JPanel();
        zonePlayer.setLayout(new GridBagLayout());
        zonePlayer.setBackground(Color.WHITE);
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

        plateauPanel.add(zonePlayer, gbcZonePlayer);

        // ZONE PLATEAU ( 3/4 de l'ecran a partir de la droite, les
        // cartes,pioches,defausses et score seront ici)
        this.zonePlateau = new JPanel();
        zonePlateau.setLayout(new BorderLayout());
        zonePlateau.setBackground(Color.WHITE);

        GridBagConstraints gbcZonePlateau = new GridBagConstraints();
        gbcZonePlateau.gridx = 2;
        gbcZonePlateau.gridy = 0;
        gbcZonePlateau.gridwidth = 2;
        gbcZonePlateau.gridheight = 2;
        gbcZonePlateau.weightx = 5;
        gbcZonePlateau.weighty = 1;
        gbcZonePlateau.anchor = GridBagConstraints.EAST;
        gbcZonePlateau.fill = GridBagConstraints.BOTH;

        plateauPanel.add(zonePlateau, gbcZonePlateau);

        // panel grille pour les cartes
        JPanel cardsGrid = new JPanel(new GridBagLayout());
        cardsGrid.setOpaque(true);
        cardsGrid.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(cardSpacing, cardSpacing, cardSpacing, cardSpacing);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        actionGuide = new JLabel("PLACEHOLDER", SwingConstants.CENTER);
        actionGuide.setFont(new Font("Arial", Font.BOLD, 14));
        actionGuide.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        cardsGrid.add(actionGuide, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Container vertical pour message + cartes
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BorderLayout());
        cardsContainer.setOpaque(false);

        // %essage d'action
        playerName = new JLabel("PLACEHOLDER", SwingConstants.CENTER);
        playerName.setFont(new Font("Arial", Font.BOLD, 16));
        playerName.setForeground(Color.BLACK);
        playerName.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        cardsContainer.add(playerName, BorderLayout.NORTH);
        cardsContainer.add(cardsGrid, BorderLayout.CENTER);

        // Panel des cartes actions dans la main du joueur
        actionHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        actionHandPanel.setOpaque(false);
        // pre-creation des cartes actions
        for (int i = 0; i < MAX_ACTION_CARDS; i++) {
            CardButton btn = new CardButton();
            btn.setAnimationEnabled(EnableHandCardAnimations);
            btn.setCardSize(75, 100);
            btn.setVisible(false);
            actionCardButtons[i] = btn;
            actionHandPanel.add(btn);
        }

        // creer les 12 CardButton
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                gbc.gridx = col;
                gbc.gridy = row + 1;
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

        // bouton retour en bas temporaire
        btnRetour = new JButton("Retour");
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnRetour);
        zonePlateau.add(bottomPanel, BorderLayout.SOUTH);

        // ZONE PREVIEW OTHER PLAYERS ( apparait au dessus de tous les autres panels
        // quand on hover ou clique sur un button)
        previewPanel = new PlayerPreviewPanel();
        previewPanel.setVisible(false);
        // previewPanel.setBounds(400, 200, 500, 500); // taille fix pour le test
        layeredPane.add(previewPanel, JLayeredPane.MODAL_LAYER);

        this.nextRoundPanel = new NextRoundPanel(currentPlayer);
        layeredPane.add(nextRoundPanel, JLayeredPane.PALETTE_LAYER);

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
        int score = player.getHand().getVisibleCardScore();
        int total = player.getTotalScore();
        playerName.setText("Main de " + player.getName() + " - Manche : " + score + " | Total : " + total);
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
    }

    /**
     * maj les boutons carte action
     */
    public void showActionHand(PlayerModel player) {
        ArrayList<ActionCardModel.ActionType> actions = player.getActionHand();
        visibleActionCount = (actions == null) ? 0 : actions.size();

        for (int i = 0; i < MAX_ACTION_CARDS; i++) {
            if (i < visibleActionCount) {
                ActionType type = actions.get(i);
                actionCardTypes[i] = type;
                ActionCardModel tempCard = new ActionCardModel(false, type); // juste pour recup l'image de la bonne carte
                actionCardButtons[i].setCardImage(getCardImagePath(tempCard));
                actionCardButtons[i].setVisible(true);
            } else {
                actionCardTypes[i] = null;
                actionCardButtons[i].setVisible(false);
            }
        }

        actionHandPanel.revalidate(); //ça sert a quoi ?
        actionHandPanel.repaint();
    }

    /**
     * Getters pour que le controller puisse ajouter ses listeners
     */
    public CardButton[] getActionCardButtons() {
        return actionCardButtons;
    }

    public ActionType getActionCardType(int index) {
        return actionCardTypes[index];
    }

    public int getVisibleActionCount() {
        return visibleActionCount;
    }

    /**
     * Retourne le chemin de l'image pour une carte
     */
    public String getCardImagePath(CardModel card) {
        if (card.isFaceDown()) {
            return "/cartes/carteDos.png";
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
                    return "/cartes/carteDos.png";
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
        for (PlayerModel pm : listPlayer) {
            JButton p = new JButton(pm.getName());
            // p.setOpaque(true);
            // p.setBorderPainted(false);
            p.setBackground(Color.WHITE);
            p.setForeground(Color.BLACK);
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            //////////// un listner dans viewwwwwwwwww // c'est un listener pour modifier
            //////////// des couleurs, faut le laisser la
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    p.setBackground(new Color(242, 242, 242)); // ne marche pas sur MacOS sans modifier la nature du bouton // ah bon
                    // p.setForeground(new Color(73, 134, 196));
                    // showOtherPlayerMain(true);
                    isPinned = false;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isPinned) {
                        p.setBackground(Color.WHITE);
                        p.setForeground(Color.BLACK);
                        // showOtherPlayerMain(false);
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

    public JButton getBtnRetour() {
        return this.btnRetour;
    }

    public CardButton[][] getCardButtons() {
        return this.cardButtons;
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
        actionHandPanel.setVisible(visible);
        actionHandPanel.revalidate();
        actionHandPanel.repaint();
    }

    public void setActionGuide(String message) {
        actionGuide.setText(message);
    }

    public NextRoundPanel getNextRoundPanel() {
        return this.nextRoundPanel;
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
     * Mets à jour l'affichage des cartes de la pioche et de la défausse
     * 
     * @param topDiscard   Carte du haut de la défausse
     * @param drawHasCards
     */
    public void updateCardDisplay(CardModel topDiscard, boolean drawHasCards) {
        // Pioche
        if (drawHasCards) {
            btnPioche.setText("");
            btnPioche.setCardImage("/cartes/carteDos.png");
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
            btnPiocheAction.setCardImage("/cartes/carteDos.png");
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
            turnTimerLabel.setText("Temps restant : "+ text +" s");
        }
    }
}
