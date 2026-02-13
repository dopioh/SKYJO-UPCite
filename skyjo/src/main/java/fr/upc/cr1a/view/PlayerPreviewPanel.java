package fr.upc.cr1a.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;

/**
 * Classe pour le panel de preview des autres joueurs
 * Les previews de leurs cartes devront etre ajouter ici
 * 
 * @author Mei
 */
public class PlayerPreviewPanel extends JPanel{

    private CardButton[][] cardButtons;
    
    public PlayerPreviewPanel(){
        setLayout(new GridBagLayout()); // modifier la taille du grid bag layout plus tard pour y mettres les cartes en 3*4
        setOpaque(false);
        //setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); //marge interieur 
        setVisible(false);
        //showOtherPlayerHand();
    }

    // rajouter une methode ici pour dessiner les cartes

    public void showOtherPlayerHand(PlayerModel player){
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
        revalidate();
        repaint();
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

    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)  g;
        g2.setColor(new Color(232, 232, 232)); // gris clair
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // arrondi un peu les bords
        g2.dispose();
        super.paintComponent(g);
    }
}
