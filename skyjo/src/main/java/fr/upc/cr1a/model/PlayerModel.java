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

    /**
     * Stocke les détails du score de la dernière manche
     */
    public static class RoundScore {
        public int handScore;
        public int actionPenalty;
        public int totalAdded;
        public boolean wasDoubled;  // Indique si le score a été doublé

        public RoundScore(int handScore, int actionPenalty, int totalAdded) {
            this.handScore = handScore;
            this.actionPenalty = actionPenalty;
            this.totalAdded = totalAdded;
            this.wasDoubled = false;
        }

        public RoundScore(int handScore, int actionPenalty, int totalAdded, boolean wasDoubled) {
            this.handScore = handScore;
            this.actionPenalty = actionPenalty;
            this.totalAdded = totalAdded;
            this.wasDoubled = wasDoubled;
        }
    }

    private final String name; // id unique qui permettra de retrouver le player dans la db
    private PlayerHandModel hand;
    private ArrayList<ActionType> actionHand;
    private int totalScore;
    private RoundScore lastRoundScore; //détails de la dernière manche
    
    
    //constructeur
    public PlayerModel(PlayerData playerData){
        this.name = playerData.getID();
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

    public void resetHand(){
        this.hand = new PlayerHandModel();
        this.actionHand.clear();
    }

    public RoundScore getLastRoundScore() {
        return lastRoundScore;
    }

    public void setLastRoundScore(RoundScore score) {
        this.lastRoundScore = score;
    }

    public static PlayerModel getPlayerModel(int nbr, ArrayList<PlayerModel> listPlayerModel){
        return listPlayerModel.get(nbr);
    }
    
}
