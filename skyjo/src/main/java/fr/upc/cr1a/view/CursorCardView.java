package fr.upc.cr1a.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import fr.upc.cr1a.utils.ImageCache;

/**
 * Petite carte affichée sous le curseur pendant les actions de pioche/défausse.
 * Comporte une classe interne Overlay qui se charge de dessiner la petite carte
 * @author Nassim
 */
public class CursorCardView {
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 100;
    private static final int CURSOR_OFFSET_X = 5;
    private static final int CURSOR_OFFSET_Y = 5;

    private static final int CROP_LEFT = 446;
    private static final int CROP_RIGHT = 385;
    private static final int CROP_TOP = 147;
    private static final int CROP_BOTTOM = 147;

    private final JComponent gameboardview;
    private final Overlay overlay;
    private final Map<String, BufferedImage> imageCropCache = new HashMap<>(); // nouveau imageCache pour les cartes croppées (ne sert que pour CursorCard)
    private final Timer followTimer;

    private BufferedImage image;
    private int x;
    private int y;
    private boolean visible;

    public CursorCardView(JComponent gameboardview) {
        this.gameboardview = gameboardview;
        this.overlay = new Overlay();
        this.followTimer = new Timer(16, e -> updatePosition());
    }

    public JComponent getOverlay() {
        return overlay;
    }

    public void setOverlayBounds(int width, int height) {
        overlay.setBounds(0, 0, width, height);
    }

    public void showCard(String imgPath) {
        BufferedImage img = getCroppedCardImage(imgPath);
        if (img == null) {
            hideCard();
            return;
        }
        this.image = img;
        this.visible = true;
        updatePosition();
        if (!followTimer.isRunning()) {
            followTimer.start();
        }
        overlay.repaint();
    }

    public void hideCard() {
        visible = false;
        image = null;
        followTimer.stop();
        overlay.repaint();
    }

    private void updatePosition() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return;
        }
        Point p = pointerInfo.getLocation();
        SwingUtilities.convertPointFromScreen(p, gameboardview);
        x = p.x + CURSOR_OFFSET_X;
        y = p.y + CURSOR_OFFSET_Y;
        overlay.repaint();
    }

    private BufferedImage getCroppedCardImage(String imgPath) {
        if (imgPath == null) {
            return null;
        }
        if (imageCropCache.containsKey(imgPath)) {
            return imageCropCache.get(imgPath);
        }

        BufferedImage original = ImageCache.get(imgPath);
        if (original == null) {
            return null;
        }

        int cropW = original.getWidth() - CROP_LEFT - CROP_RIGHT;
        int cropH = original.getHeight() - CROP_TOP - CROP_BOTTOM;
        if (cropW <= 0 || cropH <= 0) {
            return original;
        }

        BufferedImage cropped = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = cropped.createGraphics();
        g2.drawImage(original.getSubimage(CROP_LEFT, CROP_TOP, cropW, cropH), 0, 0, null);
        g2.dispose();

        imageCropCache.put(imgPath, cropped);
        return cropped;
    }

    // on fabrique l'overlay pour dessiner la carte
    private class Overlay extends JComponent {
        Overlay() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!visible || image == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.drawImage(image, x, y, CARD_WIDTH, CARD_HEIGHT, null);
            g2.dispose();
        }
    }
}
