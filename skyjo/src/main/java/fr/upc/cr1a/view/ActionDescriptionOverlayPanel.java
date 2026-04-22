package fr.upc.cr1a.view;

import fr.upc.cr1a.model.AnimLoop;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Petit overlay centré pour expliquer l'effet d'une carte action
 * basé sur PlayerPreviewPanel
 */
public class ActionDescriptionOverlayPanel extends JPanel {

    private static final Color CARD_COLOR = new Color(248, 243, 226, 255);
    private static final Color CARD_BORDER_COLOR = new Color(54, 45, 34, 215);
    private static final Color TITLE_COLOR = new Color(46, 38, 26);
    private static final Color TEXT_COLOR = new Color(38, 33, 25);

    private final JLabel titleLabel;
    private final JLabel descriptionLabel;
    private final Font titleFont;
    private final Font bodyFont;

    private float opaque = 0.0f;

    public ActionDescriptionOverlayPanel() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setVisible(false);

        titleFont = loadFont(26f, Font.BOLD);
        bodyFont = loadFont(24f, Font.PLAIN);

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(TITLE_COLOR);

        descriptionLabel = new JLabel("", SwingConstants.CENTER);
        descriptionLabel.setOpaque(false);
        descriptionLabel.setFont(bodyFont);
        descriptionLabel.setForeground(TEXT_COLOR);
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

        add(titleLabel, BorderLayout.NORTH);
        add(descriptionLabel, BorderLayout.CENTER);
    }

    public void showDescription(String title, String description) {
        titleLabel.setText(title);
        descriptionLabel.setText(centrerTexte(description));
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
    /**
     * centre le texte dans l'overlay grace a html
     * @param text le texte a centrer
     * @return du code html
     */
    private String centrerTexte(String text) {
        // Java reconnait mal la police donc ça prend une police par defaut mais je trouve que ça passe
        if (text == null) {
            return "";
        }
        return "<html><div style='text-align:center;'>" + text + "</div></html>";
    }

    private Font loadFont(float size, int style) {
        try (InputStream stream = getClass().getResourceAsStream("/PatrickHandSC.ttf")) {
            if (stream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(style, size);
            }
            return new Font("Arial", style, Math.round(size));
        } catch (FontFormatException | IOException ignored) {
            return new Font("Arial", style, Math.round(size));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CARD_COLOR);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.setColor(CARD_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
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
