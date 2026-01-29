package fr.upc.cr1a.model;

/**
 * Modèle représentant un joueur
 */

public class PlayerModel {

    private String name;
    private PlayerHandModel hand;
    private int totalScore;
    
    
    //constructeur
    public PlayerModel(String name){
        this.name = name;
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
    
}
