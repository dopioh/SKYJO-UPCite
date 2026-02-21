package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.AudioManager;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.PlayerData;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.MainMenuPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;

/**
 * Controller du menu principal avec les bouttons existants (jouer, boutique, paramètres)
 * 
 * @author Hugo
 * @author Ian
 */
public class MainMenuController {

    private final Frame frame;
    private final MainMenuPanel view;
    private final AudioManager audio;
    private final Scanner scanner = new Scanner(System.in);

    public MainMenuController(Frame frame, MainMenuPanel view) {
        this.frame = frame;
        this.view = view;
        this.audio = frame.getAudioManager();

        // listeners
        ajouterListeners(view.getShopButton(), () -> clicShop());
        ajouterListeners(view.getPlayButton(), () -> clicPlay());
        ajouterListeners(view.getSettingsButton(), () -> clicSettings());
    }

    // ajoute hover + clic
    private void ajouterListeners(DoodleButton btn, Runnable action) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.onMouseEnter(); }
            public void mouseExited(MouseEvent e) { btn.onMouseExit(); }
            public void mouseClicked(MouseEvent e) { action.run(); }
        });
    }

    private void clicShop() {
        // ici mettreeeee
    }

    private void clicPlay() {
        //audio.playBgm("/music/calmbgm.wav");
        frame.showPanel("PLAYERS");
    }

    private void clicSettings() {
        frame.showSettingsOverlay();
    }
}
