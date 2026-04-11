package fr.upc.cr1a.controller.network;

import java.util.HashMap;

import fr.upc.cr1a.controller.network.UDP.ServerListener;
import fr.upc.cr1a.controller.network.UDP.UDPReceiver;
import fr.upc.cr1a.controller.network.connexion.GameClient;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.view.Frame;

/**
 * Classe pour se connecter à un serveur (Host)
 * Chaque joueur ( y compris le host ) doit en posseder une
 */
public class Client {

    private GameClient gameClient;

    private UDPReceiver receiver;

    private final static int port = 1234;

    private HashMap<String,String> lobbyfound;

    private Frame frame;
    private PlayerData playerData;

    /**
     * @param frame la fenetre principale
     * @param playerData le playerData du joueur actuel (connecté)
     */
    public Client(Frame frame, PlayerData playerData){
        this.frame = frame;
        this.playerData = playerData;
        this.lobbyfound = new HashMap<>();
        this.gameClient = new GameClient(frame, playerData);
    }

    /**
     * Recherche les salons disponibles sur le réseau local (non bloquant)
     * @param durationMs durée de recherche en millisecondes
     * @param onComplete callback exécuté sur l'EDT à la fin de la recherche
     */
    public void discoverLobbies(int durationMs, Runnable onComplete){
        lobbyfound.clear();

        this.receiver = new UDPReceiver(port, new ServerListener() {
            @Override
            public void foundServer(String msg, String ip) {
                if (!lobbyfound.containsKey(ip)){
                    lobbyfound.put(ip, msg);
                }
            }
        });
        receiver.start();

        new Thread(() -> {
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            receiver.stop();
            if (onComplete != null){
                javax.swing.SwingUtilities.invokeLater(onComplete);
            }
        }, "LobbyDiscovery").start();
    }

    /**
     * Connecte le Client à un Host via TCP
     * @param ip de l'Host
     * @return true si la connexion a réussi
     */
    public boolean connect(String ip){
        return gameClient.connect(ip, port);
    }

    /**
     * Déconnecte proprement le client
     */
    public void disconnect(){
        if (receiver != null){
            receiver.stop();
        }
        gameClient.disconnect();
    }

    public HashMap<String,String> getLobbyAvailable(){
        return this.lobbyfound;
    }

    public GameClient getGameClient(){
        return this.gameClient;
    }
}
