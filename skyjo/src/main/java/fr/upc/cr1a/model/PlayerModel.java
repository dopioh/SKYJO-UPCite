package fr.upc.cr1a.model;

import java.util.ArrayList;

import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.player.PlayerData;

/**
 * Modèle représentant un joueur
 * 
 * @author Billy
 */

public class PlayerModel {

    private final String name; // nom du joueur
    //private final String id; // id unique qui permettra de retrouver le player dans la db
    private PlayerHandModel hand;
    private ArrayList<ActionType> actionHand;
    private int totalScore;

    private PlayerData playerData;
    
    
    //constructeur
    public PlayerModel(PlayerData playerData){
        this.playerData = playerData;
        this.name = playerData.getUsername();
        //this.id = playerData.getID();
        this.hand = new PlayerHandModel();
        this.actionHand = new ArrayList<>();;
        this.totalScore = 0;
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

    public PlayerData getPlayerData(){
        return this.playerData;
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

    public void addScore(int score){
        this.totalScore += score;
    }

    /**
     *calcule le score de la manche avec les cartes de la main et les cartes actions
     * @return le score total de la manche avec les actions
     */
    public int calculateRoundScore(){
        int handScore = hand.calculateScore();
        int actionScore = actionHand.size() * 10; // +10 pts par carte action
        return handScore + actionScore;
    }

    public void resetHand(){
        this.hand = new PlayerHandModel();
        this.actionHand.clear(); 
    }

    public static PlayerModel getPlayerModel(int nbr, ArrayList<PlayerModel> listPlayerModel){
        return listPlayerModel.get(nbr);
    }
    
}
