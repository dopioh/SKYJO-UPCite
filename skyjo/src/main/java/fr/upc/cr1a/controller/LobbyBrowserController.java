package fr.upc.cr1a.controller;

import fr.upc.cr1a.controller.network.Client;
import fr.upc.cr1a.controller.network.Host;
import fr.upc.cr1a.model.LobbyModel;
import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.view.CreateLobbyDialog;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.LobbyBrowserPanel;
import fr.upc.cr1a.view.LobbyItemPanel;

import javax.swing.JOptionPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *controller pour le LobbyBrowserPanel
 *
 */
public class LobbyBrowserController {

    private final Frame frame;
    private final LobbyBrowserPanel view;
    private List<LobbyModel> availableLobbies;

    private Client discoveryClient;
    private boolean isLoading = false;

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
            public void mousePressed(MouseEvent e) { action.run(); }
        });
    }

    private void clicRetour() {
        cleanupDiscovery();
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

            createLobby(lobbyName, maxPlayers, actionMode);
        }
    }

    private void clicRafraichir() {
        loadLobbies();
        System.out.println("Refresh");
    }

    /**
     * Crée un salon en tant qu'hôte via le système P2P
     */
    private void createLobby(String name, int maxPlayers, boolean actionMode) {
        cleanupDiscovery();

        // Créer le Host qui démarre le serveur TCP, connecte le host en tant que client, et lance le broadcast UDP
        Host host = new Host(maxPlayers, actionMode, name, frame);

        String lobbyId = "lobby_" + System.currentTimeMillis();

        // Passer le Host au LobbyRoomController
        LobbyRoomController lobbyRoom = frame.getLobbyRoomController();
        lobbyRoom.setNetworkHost(host);
        lobbyRoom.setNetworkClient(null);
        lobbyRoom.initializeLobby(lobbyId, name, true, maxPlayers, actionMode);

        frame.showPanel("LOBBY_ROOM");
    }

    /**
     * Recherche les salons disponibles sur le réseau local via UDP
     */
    public void loadLobbies() {
        PlayerData account = PlayerAccount.actuelAccount;
        if (account == null) {
            availableLobbies = new ArrayList<>();
            view.showNoLobbiesMessage();
            return;
        }

        if (isLoading) {
            return; //Pour éviter l'erreur de double recherche
        }

        cleanupDiscovery();
        isLoading = true;
        view.getRefreshButton().setEnabled(false);
        view.getRefreshButton().setText("Recherche...");

        // Vide la liste affichee
        availableLobbies = new ArrayList<>();
        view.showNoLobbiesMessage();

        final Client localClient = new Client(frame, account);
        discoveryClient = localClient;

        localClient.discoverLobbies(2000, () -> {
            try {
                if (discoveryClient != localClient) {
                    return;
                }
                Map<String, String> found = localClient.getLobbyAvailable();
                availableLobbies = new ArrayList<>();

                for (Map.Entry<String, String> entry : found.entrySet()) {
                    String ip = entry.getKey();
                    String info = entry.getValue(); // "lobbyName:currentPlayers:maxPlayers:modeAction"
                    String[] parts = info.split(":");

                    if (parts.length >= 4) {
                        String lobbyName = parts[0];
                        int currentPlayers;
                        int maxPlayers;
                        try {
                            currentPlayers = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            currentPlayers = 0;
                        }
                        try {
                            maxPlayers = Integer.parseInt(parts[2]);
                        } catch (NumberFormatException e) {
                            maxPlayers = 8;
                        }
                        boolean actionMode = Boolean.parseBoolean(parts[3]);

                        // On utilise l'IP comme lobbyId (nécessaire pour se connecter)
                        LobbyModel lobby = new LobbyModel(ip, lobbyName, "Hôte", currentPlayers, maxPlayers, actionMode);
                        availableLobbies.add(lobby);
                    }
                }
                refreshView();
            } catch (Exception ex) {
                System.err.println("[LOBBY] erreur callback discoverLobbies: " + ex.getMessage());
            } finally {
                // Toujours debloquer le bouton, meme en cas d'exception ou d'annulation.
                isLoading = false;
                view.getRefreshButton().setEnabled(true);
                view.getRefreshButton().setText("Rafraîchir");
            }
        });
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
     * Rejoint un salon via TCP en utilisant l'IP stockée dans lobbyId
     */
    private void joinLobby(LobbyModel lobby) {
        String ip = lobby.getLobbyId(); // l'IP du host

        if (discoveryClient == null) {
            JOptionPane.showMessageDialog(frame, "Erreur : client non initialisé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean connected = discoveryClient.connect(ip);
        if (!connected) {
            JOptionPane.showMessageDialog(frame, "Impossible de se connecter au salon.", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Passer le Client au LobbyRoomController
        LobbyRoomController lobbyRoom = frame.getLobbyRoomController();
        lobbyRoom.setNetworkHost(null);
        lobbyRoom.setNetworkClient(discoveryClient);
        lobbyRoom.initializeLobby(
            lobby.getLobbyId(),
            lobby.getLobbyName(),
            false,
            lobby.getMaxPlayers(),
            lobby.isActionMode()
        );

        // Le discoveryClient est géré par le LobbyRoomController
        discoveryClient = null;

        frame.showPanel("LOBBY_ROOM");
    }

    /**
     * Nettoie le client de découverte s'il existe + reset l'etat de chargement.
     * Indispensable pour eviter que le bouton "Rafraichir" reste bloque "Recherche..."
     * apres une interruption (navigation, deconnexion, exception dans la callback).
     */
    private void cleanupDiscovery() {
        if (discoveryClient != null) {
            try {
                discoveryClient.disconnect();
            } catch (Exception ignored) {}
            discoveryClient = null;
        }
        // toujours reset le flag : meme si la callback de discoverLobbies est encore en attente,
        // elle verifiera que discoveryClient != localClient et retournera sans toucher au flag.
        if (isLoading) {
            isLoading = false;
            if (view.getRefreshButton() != null) {
                view.getRefreshButton().setEnabled(true);
                view.getRefreshButton().setText("Rafraîchir");
            }
        }
    }
}
