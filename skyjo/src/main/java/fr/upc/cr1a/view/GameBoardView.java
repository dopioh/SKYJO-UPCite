package fr.upc.cr1a.view;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import fr.upc.cr1a.model.PlayerModel;

public class GameBoardView extends JPanel{

    // elements important : 
    private ArrayList<PlayerModel> listPlayer; // jusqu'a 8 normalement

    private ArrayList<JButton> ButtonlistPlayer; // jusqu'a 8 normalement

    public GameBoardView(ArrayList<PlayerModel> listPlayerModels){
        this.listPlayer = listPlayerModels;
        setLayout(new GridBagLayout());
        setOpaque(true);
        setVisible(true);
        initComponents();

        revalidate();
        repaint();
    }

    public void initComponents(){
        System.out.println("initComponents Called");

        JPanel zonePlayer = new JPanel();
        zonePlayer.setLayout(new GridBagLayout());
        zonePlayer.setBackground(Color.YELLOW);
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

        JPanel zonePlateau = new JPanel();
        zonePlateau.setLayout(new GridBagLayout());
        zonePlateau.setBackground(Color.GRAY);

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

        
        JButton test = new JButton("JE SUISSSSS LAAA");
        zonePlayer.add(test);
    }
    
    private void playerButton(){
        for (PlayerModel pm : listPlayer){
            
        }
    }
}
