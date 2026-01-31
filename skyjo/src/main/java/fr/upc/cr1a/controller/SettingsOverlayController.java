package fr.upc.cr1a.controller;

import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.SettingsOverlay;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;

/**
 * Controller de l'overlay des paramètres
 * 
 * @author Hugo
 */
public class SettingsOverlayController {
    private final Frame frame;
    public SettingsOverlayController(SettingsOverlay overlay, Frame frame) {
        this.frame = frame;
        //Les clics n'étaient pas bloqués sur la couche principale
        MouseInputAdapter block = new MouseInputAdapter() {     
            @Override public void mousePressed(MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(MouseEvent e) { e.consume(); }
            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
            @Override public void mouseMoved(MouseEvent e) { e.consume(); }
            @Override public void mouseDragged(MouseEvent e) { e.consume(); }
        };

        overlay.addMouseListener(block);
        overlay.addMouseMotionListener(block);
        overlay.addMouseWheelListener(e -> e.consume());

        //Bouton Fermer
        overlay.getBackButton().addActionListener(e -> {
            frame.hideSettingsOverlay();
        });

        //Actionne l'actionListener lié au cointroller pour la bande

        overlay.getTabJeuPanel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                overlay.getTabJeu().doClick(); 
                e.consume();
            }
        });

        overlay.getTabAudioPanel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                overlay.getTabAudio().doClick();
                e.consume();
            }
        });

        overlay.getTabAutresPanel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                overlay.getTabAutres().doClick();
                e.consume();
            }
        });

        var cl = overlay.getCardLayout();
        var content = overlay.getContentPanel();
        overlay.getTabJeu().addActionListener(e -> {
            cl.show(content, "JEU");
            overlay.setSelectedTabPanel(overlay.getTabJeuPanel());
        });

        overlay.getTabAudio().addActionListener(e -> {
            cl.show(content, "AUDIO");
            overlay.setSelectedTabPanel(overlay.getTabAudioPanel());
        });

        overlay.getTabAutres().addActionListener(e -> {
            cl.show(content, "AUTRES");
            overlay.setSelectedTabPanel(overlay.getTabAutresPanel());
        });
        
        cl.show(content, "JEU");
        overlay.setSelectedTabPanel(overlay.getTabJeuPanel());
    }
}