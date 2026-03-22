package fr.upc.cr1a.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.utils.ImageCache;

/**
 * JButton qui affiche une image de carte cliquable
 * L'image est redimensionnée une seule fois et gardée en cache
 * 
 * @author Ian
 */
public class CardButton extends JButton {
    // crop car la carte n'est pas centrée
    // c'est 1217x1754 apres crop
    private static final int CropLeft = 446;
    private static final int CropRight = 385;
    private static final int CropTop = 147;
    private static final int CropBottom = 147;


    private static final java.util.HashMap<String, BufferedImage> imageCache = new java.util.HashMap<>();
    private static final java.util.LinkedHashSet<CardButton> cartesAnimees = new java.util.LinkedHashSet<>(); //LinkedHashSet = pas de doublon, garde l'ordre de arrivée pour eviter des bugs
    private static final boolean IS_LINUX = System.getProperty("os.name", "").toLowerCase().contains("linux");

    private BufferedImage cardImage;

    // animation d'une carte
    private static final int FloatWide = 2; // largeur du flottement max
    private static final int FloatHigh = 3; // hauteur du flottement max
    private static final double FloatSpeed = 0.015; // vitesse du flottement
    private static final double ScaleSmoothness = 0.12; // vitesse du resize
    private double phase = Math.random() * Math.PI * 2.0; // chaque carte a une vitesse differente
    private double floatOffsetX = 0.0; // position x au debut du flottement
    private double floatOffsetY = 0.0; // position y au debut du flottement
    private double scale = 1.0;
    private double targetScale = 1.0;
    private boolean animationEnabled = false;


    public CardButton() {
        // setPreferredSize(new Dimension(CardW, CardH));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        
        // grossissement de la carte selon le comportement de la souris
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                animateTo(1.05);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                animateTo(1.0);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                animateTo(0.96);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                animateTo(1.08);
            }
    });

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

    /**
     * Sert pour le plein ecran, taille des cartes
     */
    public void setCardSize(int w, int h) {
        Dimension d = new Dimension(w, h);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        revalidate();
        repaint();
    }

    /**
     * Modifie la taille de la carte grace a un multiplicateur
     * @param newTarget le multiplicateur
     */
    private void animateTo(double newTarget) {
        if (!animationEnabled) {
            return;
        }
        targetScale = newTarget;
    }

    /**
     * Active ou désactive les animations de la carte
     * @param enabled true pour animer la carte
     */
    public void setAnimationEnabled(boolean enabled) {
        animationEnabled = enabled;
        if (enabled) {
            boolean premier = cartesAnimees.isEmpty();
            cartesAnimees.add(this);
            // on s'inscrit qu'une seule fois pour toute la classe
            if (premier) {
                AnimLoop.add(CardButton.class, dt -> {
                    CardButton[] cartes = cartesAnimees.toArray(new CardButton[0]);
                    for (CardButton carte : cartes) {
                        if (!IS_LINUX) {
                            carte.phase += FloatSpeed * dt;
                            carte.floatOffsetX = Math.cos(carte.phase) * FloatWide;
                            carte.floatOffsetY = Math.sin(carte.phase) * FloatHigh;
                        }
                        carte.scale += (carte.targetScale - carte.scale) * ScaleSmoothness * dt;
                    }
                    // un seul repaint du parent
                    if (cartes.length > 0 && cartes[0].getParent() != null) {
                        cartes[0].getParent().repaint();
                    }
                });
            }
        } else {
            cartesAnimees.remove(this);
            if (cartesAnimees.isEmpty()) {
                AnimLoop.remove(CardButton.class);
            }
            floatOffsetX = 0.0;
            floatOffsetY = 0.0;
            scale = 1.0;
            targetScale = 1.0;
            repaint();
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cardImage != null) {
            // on fait le scale au moment de paint pour une qualité pro max
            Graphics2D g2 = (Graphics2D) g.create();
 
            int w = getWidth();
            int h = getHeight();
            g2.translate(w / 2.0 + floatOffsetX, h / 2.0 + floatOffsetY); // changement origine du bouton au centre de la carte
            g2.scale(scale, scale); // resize depuis la nouvelle origine
            g2.translate(-w / 2.0, -h / 2.0); // replacage de l'origine en haut a gauche pour dessiner la carte 
            g2.drawImage(cardImage, 0, 0, w, h, this);
            
            g2.dispose();
        }
    }
}
