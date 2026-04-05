package fr.upc.cr1a.controller.network.connexion;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolServer;
import fr.upc.cr1a.model.player.PlayerData;

/**
 * Sert à heberger un serveur sur le pc d'un joueur
 * 
 * @author Mei
 */
public class GameHost implements Runnable, NetworkProtocolServer{ // GameServer au lieu de GameHost ? 
    
    private int port; // pour se connecter, en ipv4 ou ipv6 ? jsp encore // non c'est un port d'acces de meme type que entre Render et l'api 
    private ServerSocket serverSocket; // permet de se recup des requests
    private boolean isRunning = false;

    private ArrayList<ClientHandler> connectedClient = new ArrayList<>(); // pour garder une trace des joueurs connectés 

    private HashMap<String, String> lobbyPlayers = new HashMap<>();

    private Thread thread;

    public GameHost(int port){
        this.port = port;
        this.thread = new Thread(this);
    }

    public void startServer(){
        isRunning = true;
        thread.start();
    }

    // methode pour fermer un serveur ( GameHost ) et kick tous les membres dedans
    public void stopServer(){
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed() ){
                serverSocket.close();
            }

            for (ClientHandler clientHandler : connectedClient){
                clientHandler.closeClient();
            }
            connectedClient.clear(); //la trace des clients connecté on la garde non ? 
            lobbyPlayers.clear();
            System.out.println("[SERVEUR] Fermerture du serveur et deconnexion de tous les clients.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        thread.interrupt(); // ça stop/detruit completement contrairement à sleep // pas vrmt..? isInterrupt()
    }

    @Override
    public void run(){

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[SERVEUR] Serveur démarré sur le port " + port);

            while (isRunning) {
                Socket socketClient = serverSocket.accept();
                System.out.println("[SERVEUR] Nouveau client connecté : " + socketClient.getInetAddress()); // recup l'adresse ip

                // on delegue ça au client handler 

                ClientHandler clientHandler = new ClientHandler(socketClient,this);
                connectedClient.add(clientHandler);
                new Thread(clientHandler).start();
            }

        } catch (Exception e) {

            if (isRunning) { // erreur imprevu
                isRunning = false;
                e.printStackTrace();

            } else { // erreur prevu
                System.out.println("[SERVEUR] Le thread du serveur s'est arrêté proprement.");
            }
        }
    }

    public void addPlayerToLobby(String id, String username){  
        lobbyPlayers.put(id, username);
        System.out.println("[LOBBY] " + username + " a rejoint la salle d'attente !");

        broadcastUpdateLobby();
    }

    public void kickPlayer(String id){ // plus sécu avec l'id que l'username
        for ( ClientHandler client : connectedClient ){
            if (client.getID().equals(id)){
                String username = client.getUsername();
                removeClient(client, id, username);
                client.closeClient();

                System.out.println("[SERVEUR] Expulsion du joueur : " + username);

                break; // ça sert à rien de parcourir le reste
            }
        }
    }

    // 
    /**
     * Methode auxillaire pour disconnect qqln avec que son username
     * Mais appelé en methode principale dans le finally de ClientHandler.run()
     * @param clientHandler
     * @param id
     * @param username
     */
    public void removeClient(ClientHandler clientHandler, String id, String username){
       connectedClient.remove(clientHandler);

       if (id != null){
        lobbyPlayers.remove(id); // faut mettre la key
        System.out.println("[SERVEUR] " + username + " a quitté le salon");

        broadcastUpdateLobby();
       }
    }


    // PARTI PROTOCOL SERVER

    @Override
    public synchronized void broadcastStartGame(){
        String message = NetworkProtocolServer.UPDATE_START_GAME;

        for (ClientHandler client : connectedClient) {
            client.sendMessage(message.toString());
        }
    }

    @Override
    public synchronized void broadcastUpdateLobby(){

        StringBuilder message = new StringBuilder("[LOBBY_UPDATE] Players in lobby : ");
        for (String pseudo : lobbyPlayers.values()) {
            message.append(pseudo).append(",");
        }

        // On l'envoie à tous les ClientHandlers
        for (ClientHandler client : connectedClient) {
            client.sendMessage(message.toString());
        }
    }

    @Override
    public synchronized void broadcastCardFlipped(String id, int col, int row, int valeur){
        String message = NetworkProtocolServer.UPDATE_CARD_FLIPPED + ":" + id + ":" + col + ":" + row + ":" +valeur;

        for (ClientHandler client : connectedClient) {
            client.sendMessage(message.toString());
        }
    }

    @Override
    public synchronized void broadcastTurnChanged(String id){
        String message = NetworkProtocolServer.UPDATE_TURN_CHANGED + ":" + id;

        for (ClientHandler client : connectedClient) {
            client.sendMessage(message.toString());
        }
    }

    // second test client simultané ( faut un host compatible )
    public static void main(String[] args) {
        PlayerData player = new PlayerData("9846220");

        GameClient gameClient = new GameClient(player);

        gameClient.connect("localhost", 1234);
    }
}
