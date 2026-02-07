package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

import fr.upc.cr1a.model.PlayerModel;

public class GameBoardView extends JPanel{

    // elements important : 
    private ArrayList<PlayerModel> listPlayer; // jusqu'a 8 normalement

    // a transferer : 
    private ArrayList<JButton> buttonlistPlayer; // jusqu'a 8 normalement

    // panel : 
    private JPanel zonePlayer;
    private JPanel zonePlateau;

    private PlayerPreviewPanel previewPanel; // Le nouveau panel
    private JLayeredPane layeredPane; // gestion des couches

    // pour fixer le clique
    private boolean isPinned = false;

    public GameBoardView(ArrayList<PlayerModel> listPlayerModels){
        this.listPlayer = listPlayerModels;
        this.buttonlistPlayer = new ArrayList<JButton>();
        setLayout(new BorderLayout());
        setOpaque(true);
        setVisible(true);

        layeredPane = new JLayeredPane(); // gestionnaire de couche (doc : https://docs.oracle.com/javase/8/docs/api/javax/swing/JLayeredPane.html)
        add(layeredPane, BorderLayout.CENTER);

        initComponents();

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
        zonePlateau.setLayout(new GridBagLayout());
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
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered( MouseEvent e) {
                    p.setBackground(new Color(242, 242, 242)); // ne marche pas sur MacOS sans modifier la nature du bouton
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
     * Methode interne pour resize des qu'on touche un element concernes
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
}
