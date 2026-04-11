package fr.upc.cr1a.controller.network.network_protocol;

public interface NetworkProtocolServer {
    
    // Commandes envoyées par le SERVEUR vers TOUS LES CLIENTS (Broadcast)
    public static final String UPDATE_START_GAME = "START_GAME";
    public static final String UPDATE_LOBBY = "LOBBY_UPDATE";
    public static final String UPDATE_CARD_FLIPPED = "UPDATE_CARD_FLIPPED";
    public static final String UPDATE_TURN_CHANGED = "TURN_CHANGED";

    public static final String UPDATE_PIOCHE_TAKEN = "PIOCHE_TAKE";
    public static final String UPDATE_DISCARD_TAKEN = "DISCARD_TAKEN";
    public static final String UPDATE_DISCARD = "DISCARD";
    
    public abstract void broadcastStartGame();

    public abstract void broadcastUpdateLobby();

    public abstract void broadcastCardFlipped(String id, int col, int row, int valeur);

    public abstract void broadcastTurnChanged(String id);

    public abstract void broadcastUpdatePiocheTaken(String id, int valeur);

    /**
     * Pas de parametre pour cardValue car elle est sensé etre deja visible et la meme pour tout le monde
     * @param id du joueur concerné
     */
    public abstract void broadcastUpdateDiscardTaken(String id);


    public abstract void broadcastUpdateDiscard(String id, int value);



}
