package fr.upc.cr1a.view;

import javax.swing.JPanel;

import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;

import java.awt.*;

/**
 * Classe pour le panel de preview des autres joueurs
 * Les previews de leurs cartes devront etre ajouter ici
 * 
 * @author Mei
 */
public class PlayerPreviewPanel extends JPanel{

    private CardButton[][] cardButtons;
    private static final int cardSpacing = 2;
    
    public PlayerPreviewPanel(){
        setLayout(new BorderLayout());
        setOpaque(false);
        //setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); //marge interieur 
        setVisible(false);
        //showOtherPlayerHand();
        this.cardButtons = new CardButton[3][4];
    }


    public void showOtherPlayerHand(PlayerModel player){
        removeAll(); // TRES IMPORTANT sinon les cartes facedown du debut seront tjr au dessus visuellement
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

        this.add(cardsGrid, BorderLayout.CENTER);
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
        //System.out.println("getCardImagePath called"); // est called
        if (card.isFaceDown()) {
            return "/cartes/carteDos.png";
        }
        int val = card.getCardValue();
        if (val == -2) return "/cartes/carteMoins2.png";
        if (val == -1) return "/cartes/carteMoins1.png";
        return "/cartes/carte" + val + ".png";
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create(); // copie du pinceau
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // bord plus lisses
        g2.setColor(new Color(232, 232, 232)); // gris clair
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
        
        g2.dispose(); // seul la copie est detruite, les cartes peuvent etre dessiner avec le pinceau original
    }
}
