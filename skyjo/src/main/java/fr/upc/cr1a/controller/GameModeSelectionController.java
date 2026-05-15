package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameModeSelectionPanel;

import javax.swing.JOptionPane;
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
        ajouterListeners(view.getTutorialButton(), () -> clicTutorial());
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
        if (PlayerAccount.actuelAccount == null) {
            JOptionPane.showMessageDialog(frame,"Vous devez être connecté avec un compte pour jouer en multijoueur.","Connexion requise",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (frame.getLobbyBrowserController() != null) {
            frame.getLobbyBrowserController().loadLobbies();
        }
        frame.showPanel("LOBBY_BROWSER");
    }

    private void clicRetour() {
        // Retour au menu principal
        frame.showPanel("MENU");
    }

    private void clicTutorial() {
        // Affiche le tutoriel
        frame.getTutorialController().showTutorial();
    }
}
