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

    public GameBoardView(ArrayList<PlayerModel> listPlayerModels){
        this.listPlayer = listPlayerModels;
        this.buttonlistPlayer = new ArrayList<JButton>();
        setLayout(new GridBagLayout());
        setOpaque(true);
        setVisible(true);
        initComponents();

        revalidate();
        repaint();
    }

    public void initComponents(){
        System.out.println("initComponents Called");

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

        add(zonePlayer, gbcZonePlayer);

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

        add(zonePlateau, gbcZonePlateau);

        
        //JButton test = new JButton("JE SUISSSSS LAAA");
        //zonePlayer.add(test);

        playerButton();
    }
    
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
            }

            @Override
            public void mouseExited( MouseEvent e) {
                p.setBackground(Color.WHITE);
                p.setForeground(Color.BLACK);
        
        }});
            buttonlistPlayer.add(p);
            zonePlayer.add(p,gbcPlayerButton);
            gbcPlayerButton.gridy ++;
        }
    }

    // getter 

    public ArrayList<JButton> getListPlayerButton(){
        return this.buttonlistPlayer;
    }
}
