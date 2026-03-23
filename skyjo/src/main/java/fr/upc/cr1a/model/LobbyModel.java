package fr.upc.cr1a.model;

/**
 * Modèle représentant un salon de jeu multijoueur
 *
 */
public class LobbyModel {

    private String lobbyId;
    private String lobbyName;
    private String hostName;
    private int currentPlayers;
    private int maxPlayers;
    private boolean actionMode;
    private LobbyStatus status;

    public enum LobbyStatus {
        WAITING,    //en attente de joueurs
        PLAYING,    //partie en cours
        FULL        //salon complet
    }

    public LobbyModel(String lobbyId, String lobbyName, String hostName, int currentPlayers, int maxPlayers, boolean actionMode) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
        this.hostName = hostName;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.actionMode = actionMode;
        updateStatus();
    }

    private void updateStatus() {
        if (currentPlayers >= maxPlayers) {
            this.status = LobbyStatus.FULL;
        } else {
            this.status = LobbyStatus.WAITING;
        }
    }

    //getters
    public String getLobbyId() {
        return lobbyId;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public String getHostName() {
        return hostName;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isActionMode() {
        return actionMode;
    }

    public LobbyStatus getStatus() {
        return status;
    }

    public boolean canJoin() {
        return status == LobbyStatus.WAITING;
    }

    //setters
    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
        updateStatus();
    }

    public void setStatus(LobbyStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Lobby[%s] %s - Host: %s - Players: %d/%d - Actions: %s",
                lobbyId, lobbyName, hostName, currentPlayers, maxPlayers, actionMode ? "ON" : "OFF");
    }
}
