package fr.upc.cr1a.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameBoardView;

public class GameBoardModel {

    private GameBoardController gameBoardController;
    private GameBoardView gameBoardView;
    private ArrayList<PlayerModel> listPlayer;
    private Frame frame; // pour revenir au menu
    
    public GameBoardModel(ArrayList<PlayerModel> listPlayerModels, Frame frame){
        this.listPlayer = listPlayerModels;
        this.frame = frame;
        this.gameBoardView = new GameBoardView(listPlayerModels);
        this.gameBoardController = new GameBoardController(this, gameBoardView);
    }

    public GameBoardView getGameBoardView() {
        return gameBoardView;
    }

    /**
     * Quand on appuie sur un bouton joueur, on affiche ses cartes
     * @param nbr le numero du joueur (commence a 1)
     */
    public void buttonPlayerCalled(int nbr){
        System.out.println("Button Called " + nbr );
        if (nbr >= 1 && nbr <= listPlayer.size()) {
            PlayerModel player = listPlayer.get(nbr - 1);
            gameBoardView.showPlayerCards(player);
        }
    }

    public void retourMenuCalled(){
        System.out.println("Retour au menu");
        if (frame != null) {
            frame.showPanel("MENU");
        }
    }
    
}
