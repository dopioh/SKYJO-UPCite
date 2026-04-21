package fr.upc.cr1a.controller.network.connexion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.ServerSocket;
import java.net.Socket;
//import java.util.ArrayList;
import java.util.ArrayList;

import fr.upc.cr1a.controller.GameBoardController;
//import fr.upc.cr1a.controller.network.UDP.UDPBroadcaster;
import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolClient;
import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolServer;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GameSession;
import fr.upc.cr1a.model.NumberCardModel;
import fr.upc.cr1a.model.player.PlayerData;

import java.io.*;

import javax.swing.SwingUtilities;

import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameBoardView;

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

    private Frame frame;

    private ArrayList<PlayerData> listPlayersData;
    private boolean modeAction;

    public GameClient( Frame frame, PlayerData playerData){
        this.frame = frame;
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

            GameSession.status = GameSession.STATUS.CLIENT;
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
            GameSession.status = GameSession.STATUS.OFFLINE;
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
                    // Format : [LOBBY_UPDATE]:totalCount:maxPlayers:id:name:isBot:botType:ready:...
                    String[] parts = message.split(":");
                    int totalCount = Integer.parseInt(parts[1]);
                    int maxPlayers = Integer.parseInt(parts[2]);
                    this.modeAction = false; // mode action retire du online
                    this.listPlayersData = new ArrayList<>();
                    ArrayList<Boolean> readyStates = new ArrayList<>();

                    int index = 3;
                    for (int i = 0 ; i < totalCount ; i++){
                        String key = parts[index++];
                        String name = parts[index++];
                        boolean isBot = "1".equals(parts[index++]);
                        String botType = parts[index++];
                        boolean ready = "1".equals(parts[index++]);

                        PlayerData tmp = new PlayerData(key, name, isBot, isBot ? botType : null);
                        this.listPlayersData.add(tmp);
                        readyStates.add(ready);
                    }

                    // Met à jour l'interface du lobby avec les joueurs reçus du serveur
                    ArrayList<PlayerData> snapshot = new ArrayList<>(this.listPlayersData);
                    ArrayList<Boolean> readySnapshot = new ArrayList<>(readyStates);
                    int maxPlayersSnapshot = maxPlayers;
                    SwingUtilities.invokeLater(() -> {
                        if (frame.getLobbyRoomController() != null) {
                            frame.getLobbyRoomController().updatePlayersFromNetwork(snapshot, readySnapshot, maxPlayersSnapshot);
                        }
                    });
                }

                // NetworkProtocol du serveur qui vont definir des actions pour chaque client

                if (message.startsWith(NetworkProtocolServer.UPDATE_START_GAME)){
                    System.out.println("[SYSTEM] message start with \""+NetworkProtocolServer.UPDATE_START_GAME+"\"");
                    // Seuls les CLIENTS créent le GameManager ici ; le HOST le crée via GameHost.startAGame()
                    if (GameSession.status != GameSession.STATUS.HOST) {
                        SwingUtilities.invokeLater(() -> {
                            GameManager game = new GameManager(this.frame, this.listPlayersData, this.modeAction);
                            game.setNetwork(this);
                            frame.setCurrentGame(game);
                            System.out.println("[SYSTEM] Game launched (client side)");
                        });
                    }
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_CARD_FLIPPED)){
                    String[] parts = message.split(":");
                    
                    String id = parts[1];
                    int col = Integer.parseInt(parts[2]);
                    int row = Integer.parseInt(parts[3]);
                    int valeur = Integer.parseInt(parts[4]);
                    
                    GameManager game = frame.getCurrentGame();
                    
                    if (game != null) {
                        game.forceRevealCardFromNetwork(id, col, row, valeur);
                    }
                }

                if(message.startsWith(NetworkProtocolServer.UPDATE_TURN_CHANGED)){
                    String[] parts = message.split(":");
                    
                    if (parts.length >= 2) {
                        String idJoueurActif = parts[1];
                        
                        GameManager game = frame.getCurrentGame();
                        
                        if (game != null) {
                            game.forceTurnFromNetwork(idJoueurActif);
                        }
                    }
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_DISCARD_TAKEN)) {
                    //String[] parts = message.split(":");
                    //String playerId = parts[1];
                    
                    GameManager game = frame.getCurrentGame();
                    if (game != null) {
     
                        // tlm fait l'action localement
                        game.takeDiscard();
                        

                        if (game.getGameBoardModel() != null) {
                            game.getGameBoardModel().getGameBoardController().updateAllDecksDisplay(); // mettra une carte au hasard, il faut attendre la view pour tester
                            game.getGameBoardModel().getGameBoardController().refreshGuide();
                        }
                    }
                }

                // requete secrete ( broadcast que le client handler concerné normalement)
                if (message.startsWith("UPDATE_DRAWN_CARD")) { // valeur de la pioche 
                    String[] parts = message.split(":");
                    int valeur = Integer.parseInt(parts[1]);

                    GameManager game = frame.getCurrentGame();
                    if (game != null) {
                        NumberCardModel vraieCarte = new NumberCardModel(false, valeur);
                        
                        
                        game.setCurrentDrawCard(vraieCarte); // ça set que dans la main du client concerné 
                        
                        // phase de choix entre echanger ou jeter
                        game.setTurnPhase(GameManager.TurnPhase.CARD_DRAW_FROM_DECK);
                        

                        if (game.getGameBoardModel() != null) {
                            GameBoardView view = game.getGameBoardModel().getGameBoardView();
                            GameBoardController controller = game.getGameBoardModel().getGameBoardController();
                            
                            controller.refreshGuide(); 
                            view.showCursorCard(view.getCardImagePath(vraieCarte)); // carte sous la souris du joueur
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[CLIENT] Déconnecté du serveur.");
            GameSession.status = GameSession.STATUS.OFFLINE;
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

    /**
     * Request pour changer l'etat "pret" d'un joueur
     * STRUCTURE = id:protocol:ready(true/false)
     */
    @Override
    public void sendReadyRequest(String id, boolean ready){
        if (out != null){
            out.println(id + ":" + NetworkProtocolClient.REQ_READY + ":" + ready);
        }
    }

    // test host
    /*
    public static void main(String[] args) {
        GameHost gm = new GameHost(1234);
        gm.startServer();
        
        UDPBroadcaster.broadcast(1234, "SALEMMMM");

        double deltaTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - deltaTime < 20000 ){} 

        //System.out.println("[SERVEUR] Fermerture du serveur et deconnexion de tous les clients.");
        gm.stopServer();
        
    }
    */
}
