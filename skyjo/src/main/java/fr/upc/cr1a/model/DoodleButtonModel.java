package fr.upc.cr1a.model;

import java.awt.Color;

/**
 * modele pour le bouton style doodle
 * 
 * @author Ian
 */
public class DoodleButtonModel {

    // couleurs prédéfinie
    public enum ButtonColor {
        YELLOW(new Color(241, 192, 31)),
        GREEN(new Color(103, 187, 93)),
        RED(new Color(238, 122, 67));

        private final Color color;
        ButtonColor(Color c) { this.color = c; }
        public Color getColor() { return color; }
    }

    private String text;
    private ButtonColor color;
    private int width, height;
    private boolean hovered;
    private String imagePath; // chemin icone, peut etre null

    public DoodleButtonModel(String text, ButtonColor couleur, int w, int h) {
        this(text, couleur, w, h, null);
    }

    public DoodleButtonModel(String text, ButtonColor couleur, int w, int h, String imagePath) {
        this.text = text;
        this.color = couleur;
        this.width = w;
        this.height = h;
        this.imagePath = imagePath;
        this.hovered = false;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Color getColor() { return color.getColor(); }
    public ButtonColor getButtonColor() { return color; }
    public void setColor(ButtonColor c) { this.color = c; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isHovered() { return hovered; }
    public void setHovered(boolean h) { this.hovered = h; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String p) { this.imagePath = p; }
}
