package fr.upc.cr1a.model;

import java.util.ArrayList;

/*
*Modèle définissant les règles du jeu Skyjo
*Aide au suivie des regles pendant une game et donc moins de code dans le gameManager (Happy MEI ?)
*@author Billy
*/

public class GameRuleModel {
    public static final int INITIAL_REVEALED_CARD = 2;
    public static final int WINNING_SCORE = 100;
    public static final int COLUMN_SIZE = 4;
    public static final int ROWNS_SIZE = 3;
    
//Regarde si la pile de défausse est vide
    public static boolean canTakeFromDiscard(DiscardPile discard){
        return (!discard.isEmpty());
    }

    //regarde si le joueur peut piocher une carte
    public static boolean canDrawFromDeck(Deck deck){
        return (!deck.isEmpty());
    }

    //regarde si le joueur doit échanger une carte
    public static boolean mustExchangeCard(GameManager.TurnPhase phase){
        return (phase == GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD);
    }

    //regarde si le joueur peut défausser une carte
    public static boolean canDiscardCard(GameManager.TurnPhase phase){
        return (phase == GameManager.TurnPhase.CARD_DRAW_FROM_DECK);
    }

    //fin de partie 
    public static boolean isGameOver(ArrayList<PlayerModel> players){
        for (PlayerModel player : players){
            if (player.getTotalScore() >= WINNING_SCORE){
                return true;
            }
        }
        return false;
    }

    //donne le joueur qui a le moins de points
    public static PlayerModel determineWinner(ArrayList<PlayerModel> players){
        PlayerModel winner = players.get(0);
        for (PlayerModel player : players){
            if (player.getTotalScore() < winner.getTotalScore()){
                winner = player;
            }
        }
        return winner;
    }

}