package fr.upc.cr1a.model;

import java.util.ArrayList;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameBoardView;
import fr.upc.cr1a.view.PlayerPreviewPanel;

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
            GameManager  game = frame.getCurrentGame();
            if (game != null) {
                game.stopGame();
            }
            frame.clearCurrentGame();
            frame.getMainMenuPanel().resetMenu();
            frame.showPanel("MENU");
        }
    }
    
}
