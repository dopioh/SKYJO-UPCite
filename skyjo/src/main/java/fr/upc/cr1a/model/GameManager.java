package fr.upc.cr1a.model;

import java.util.List;

/**
 * Classe principal pour initialiser une partie (et pas le menu !)
 * Et relier toutes vos features in game au jeu 
 * 
 * Faites vos propres fichiers pour vos features !!
 * Qu'on se retrouve pas encore avec 2k lignes !! // oui patronne 
 */
public class GameManager {


    private List<PlayerModel> players;
    private PiocheModel deck;
    private DefausseModel discardPile;
    private int currentPlayerIndex;
    private CardModel currentDrawnCard;
    
    /**
     * Constructeur qui doit etre appeler pour lancer une partie
     */
    public GameManager(){

    }

    public GameManager(List<String> playersNames){
        //initialiser les joueurs
        for (String name : playersNames) {
            players.add(new PlayerModel(name));
        }

        currentPlayerIndex = 0;
        deck = new PiocheModel();
        discardPile = new DefausseModel();
        currentDrawnCard = null;
    }

    /*
    *classe local enum pour gérer les états du jeu
    */
    public enum GameState {
        WAITING_FOR_PLAYERS,    // attende des joueurs
        IS_PLAYING,             // en train de jouer
        LAST_ROUND,             // dernier tour (un joueur a révélé toutes ses cartes)
        ROUND_ENDED,            // manche terminée
        GAME_ENDED              // jeu terminé (un joueur atteint 100 points)
    }

    public enum TurnPhase {
        DRAW_DECISION,              // joueur doit choisir ; pioche ou défausse
        CARD_DRAW_FROM_DECK,        // carte piochée du deck ; peut échanger ou défausser
        CARD_TAKE_FROM_DISCARD,     // carte prise de la défausse ; doit échanger
        TURN_ENDED                  // tour terminé
}

}
