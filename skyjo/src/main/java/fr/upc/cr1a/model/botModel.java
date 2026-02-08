package fr.upc.cr1a.model;

/**
 * Modèle qui représente un bot
 * 
 * @author Billy
 * 
 */

public class BotModel extends PlayerModel  {

    private final String name; // id unique qui permettra de retrouver le player dans la db
    private BotHandModel hand;
    private int totalScore;
    
    
    //constructeur
    public BotModel(PlayerData playerData){
        super(playerData);
        this.name = playerData.getID();
        this.hand = new BotHandModel();
        this.totalScore = 0;
    }
    
    public String getName(){
        return name;
    }

    public BotHandModel getHand(){
        return hand;
    }

    public int getTotalScore(){
        return totalScore;
    }

    public void addScore(int score){
        this.totalScore += score;
    }
    public void resetHand(){
        this.hand = new BotHandModel();
    }
    
}
