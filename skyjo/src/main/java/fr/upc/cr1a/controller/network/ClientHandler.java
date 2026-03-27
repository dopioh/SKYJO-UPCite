package fr.upc.cr1a.controller.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import fr.upc.cr1a.model.PlayerData;

/**
 * Classe Runnable qui gère la connexion entre le host (server) et le client
 * 
 * @author Mei
 */
public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private GameHost gameHost;

    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket clientSocket,GameHost gameHost){
        this.clientSocket = clientSocket;
        this.gameHost = gameHost;
    }

    public void sendMessage(String msg){
        out.println(msg);
    }


    @Override
    public void run(){
        try {

            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;

            while ((message = in.readLine()) != null){
                System.out.println("[SERVEUR a entendu] = " + message);

                if (message.startsWith("JOIN")) {
                    System.out.println("[SYSTEM] message start with \"JOIN\"");
                    String[] parts = message.split(":");

                    for ( int i = 0 ; i < parts.length ; i++){
                        System.out.println("[SYSTEM] parts[" + i + "] = " + parts[i]);
                    }
                
                    if (parts.length == 3){
                        System.out.println("[SYSTEM] parts has lenght 3");
                        String username = parts[1];
                        String id = parts[2];
                     
                        gameHost.addPlayerToLobby(id, username);
                        out.println("[LOBBY_UPDATE] Tu es bien connecté au serveur !");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[CLIENT] Déconnecté du serveur.");
            e.printStackTrace();
        }
    }

    /* // Classe test client ( ne marche que si y'a un host correspondant)
    public static void main(String[] args) {
        PlayerData player = new PlayerData("POPOPO");

        GameClient gameClient = new GameClient(player);

        gameClient.connect("localhost", 1234);
    }
    */
}
