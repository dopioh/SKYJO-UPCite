package fr.upc.cr1a.controller.network;

import fr.upc.cr1a.controller.network.UDP.UDPBroadcaster;
import fr.upc.cr1a.controller.network.connexion.GameHost;
import fr.upc.cr1a.model.GameSession;
import fr.upc.cr1a.model.player.PlayerAccount;
import fr.upc.cr1a.view.Frame;

/**
 * Classe appelé lorqu'un joueur crée un salon
 * Il defini les parametres du salon ( qui peuvent etre modifiable apres avoir eté creer ) 
 */
public class Host {
    private GameHost gameHost;
    private static final int port = 1234; // c'est 1234 dans Client aussi, au cas on modif ici faut modif la bas aussi

    private Client client; //normalement un host est un client aussi ? 

    // viens de l'interface user

    private int nunmberPlayersMax;
    private boolean modeAction;

    private final String lobbyName; // ça ça bouge pas par contre

    private boolean isBroadcasting = false;
    private Thread threadBroadcast;

    private Frame frame;


    /**
     * Ce constructeur doit etre appelé lorsque un joueur creer un salon ( avec tous les parametres renseignés )
     * @param nbrPlayers max 8 mais le joueur peux decider entre 2 et 8 inclus
     * @param modeAction 
     * @param lobbyName nom du lobby (exp "Salon de Mei")
     */
    public Host( int nbrPlayers, boolean modeAction, String lobbyName,Frame frame){
        //this.port = port;
        this.frame = frame;
        this.gameHost = new GameHost(port,modeAction,frame);
        
        this.nunmberPlayersMax = nbrPlayers;
        this.modeAction = modeAction;

        this.lobbyName = lobbyName;

        this.gameHost.startServer();

        this.client = new Client(frame, PlayerAccount.actuelAccount);
        client.connect("localhost"); // l'host va se connecter sur son propre pc donc localhost
        GameSession.status = GameSession.STATUS.HOST; // rétablir le statut HOST (écrasé par GameClient.connect)

        startBroadcasting();
    }

    /**
     * Envoie un msg sur toutes les cartes reseaux de l'Host pour dire qu'il y'a un serveur dispo
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
                    Thread.sleep(1500);  // on attend 1.5 sec avant de relancer
                } catch (InterruptedException e) {
                    // fermeture du salon ( ou lancement d'une partie)
                    isBroadcasting = false;
                    break;
                }
            }
        });

        this.threadBroadcast.start();

    }

    /**
     * Lorsque l'hote lance la partie, il faut arreter le Broadcast puisqu'il ne cherche plus de joueur pour son salon
     * Risque d'excptions si on stop pas le BroadCast quand l'hote est en game
     */
    public void stopBroadcasting(){
        if (isBroadcasting){
            threadBroadcast.interrupt();
            isBroadcasting = false;
        }
    }

    /**
     * Lance la partie : arrête le broadcast, prévient tous les clients, et crée le GameManager coté host
     */
    public void startGame(){
        stopBroadcasting();
        gameHost.broadcastStartGame();
        gameHost.startAGame();
    }

    /**
     * Quand l'hote se deconnecte ( avec la methode ici),
     * Alors ça deconnecte egalement tous les joueurs du lobby proprement
     */
    public void quit(){
        stopBroadcasting();
        gameHost.stopServer();
    }

    /**
     * L'hote peut kick un joueur en particulier de son lobby en ayant juste son ID
     * @param id
     */
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

    public Client getClient(){
        return this.client;
    }

    public GameHost getGameHost(){
        return this.gameHost;
    }

}
