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
import fr.upc.cr1a.model.CardModel;
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
    private boolean wasKicked = false;

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

                        // botType en lowercase pour matcher les strats dans GameManager
                        PlayerData tmp = new PlayerData(key, name, isBot, isBot ? botType.toLowerCase() : null);
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

                if (message.startsWith(NetworkProtocolServer.UPDATE_KICKED)){
                    wasKicked = true;
                    SwingUtilities.invokeLater(() -> {javax.swing.JOptionPane.showMessageDialog(frame,
                            "Vous avez ete expulsé du salon par l'hôte.",
                            "Kick",
                            javax.swing.JOptionPane.WARNING_MESSAGE
                        );
                        frame.showPanel("LOBBY_BROWSER");
                        // Lancer une recherche de lobby apres avoir ete kick
                        if (frame.getLobbyBrowserController() != null) {
                            frame.getLobbyBrowserController().loadLobbies();
                        }
                    });
                    break;
                }

                // NetworkProtocol du serveur qui vont definir des actions pour chaque client

                if (message.startsWith(NetworkProtocolServer.UPDATE_START_GAME)){
                    System.out.println("[SYSTEM] message start with \""+NetworkProtocolServer.UPDATE_START_GAME+"\"");
                    // [UPDATE_START_GAME:seed] pour éviter plusieurs decks de cartes différents sur les appareils 
                    String[] sgParts = message.split(":");
                    long seed = 0L;
                    boolean hasSeed = false;
                    if (sgParts.length >= 2) {
                        try { seed = Long.parseLong(sgParts[1]); hasSeed = true; }
                        catch (NumberFormatException e) {}
                    }
                    final long finalSeed = seed;
                    final boolean finalHasSeed = hasSeed;
                    // Seuls les CLIENTS créent le GameManager ici ; le HOST le crée via GameHost.startAGame()
                    if (GameSession.status != GameSession.STATUS.HOST) {
                        SwingUtilities.invokeLater(() -> {GameManager game = finalHasSeed
                                ? new GameManager(this.frame, this.listPlayersData, this.modeAction, finalSeed)
                                : new GameManager(this.frame, this.listPlayersData, this.modeAction);
                            game.setNetwork(this);
                            game.setLocalPlayer(fr.upc.cr1a.model.player.PlayerAccount.actuelAccount);
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
                        if (GameSession.status != GameSession.STATUS.HOST) {
                            game.forceRevealCardFromNetwork(id, col, row, valeur);
                        } else {
                            game.animateRevealFromNetwork(id, row, col);
                        }
                    }
                }

                if(message.startsWith(NetworkProtocolServer.UPDATE_TURN_CHANGED)){
                    String[] parts = message.split(":");
                    if (parts.length >= 2) {
                        String idJoueurActif = parts[1];
                        String phaseName = (parts.length >= 3) ? parts[2] : null;
                        GameManager game = frame.getCurrentGame();
                        if (game != null) {
                            game.forceTurnFromNetwork(idJoueurActif, phaseName);
                        }
                    }
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_DISCARD_TAKEN)) {
                    //String[] parts = message.split(":");
                    //String playerId = parts[1];
                    GameManager game = frame.getCurrentGame();
                    if (game != null) {
                        if (GameSession.status != GameSession.STATUS.HOST) {
                            game.takeDiscard();
                        }

                        if (game.getGameBoardModel() != null) {
                            SwingUtilities.invokeLater(() -> {
                                GameBoardController c = game.getGameBoardModel().getGameBoardController();
                                GameBoardView v = game.getGameBoardModel().getGameBoardView();
                                c.updateAllDecksDisplay();
                                c.refreshGuide();
                                // Seul le joueur actif voit la carte sous le curseur
                                if (game.isLocalPlayerTurn() && v != null) {
                                    CardModel drawn = game.getCurrentDrawCard();
                                    if (drawn != null) {
                                        v.showCursorCard(v.getCardImagePath(drawn));
                                    }
                                }
                            });
                        }
                    }
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_EXCHANGE)) {
                    //[EXCHANGE:playerId:col:row:newValue:discardedValue]
                    String[] parts = message.split(":");
                    if (parts.length >= 6) {
                        String id = parts[1];
                        int col = Integer.parseInt(parts[2]);
                        int row = Integer.parseInt(parts[3]);
                        int newValue = Integer.parseInt(parts[4]);
                        int discardedValue = Integer.parseInt(parts[5]);

                        GameManager game = frame.getCurrentGame();
                        if (game != null) {
                            if (GameSession.status != GameSession.STATUS.HOST) {
                                game.forceExchangeFromNetwork(id, col, row, newValue, discardedValue);
                            }
                            if (game.getGameBoardModel() != null) {
                                SwingUtilities.invokeLater(() -> {
                                    GameBoardController c = game.getGameBoardModel().getGameBoardController();
                                    GameBoardView v = game.getGameBoardModel().getGameBoardView();
                                    c.updateAllDecksDisplay();
                                    c.refreshGuide();
                                    if (v != null) {
                                        v.hideCursorCard();
                                        v.showPlayerCards(game.getCurrentPlayer());
                                    }
                                });
                            }
                        }
                    }
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_DISCARD_DRAW)) {
                    // Le joueur actif a jete la carte piochee
                    GameManager game = frame.getCurrentGame();
                    if (game != null) {
                        // Le host a deja applique via ClientHandler
                        if (GameSession.status != GameSession.STATUS.HOST) {
                            game.discardDrawCard();
                        }
                        if (game.getGameBoardModel() != null) {
                            SwingUtilities.invokeLater(() -> {
                                GameBoardController c = game.getGameBoardModel().getGameBoardController();
                                GameBoardView v = game.getGameBoardModel().getGameBoardView();
                                c.updateAllDecksDisplay();
                                c.refreshGuide();
                                if (v != null) {
                                    v.hideCursorCard();
                                }
                            });
                        }
                    }
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_ROUND_ENDED)) {
                    //la manche est terminee, on affiche le RoundEndPanel.
                    //  [ROUND_ENDED:lastRoundPlayerId]
                    String[] reParts = message.split(":");
                    final String lrpId = (reParts.length >= 2 && !reParts[1].isEmpty()) ? reParts[1] : null;
                    SwingUtilities.invokeLater(() -> {
                        GameManager game = frame.getCurrentGame();
                        if (game == null) return;
                        if (game.getGameBoardModel() == null|| game.getGameBoardModel().getGameBoardController() == null) return;
                        game.finalizeRoundFromNetwork(lrpId);
                        game.getGameBoardModel().getGameBoardController().showRoundEndScreenFromLoop();
                    });
                    continue;
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_NEW_ROUND)) {
                    String[] nrParts = message.split(":");
                    long seed = 0L;
                    if (nrParts.length >= 2) {
                        try { seed = Long.parseLong(nrParts[1]); } catch (NumberFormatException ignored) {}
                    }
                    final long finalSeed = seed;
                    SwingUtilities.invokeLater(() -> {
                        GameManager game = frame.getCurrentGame();
                        if (game == null) return;
                        if (game.getGameBoardModel() == null
                            || game.getGameBoardModel().getGameBoardController() == null) return;
                        game.getGameBoardModel().getGameBoardController().startLocalNewRound(finalSeed);
                    });
                    continue;
                }

                // Remplacé par un bot message
                if (message.startsWith(NetworkProtocolServer.UPDATE_AFK_REPLACED)) {
                    //[AFK_REPLACED:playerId]
                    String[] afkParts = message.split(":");
                    final String afkPlayerId = (afkParts.length >= 2) ? afkParts[1] : null;
                    SwingUtilities.invokeLater(() -> {
                        if (afkPlayerId != null&& playerData != null&& afkPlayerId.equals(playerData.getID())) {
                            javax.swing.JOptionPane.showMessageDialog(frame,"AFK : vous êtes remplacé par un bot.","Inactivité",javax.swing.JOptionPane.WARNING_MESSAGE);
                        }
                    });
                    continue;
                }

                if (message.startsWith(NetworkProtocolServer.UPDATE_GAME_ENDED)) {
                    // Format : GAME_ENDED:lastRoundPlayerId (peut etre vide)
                    String[] geParts = message.split(":");
                    final String lrpId = (geParts.length >= 2 && !geParts[1].isEmpty()) ? geParts[1] : null;
                    SwingUtilities.invokeLater(() -> {
                        GameManager game = frame.getCurrentGame();
                        if (game == null|| game.getGameBoardModel() == null|| game.getGameBoardModel().getGameBoardController() == null) {
                            // retour direct au menu si pas de partie active
                            GameSession.status = GameSession.STATUS.OFFLINE;
                            frame.showPanel("MENU");
                            return;
                        }
                        game.finalizeRoundFromNetwork(lrpId);
                        game.setGameStateNetwork(GameManager.GameState.GAME_ENDED);
                        // Afficher RoundEndPanel mais fin de partie au lieu de pouvoir continuer
                        game.getGameBoardModel().getGameBoardController().showRoundEndScreenFromLoop();
                    });
                    break;
                }

                // requete secrete ( broadcast que le client handler concerné normalement)
                if (message.startsWith("UPDATE_DRAWN_CARD")) { // valeur de la pioche
                    String[] parts = message.split(":");
                    int valeur = Integer.parseInt(parts[1]);

                    GameManager game = frame.getCurrentGame();
                    if (game != null) {
                        if (GameSession.status != GameSession.STATUS.HOST) {
                            NumberCardModel vraieCarte = new NumberCardModel(false, valeur);
                            game.setCurrentDrawCard(vraieCarte);
                            // phase de choix entre echanger ou jeter
                            game.setTurnPhase(GameManager.TurnPhase.CARD_DRAW_FROM_DECK);
                        }

                        if (game.getGameBoardModel() != null) {
                            GameBoardView view = game.getGameBoardModel().getGameBoardView();
                            GameBoardController controller = game.getGameBoardModel().getGameBoardController();

                            SwingUtilities.invokeLater(() -> {
                                controller.refreshGuide();
                                CardModel drawn = game.getCurrentDrawCard();
                                if (drawn != null) {
                                    if (drawn.isFaceDown()) drawn.returnCard();
                                    String drawnPath = view.getCardImagePath(drawn);
                                    view.getBtnPioche().flip(() -> view.getBtnPioche().setCardImage(drawnPath));
                                    view.showCursorCard(drawnPath);
                                    view.updateDiscardOnly(
                                        game.getDiscardPile().isEmpty() ? null : game.getDiscardPile().getTopCard()
                                    );
                                }
                            });
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
     * Requete pour jeter la carte piochee sans l'echanger.
     * STRUCTURE = id:protocol
     */
    @Override
    public void sendDiscardDrawRequest(String id){
        if (out != null){
            out.println(id + ":" + NetworkProtocolClient.REQ_DISCARD_DRAW);
        }
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
        
        UDPBroadcaster.broadcast(1234, "SALEMMMM"); Pas de Salem ici

        double deltaTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - deltaTime < 20000 ){} 

        //System.out.println("[SERVEUR] Fermerture du serveur et deconnexion de tous les clients.");
        gm.stopServer();
        
    }
    */
}
