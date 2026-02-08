package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;

/**
 * Vue graphique du plateau de jeu avec affichage des cartes et boutons joueurs
 * 
 * @author Mei
 * @author Ian
 */
public class GameBoardView extends JPanel{

    private static final int cardSpacing = 2; // <----------------

    // elements important : 
    private ArrayList<PlayerModel> listPlayer; // jusqu'a 8 normalement

    // a transferer : 
    private ArrayList<JButton> buttonlistPlayer; // jusqu'a 8 normalement

    // panel : 
    private JPanel zonePlayer;
    private JPanel zonePlateau;

    // grille de boutons cartes
    private CardButton[][] cardButtons;
    private JLabel playerNameLabel;

    // bouton retour vers le menu
    private JButton btnRetour;

    // joueur actuellement affiche
    private PlayerModel currentPlayer;

    private PlayerPreviewPanel previewPanel; // Le nouveau panel
    private JLayeredPane layeredPane; // gestion des couches

    // pour fixer le clique
    private boolean isPinned = false;

    public GameBoardView(ArrayList<PlayerModel> listPlayerModels){
        this.listPlayer = listPlayerModels;
        this.buttonlistPlayer = new ArrayList<JButton>();
        this.cardButtons = new CardButton[3][4];
        setLayout(new BorderLayout());
        setOpaque(true);
        setVisible(true);

        layeredPane = new JLayeredPane(); // gestionnaire de couche (doc : https://docs.oracle.com/javase/8/docs/api/javax/swing/JLayeredPane.html)
        add(layeredPane, BorderLayout.CENTER);

        // pour resize des que on change de taille de fenetre
        addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            resizeLayers();
        }
        });

        initComponents();

        // afficher les cartes du premier joueur par defaut
        if (!listPlayer.isEmpty()) {
            showPlayerCards(listPlayer.get(0));
        }

        revalidate();
        repaint();
    }

    public void initComponents(){
        //System.out.println("initComponents Called");

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

        // ZONE PLATEAU ( 3/4 de l'ecran a partir de la droite, les cartes,pioches,defausses et score seront ici)
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

        // label pour le nom du joueur affiché en haut
        playerNameLabel = new JLabel("Selectionnez un joueur", SwingConstants.CENTER);
        playerNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        playerNameLabel.setForeground(Color.BLACK);
        zonePlateau.add(playerNameLabel, BorderLayout.NORTH);

        // panel grille pour les cartes
        JPanel cardsGrid = new JPanel(new GridBagLayout());
        cardsGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(cardSpacing, cardSpacing, cardSpacing, cardSpacing);

        // creer les 12 CardButton
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                gbc.gridx = col;
                gbc.gridy = row;
                CardButton btn = new CardButton();
                cardButtons[row][col] = btn;
                cardsGrid.add(btn, gbc);
            }
        }

        zonePlateau.add(cardsGrid, BorderLayout.CENTER);

        // bouton retour en bas temporaire
        btnRetour = new JButton("Retour");
        btnRetour.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnRetour);
        zonePlateau.add(bottomPanel, BorderLayout.SOUTH);

        // ZONE PREVIEW OTHER PLAYERS ( apparait au dessus de tous les autres panels quand on hover ou clique sur un button)
        previewPanel = new PlayerPreviewPanel();
        previewPanel.setVisible(false);
        previewPanel.setBounds(400, 200, 600, 500); // taille fix pour le test
        layeredPane.add(previewPanel, JLayeredPane.PALETTE_LAYER);


        // BOUTTONS PLAYERS ( a gauche )
        playerButton();

        SwingUtilities.invokeLater(() -> resizeLayers()); // doit s'executer apres que tous les components soit creer
    }

    /**
     * Affiche les cartes d'un joueur qu'on veut à droite
     */
    public void showPlayerCards(PlayerModel player) {
        this.currentPlayer = player;
        playerNameLabel.setText("Main de " + player.getName());
        PlayerHandModel hand = player.getHand();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                CardButton btn = cardButtons[row][col];
                CardModel card = hand.getCard(row, col);

                if (card == null) {
                    btn.clearImage();
                    btn.setText("vide");
                } else {
                    btn.setText("");
                    String imgPath = getCardImagePath(card);
                    btn.setCardImage(imgPath);
                }
            }
        }

        zonePlateau.revalidate();
        zonePlateau.repaint();
    }

    /**
     * Retourne le chemin de l'image pour une carte
     */
    private String getCardImagePath(CardModel card) {
        if (card.isFaceDown()) {
            return "/cartes/carteDos.png";
        }
        int val = card.getCardValue();
        if (val == -2) return "/cartes/carteMoins2.png";
        if (val == -1) return "/cartes/carteMoins1.png";
        return "/cartes/carte" + val + ".png";
    }
    
    /**
     * Initialise tous les boutons selon le nombre de joueur dans la partie
     * Recuperer l'info de la list de PlayerModel
     * Creer dans le GameManager (pour le moment)
     */
    private void playerButton(){
        GridBagConstraints gbcPlayerButton = new GridBagConstraints();
        gbcPlayerButton.gridx = 0;
        gbcPlayerButton.gridy = 0;
        gbcPlayerButton.weightx = 1;
        gbcPlayerButton.weighty = 1;
        for (PlayerModel pm : listPlayer){
            JButton p = new JButton(pm.getName());
            //p.setOpaque(true);
            //p.setBorderPainted(false);
            p.setBackground(Color.WHITE);
            p.setForeground(Color.BLACK);
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            //////////// un listner dans viewwwwwwwwww
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered( MouseEvent e) {
                    p.setBackground(new Color(242, 242, 242)); // ne marche pas sur MacOS sans modifier la nature du bouton     ah bon
                    //p.setForeground(new Color(73, 134, 196));
                    showOtherPlayerMain(true);
                    isPinned = false;
                }

                @Override
                public void mouseExited( MouseEvent e) {
                    if (!isPinned){
                        p.setBackground(Color.WHITE);
                        p.setForeground(Color.BLACK);
                        showOtherPlayerMain(false);
                    }
                }
            });
            buttonlistPlayer.add(p);
            zonePlayer.add(p,gbcPlayerButton);
            gbcPlayerButton.gridy ++;
        }
    }

    /**
     * Affiche, selon le boolean, la preview
     * @param isVisible
     */
    public void showOtherPlayerMain(boolean isVisible){
        previewPanel.setVisible(isVisible);
    }

    /**
     * Methode interne pour resize selon la taille de la fenetre swing actuelle
     */
    private void resizeLayers() {
        Dimension size = getSize(); // size de la fenetre ?
        Component[] compsD = layeredPane.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER); // recupere les elements de la couche DEFAULT
        for (Component c : compsD) {
            c.setBounds(0, 0, size.width, size.height);
        }

        Component[] compsP = layeredPane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
        for (Component c : compsP){
            c.setBounds(size.width/5, size.height/10, size.width - size.width/3, size.height - size.height/5);
        }

        
    }

    // getter 
    public ArrayList<JButton> getListPlayerButton(){
        return this.buttonlistPlayer;
    }

    public JButton getBtnRetour(){
        return this.btnRetour;
    }

    public CardButton[][] getCardButtons(){
        return this.cardButtons;
    }

    public PlayerModel getCurrentPlayer(){
        return this.currentPlayer;
    }
}
