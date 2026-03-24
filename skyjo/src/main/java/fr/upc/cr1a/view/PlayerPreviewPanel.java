package fr.upc.cr1a.view;

import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.NumberCardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;

import java.awt.*;
import javax.swing.*;

/**
 * Classe pour le panel de preview des autres joueurs
 * Les previews de leurs cartes devront etre ajouter ici
 * 
 * @author Mei
 */
public class PlayerPreviewPanel extends JPanel{

    private CardButton[][] cardButtons;
    private static final int cardSpacing = 2;

    private int lastCardW = 95;
    private int lastCardH = 135;

    private PlayerModel player;

    // Animation fondu : 
    private float opaque = 0.0f; // 0.0 = invisible, 1.0 = totalement opaque
    
    public PlayerPreviewPanel(){
        setLayout(new BorderLayout());
        setOpaque(false);
        //setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); //marge interieur 
        setVisible(false);
        //showOtherPlayerHand();
        this.cardButtons = new CardButton[3][4];
    }


    public void showOtherPlayerHand(PlayerModel player){
        this.player = player;
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
                btn.setAnimationEnabled(false);
                btn.setCardSize(lastCardW, lastCardH);
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
                    btn.setCardImage("/cartes/carteVide.png");
                    btn.setEnabled(false);
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
        if (card instanceof NumberCardModel numberCard && numberCard.isStar()) {
            return "/cartes/carteEtoile.png";
        }
        int val = card.getCardValue();
        if (val == -2) return "/cartes/carteMoins2.png";
        if (val == -1) return "/cartes/carteMoins1.png";
        return "/cartes/carte" + val + ".png";
    }

    /**
     * Appelle directement la taille des cartes de card model
     * @param width
     * @param height
     */
    public void setCardSize(int width, int height) {
        lastCardW = width;
        lastCardH = height;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (cardButtons[row][col] != null) {
                    cardButtons[row][col].setCardSize(width, height);
                }
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Methode qui remplace 'setOpaque()'
     * Fait apparaitre le panel avec un effet de fondu
     * @param fadeOn si true, fais apparaitre le panel (et inversement)
     */
    public void fade(boolean fadeOn){
        AnimLoop.remove(this);
            if (fadeOn){
                // le delay geré par AnimLoop
                setVisible(true);
                AnimLoop.add(this, dt -> {
                opaque += 0.05f * (float) dt; // vitesse d'apparition
                if (opaque >= 1.0f){
                    opaque = 1;
                    AnimLoop.remove(this);
                }
                repaint();
            });

            } else {
                AnimLoop.add(this, dt -> {
                    opaque -= 0.05f * (float) dt;
                    if (opaque <= 0.0f){
                        opaque = 0;
                        AnimLoop.remove(this);
                        setVisible(false);
                    }
                    repaint();
                });
            }
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create(); // copie du pinceau
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // bord plus lisses
        g2.setColor(new Color(168, 168, 168)); // gris clair
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
        
        g2.dispose(); // seul la copie est detruite, les cartes peuvent etre dessiner avec le pinceau original
    }

    @Override
    protected void paintChildren(Graphics g){
        if (player == null) return;
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Arial", Font.BOLD,18)); // fix la version full screen avec les cartes aussi
        String text = "Preview de la main du joueur " + player.getName() + " :";

        FontMetrics fm = g2.getFontMetrics();

        int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + textHeight) / 12;
    
        g2.setColor(Color.BLACK);
        g2.drawString(text , x, y);
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.opaque));
        super.paint(g2);
        g2.dispose();
    }
}
