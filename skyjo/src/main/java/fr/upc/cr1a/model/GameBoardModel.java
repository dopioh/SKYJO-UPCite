package fr.upc.cr1a.model;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.view.GameBoardView;

public class GameBoardModel {

    private GameBoardController gameBoardController;
    private GameBoardView gameBoardView;
    
    public GameBoardModel(){
        this.gameBoardView = new GameBoardView();
        this.gameBoardController = new GameBoardController(this, gameBoardView);
    }

    public GameBoardView getGameBoardView() {
        return gameBoardView;
    }
    
}
