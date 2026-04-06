package fr.upc.cr1a.controller.network.network_protocol;

public interface NetworkProtocolServer {
    
    // Commandes envoyées par le SERVEUR vers TOUS LES CLIENTS (Broadcast)
    public static final String UPDATE_START_GAME = "START_GAME";
    public static final String UPDATE_LOBBY = "LOBBY_UPDATE";
    public static final String UPDATE_CARD_FLIPPED = "UPDATE_CARD_FLIPPED";
    public static final String UPDATE_TURN_CHANGED = "TURN_CHANGED";

    public abstract void broadcastStartGame();

    public abstract void broadcastUpdateLobby();

    public abstract void broadcastCardFlipped(String id, int col, int row, int valeur);

    public abstract void broadcastTurnChanged(String id);
}
