package fr.upc.cr1a.view;

import java.awt.*;
import javax.swing.*;
import fr.upc.cr1a.model.AnimLoop;

public class PlayerTransitionPanel extends JPanel {

    private float step = 0f;
    private float alpha = 0f;
    private String playerName = "";
    private Color playerColor = Color.WHITE;
    private Runnable onDone;
    private Font nameFont;

    public PlayerTransitionPanel() {
        setOpaque(false);
        setVisible(false);

        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            nameFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.BOLD, 64f);
        } catch (Exception e) {
            nameFont = new Font("Arial", Font.BOLD, 64);
        }
    }

    public void showTransition(String name, Color color, Runnable onDone) {
        this.playerName = name;
        this.playerColor = color;
        this.onDone = onDone;
        this.step = 0f;
        this.alpha = 0f;

        setVisible(true);
        AnimLoop.add(this, dt -> tick((float) dt));
    }

    private void tick(float dt) {
        step += dt;

        // apparition
        if (step <= 10) {
            alpha = step / 10f;
        }
        // hold
        else if (step <= 28) {
            alpha = 1f;
            if (onDone != null) {
                onDone.run();
                onDone = null;
            }
        }
        // disparition
        else {
            float progress = (step - 28) / 22f;
            alpha = 1f - progress * progress;
        }

        if (step >= 50) {
            alpha = 0f;
            AnimLoop.remove(this);
            setVisible(false);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha <= 0f) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int bandHeight = panelHeight / 5;
        int bandY = (panelHeight - bandHeight) / 2;

        // fond sombre un peu transparent
        g2.setColor(new Color(0, 0, 0, (int)(alpha * 100)));
        g2.fillRect(0, 0, panelWidth, panelHeight);

        // bande en couleur joueur
        g2.setColor(new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), (int)(alpha * 230)));
        g2.fillRect(0, bandY, panelWidth, bandHeight);

        // nom du joueur
        g2.setFont(nameFont);
        FontMetrics fm = g2.getFontMetrics();

        // center le texte
        int textX = (panelWidth - fm.stringWidth(playerName)) / 2;
        int textY = bandY + (bandHeight + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
        g2.drawString(playerName, textX, textY);

        g2.dispose();
    }
}
