package fr.upc.cr1a.controller.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private GameHost gameHost;

    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket clientSocket,GameHost gameHost){
        this.clientSocket = clientSocket;
        this.gameHost = gameHost;
    }


    @Override
    public void run(){
        try {

            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;

            while ((message = in.readLine()) != null){
                System.out.println("[SERVEUR a entendu] : " + message);

                if (message.startsWith("[JOIN]")) {
                    out.println("[LOBBY_UPDATE] Tu es bien connecté au serveur !");
                }
            }

        } catch (Exception e) {
            System.out.println("[CLIENT] Déconnecté du serveur.");
        }
    }
}
