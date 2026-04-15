package fr.upc.cr1a.view;

import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.NumberCardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import com.jhlabs.image.GaussianFilter;
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

    private Font doodleFont;
    private BufferedImage blurredBg; // fond flouté capturé au moment de l'affichage

    // Animation fondu :
    private float opaque = 0.0f; // 0.0 = invisible, 1.0 = totalement opaque

    public PlayerPreviewPanel(){
        setLayout(new BorderLayout());
        setOpaque(false);
        //setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); //marge interieur 
        setVisible(false);
        //showOtherPlayerHand();
        this.cardButtons = new CardButton[3][4];

        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            doodleFont = Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (Exception e) {
            doodleFont = new Font("Arial", Font.BOLD, 12);
        }
    }


    public void showOtherPlayerHand(PlayerModel player){
        removeAll(); // TRES IMPORTANT sinon les cartes facedown du debut seront tjr au dessus visuellement

        JLabel title = new JLabel("Main de " + player.getName(), JLabel.CENTER);
        title.setFont(doodleFont.deriveFont(Font.BOLD, 30f));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
        this.add(title, BorderLayout.NORTH);

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
            return CaseOpeningOverlayPanel.getSelectedSkinPath();
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

    // capture le fond, le floute, et met dans blurredBg
    private void updateBlurredBackground() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        Component parent = getParent(); // il faut le parent car sinon elle ne prenais pas en compte le background pour flouter
        if (parent == null) return;
        Component root = parent;
        if (parent.getParent() != null) root = parent.getParent();
        // se cacher avant de capturer pour ne pas flouter notre propre flou
        boolean visible = isVisible();
        setVisible(false);

        // calculer la position de ce panel par rapport à root
        int offX = 0, offY = 0;
        Component c = this;
        while (c != root && c != null) {
            offX += c.getX();
            offY += c.getY();
            c = c.getParent();
        }
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.translate(-offX, -offY);
        root.paint(g2);
        g2.dispose();
        blurredBg = new GaussianFilter(22).filter(img, null);
        setVisible(visible);
    }

    /**
     * Methode qui remplace 'setOpaque()'
     * Fait apparaitre le panel avec un effet de fondu
     * @param fadeOn si true, fais apparaitre le panel (et inversement)
     */
    public void fade(boolean fadeOn){
        AnimLoop.remove(this);
            if (fadeOn){
                if (!isVisible()) opaque = 0; // reset seulement si le panel etait caché
                updateBlurredBackground();
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
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // flou en arrondi
        if (blurredBg != null) {
            g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40));
            g2.drawImage(blurredBg, 0, 0, null);
            g2.setClip(null);
        }

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
        g2.dispose();
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.opaque));
        super.paint(g2);
        g2.dispose();
    }
}
