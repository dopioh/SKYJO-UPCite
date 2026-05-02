package fr.upc.cr1a.model;

import fr.upc.cr1a.model.interfaces.BotStrategyInterface;
import fr.upc.cr1a.model.player.PlayerData;

/**
 * Modèle qui représente un bot
 */

public class BotModel extends PlayerModel  {

    private final String name; // id unique qui permettra de retrouver le player dans la db
    private BotHandModel hand;
    private int totalScore;
    private BotStrategyInterface strategy; // stratégie de jeu du bot
    
    
    //constructeur
    public BotModel(PlayerData playerData){
        super(playerData);
        this.name = playerData.getUsername() != null ? playerData.getUsername() : playerData.getID();
        this.hand = new BotHandModel();
        this.totalScore = 0;
        this.strategy = null; // stratégie par défaut
    }
    
    //constructeur avec stratégie personnalisée
    public BotModel(PlayerData playerData, BotStrategyInterface strategy){
        super(playerData);
        this.name = playerData.getUsername() != null ? playerData.getUsername() : playerData.getID();
        this.hand = new BotHandModel();
        this.totalScore = 0;
        this.strategy = strategy;
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
    
    public BotStrategyInterface getStrategy(){
        return strategy;
    }
    
    public void setStrategy(BotStrategyInterface strategy){
        this.strategy = strategy;
    }

    public void addScore(int score){
        this.totalScore += score;
    }
    public void resetHand(){
        this.hand = new BotHandModel();
    }
    
}
