package fr.upc.cr1a.controller;

import javax.swing.JButton;

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

        listener();
    }
        
    public void listener(){
        int s = 1;
        for (JButton jb : gameBoardView.getListPlayerButton()){
            int tmp = s;
            jb.addActionListener(e -> gameBoardModel.buttonPlayerCalled(tmp));
            s++;
        }

        JButton retour = gameBoardView.getBtnRetour();
        retour.addActionListener(e -> gameBoardModel.retourMenuCalled());

        // listeners pour les cartes
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                final int r = row;
                final int c = col;
                gameBoardView.getCardButtons()[row][col].addActionListener(e -> {
                    if (gameBoardView.getCurrentPlayer() != null) {
                        System.out.println(gameBoardView.getCurrentPlayer().getName() + " - Carte [" + r + "][" + c + "]");
                    }
                });
            }
        }
    }
}
