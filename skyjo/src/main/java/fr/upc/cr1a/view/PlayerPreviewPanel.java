package fr.upc.cr1a.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Classe pour le panel de preview des autres joueurs
 * Les previews de leurs cartes devront etre ajouter ici
 * 
 * @author Mei
 */
public class PlayerPreviewPanel extends JPanel{
    
    public PlayerPreviewPanel(){
        setLayout(new GridBagLayout()); // modifier la taille du grid bag layout plus tard pour y mettres les cartes en 3*4
        setOpaque(false);
        //setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); //marge interieur 
        setVisible(false);
    }

    // rajouter une methode ici pour dessiner les cartes

    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)  g;
        g2.setColor(new Color(232, 232, 232)); // gris clair
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // arrondi un peu les bords
        g2.dispose();
        super.paintComponent(g);
    }
}
