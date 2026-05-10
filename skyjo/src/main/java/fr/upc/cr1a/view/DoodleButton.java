package fr.upc.cr1a.view;

import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.model.DoodleButtonModel;
import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import fr.upc.cr1a.utils.ImageCache;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * bouton stylé doodle avec scribble au hover
 *
 * // usage 
 * DoodleButton btn = new DoodleButton("Jouer", ButtonColor.YELLOW, 300, 100, "/Button/Play.png");
 *
 * // icône collée à droite ou à gauche (optionnel)
 * btn.setIconSide(IconSide.RIGHT);
 *
 * // dans le controller
 * ajouterListeners(btn, () -> monAction());
 *
 * @author Ian
 */
public class DoodleButton extends JComponent {

    public enum IconSide { LEFT, RIGHT }

    private static AudioManager audioManager;
    public static void setAudioManager(AudioManager am) { audioManager = am; }

    private String sfxPath = "/sfx/button_click.wav";
    public void setSfxPath(String path) { this.sfxPath = path; }

    private DoodleButtonModel model;
    private Font font;
    private BufferedImage scribbleImg;
    private BufferedImage icon;
    private BufferedImage bgImage;
    private float scribbleProgress;
    private float hoverScale = 1f;
    private boolean animRunning = false;
    private IconSide iconSide = null; // null = groupé avec le texte (truc par défaut)
    private boolean active = true;
    private float fontSize = 48f;

    // juste texte
    public DoodleButton(String text) {
        this(text, ButtonColor.YELLOW, 300, 100, null);
    }

    // texte + couleur
    public DoodleButton(String text, ButtonColor color) {
        this(text, color, 300, 100, null);
    }

    // texte + couleur + taille
    public DoodleButton(String text, ButtonColor color, int w, int h) {
        this(text, color, w, h, null);
    }

    /**
     * vrai constructeur avec tout
     * @param text texte du bouton
     * @param color couleur prédéfinie (ButtonColor.YELLOW, GREEN, RED)
     * @param w largeur du bouton
     * @param h hauteur du bouton
     * @param imagePath chemin de l'image ou null
     */
    public DoodleButton(String text, ButtonColor color, int w, int h, String imagePath) {
        this.model = new DoodleButtonModel(text, color, w, h, imagePath);
        this.scribbleProgress = 0f;
        setPreferredSize(new Dimension(w + 20, h + 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loadResources();
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (audioManager != null && sfxPath != null && !sfxPath.isBlank()) audioManager.playSfx(sfxPath);
            }
        });
    }

    // charge police, scribble, icone
    private void loadResources() {
        // police
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            if (stream != null) {
                font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.BOLD, fontSize);
            } else {
                font = new Font("Arial", Font.BOLD, (int) fontSize);
            }
        } catch (Exception e) {
            font = new Font("Arial", Font.BOLD, (int) fontSize);
        }

        // fond selon couleur
        String bgPath = "/Button/button_" + model.getButtonColor().name().toLowerCase() + ".png";
        bgImage = ImageCache.get(bgPath);

        // scribble selon couleur
        String scribblePath = "/Button/scribble_" + model.getButtonColor().name().toLowerCase() + ".png";
        scribbleImg = ImageCache.get(scribblePath);

        // icone a coté si existe
        loadIcon(model.getImagePath());
    }

    private void loadIcon(String path) {
        icon = ImageCache.get(path);
    }

    private void startAnimationTimer() {
        if (animRunning) return;
        animRunning = true;
        AnimLoop.add(this, dt -> {
            // scale vers la cible (1.04 si hover, 1.0 sinon)
            float target = model.isHovered() ? 1.04f : 1f;
            hoverScale += (target - hoverScale) * 0.3f * (float) dt;

            // scribble progresse pendant le hover
            if (model.isHovered() && scribbleProgress < 1f) {
                scribbleProgress = Math.min(scribbleProgress + 0.15f * (float) dt, 1f);
            }

            // Arrête l'animation quand tout est revenu à l'état normal
            if (!model.isHovered() && scribbleProgress == 0f && Math.abs(hoverScale - 1.0f) < 0.001f) {
                animRunning = false;
                AnimLoop.remove(this);
            }

            repaint();
        });
    }

    private void startScribbleAnim() {
        scribbleProgress = 0f;
        startAnimationTimer();
    }

    private void stopAnim() {
        scribbleProgress = 0f;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // moins pixéliee

        // scale hover centré
        if (hoverScale != 1f) {
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
            g2.translate(cx, cy);
            g2.scale(hoverScale, hoverScale);
            g2.translate(-cx, -cy);
        }

        int w = getWidth() - 20;
        int h = getHeight() - 20;
        int x = 10, y = 10;

        // fond image
        if (bgImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(bgImage, x, y, w, h, null);
        }

        // texte
        g2.setFont(font);
        g2.setColor(active ? Color.WHITE : new Color(255, 255, 255, 100));
        FontMetrics fm = g2.getFontMetrics();
        String txt = model.getText();
        int txtW = fm.stringWidth(txt);
        int taille = fm.getAscent();
        int iconSize = (int)(taille * 1.3);

        int txtY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 5;
        int textCenterY = txtY - (fm.getAscent() - fm.getDescent()) / 2;

        // position du texte et de l'icone
        int txtX;
        if (iconSide == IconSide.LEFT && icon != null) {
            // texte centré
            int iconEnd = x + 5 + iconSize + 5;
            txtX = Math.max((getWidth() - txtW) / 2, iconEnd);
        } else if (iconSide == IconSide.RIGHT && icon != null) {
            // icone collée à droite
            txtX = (getWidth() - txtW) / 2;
        } else if (icon != null) {
            // text icone centré ensemble
            int largeurTotale = txtW + 10 + iconSize;
            txtX = (getWidth() - largeurTotale) / 2;
        } else {
            txtX = (getWidth() - txtW) / 2;
        }

        // rotation text
        AffineTransform avant = g2.getTransform();
        g2.rotate(Math.toRadians(-2), getWidth() / 2.0, getHeight() / 2.0);

        // icone si y en a une
        if (icon != null) {
            int iconeX;
            if (iconSide == IconSide.RIGHT) {
                iconeX = x + w - iconSize - 10;
            } else if (iconSide == IconSide.LEFT) {
                iconeX = x + 5;
            } else {
                iconeX = txtX + txtW + 10;
            }
            int iconeY = textCenterY - iconSize / 2;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(icon, iconeX, iconeY, iconSize, iconSize, null);
        }

        g2.drawString(txt, txtX, txtY);
        g2.setTransform(avant);

        // scribble au hover
        if (model.isHovered() && scribbleImg != null && scribbleProgress > 0) {
            float eased = 1f - (float) Math.pow(1 - scribbleProgress, 3); // ease-out
            int scribbleW = txtW;
            int scribbleH = scribbleImg.getHeight() * scribbleW / scribbleImg.getWidth();
            Shape oldClip = g2.getClip();
            g2.clipRect(txtX, txtY + 5, (int)(scribbleW * eased), scribbleH);
            g2.drawImage(scribbleImg, txtX, txtY + 5, scribbleW, scribbleH, null);
            g2.setClip(oldClip);
        }
    }

    // appelé par le controller
    public void onMouseEnter() {
        model.setHovered(true);
        startScribbleAnim();
    }

    public void onMouseExit() {
        model.setHovered(false);
        stopAnim();
    }

    // simule un clic (pour les tests)
    public void doClick() {
        MouseEvent e = new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 0, 0, 1, false);
        for (MouseListener l : getMouseListeners()) {
            l.mousePressed(e);
        }
    }

    // setters
    public void setActive(boolean active) { this.active = active; repaint(); }
    public void setCouleur(ButtonColor c) { model.setColor(c); repaint(); }
    public void setText(String t) { model.setText(t); repaint(); }
    public void setImage(String path) { model.setImagePath(path); loadIcon(path); repaint(); }
    public void setIconSide(IconSide side) { this.iconSide = side; repaint(); }
    public void setFontSize(float size) { this.fontSize = size; loadResources(); repaint(); }

    // getter 
    public String getText() { return model.getText(); }
    public DoodleButtonModel getDoodleButtonModel(){return this.model;}
}
