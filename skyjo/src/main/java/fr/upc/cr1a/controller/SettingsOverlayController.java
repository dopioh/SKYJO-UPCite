package fr.upc.cr1a.controller;

import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.SettingsOverlay;
import fr.upc.cr1a.model.AudioManager;

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
    private final AudioManager audioManager;

    public SettingsOverlayController(SettingsOverlay overlay, Frame frame, AudioManager audioManager) {
        this.frame = frame;
        this.audioManager = audioManager;
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
            @Override public void mousePressed(MouseEvent e) {
                overlay.getTabJeu().doClick();
                e.consume();
            }
        });

        overlay.getTabAudioPanel().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                overlay.getTabAudio().doClick();
                e.consume();
            }
        });

        overlay.getTabAutresPanel().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                overlay.getTabAutres().doClick();
                e.consume();
            }
        });

        //Changer la page selon la catégorie
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
        
        //Changer le label du bot 
        //A modifier plus tard 
        overlay.getBotDifficultyButton().addActionListener(e -> { 
            String current = overlay.getBotDifficultyButton().getText();
            switch (current) {
                case "Facile" -> overlay.getBotDifficultyButton().setText("Moyen");
                case "Moyen"  -> overlay.getBotDifficultyButton().setText("Goat");
                default       -> overlay.getBotDifficultyButton().setText("Facile");
            }
        });

        //Activer/OFF le plein ecran
        overlay.getFullscreenButton().addActionListener(e -> {
            frame.toggleFullscreen();
            overlay.getFullscreenButton().setText(frame.isFullscreen() ? "ON" : "OFF");
        });

        //ON OFF Son
        overlay.getSoundButton().addActionListener(e -> {
            boolean newEnabled = !audioManager.isEnabled();
            audioManager.setEnabled(newEnabled);
            overlay.getSoundButton().setText(newEnabled ? "ON" : "OFF");
        });

        // Fleche gauche
        overlay.getVolumeLeftButton().addActionListener(e -> {
            int step = Math.max(1, audioManager.getVolume() / 10);
            step = Math.max(1, step - 1);
            audioManager.setVolume(step * 10);
            overlay.getVolumeValueButton().setText(String.valueOf(step));
        });

        // Fleche droite
        overlay.getVolumeRightButton().addActionListener(e -> {
            int step = Math.max(1, audioManager.getVolume() / 10);
            step = Math.min(10, step + 1);
            audioManager.setVolume(step * 10);
            overlay.getVolumeValueButton().setText(String.valueOf(step));
        }); 

        overlay.getSoundButton().setText(audioManager.isEnabled() ? "ON" : "OFF");
        overlay.getVolumeValueButton().setText(String.valueOf(Math.max(1, audioManager.getVolume() / 10)));
    }
}