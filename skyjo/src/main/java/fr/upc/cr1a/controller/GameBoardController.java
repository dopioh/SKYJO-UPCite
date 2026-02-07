package fr.upc.cr1a.controller;

import java.awt.event.*;

import javax.swing.JButton;

import fr.upc.cr1a.model.GameBoardModel;
import fr.upc.cr1a.view.GameBoardView;

/**
 * Controller pour gerer les connections entre la view et le model
 * Du plateau de jeu
 * 
 * @see GameBoardView vue graphique
 * @see GameBoardModel logique du plateau
 * 
 * @author Mei
 */
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
    
    /**
     * listener qui lance un appel lorsque :
     * On clique sur un joueur
     * On survole un joueur
     */
    public void listener(){
        int s = 1;
        for (JButton jb : gameBoardView.getListPlayerButton()){
            int tmp = s;
            jb.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e){
                    gameBoardModel.buttonPlayerCalled(tmp);
                }

                @Override
                public void mouseClicked(MouseEvent e){
                    gameBoardModel.buttonPlayerCalled(tmp);
                }
            });
            s++;
        }
    }
}
