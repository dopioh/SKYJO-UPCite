package fr.upc.cr1a.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.view.GameBoardView;

public class GameBoardModel {

    private GameBoardController gameBoardController;
    private GameBoardView gameBoardView;
    
    public GameBoardModel(ArrayList<PlayerModel> listPlayerModels ){
        this.gameBoardView = new GameBoardView(listPlayerModels);
        this.gameBoardController = new GameBoardController(this, gameBoardView);
    }

    public GameBoardView getGameBoardView() {
        return gameBoardView;
    }

    /**
     * @param
     */
    public void buttonPlayerCalled(int nbr){
        System.out.println("Button Called " + nbr );
        // va appeler une fonction dans la view selon le numero de bouton appuyé
        // pour print la futurre main du joueur
    }
    
}
