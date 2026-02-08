package fr.upc.cr1a.model;

import java.util.ArrayList;

import fr.upc.cr1a.controller.GameBoardController;
import fr.upc.cr1a.view.Frame;
import fr.upc.cr1a.view.GameBoardView;

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
     * Methode called quand on appuie ou survol un bouton joueur
     * @param nbr position du player dans l'ArrayList Button/Player
     */
    public void buttonPlayerCalled(int nbr){
        System.out.println("Button Called " + nbr );
        // desactivé pour l'instant pour pas changer de main (à droite) au clic
        // if (nbr >= 1 && nbr <= listPlayer.size()) {
        //     PlayerModel player = listPlayer.get(nbr - 1);
        //     gameBoardView.showPlayerCards(player);
        // }
    }

    public void retourMenuCalled(){
        System.out.println("Retour au menu");
        if (frame != null) {
            frame.showPanel("MENU");
        }
    }
    
}
