package fr.upc.cr1a.view;

import fr.upc.cr1a.model.AnimLoop;
import fr.upc.cr1a.model.DoodleButtonModel;
import fr.upc.cr1a.model.DoodleButtonModel.ButtonColor;

import javax.swing.*;
import fr.upc.cr1a.utils.ImageCache;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * bouton stylé doodle avec scribble au hover
 * 
 * @author Ian
 */
public class DoodleButton extends JComponent {

    private DoodleButtonModel model;
    private Font font;
    private BufferedImage scribbleImg;
    private BufferedImage icon;
    private BufferedImage bgImage;
    private float scribbleProgress;
    private float hoverScale = 1f;
    private boolean animRunning = false;

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
    }

    // charge police, scribble, icone
    private void loadResources() {
        // police
        try {
            var stream = getClass().getResourceAsStream("/PatrickHandSC.ttf");
            if (stream != null) {
                font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.BOLD, 48f);
            } else {
                font = new Font("Arial", Font.BOLD, 48);
            }
        } catch (Exception e) {
            font = new Font("Arial", Font.BOLD, 48);
        }

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
        int seed = model.getText().hashCode(); // pour que chaque bouton = wiggle different

        // fond : image ou wiggle
        if (bgImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(bgImage, x, y, w, h, null);
        } else {
            // ombre
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fill(wiggleRect(x, y + 4, w, h, 35, seed));
            // fond en wiggle
            Path2D forme = wiggleRect(x, y, w, h, 35, seed);
            g2.setColor(model.getColor());
            g2.fill(forme);
        }

        // texte
        g2.setFont(font);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String txt = model.getText();
        int txtW = fm.stringWidth(txt);
        int taille = fm.getAscent();
        int iconSize = (int)(taille * 1.3);

        // position du texte (decaler icone not null)
        int largeurTotale = (icon != null) ? iconSize + 10 + txtW : txtW;
        int debutX = (getWidth() - largeurTotale) / 2;
        int txtX = debutX;
        int txtY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 5;
        int textCenterY = txtY - (fm.getAscent() - fm.getDescent()) / 2;

        // rotation text
        AffineTransform avant = g2.getTransform();
        g2.rotate(Math.toRadians(-2), getWidth() / 2.0, getHeight() / 2.0);

        // icone si y en a une
        if (icon != null) {
            int iconeX = debutX + txtW + 10;
            int iconeY = textCenterY - iconSize / 2 + (bgImage == null ? 4 : 0);
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

    // setters
    public void setCouleur(ButtonColor c) { model.setColor(c); repaint(); }
    public void setText(String t) { model.setText(t); repaint(); }
    public void setImage(String path) { model.setImagePath(path); loadIcon(path); repaint(); }
    public void setBgImage(String path) {
        bgImage = ImageCache.get(path);
        repaint();
    }
    
    /**
     * créateur de un rectangle arrondi avec wiggle
     * @param x position gauche du rectangle
     * @param y position haut du rectangle
     * @param w largeur du rectangle
     * @param h hauteur du rectangle
     * @param coin rayon des coins arrondis
     * @param seed nombre pour générer aléatoirelent comme minecrafttt
     */
    private Path2D wiggleRect(int x, int y, int w, int h, int coin, int seed) {
        Random rand = new Random(seed);
        Path2D p = new Path2D.Double();
        int wig = 6; // taux de wiggle <-------------------

        p.moveTo(x + coin, y + rand.nextInt(wig));
        p.lineTo(x + w - coin, y + rand.nextInt(wig));

        p.quadTo(x + w, y, x + w + rand.nextInt(wig), y + coin);
        p.lineTo(x + w + rand.nextInt(wig), y + h - coin);

        p.quadTo(x + w, y + h, x + w - coin, y + h + rand.nextInt(wig));
        p.lineTo(x + coin, y + h + rand.nextInt(wig));

        p.quadTo(x, y + h, x - rand.nextInt(wig), y + h - coin);
        p.lineTo(x - rand.nextInt(wig), y + coin);

        p.quadTo(x, y, x + coin, y + rand.nextInt(wig));
        p.closePath();

        return p;
    }
}
