package fr.upc.cr1a.controller.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.ServerSocket;
import java.net.Socket;
//import java.util.ArrayList;

import fr.upc.cr1a.model.PlayerData;
import java.io.*;

/**
 * Pour se connecter à un GameHost
 * 
 * @author Mei
 */
public class GameClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private PlayerData playerData;
    //private LobbyCallback lobbyCallback

    public GameClient( PlayerData playerData){
        this.playerData = playerData;

    }

    public boolean connect(String ip, int port){
        try {
            this.socket = new Socket(ip, port);
            this.out = new PrintWriter(socket.getOutputStream(), true); 
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("JOIN : " + playerData.getUsername() +" : "+ playerData.getID()); // on envoie une requete de connexion
            // System.out.println(); -> print console alors que l'autre est envoyé au receveur

            new Thread(this::listenerToServer).start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    public void disconnect(){
        try {
            if (socket != null && !socket.isClosed()){
                socket.close();
            }

            if (in != null) in.close();
            if (out != null) out.close();
            System.out.println("[CLIENT] Déconnecté du serveur");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listenerToServer(){
        try {
            String message;
            while ((message = in.readLine()) != null){
                if ( message.startsWith("[LOBBY_UPDATE]")){
                    System.out.println("Nouveau message : \" " + message + " \" ");
                }
            }
        } catch (Exception e) {
            System.out.println("[CLIENT] Déconnecté du serveur.");
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    // test host
    public static void main(String[] args) {
        GameHost gm = new GameHost(1234);
        gm.startServer();

        double deltaTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - deltaTime < 20000 ){} 

        //System.out.println("[SERVEUR] Fermerture du serveur et deconnexion de tous les clients.");
        gm.stopServer();
    }
}
