package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.MainMenuPanel;

/**
 * Controller du menu principal avec les bouttons existants (jouer, boutique, paramètres)
 * 
 * @author Hugo
 */
public class MainMenuController {

    private final Frame frame;
    private final MainMenuPanel view;
    AudioManager audio = new AudioManager();

    public MainMenuController(Frame frame, MainMenuPanel view) {
        this.frame = frame;
        this.view = view;

        //Bouton paramètres
        view.getSettingsButton().addActionListener(e -> {
            frame.showSettingsOverlay();
            frame.toggleFullscreen();
        });

        //Bouton jouer
        view.getPlayButton().addActionListener(e -> {
            audio.playBgm("/music/calmbgm.wav");
        // audio.playSfx("/sfx/UIclick.wav");
        });

        // view.getShopButton().addActionListener(e -> {
        //     
        // });
    }
}
