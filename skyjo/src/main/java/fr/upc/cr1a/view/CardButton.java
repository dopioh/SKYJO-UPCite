package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import fr.upc.cr1a.utils.ImageCache;

/**
 * JButton qui affiche une image de carte cliquable
 * L'image est redimensionnée une seule fois et gardée en cache
 * 
 * @author Ian
 */
public class CardButton extends JButton {

    private static final int CardW = 80;
    private static final int CardH = 115; 

    // crop car la carte n'est pas centrée
    // c'est 1217x1754 apres crop
    private static final int CropLeft = 446;
    private static final int CropRight = 385;
    private static final int CropTop = 147;
    private static final int CropBottom = 147;

    private static final java.util.HashMap<String, BufferedImage> imageCache = new java.util.HashMap<>();

    private BufferedImage cardImage;

    public CardButton() {
        setPreferredSize(new Dimension(CardW, CardH));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
    }

    /**
     * Met a jour l'image affichée sur le bouton
     * @param imgPath chemin vers l'image dans les resources
     */
    public void setCardImage(String imgPath) {

        // caher carte, utile quand on déplace ?
        if (imgPath == null) {
            this.cardImage = null;
            repaint();
            return;
        }

        // si deja dans le cache on reprend
        if (imageCache.containsKey(imgPath)) {
            this.cardImage = imageCache.get(imgPath);
            repaint();
            return;
        }

        // pas dans le cache donc on crop
        BufferedImage original = ImageCache.get(imgPath);
        if (original != null) {
            int cropW = original.getWidth() - CropLeft - CropRight;
            int cropH = original.getHeight() - CropTop - CropBottom;

            // nouvelle image cropée
            BufferedImage cropped = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = cropped.createGraphics();
            g2.drawImage(original.getSubimage(CropLeft, CropTop, cropW, cropH), 0, 0, null);
            g2.dispose();

            this.cardImage = cropped;
            imageCache.put(imgPath, this.cardImage);
        } else {
            this.cardImage = null;
        }
        repaint();
    }

    /**
     * Enleve l'image du bouton, peut etre utile
     */
    public void clearImage() {
        this.cardImage = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cardImage != null) {
            // on fait le scale au moment de paint pour une qualité pro max
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(cardImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
