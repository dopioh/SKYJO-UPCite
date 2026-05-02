package fr.upc.cr1a.model;

import java.util.ArrayList;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameBoardView;
import fr.upc.cr1a.view.PlayerPreviewPanel;
import fr.upc.cr1a.controller.network.Host;

/**
 * Gere la logique du Plateau de jeu
 * 
 * @see GameBoardView pour la vue graphique
 * @see GameBoardController pour la gestion des cliques/actions
 * 
 * @author Mei
 * @author Ian
 */
public class GameBoardModel {

    private GameBoardController gameBoardController;
    private GameBoardView gameBoardView;
    private ArrayList<PlayerModel> listPlayer;
    private Frame frame; // pour revenir au menu
    private GameManager gameManager;
    private PlayerPreviewPanel playerPreviewPanel;
    
    public GameBoardModel(ArrayList<PlayerModel> listPlayerModels, Frame frame, GameManager gameManager){
        this.listPlayer = listPlayerModels;
        this.frame = frame;
        this.gameManager = gameManager;
        this.gameBoardView = new GameBoardView(listPlayerModels);
        this.playerPreviewPanel = gameBoardView.getPlayerPreviewPanel();
        this.gameBoardController = new GameBoardController(this, gameBoardView, gameManager);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public GameBoardView getGameBoardView() {
        return gameBoardView;
    }

    public GameBoardController getGameBoardController(){
        return gameBoardController;
    }

    /**
     * Methode called quand on appuie ou survol un bouton joueur
     * @param nbr position du player dans l'ArrayList Button/Player
     */
    public void buttonPlayerCalled(int nbr){
        System.out.println("Button Called " + nbr );
        PlayerModel tmp = PlayerModel.getPlayerModel(nbr, listPlayer);
        playerPreviewPanel.showOtherPlayerHand(tmp);
    }

    public Frame getFrame() {
        return frame;
    }

    public void retourMenuCalled(){
        System.out.println("Retour au menu");
        if (frame != null) {
            // Si host en multi : prevenir les clients + fermer le serveur proprement
            if (GameSession.status == GameSession.STATUS.HOST
                && frame.getLobbyRoomController() != null
                && frame.getLobbyRoomController().getHost() != null) {
                Host host = frame.getLobbyRoomController().getHost();
                if (host.getGameHost() != null) {
                    host.getGameHost().broadcastGameEnded(null);
                }
                // Laisser le temps aux clients de recevoir GAME_ENDED avant de fermer les sockets,
                // sinon la fermeture brutale peut couper le message TCP en transit.
                Host hostRef = host;
                new Thread(() -> {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    hostRef.quit();
                }, "host-delayed-quit").start();
                GameSession.status = GameSession.STATUS.OFFLINE;
            } else if (GameSession.status == GameSession.STATUS.CLIENT
                && frame.getLobbyRoomController() != null
                && frame.getLobbyRoomController().getClient() != null) {
                // Cote client : se deconnecter proprement
                frame.getLobbyRoomController().getClient().disconnect();
                GameSession.status = GameSession.STATUS.OFFLINE;
            }

            GameManager  game = frame.getCurrentGame();
            if (game != null) {
                game.stopGame();
            }
            frame.clearCurrentGame();
            // Nettoyer les references reseau du LobbyRoomController pour permettre une nouvelle partie
            if (frame.getLobbyRoomController() != null) {
                frame.getLobbyRoomController().setNetworkHost(null);
                frame.getLobbyRoomController().setNetworkClient(null);
            }
            frame.showPanel("MENU");
        }
    }
    
}
