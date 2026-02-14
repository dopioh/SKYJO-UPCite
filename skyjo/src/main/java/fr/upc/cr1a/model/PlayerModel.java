package fr.upc.cr1a.model;

import java.util.ArrayList;

/**
 * Modèle représentant un joueur
 * 
 * @author Billy
 */

public class PlayerModel {

    private final String name; // id unique qui permettra de retrouver le player dans la db
    private PlayerHandModel hand;
    private int totalScore;
    
    
    //constructeur
    public PlayerModel(PlayerData playerData){
        this.name = playerData.getID();
        this.hand = new PlayerHandModel();
        this.totalScore = 0;
    }
    
    public String getName(){
        return name;
    }

    public PlayerHandModel getHand(){
        return hand;
    }

    public int getTotalScore(){
        return totalScore;
    }

    public void addScore(int score){
        this.totalScore += score;
    }
    public void resetHand(){
        this.hand = new PlayerHandModel();
    }

    public static PlayerModel getPlayerModel(int nbr, ArrayList<PlayerModel> listPlayerModel){
        return listPlayerModel.get(nbr);
    }
    
}
