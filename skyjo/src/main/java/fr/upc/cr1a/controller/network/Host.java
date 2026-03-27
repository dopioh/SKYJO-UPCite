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
    private static final int port = 1234;

    //private GameClient gameClient; normalement un host est un client aussi ? 

    // viens de l'interface user

    private int nunmberPlayersMax;
    private boolean modeAction;

    private final String lobbyName; // ça ça bouge pas par contre

    private boolean isBroadcasting = false;
    private Thread threadBroadcast;


    public Host( int nbrPlayers, boolean modeAction, String lobbyName){
        //this.port = port;
        this.gameHost = new GameHost(port);
        
        this.nunmberPlayersMax = nbrPlayers;
        this.modeAction = modeAction;

        this.lobbyName = lobbyName;

        this.gameHost.startServer();

        startBroadcasting();
    }

    /**
     * Envoie un msg sur toutes les cartes reseaux de l'Host pour dire qu'il y'a un serveur dispo
     * 
     * Methode qui envoie un signal en boucle automatiquement
     */
    public void startBroadcasting(){
        isBroadcasting = true;

        //UDPBroadcaster.broadcast(this.port, lobbyName);
        this.threadBroadcast = new Thread(() -> {
            while (isBroadcasting) {
                
                String message = lobbyName + ":" + nunmberPlayersMax + ":" + modeAction;
                
                UDPBroadcaster.broadcast(port, message);
                
                try {
                    Thread.sleep(1500);  // on attend 1.5 sec avant de relancer ( le signe est lancé pdt une minute en continue)
                } catch (InterruptedException e) {
                    // fermeture du salon ( ou lancement d'une partie)
                    isBroadcasting = false;
                    break;
                }
            }
        });

    }

    public void quit(){
        gameHost.stopServer();
    }

    public void kickPlayer(String id){
        gameHost.kickPlayer(id);
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
    }

    public void setModeAction(boolean isModeAction){
        this.modeAction = isModeAction;
    }

}
