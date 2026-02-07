package fr.upc.cr1a.model;

import java.util.ArrayList;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.view.GameBoardView;

/**
 * Gere la logique du Plateau de jeu
 * 
 * @see GameBoardView pour la vue graphique
 * @see GameBoardController pour la gestion des cliques/actions
 * 
 * @author Mei
 */
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
     * Methode called quand on appuie ou survol un bouton joueur
     * @param nbr position du player dans l'ArrayList Button/Player
     */
    public void buttonPlayerCalled(int nbr){
        System.out.println("Button Called " + nbr );
        // va appeler une fonction dans la view selon le numero de bouton appuyé
        // pour print la future main du joueur


    }
    
}
