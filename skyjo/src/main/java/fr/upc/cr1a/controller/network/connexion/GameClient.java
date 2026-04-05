package fr.upc.cr1a.controller.network.connexion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.ServerSocket;
import java.net.Socket;
//import java.util.ArrayList;

import fr.upc.cr1a.controller.network.UDP.UDPBroadcaster;
import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolClient;
import fr.upc.cr1a.model.player.PlayerData;

import java.io.*;

/**
 * Pour se connecter à un GameHost
 * 
 * @author Mei
 */
public class GameClient implements NetworkProtocolClient{

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

            out.println("JOIN :" + playerData.getUsername() +":"+ playerData.getID()); // on envoie une requete de connexion
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

    // implementation de l'interface pour send les requests in game 
    // faut dire qu'on peut les envoyer que si on est bien connecté a un host et que une game est en cours

    /**
     * Request pour retourner une carte
     * STUCTURE = id:protocol:col:row
     */
    @Override
    public void sendFlipCardRequest(String id, int col, int row){
        out.println(id +":"+NetworkProtocolClient.REQ_FLIP_CARD+":"+col +":" + row);
    }

    /**
     * Request pour piocher une carte
     * STRUCTURE = id:protocol
     */
    @Override
    public void sendPickPiocheRequest(String id){
        out.println(id + ":" + NetworkProtocolClient.REQ_PICK_PIOCHE);
    }

    /**
     * Request pour prendre une carte de la defausse
     * STRUCTURE = id:protocol
     */
    @Override
    public void sendPickDefausseRequest(String id){
        out.println(id + ":" + NetworkProtocolClient.REQ_PICK_DEFAUSSE);
    }

    /**
     * Request pour jeter une carte dans la defausse
     * STRUCTURE = id:protocol:col:row
     */
    @Override
    public void sendDisquardRequest(String id, int col, int row){
        out.println(id +":"+NetworkProtocolClient.REQ_DISCARD+":"+col +":" + row);
    }

    // test host
    public static void main(String[] args) {
        GameHost gm = new GameHost(1234);
        gm.startServer();
        
        UDPBroadcaster.broadcast(1234, "SALEMMMM");

        double deltaTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - deltaTime < 20000 ){} 

        //System.out.println("[SERVEUR] Fermerture du serveur et deconnexion de tous les clients.");
        gm.stopServer();
        
    }
}
