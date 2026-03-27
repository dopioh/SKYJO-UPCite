package fr.upc.cr1a.controller.network;

import fr.upc.cr1a.controller.network.UDP.UDPBroadcaster;
//import fr.upc.cr1a.controller.network.connexion.GameClient;
import fr.upc.cr1a.controller.network.connexion.GameHost;
/*
port unique pour ceux qui veulent (donc pour se connecter à un salon privé)
mais sinon ça sera un port unique du genre 1234 pour tout le monde ? 

mettre cette classe dans model ? 
 */

/**
 * Classe appelé lorqu'un joueur crée un salon
 * Il defini les parametres du salon ( qui peuvent etre modifiable apres avoir eté creer ) 
 */
public class Host {
    private GameHost gameHost;
    private int port;

    //private GameClient gameClient; normalement un host est un client aussi ? 

    // viens de l'interface user

    private int nunmberPlayersMax;
    private boolean modeAction;

    private final String lobbyName; // ça ça bouge pas par contre


    public Host(int port, int nbrPlayers, boolean modeAction, String lobbyName){
        this.port = port;
        this.gameHost = new GameHost(port);
        
        this.nunmberPlayersMax = nbrPlayers;
        this.modeAction = modeAction;

        this.lobbyName = lobbyName;
    }

    /**
     * Envoie un msg sur toutes les cartes reseaux de l'Host pour dire qu'il y'a un serveur dispo
     * 
     * sendSignals devra etre appelé à chaque fois qu'un parametre aura changé
     */
    public void sendSignals(){
        UDPBroadcaster.broadcast(this.port, lobbyName);
    }

    // getter 

    public int getNbrPlayer(){
        return nunmberPlayersMax;
    }

    public boolean isModeAction(){
        return this.modeAction;
    }

    public String getLobbyName(){
        return lobbyName;
    }


    // setter

    public void setNbrPlayer(int nbrPlayers){
        this.nunmberPlayersMax = nbrPlayers;
        sendSignals();
    }

    //public void 

}
