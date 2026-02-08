package fr.upc.cr1a.controller;

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
 * @author Ian
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
     * les listeners du plateau :
     * - bouton retour vers le menu
     * - clic sur les cartes affichées
     * 
     * Les boutons joueur a gauche ne changent plus la main affichée mais
     * la fonction buttonPlayerCalled existe toujours dans le model si
     * on a besoin de changer la main affichée a droite
     */
    public void listener(){

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
