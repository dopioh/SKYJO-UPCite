package fr.upc.cr1a.controller.network.network_protocol;

/**
 * Evite les erreurs de frappes
 */
public interface NetworkProtocolClient {
    
    // Commandes envoyées par le CLIENT vers le SERVEUR
    //public static final String REQ_JOIN = "JOIN";
    public static final String REQ_FLIP_CARD = "FLIP_CARD";
    public static final String REQ_PICK_PIOCHE = "PICK_PIOCHE";
    public static final String REQ_PICK_DEFAUSSE = "PICK_DEFAUSSE";
    public static final String REQ_DISCARD = "DISCARD";
    public static final String REQ_DISCARD_DRAW = "DISCARD_DRAW"; // jeter la carte piochee sans l'echanger
    public static final String REQ_READY = "READY";

    public abstract void sendFlipCardRequest(String id, int col, int row);

    public abstract void sendPickPiocheRequest(String id);

    public abstract void sendPickDefausseRequest(String id);

    public abstract void sendDisquardRequest(String id, int col, int row);

    public abstract void sendDiscardDrawRequest(String id);

    public abstract void sendReadyRequest(String id, boolean ready);
}
