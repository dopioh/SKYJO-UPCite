package fr.upc.cr1a.view;

import javax.swing.*;
import java.awt.*;

/**
 * Overlay des paramètres
 * 
 * @author Hugo
 */
public class SettingsOverlay extends JComponent {
    public SettingsOverlay() {
        setOpaque(false);
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        //Fond semi-transparent
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
