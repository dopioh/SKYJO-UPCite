package fr.upc.cr1a.controller.network;

public class NetworkProtocol {
    
    // Commandes envoyées par le CLIENT vers le SERVEUR
    public static final String REQ_JOIN = "JOIN";
    public static final String REQ_FLIP_CARD = "FLIP_CARD";
    public static final String REQ_DRAW_PIOCHE = "DRAW_PIOCHE";
    public static final String REQ_DRAW_DEFAUSSE = "DRAW_DEFAUSSE";
    public static final String REQ_DISCARD = "DISCARD";

    // Commandes envoyées par le SERVEUR vers TOUS LES CLIENTS (Broadcast)
    public static final String UPDATE_LOBBY = "LOBBY_UPDATE";
    public static final String UPDATE_CARD_FLIPPED = "UPDATE_CARD_FLIPPED";
    public static final String UPDATE_TURN_CHANGED = "TURN_CHANGED";
    
}
