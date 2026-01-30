package fr.upc.cr1a.controller;

import fr.upc.cr1a.view.SettingsOverlay;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Controller de l'overlay des paramètres
 * 
 * @author Hugo
 */
public class SettingsOverlayController {
    public SettingsOverlayController(SettingsOverlay overlay) {
        //Les clics n'étaient pas bloqués sur la couche principale
        //Temporaire
        MouseAdapter block = new MouseAdapter() {       
            @Override public void mousePressed(MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(MouseEvent e) { e.consume(); }
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
            @Override public void mouseMoved(MouseEvent e) { e.consume(); }
            @Override public void mouseDragged(MouseEvent e) { e.consume(); }
        };

        overlay.addMouseListener(block);
        overlay.addMouseMotionListener(block);
        overlay.addMouseWheelListener(e -> e.consume());
    }
}
