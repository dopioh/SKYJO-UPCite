package fr.upc.cr1a.model;

/**
 * Modèle représentant un joueur dans un salon d'attente
 *
 */
public class LobbyPlayerModel {

    private String playerId;
    private String playerName;
    private boolean isHost;
    private boolean isReady;
    private boolean isBot;

    public LobbyPlayerModel(String playerId, String playerName, boolean isHost, boolean isBot) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.isHost = isHost;
        this.isBot = isBot;
        this.isReady = isBot; //bot tjrs pret
    }

    //getters
    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isHost() {
        return isHost;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isBot() {
        return isBot;
    }

    //setters
    public void setReady(boolean ready) {
        if (!isBot) {
            this.isReady = ready;
        }
    }

    public void setHost(boolean host) {
        this.isHost = host;
    }

    @Override
    public String toString() {
        return String.format("Player[%s] %s - Host: %s, Ready: %s, Bot: %s",
                playerId, playerName, isHost, isReady, isBot);
    }
}
