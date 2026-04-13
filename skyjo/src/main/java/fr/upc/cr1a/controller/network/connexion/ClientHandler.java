package fr.upc.cr1a.controller.network.connexion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
//import java.util.HashMap;
//import java.util.HashSet;

import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolClient;
import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.NumberCardModel;

//import fr.upc.cr1a.controller.network.UDP.ServerListener;
//import fr.upc.cr1a.controller.network.UDP.UDPReceiver;
//import fr.upc.cr1a.model.player.PlayerData;

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

    private String id;
    private String username;

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

                    //for ( int i = 0 ; i < parts.length ; i++){
                    //    System.out.println("[SYSTEM] parts[" + i + "] = " + parts[i]);
                    //}
                
                    if (parts.length == 3){
                        System.out.println("[SYSTEM] parts has lenght 3");
                        this.username = parts[1];
                        this.id = parts[2];
                     
                        gameHost.addPlayerToLobby(id, username);
                        out.println("[LOBBY_UPDATE] Tu es bien connecté au serveur !");
                    }
                }

                if (message.contains(NetworkProtocolClient.REQ_FLIP_CARD)) {
                    String[] parts = message.split(":");
                    String playerId = parts[0];
                    int col = Integer.parseInt(parts[2]);
                    int row = Integer.parseInt(parts[3]);
                    
                    // verifier que le coup est valide
                   //gameHost.getGameManager().

                   GameManager game = gameHost.getGameManager();

                   // Verifie que c'est le tour du bon joueur de joué 
                    if (game != null && game.getCurrentPlayer().getPlayerData().getID().equals(playerId)) { 
                        GameManager.TurnPhase phase = game.getTurnPhase();
                        
                        // cas du retournement de cartes PRE MANCHE
                        if (phase == GameManager.TurnPhase.INIT_REVEAL) {
                            boolean success = game.revealInitCard(col, row);

                            if (success) {
                                CardModel card = game.getCurrentPlayer().getHand().getCard(row, col);
                                if (card instanceof NumberCardModel) {
                                    int val = ((NumberCardModel) card).getCardValue();
                                    
                                    // braodcast de la carte retourné
                                    gameHost.broadcastCardFlipped(playerId, col, row, val);
                                }
                                
                                gameHost.broadcastTurnChanged(game.getCurrentPlayer().getPlayerData().getID());
                            }
                        } 
                        // cas du retournement obligatoire apres avoir jeté une pioche

                        else if (phase == GameManager.TurnPhase.CARD_DISCARD_FROM_DRAW) {

                            game.revealCardInHand(col, row);
                            CardModel card = game.getCurrentPlayer().getHand().getCard(row, col);

                            if (card instanceof NumberCardModel) {
                                int val = ((NumberCardModel) card).getCardValue();
                                gameHost.broadcastCardFlipped(playerId, col, row, val);
                            }
                            
                            game.nextPlayer();
                            gameHost.broadcastTurnChanged(game.getCurrentPlayer().getPlayerData().getID());
                        }
                    }
                }
                // PIOCHE UNE CARTE DANS LA DEFAUSSE
                if (message.contains(NetworkProtocolClient.REQ_PICK_DEFAUSSE)) {
                    String[] parts = message.split(":");
                    String playerId = parts[0];

                    GameManager game = gameHost.getGameManager();

                    if (game != null && game.getCurrentPlayer().getPlayerData().getID().equals(playerId)) {

                        if (game.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                            
                            // execution dans le jeu
                            game.takeDiscard();
                            gameHost.broadcastUpdateDiscardTaken(playerId); 
                        }
                    }
                }

                // Echanger une carte ( jeter dans la defausse )
                else if (message.contains(NetworkProtocolClient.REQ_DISCARD)) {
                    String[] parts = message.split(":");
                    String playerId = parts[0];
                    int col = Integer.parseInt(parts[2]);
                    int row = Integer.parseInt(parts[3]);

                    GameManager game = gameHost.getGameManager();
                    if (game != null && game.getCurrentPlayer().getPlayerData().getID().equals(playerId)) {
                        GameManager.TurnPhase phase = game.getTurnPhase();
                        
                        // verifie que le joueur a une pioche en main
                        if (phase == GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD || phase == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
                            game.exchangeCardInHand(col, row);
                            
                            // on revele la carte a tlm
                            CardModel card = game.getCurrentPlayer().getHand().getCard(row, col);
                            if (card instanceof NumberCardModel) {
                                int val = ((NumberCardModel) card).getCardValue();
                                gameHost.broadcastCardFlipped(playerId, col, row, val);
                            }
                            
                            // Le tour est fini
                            game.nextPlayer();
                            gameHost.broadcastTurnChanged(game.getCurrentPlayer().getPlayerData().getID());
                        }
                    }
                }

                else if (message.contains(NetworkProtocolClient.REQ_PICK_PIOCHE)) {
                    String[] parts = message.split(":");
                    String playerId = parts[0];

                    GameManager game = gameHost.getGameManager();
                    if (game != null && game.getCurrentPlayer().getPlayerData().getID().equals(playerId)) {
                        if (game.getTurnPhase() == GameManager.TurnPhase.DRAW_DECISION) {
                            CardModel drawnCard = game.drawDeck();
                            

                            if (drawnCard instanceof NumberCardModel) {
                                int val = ((NumberCardModel) drawnCard).getCardValue();
                                sendMessage("UPDATE_DRAWN_CARD:" + val); // pas dans NetworkProtocol pour le moment car requete unique à un joueur
                            }
                            
                            gameHost.broadcastTurnChanged(playerId);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[CLIENT] Déconnecté du serveur.");
            //e.printStackTrace();
        } finally {
            gameHost.removeClient(this, id, username);
        }
    }

    public void closeClient(){
        try {
            if (clientSocket != null && !clientSocket.isClosed()){
                clientSocket.close();
            }
            if (in != null) in.close();
            if (out != null) out.close();
            System.out.println("[SERVEUR] ClientHandler fermé -- deconnexion du Client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getID(){
        return this.id;
    }

    public String getUsername(){
        return this.username;
    }

    // test client ( ne marche que si y'a un host correspondant)
    /*
    public static void main(String[] args) {
        //PlayerData player = new PlayerData("POPOPO");

        //GameClient gameClient = new GameClient(player);

        //gameClient.connect("localhost", 1234);

        HashMap<String,String> lobbyfound = new HashMap<>();

        System.out.println("[MAIN] Lancement du test Récepteur...");

        UDPReceiver receiver = new UDPReceiver(1234, new ServerListener() { // pq -> ça marche pas ??? c'est une interface fonctionnel pourtant
            @Override
            public void foundServer(String msg, String ip) {
                if (!lobbyfound.containsKey(ip)){
                    lobbyfound.put(ip, msg);
                }
            }
        });
        receiver.start();

        System.out.println("[MAIN] Thread lancé. Le programme doit rester ouvert en écoutant.");
    }
    */
}
