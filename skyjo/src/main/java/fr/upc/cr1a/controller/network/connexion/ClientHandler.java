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
                        out.println("[INFO] Tu es bien connecté au serveur !");
                    }
                }

                if (message.contains(":" + NetworkProtocolClient.REQ_READY + ":")) {
                    String[] parts = message.split(":");
                    if (parts.length >= 3) {
                        String playerId = parts[0];
                        boolean ready = Boolean.parseBoolean(parts[2]);
                        gameHost.setPlayerReady(playerId, ready);
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
                        
                        //retournement de cartes PRE MANCHE
                        if (phase == GameManager.TurnPhase.INIT_REVEAL) {
                            CardModel card = game.getCurrentPlayer().getHand().getCard(row, col);
                            String previousPlayerId = playerId;

                            boolean success = game.revealInitCard(col, row);

                            if (success) {
                                if (card instanceof NumberCardModel) {
                                    int val = ((NumberCardModel) card).getCardValue();
                                    gameHost.broadcastCardFlipped(previousPlayerId, col, row, val);
                                }

                                String newCurrentId = game.getCurrentPlayer().getPlayerData().getID();
                                boolean playerChanged = !newCurrentId.equals(previousPlayerId);
                                boolean phaseChanged = (game.getTurnPhase() != GameManager.TurnPhase.INIT_REVEAL);
                                if (playerChanged || phaseChanged) {
                                    triggerProceedAfterAction(game);
                                } else {
                                    gameHost.broadcastTurnChanged(newCurrentId, game.getTurnPhase().name());
                                }
                            }
                        }

                        // apres avoir jeté une pioche

                        else if (phase == GameManager.TurnPhase.CARD_DISCARD_FROM_DRAW) {

                            game.revealCardInHand(col, row);
                            CardModel card = game.getCurrentPlayer().getHand().getCard(row, col);

                            if (card instanceof NumberCardModel) {
                                int val = ((NumberCardModel) card).getCardValue();
                                gameHost.broadcastCardFlipped(playerId, col, row, val);
                            }
                            triggerProceedAfterAction(game);
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
                            gameHost.broadcastTurnChanged(playerId, game.getTurnPhase().name());
                        }
                    }
                }

                // Echanger une carte [id:DISCARD:col:row]
                else if (message.contains(":" + NetworkProtocolClient.REQ_DISCARD + ":")) {
                    String[] parts = message.split(":");
                    String playerId = parts[0];
                    int col = Integer.parseInt(parts[2]);
                    int row = Integer.parseInt(parts[3]);

                    GameManager game = gameHost.getGameManager();
                    if (game != null && game.getCurrentPlayer().getPlayerData().getID().equals(playerId)) {
                        GameManager.TurnPhase phase = game.getTurnPhase();

                        // verifie que le joueur a une pioche en main
                        if (phase == GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD || phase == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
                            int newValue = -1; // Carte qu'on vient d'avoir
                            int discardedValue = -1; // Carte d'avant 
                            CardModel drawn = game.getCurrentDrawCard();
                            if (drawn instanceof NumberCardModel) {
                                newValue = ((NumberCardModel) drawn).getCardValue();
                            }
                            CardModel oldHand = game.getCurrentPlayer().getHand().getCard(row, col);
                            if (oldHand instanceof NumberCardModel) {
                                discardedValue = ((NumberCardModel) oldHand).getCardValue();
                            }

                            game.exchangeCardInHand(col, row);
                            gameHost.broadcastExchange(playerId, col, row, newValue, discardedValue);
                            triggerProceedAfterAction(game);
                        }
                    }
                }
                
                else if (message.contains(":" + NetworkProtocolClient.REQ_DISCARD_DRAW)) {
                    String[] parts = message.split(":");
                    String playerId = parts[0];

                    GameManager game = gameHost.getGameManager();
                    if (game != null && game.getCurrentPlayer().getPlayerData().getID().equals(playerId)) {
                        if (game.getTurnPhase() == GameManager.TurnPhase.CARD_DRAW_FROM_DECK) {
                            CardModel drawn = game.getCurrentDrawCard();
                            int val = -1;
                            if (drawn instanceof NumberCardModel) {
                                val = ((NumberCardModel) drawn).getCardValue();
                            }
                            game.discardDrawCard();
                            gameHost.broadcastUpdateDiscardDraw(playerId, val);
                            gameHost.broadcastTurnChanged(playerId, game.getTurnPhase().name());
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
                            
                            // Bug la carte ne se retournait pas
                            if (drawnCard != null && drawnCard.isFaceDown()) drawnCard.returnCard();

                            if (drawnCard instanceof NumberCardModel) {
                                int val = ((NumberCardModel) drawnCard).getCardValue();
                                sendMessage("UPDATE_DRAWN_CARD:" + val); // pas dans NetworkProtocol pour le moment car requete unique à un joueur
                            }

                            gameHost.broadcastTurnChanged(playerId, game.getTurnPhase().name());
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

    /**
     * Appelle GameLoop.proceedAfterAction() sur pour avoir la meme chose qu'en local
     */
    private void triggerProceedAfterAction(GameManager game){
        try {
            if (game.getGameBoardModel() != null
                && game.getGameBoardModel().getGameBoardController() != null
                && game.getGameBoardModel().getGameBoardController().getGameLoop() != null) {
                javax.swing.SwingUtilities.invokeLater(() ->
                    game.getGameBoardModel().getGameBoardController().getGameLoop().proceedAfterAction()
                );
                return;
            }
        } catch (Exception e) {
        }
        game.nextPlayer();
        game.startTurn();
        gameHost.broadcastTurnChanged(game.getCurrentPlayer().getPlayerData().getID(), game.getTurnPhase().name());
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
