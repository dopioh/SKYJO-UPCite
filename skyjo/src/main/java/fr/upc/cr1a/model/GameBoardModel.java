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
    
}
