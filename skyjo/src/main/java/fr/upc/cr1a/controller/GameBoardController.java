package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.GameBoardModel;
import fr.upc.cr1a.view.GameBoardView;

public class GameBoardController {

    GameBoardModel gameBoardModel;
    GameBoardView gameBoardView;

    /**
     * Initialise la view et le Model du GameBoard
     */
    public GameBoardController(GameBoardModel gameBoardModel, GameBoardView gameBoardView){
        this.gameBoardModel = gameBoardModel;
        this.gameBoardView = gameBoardView;
    }
        
}
