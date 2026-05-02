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
    public static final String UPDATE_DISCARD_DRAW = "DISCARD_DRAW";
    public static final String UPDATE_EXCHANGE = "EXCHANGE";
    public static final String UPDATE_KICKED = "KICKED";
    public static final String UPDATE_GAME_ENDED = "GAME_ENDED";
    public static final String UPDATE_ROUND_ENDED = "ROUND_ENDED"; 
    public static final String UPDATE_NEW_ROUND = "NEW_ROUND";
    public static final String UPDATE_AFK_REPLACED = "AFK_REPLACED"; 
    
    public abstract void broadcastStartGame();

    public abstract void broadcastUpdateLobby();

    public abstract void broadcastCardFlipped(String id, int col, int row, int valeur);

    /**
     * Broadcast le changement de tour ET la phase authoritaire du serveur.
     * La phase doit etre le nom d'une valeur  (ex: "DRAW_DECISION", "INIT_REVEAL", "CARD_DRAW_FROM_DECK", "CARD_TAKE_FROM_DISCARD","CARD_DISCARD_FROM_DRAW", "TURN_ENDED"). 
     * Les clients appliquent cette phase via
     * {@code GameManager.forceTurnFromNetwork}.
     *
     * @param id playerId du joueur dont c'est le tour apres le changement
     * @param phase nom de la phase (enum name) authoritaire cote serveur
     */
    public abstract void broadcastTurnChanged(String id, String phase);

    public abstract void broadcastUpdatePiocheTaken(String id, int valeur);

    /**
     * Pas de parametre pour cardValue car elle est sensé etre deja visible et la meme pour tout le monde
     * @param id du joueur concerné
     */
    public abstract void broadcastUpdateDiscardTaken(String id);

    /**
     * Broadcast lorsqu'un joueur a jete la carte piochee dans la defausse (sans l'echanger).
     * @param id du joueur concerne
     * @param valeur valeur de la carte jetee (pour que les clients mettent a jour leur defausse)
     */
    public abstract void broadcastUpdateDiscardDraw(String id, int valeur);

    /**
     * Broadcast en fin de partie pour ramener tous les clients au menu.
     */
    public abstract void broadcastGameEnded(String lastRoundPlayerId);

    /**
     * Broadcast en fin de manche : les clients affichent leur RoundEndPanel.
     * @param lastRoundPlayerId id du joueur ayant declenche la fin de manche (peut etre null)
     */
    public abstract void broadcastRoundEnded(String lastRoundPlayerId);

    /**
     * Broadcast au demarrage d'une nouvelle manche : seed pour synchroniser le deck.
     */
    public abstract void broadcastNewRound(long seed);

    /**
     * Broadcast quand le joueur actif a echange une carte en main (via pioche OU defausse).
     * Les clients doivent : clear leur currentDrawCard, placer newValue en hand[row,col] et
     * pousser discardedValue au sommet de la defausse.
     *
     * @param id playerId du joueur qui a echange
     * @param col colonne de la carte echangee
     * @param row ligne de la carte echangee
     * @param newValue valeur de la carte qui arrive dans la main (venait de la pioche ou defausse)
     * @param discardedValue valeur de la carte jetee dans la defausse (l'ancienne carte de la main)
     */
    public abstract void broadcastExchange(String id, int col, int row, int newValue, int discardedValue);

    /**
     * Broadcast quand un joueur humain a ete AFK 2 fois et est desormais auto-controle (remplace par un bot).
     * Le client local du joueur concerne affichera un message "AFK : vous etes remplace par un bot".
     * @param playerId id du joueur AFK remplace
     */
    public abstract void broadcastAfkReplaced(String playerId);


    //public abstract void broadcastUpdatePioche(String id, int value);



}
