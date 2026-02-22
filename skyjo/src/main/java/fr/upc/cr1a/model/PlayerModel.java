package fr.upc.cr1a.model;

import java.util.ArrayList;

import fr.upc.cr1a.model.ActionCardModel.ActionType;

/**
 * Modèle représentant un joueur
 * 
 * @author Billy
 */

public class PlayerModel {

    private final String name; // id unique qui permettra de retrouver le player dans la db
    private PlayerHandModel hand;
    private ArrayList<ActionType> actionHand;
    private int totalScore;
    private int starsScore; // le nombre de carte etoile dans la main du joueur
    
    
    //constructeur
    public PlayerModel(PlayerData playerData){
        this.name = playerData.getID();
        this.hand = new PlayerHandModel();
        this.actionHand = new ArrayList<>();;
        this.totalScore = 0;
        this.starsScore = 0;
    }
    
    public String getName(){
        return name;
    }

    public PlayerHandModel getHand(){
        return hand;
    }

    public ArrayList<ActionType> getActionHand() {
        return this.actionHand;
    }

    public String showActionHand() {
        String res = "";
        for (ActionType type : this.actionHand) {
            res+=type;
            res+=" ";
        }
        return res;
    }

    public int getTotalScore(){
        return totalScore;
    }

    public int getStarsScore(){
        return starsScore;
    }

    public void addStarsScore(){
        this.starsScore++;
    }

    public void subStarsScore(){
        this.starsScore--;
    }

    public void addScore(int score){
        this.totalScore += score;
    }

    public void resetHand(){
        this.hand = new PlayerHandModel();
        this.actionHand.clear(); 
    }

    public static PlayerModel getPlayerModel(int nbr, ArrayList<PlayerModel> listPlayerModel){
        return listPlayerModel.get(nbr);
    }
    
}
