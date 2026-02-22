package fr.upc.cr1a.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
*Modèle pour le calcul des scores
*@author Billy
*/

public class ScoreCalculator {

    /**
     * calcule le score total d'une main de joueur
     * @param hand La main du joueur
     * @return Le score total de la main
     */
    public static int calculateHandScore(PlayerHandModel hand){
        int score = 0;
        for (int i = 0; i < hand.getRows(); i++) {
            for (int j = 0; j < hand.getColumns(); j++) {
                CardModel card = hand.getCard(i, j);
                if (card != null && card.isFaceDown()) {
                    score += card.getCardValue();
                }
            }
        }
        return score;
    }

    /**
     *calcule les scores de tous les joueurs pour la manche
     *@param players Liste des joueurs
     *@return Map associant chaque joueur à son score de manche
     */
    public static Map<PlayerModel, Integer> calculateRoundScores(List<PlayerModel> players){
        Map<PlayerModel, Integer> scores = new HashMap<>();
        
        for (PlayerModel player : players) {
            int score = calculateHandScore(player.getHand());
            scores.put(player, score);
        }
        
        return scores;
    }
    
    /**
     * cherche le joueur avec le score le plus bas
     * @param players Liste des joueurs
     * @return Le joueur avec le score le plus bas
     */
    public static PlayerModel getLowestScorePlayer(List<PlayerModel> players){
        if (players == null || players.isEmpty()) {
            return null;
        }
        
        PlayerModel lowestPlayer = players.get(0);
        int lowestScore = calculateHandScore(lowestPlayer.getHand());
        
        for (int i = 1; i < players.size(); i++) {
            PlayerModel player = players.get(i);
            int score = calculateHandScore(player.getHand());
            
            if (score < lowestScore) {
                lowestScore = score;
                lowestPlayer = player;
            }
        }
        
        return lowestPlayer;
    }
    
    /**
     * regle qui dit que si le joueur qui active la fin de la manche a le score le plus élevé, double son score de la manche
     * @param roundEndPlayer Le joueur qui a déclenché la fin de manche
     * @param Players liste des joueurs
     */
    public static void applyEndGamePenalty(PlayerModel roundEndPlayer, List<PlayerModel> Players){
        if (roundEndPlayer == null || Players == null || Players.isEmpty()) {
            return;
        }
        //calculer le score du joueur qui a terminé
        int roundEndPlayerScore = calculateHandScore(roundEndPlayer.getHand());
        //regarde s'il a le score le plus élevé
        boolean hasHighestScore = true;
        for (PlayerModel player : Players) {
            if (player != roundEndPlayer) {
                int playerScore = calculateHandScore(player.getHand());
                if (playerScore > roundEndPlayerScore) {
                    hasHighestScore = false;
                    break;
                }
            }
        }
        //si le joueur qui termine a le score le plus élevé, doubler son score
        if (hasHighestScore) {
            roundEndPlayer.addScore(roundEndPlayerScore); // Ajoute une fois de plus pour doubler
        }
    }
}
