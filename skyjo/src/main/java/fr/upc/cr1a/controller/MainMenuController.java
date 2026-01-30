package fr.upc.cr1a.controller;

import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.MainMenuPanel;

public class MainMenuController {

    private final Frame frame;
    private final MainMenuPanel view;

    public MainMenuController(Frame frame, MainMenuPanel view) {
        this.frame = frame;
        this.view = view;

        //Bouton paramètres
        view.getSettingsButton().addActionListener(e -> {
            
        });

        
        // view.getPlayButton().addActionListener(e -> {
            
        // });

        // view.getShopButton().addActionListener(e -> {
        //     
        // });
    }
}
