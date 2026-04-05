package fr.upc.cr1a.view;

import fr.upc.cr1a.model.AnimLoop;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;

/**
 * Petit overlay centré pour expliquer l'effet d'une carte action
 * basé sur PlayerPreviewPanel
 */
public class ActionDescriptionOverlayPanel extends JPanel {

    private final JLabel titleLabel;
    private final JTextArea descriptionArea;

    private float opaque = 0.0f;

    public ActionDescriptionOverlayPanel() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setVisible(false);

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);

        descriptionArea = new JTextArea(); // JTextArea permet le retour à la ligne automatique
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setLineWrap(true); // retour a la ligne automatique
        descriptionArea.setWrapStyleWord(true); // permet de pas couper les mots entre deux lignes
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setForeground(Color.BLACK);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

        add(titleLabel, BorderLayout.NORTH);
        add(descriptionArea, BorderLayout.CENTER);
    }

    public void showDescription(String title, String description) {
        titleLabel.setText(title);
        descriptionArea.setText(description);
        descriptionArea.setCaretPosition(0);
        fade(true);
    }

    public void hideDescription() {
        fade(false);
    }

    /**
     * meme effet que PlayerPreviewPanel 
     */
    public void fade(boolean fadeOn) {
        AnimLoop.remove(this);
        if (fadeOn) {
            setVisible(true);
            AnimLoop.add(this, dt -> {
                opaque += 0.08f * (float) dt;
                if (opaque >= 1.0f) {
                    opaque = 1.0f;
                    AnimLoop.remove(this);
                }
                repaint();
            });
            return;
        }

        AnimLoop.add(this, dt -> {
            opaque -= 0.08f * (float) dt;
            if (opaque <= 0.0f) {
                opaque = 0.0f;
                AnimLoop.remove(this);
                setVisible(false);
            }
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(168, 168, 168));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.opaque));
        super.paint(g2);
        g2.dispose();
    }
}
