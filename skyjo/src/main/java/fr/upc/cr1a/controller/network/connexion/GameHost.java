package fr.upc.cr1a.controller.network.connexion;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import fr.upc.cr1a.controller.network.network_protocol.NetworkProtocolServer;
import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.model.GameSession;
//import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.model.player.PlayerData;
import fr.upc.cr1a.view.Frame;

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

    private HashMap<String, String> lobbyPlayers = new HashMap<>(); // key = id, value = name
    private HashMap<String, Boolean> playerReadyState = new HashMap<>(); // id -> ready
    private HashMap<String, String> lobbyBots = new HashMap<>(); // id -> botType (nom = botType aussi cote ui)
    private int maxPlayers = 8;

    private Thread thread;

    private boolean modeAction;
    private GameManager gameManager;
    private Frame frame;

    public GameHost(int port, boolean modeAction, Frame frame){ // rajouter le nom d'un salon et toutes les infos necessaire egalement
        this.port = port;
        this.thread = new Thread(this);
        this.modeAction = modeAction;
        this.frame = frame;
    }

    public void startServer(){
        isRunning = true;
        thread.start();
        GameSession.status = GameSession.STATUS.HOST;
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
            GameSession.status = GameSession.STATUS.OFFLINE;
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
        playerReadyState.put(id, false);
        System.out.println("[LOBBY] " + username + " a rejoint la salle d'attente !");

        broadcastUpdateLobby();
    }

    /**
     * Ajoute un bot au lobby et propage aux clients
     */
    public void addBotToLobby(String id, String botType){
        lobbyBots.put(id, botType);
        System.out.println("[LOBBY] Bot " + botType + " ajoute au lobby.");
        broadcastUpdateLobby();
    }

    public void removeBotFromLobby(String id){
        if (lobbyBots.remove(id) != null) {
            broadcastUpdateLobby();
        }
    }

    /**
     * Met a jour l'etat "pret" d'un joueur et broadcast
     */
    public void setPlayerReady(String id, boolean ready){
        if (lobbyPlayers.containsKey(id)) {
            playerReadyState.put(id, ready);
            broadcastUpdateLobby();
        }
    }

    public void setMaxPlayers(int max){
        this.maxPlayers = max;
        broadcastUpdateLobby();
    }

    public int getMaxPlayers(){
        return this.maxPlayers;
    }

    /**
     * Renvoie le nombre total de participants au lobby (humains + bots)
     */
    public int getCurrentPlayerCount(){
        return lobbyPlayers.size() + lobbyBots.size();
    }

    public void kickPlayer(String id){ // plus sécu avec l'id que l'username
        for ( ClientHandler client : connectedClient ){
            if (client.getID().equals(id)){
                String username = client.getUsername();
                // Notifier le client concerne avant de fermer la connexion
                client.sendMessage(NetworkProtocolServer.UPDATE_KICKED);
                removeClient(client, id, username);
                client.closeClient();

                String msg = "[SERVEUR] Expulsion du joueur : " + username;
                System.out.println(msg);
                broadcastInfo(msg);

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
        playerReadyState.remove(id);
        String msg = "[SERVEUR] " + username + " a quitté le salon";
        System.out.println(msg);
        broadcastInfo(msg);

        broadcastUpdateLobby();
       }
    }

    /**
     * Pour broadcast des messages non predefini a tous les joueurs
     * @param msg
     */
    public synchronized void broadcastInfo(String msg){

        for (ClientHandler client : connectedClient) {
            client.sendMessage(msg.toString());
        }
    }

    public void startAGame(){
        ArrayList<PlayerData> listPlayers = new ArrayList<>();

        for (String key : lobbyPlayers.keySet()){
            PlayerData tmp = new PlayerData(key, lobbyPlayers.get(key));
            listPlayers.add(tmp);
        }
        // Ajout des bots a la liste des joueurs
        for (String botId : lobbyBots.keySet()){
            String botType = lobbyBots.get(botId);
            PlayerData tmp = new PlayerData(botId, botType, true, botType.toLowerCase());
            listPlayers.add(tmp);
        }
        this.gameManager = new GameManager(frame, listPlayers, modeAction);
        frame.setCurrentGame(gameManager);
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
        int totalCount = lobbyPlayers.size() + lobbyBots.size();
        // Format : [LOBBY_UPDATE]:totalCount:maxPlayers:id:name:isBot(0/1):botType:ready(0/1):...
        StringBuilder message = new StringBuilder("[LOBBY_UPDATE]:" + totalCount + ":" + maxPlayers);

        for (String key : lobbyPlayers.keySet()){
            boolean ready = playerReadyState.getOrDefault(key, false);
            message.append(":").append(key)
                   .append(":").append(lobbyPlayers.get(key))
                   .append(":").append("0")
                   .append(":").append("N")
                   .append(":").append(ready ? "1" : "0");
        }
        for (String botId : lobbyBots.keySet()){
            String botType = lobbyBots.get(botId);
            message.append(":").append(botId)
                   .append(":").append(botType)
                   .append(":").append("1")
                   .append(":").append(botType)
                   .append(":").append("1"); // bots tjrs prets
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
            client.sendMessage(message);
        }
    }

    @Override
    public synchronized void broadcastTurnChanged(String id){
        String message = NetworkProtocolServer.UPDATE_TURN_CHANGED + ":" + id;

        for (ClientHandler client : connectedClient) {
            client.sendMessage(message);
        }
    }

    @Override
    public synchronized void broadcastUpdateDiscardTaken(String id){
        String message = NetworkProtocolServer.UPDATE_DISCARD_TAKEN + ":" + id;

        for (ClientHandler client : connectedClient) {
            client.sendMessage(message);
        }
    }

    @Override
    public void broadcastUpdatePiocheTaken(String id, int numberCard){
        String message = NetworkProtocolServer.UPDATE_PIOCHE_TAKEN + ":" + id + ":" + numberCard;

        for (ClientHandler client : connectedClient) {
            client.sendMessage(message);
        }
    }


    public GameManager getGameManager(){
        return this.gameManager;
    }

    

    // second test client simultané ( faut un host compatible )
    /*
    
    public static void main(String[] args) {
        PlayerData player = new PlayerData("9846220");

        GameClient gameClient = new GameClient(player); // ancien gameclient

        gameClient.connect("localhost", 1234);
    }
    */
}
