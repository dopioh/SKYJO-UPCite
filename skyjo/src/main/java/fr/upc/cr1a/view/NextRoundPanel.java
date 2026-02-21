package fr.upc.cr1a.view;

import javax.swing.*;

import fr.upc.cr1a.model.PlayerModel;

import java.awt.*;

public class NextRoundPanel extends JPanel {
    // Animation fondu :
    private float opaque = 0.0f; // 0.0 = invisible, 1.0 = totalement opaque
    private Timer fadeTimer;
    private PlayerModel player;

    public NextRoundPanel(PlayerModel nxtPlayer) {
        this.player = nxtPlayer;
        setOpaque(false);
        setVisible(false);
    }

    /**
     * Methode qui remplace 'setOpaque()'
     * Fait apparaitre le panel avec un effet de fondu
     * 
     * @param fadeOn si true, fais apparaitre le panel (et inversement)
     */
    public void fade(boolean fadeOn) {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        if (fadeOn) {
            // le delay est en ms
            setVisible(true);
            fadeTimer = new Timer(15, e -> {
                opaque += 0.02f; // vitesse d'apparition
                if (opaque >= 1.0f) {
                    opaque = 1;
                    fadeTimer.stop();
                }
                repaint();
            });

            fadeTimer.start();

        } else {
            fadeTimer = new Timer(15, e -> {
                opaque -= 0.02f;
                if (opaque <= 0.0f) {
                    opaque = 0;
                    fadeTimer.stop();
                    setVisible(false);
                }
                repaint();
            });

            fadeTimer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create(); // copie du pinceau
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // bord plus lisses
        g2.setColor(new Color(255, 196, 184)); // rose tres clair
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
        
        g2.dispose(); // seul la copie est detruite, les cartes peuvent etre dessiner avec le pinceau original
    }

    @Override
    protected void paintChildren(Graphics g){
        //if (player == null) System.out.println("PLAYER NULL CA VA CRASH");;
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Arial", Font.BOLD,30)); // fix la version full screen avec les cartes aussi
        String text = "Prochain Joueur : "+ player.getName();

        FontMetrics fm = g2.getFontMetrics();

        int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + textHeight) / 2;
    
        g2.setColor(Color.BLACK);
        g2.drawString(text , x, y);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.opaque));
        super.paint(g2);
        g2.dispose();
    }

    public void setCurrentPlayer(PlayerModel player){
        this.player = player;
    }
}
