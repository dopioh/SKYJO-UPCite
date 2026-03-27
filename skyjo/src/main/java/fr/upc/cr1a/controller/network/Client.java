package fr.upc.cr1a.controller.network;

import java.util.HashMap;

import fr.upc.cr1a.controller.network.UDP.ServerListener;
import fr.upc.cr1a.controller.network.UDP.UDPReceiver;
import fr.upc.cr1a.controller.network.connexion.GameClient;
import fr.upc.cr1a.model.PlayerData;

/**
 * Classe pour se connecter à un serveur (Host)
 */
public class Client {

    private GameClient gameClient;

    private UDPReceiver receiver;

    private final static int port = 1234;

    private HashMap<String,String> lobbyfound;

    public Client(PlayerData playerData){ // est ce que ça vaut le coup de mettre le playerData ici et recup les infos plus tard ou on recup l'id et et le name avant de creer le Client
        this.gameClient = new GameClient(playerData);


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

    public void connect(String ip){
        gameClient.connect(ip, port);
    }

    public void disconnect(){
        gameClient.disconnect();
    }

    public HashMap<String,String> getLobbyAvailable(){
        return this.lobbyfound;
    }


    public static void main(String[] args) {
        Client client = new Client(new PlayerData("testID"));
        
        client.getLobbyAvailable().forEach((ip,info) -> System.out.println("Serveur a l'adresse " + ip + " : " + info));
    }
}
