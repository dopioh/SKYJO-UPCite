package fr.upc.cr1a.controller;

import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameModeSelectionPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * controller pour la sélection du mode de jeu
 *
 */
public class GameModeSelectionController {

    private final Frame frame;
    private final GameModeSelectionPanel view;

    public GameModeSelectionController(Frame frame, GameModeSelectionPanel view) {
        this.frame = frame;
        this.view = view;

        //listeners
        ajouterListeners(view.getLocalButton(), () -> clicLocal());
        ajouterListeners(view.getMultiButton(), () -> clicMulti());
        ajouterListeners(view.getBackButton(), () -> clicRetour());
    }

    // ajoute hover + clic sur les DoodleButtons
    private void ajouterListeners(DoodleButton btn, Runnable action) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.onMouseEnter(); }
            public void mouseExited(MouseEvent e) { btn.onMouseExit(); }
            public void mousePressed(MouseEvent e) { action.run(); }
        });
    }

    private void clicLocal() {
        //redirige vers la configuration de joueurs en local
        frame.showPanel("PLAYERS");
    }

    private void clicMulti() {
        //redirige vers le navigateur de salons P2P
        frame.showPanel("LOBBY_BROWSER");
    }

    private void clicRetour() {
        // Retour au menu principal
        frame.showPanel("MENU");
    }
}
