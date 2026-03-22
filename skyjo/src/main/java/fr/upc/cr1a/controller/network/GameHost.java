package fr.upc.cr1a.controller.network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import fr.upc.cr1a.model.PlayerData;

/**
 * Sert à heberger un serveur sur le pc d'un joueur
 */
public class GameHost implements Runnable{ // GameServer au lieu de GameHost ? 
    
    private int port; // pour se connecter, en ipv4 ou ipv6 ? jsp encore // non c'est un port d'acces de meme type que entre Render et l'api 
    private ServerSocket serverSocket; // permet de se recup des requests
    private boolean isRunning = false;

    private ArrayList<ClientHandler> connectedClient = new ArrayList<>(); // pour garder une trace des joueurs connectés 

    private ArrayList<PlayerData> lobbyPlayers = new ArrayList<>(); // on stock les info des joueurs connectés

    public GameHost(int port){
        this.port = port;
    }

    public void startServer(){
        isRunning = true;
        new Thread(this).start();
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
            isRunning = false;
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PlayerData player = new PlayerData("aza_dev");

        GameClient gameClient = new GameClient(player);

        gameClient.connect("localhost", 1234);
    }
}
