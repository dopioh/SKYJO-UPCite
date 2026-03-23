package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.LobbyModel;
import fr.upc.cr1a.view.CreateLobbyDialog;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.LobbyBrowserPanel;
import fr.upc.cr1a.view.LobbyItemPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *controller pour le LobbyBrowserPanel
 *
 */
public class LobbyBrowserController {

    private final Frame frame;
    private final LobbyBrowserPanel view;
    private List<LobbyModel> availableLobbies;

    public LobbyBrowserController(Frame frame, LobbyBrowserPanel view) {
        this.frame = frame;
        this.view = view;
        this.availableLobbies = new ArrayList<>();

        //listeners
        ajouterListeners(view.getBackButton(), () -> clicRetour());
        ajouterListeners(view.getCreateLobbyButton(), () -> clicCreerSalon());
        ajouterListeners(view.getRefreshButton(), () -> clicRafraichir());

        //charge les salons au démarrage
        loadLobbies();
    }

    private void ajouterListeners(DoodleButton btn, Runnable action) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.onMouseEnter(); }
            public void mouseExited(MouseEvent e) { btn.onMouseExit(); }
            public void mouseClicked(MouseEvent e) { action.run(); }
        });
    }

    private void clicRetour() {
        frame.showPanel("GAME_MODE");
    }

    private void clicCreerSalon() {
        CreateLobbyDialog dialog = new CreateLobbyDialog(frame);

        //listeners pour le dialog
        ajouterListeners(dialog.getCancelButton(), () -> {
            dialog.setConfirmed(false);
            dialog.dispose();
        });

        ajouterListeners(dialog.getCreateButton(), () -> {
            if (dialog.validateInput()) {
                dialog.setConfirmed(true);
                dialog.dispose();
            }
        });

        dialog.setVisible(true);

        //si l'utilisateur a confirmé la création
        if (dialog.isConfirmed()) {
            String lobbyName = dialog.getLobbyName();
            int maxPlayers = dialog.getMaxPlayers();
            boolean actionMode = dialog.isActionMode();

            //remplacer par l'appel au système P2P pour créer le salon avec les paramètres saisis (a faire)
            //pour l'instant, on simule la création
            createLobby(lobbyName, maxPlayers, actionMode);
        }
    }

    private void clicRafraichir() {
        loadLobbies();
    }

    /**
     *crée un nouveau salon (simulation pour l'instant)
     */
    private void createLobby(String name, int maxPlayers, boolean actionMode) {
        //remplacer par l'appel au système P2P pour créer le salon avec les paramètres saisis // a faire
        //pour l'instant, on simule simplement
        javax.swing.JOptionPane.showMessageDialog(
            frame,
            "Salon créé : " + name + "\n" +
            "Joueurs max : " + maxPlayers + "\n" +
            "Mode actions : " + (actionMode ? "Activé" : "Désactivé") + "\n\n" +
            "En attente de l'intégration P2P...",
            "Salon créé",
            javax.swing.JOptionPane.INFORMATION_MESSAGE
        );

        // Simule l'ajout du salon à la liste
        String hostName = "Vous"; //plus tard récuperer le vrai nom du joeur
        LobbyModel newLobby = new LobbyModel(
            "lobby_" + System.currentTimeMillis(),
            name,
            hostName,
            1, //le host est déjà dans le salon
            maxPlayers,
            actionMode
        );
        availableLobbies.add(0, newLobby); //ajoute en premier
        refreshView();
    }

    /**
     *charge les salons disponibles du réseau
     */
    private void loadLobbies() {
        //remplacer par l'appel au système P2P pour récupérer les salons( a faire)
        // Pour l'instant, on génère des salons de test
        availableLobbies = generateTestLobbies();
        refreshView();
    }

    /**
     * rafraîchit l'affichage de la liste des salons
     */
    private void refreshView() {
        if (availableLobbies.isEmpty()) {
            view.showNoLobbiesMessage();
        } else {
            view.refreshLobbies(availableLobbies);

            // Ajoute les listeners sur les boutons "Rejoindre"
            for (LobbyItemPanel lobbyItem : view.getLobbyItems()) {
                ajouterListeners(lobbyItem.getJoinButton(), () -> {
                    if (lobbyItem.getLobby().canJoin()) {
                        joinLobby(lobbyItem.getLobby());
                    }
                });
            }
        }
    }

    /**
     * rejoint un salon
     */
    private void joinLobby(LobbyModel lobby) {
        // remplacer par l'appel au système P2P pour rejoindre le salon
        javax.swing.JOptionPane.showMessageDialog(
            frame,
            "Vous rejoignez le salon : " + lobby.getLobbyName() + "\n" +
            "Host : " + lobby.getHostName() + "\n" +
            "Joueurs : " + lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers() + "\n\n" +
            "En attente de l'intégration P2P...",
            "Rejoindre un salon",
            javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     *salons de test (à supprimer quand le P2P sera implémenté)
     */
    private List<LobbyModel> generateTestLobbies() {
        List<LobbyModel> lobbies = new ArrayList<>();

        lobbies.add(new LobbyModel(
            "lobby_1",
            "Partie rapide",
            "Alice",
            3,
            4,
            false
        ));

        lobbies.add(new LobbyModel(
            "lobby_2",
            "Débutants bienvenus",
            "Charlie",
            1,
            3,
            false
        ));
        return lobbies;
    }
}
