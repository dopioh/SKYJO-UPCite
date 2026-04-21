package fr.upc.cr1a.controller;

import fr.upc.cr1a.controller.network.Client;
import fr.upc.cr1a.controller.network.Host;
import fr.upc.cr1a.model.LobbyPlayerModel;
import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.view.DoodleButton;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.LobbyRoomPanel;
import fr.upc.cr1a.view.PlayerLobbySlot;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour la salle d'attente
 *
 */
public class LobbyRoomController {

    private final Frame frame;
    private final LobbyRoomPanel view;
    private List<LobbyPlayerModel> players;
    private String lobbyId;
    private String lobbyName;
    private boolean isHost;
    private boolean isReady;

    // Références réseau
    private Host host;
    private Client client;

    public LobbyRoomController(Frame frame, LobbyRoomPanel view) {
        this.frame = frame;
        this.view = view;
        this.players = new ArrayList<>();
        this.isReady = false;

        //listeners de base
        ajouterListeners(view.getBackButton(), () -> clicQuitter());
        ajouterListeners(view.getReadyButton(), () -> clicReady());
        ajouterListeners(view.getStartButton(), () -> clicStart());

        //listeners pour les paramètre d'un salon (hôte uniquement)
        view.getMaxPlayersSpinner().addChangeListener(e -> onMaxPlayersChanged());
        view.getActionModeCheckbox().addActionListener(e -> onActionModeChanged());
        view.getAddBotButton().addActionListener(e -> clicAjouterBot());
    }

    /**
     * Définit le Host réseau (quand le joueur crée un salon)
     */
    public void setNetworkHost(Host host) {
        this.host = host;
    }

    /**
     * Définit le Client réseau (quand le joueur rejoint un salon)
     */
    public void setNetworkClient(Client client) {
        this.client = client;
    }

    /**
     * Initialise le salon avec les paramètres donnés
     */
    public void initializeLobby(String lobbyId, String lobbyName, boolean isHost, int maxPlayers, boolean actionMode) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
        this.isHost = isHost;
        this.isReady = false;

        //nettoyer les joueurs existants
        players.clear();
        view.clearPlayers();

        //réinitialiser le bouton Ready
        view.getReadyButton().setText("Prêt");
        view.getReadyButton().setCouleur(fr.upc.cr1a.model.DoodleButtonModel.ButtonColor.GREEN);

        view.setLobbyTitle(lobbyName);
        view.setIsHost(isHost);
        view.getMaxPlayersSpinner().setValue(maxPlayers);
        view.getActionModeCheckbox().setSelected(actionMode);

        //ajouter le joueur local avec les vraies infos du compte
        PlayerData localPlayer = PlayerAccount.actuelAccount;
        String localPlayerId = localPlayer != null ? localPlayer.getID() : "player_local";
        String localPlayerName = localPlayer != null ? localPlayer.getUsername() : "Vous";
        addPlayer(new LobbyPlayerModel(localPlayerId, localPlayerName, isHost, false));

        updateLobbyInfo();
    }

    /**
     * Appelé par GameClient quand un LOBBY_UPDATE est reçu du serveur.
     * Remplace la liste de joueurs par celle du réseau.
     */
    public void updatePlayersFromNetwork(ArrayList<PlayerData> networkPlayers) {
        if (networkPlayers == null) return;

        players.clear();
        view.clearPlayers();

        PlayerData localPlayer = PlayerAccount.actuelAccount;
        String localId = localPlayer != null ? localPlayer.getID() : null;

        for (PlayerData pd : networkPlayers) {
            boolean playerIsHost = isHost && pd.getID() != null && pd.getID().equals(localId);
            LobbyPlayerModel model = new LobbyPlayerModel(
                pd.getID(), pd.getUsername(), playerIsHost, pd.isBot()
            );
            addPlayer(model);
        }

        updateLobbyInfo();
    }

    /**
     * ajoute un joueur au salon
     */
    public void addPlayer(LobbyPlayerModel player) {
        players.add(player);
        PlayerLobbySlot slot = view.addPlayer(player);

        //listerner kick pour l'hote
        if (isHost && !player.isHost() && slot.getKickButton() != null) {
            slot.getKickButton().addActionListener(e -> clicKick(player.getPlayerId()));
        }

        updateLobbyInfo();
    }

    /**
     * retire un joueur du salon
     */
    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getPlayerId().equals(playerId));
        view.removePlayer(playerId);
        updateLobbyInfo();
    }

    /**
     * met à jour le statut d'un joueur
     */
    public void updatePlayerStatus(String playerId, boolean ready) {
        for (LobbyPlayerModel player : players) {
            if (player.getPlayerId().equals(playerId)) {
                player.setReady(ready);
                break;
            }
        }
        view.refreshPlayerSlots();
        updateLobbyInfo();
    }

    private void ajouterListeners(DoodleButton btn, Runnable action) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.onMouseEnter(); }
            public void mouseExited(MouseEvent e) { btn.onMouseExit(); }
            public void mousePressed(MouseEvent e) { action.run(); }
        });
    }

    private void clicQuitter() {
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Pourquoi tu veux partir, revient?",
            "Quitter",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            cleanup();
            frame.showPanel("LOBBY_BROWSER");
        }
    }

    private void clicReady() {
        isReady = !isReady;

        if (isReady) {
            view.getReadyButton().setText("Annuler");
            view.getReadyButton().setCouleur(fr.upc.cr1a.model.DoodleButtonModel.ButtonColor.YELLOW);
        } else {
            view.getReadyButton().setText("Prêt");
            view.getReadyButton().setCouleur(fr.upc.cr1a.model.DoodleButtonModel.ButtonColor.GREEN);
        }

        PlayerData localPlayer = PlayerAccount.actuelAccount;
        String localId = localPlayer != null ? localPlayer.getID() : "player_local";
        updatePlayerStatus(localId, isReady);
    }

    private void clicStart() {
        if (!isHost) {
            JOptionPane.showMessageDialog(frame,"Seul l'hôte peut lancer la partie.","Action non autorisée",JOptionPane.WARNING_MESSAGE);
            return;
        }

        //vérifie que tous les joueurs sont prêts
        boolean allReady = true;
        for (LobbyPlayerModel player : players) {
            if (!player.isBot() && !player.isHost() && !player.isReady()) {
                allReady = false;
                break;
            }
        }

        if (!allReady) {
            JOptionPane.showMessageDialog(
                frame,
                "Tous les joueurs doivent être prêts avant de lancer la partie.",
                "Joueurs non prêts",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        //vérifier le nombre minimum de joueurs
        if (players.size() < 2) {
            JOptionPane.showMessageDialog(
                frame,
                "Il faut au moins 2 joueurs pour lancer la partie.",
                "va chercher des amis",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Lancer la partie via le réseau
        if (host != null) {
            host.startGame();
        }
    }

    private void clicKick(String playerId) {
        LobbyPlayerModel player = players.stream()
            .filter(p -> p.getPlayerId().equals(playerId))
            .findFirst()
            .orElse(null);

        if (player == null) {
            System.out.println("[LobbyRoomController] Joueur non trouvé: " + playerId);
            return;
        }

        String playerName = player.getPlayerName();
        String confirmMsg = player.isBot() ?
            "Etes-vous sûr de vouloir retirer le bot " + playerName + " ?" :
            "Etes-vous sûr de vouloir retirer " + playerName + " ?";

        int confirm = JOptionPane.showConfirmDialog(
            frame,
            confirmMsg,
            "Retirer un joueur",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("[LobbyRoomController] Retrait de: " + playerName);

            // Kick via le réseau si on est host
            if (isHost && host != null) {
                host.kickPlayer(playerId);
            }

            removePlayer(playerId);
        }
    }

    private void clicAjouterBot() {
        int maxPlayers = (Integer) view.getMaxPlayersSpinner().getValue();

        if (players.size() >= maxPlayers) {
            JOptionPane.showMessageDialog(
                frame,
                "Le salon est plein. Augmentez le nombre maximum de joueurs pour ajouter un bot.",
                "Salon plein",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        //chossir un bot
        String[] botStrategyNames = {"BotTerminal", "BotRow", "BotLow", "BotCounting"};

        String choice = (String) JOptionPane.showInputDialog(
            frame,
            "Choisissez le type de bot à ajouter :",
            "Ajouter un bot",
            JOptionPane.QUESTION_MESSAGE,
            null,
            botStrategyNames,
            botStrategyNames[0]
        );

        if (choice != null) {
            //trouver l'index du choix
            int choiceIndex = 0;
            for (int i = 0; i < botStrategyNames.length; i++) {
                if (botStrategyNames[i].equals(choice)) {
                    choiceIndex = i;
                    break;
                }
            }

            String botId = "bot_" + System.currentTimeMillis();
            String botName = botStrategyNames[choiceIndex];
            LobbyPlayerModel bot = new LobbyPlayerModel(botId, botName, false, true);
            addPlayer(bot);
        }
    }

    private void onMaxPlayersChanged() {
        int newMax = (Integer) view.getMaxPlayersSpinner().getValue();

        //vérifie qu'on n'est pas en dessous du nombre actuel de joueurs
        if (newMax < players.size()) {
            JOptionPane.showMessageDialog(
                frame,
                "Impossible de réduire en dessous du nombre actuel de joueurs (" + players.size() + ").",
                "Erreur",
                JOptionPane.WARNING_MESSAGE
            );
            view.getMaxPlayersSpinner().setValue(players.size());
            return;
        }

        view.updatePlayersCount();
        updateLobbyInfo();
    }

    private void onActionModeChanged() {
        boolean actionMode = view.getActionModeCheckbox().isSelected();
        updateLobbyInfo();
    }

    private void updateLobbyInfo() {
        boolean actionMode = view.getActionModeCheckbox().isSelected();

        long readyCount = players.stream().filter(LobbyPlayerModel::isReady).count();
        String info = String.format("Mode %s • %d prêts",
            actionMode ? "Actions" : "Classique",
            readyCount
        );

        view.setLobbyInfo(info);
    }

    private PlayerLobbySlot findSlotForPlayer(String playerId) {
        for (PlayerLobbySlot slot : view.getPlayerSlots()) {
            if (slot.getPlayer().getPlayerId().equals(playerId)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Nettoyage réseau quand on quitte le lobby
     */
    private void cleanup() {
        if (isHost && host != null) {
            host.quit();
            host = null;
        }
        if (client != null) {
            client.disconnect();
            client = null;
        }

        players.clear();
        view.clearPlayers();
    }
}
