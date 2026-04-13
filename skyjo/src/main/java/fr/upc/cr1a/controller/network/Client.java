package fr.upc.cr1a.controller.network;

import java.util.HashMap;

import fr.upc.cr1a.controller.network.UDP.ServerListener;
import fr.upc.cr1a.controller.network.UDP.UDPReceiver;
import fr.upc.cr1a.controller.network.connexion.GameClient;
import fr.upc.cr1a.model.player.PlayerData;

/**
 * Classe pour se connecter à un serveur (Host)
 * Chaque joueur ( y compris le host ) doit en posseder une
 */
public class Client {

    private GameClient gameClient;

    private UDPReceiver receiver; // gere le broadcast coté client 

    private final static int port = 1234;

    private HashMap<String,String> lobbyfound;

    /**
     * Le playerData vient de la db quand on se connecte, donc il faut bloquer le mode multijoueur pour les joueurs invités
     * le playerData du joueur actuel (connecté) est dispo dans PlayerAccount (seule variable globale)
     * @param playerData
     */
    public Client(PlayerData playerData){

        this.lobbyfound = new HashMap<>(); // ip et String d'info : "mode action : nombre joueurs : etc.."

        UDPReceiver receiver = new UDPReceiver(port, new ServerListener() { // pq -> ça marche pas ??? c'est une interface fonctionnel pourtant
            @Override
            public void foundServer(String msg, String ip) {
                if (!lobbyfound.containsKey(ip)){
                    lobbyfound.put(ip, msg);

                    //System.out.println("[INFO] NOUVEAU SALON : " + msg + " (IP: " + ip + ")"); // pas besoin de le mettre ici, ca print deja a partir de l'UDPCLIENT
                }
            }
        });
        receiver.start();
        double deltaTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - deltaTime < 2000 ){} // cherche pdt 2 secondes et apres arrete, on peut modif la valeur si on se rends compte qu'il galere a trouver tous les serveur en 2 secondes
        receiver.stop();
    }

    /**
     * Methode pour connecter le Client à un des serveurs trouver par le broadcast et stocker dans la HashMap
     * @param ip de l'Host qu'on recuper grace au receiver juste en haut
     */
    public void connect(String ip){
        gameClient.connect(ip, port);
    }

    /**
     * Fait quitter le client du lobby proprement ( ferme ses ports et ses threads ouverts)
     */
    public void disconnect(){
        gameClient.disconnect();
    }

    public HashMap<String,String> getLobbyAvailable(){
        return this.lobbyfound;
    }

    public GameClient getGameClient(){
        return this.gameClient;
    }
}
