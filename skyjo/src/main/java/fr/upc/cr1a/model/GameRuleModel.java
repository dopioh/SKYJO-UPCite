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
    public static boolean canTakeFromDiscard(DefausseModel discard){
        return (!discard.isEmpty());
    }

    //regarde si le joueur peut piocher une carte
    public static boolean canDrawFromDeck(PiocheModel deck){
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

    /**
     * Retire les colonnes avec 3 cartes identiques
     * @param hand la main du joueur
     * @param discardPile la pile de défausse où les cartes seront placées
     */
    public static void removeIdenticalColumns(PlayerHandModel hand, DefausseModel discardPile){
        removeIdenticalColumns(hand, discardPile, null);
    }

    public static void removeIdenticalColumns(PlayerHandModel hand, DefausseModel discardPile, PlayerModel joueur){
        for (int col = 0; col < hand.getColumns(); col++) {
            if (isWildcardColumn(hand, col)) {
                for (int row = 0; row < hand.getRows(); row++) {
                    CardModel removedCard = hand.getCard(row, col);
                    discardPile.addCardAtTop(removedCard);
                    hand.replaceCard(row, col, null);
                }
            }
        }
    }

    /**
     * Retire les lignes avec 4 cartes identiques
     * @param hand la main du joueur
     * @param discardPile la pile de défausse où les cartes seront placées
     */
    public static void removeIdenticalRows(PlayerHandModel hand, DefausseModel discardPile){
        removeIdenticalRows(hand, discardPile, null);
    }

    public static void removeIdenticalRows(PlayerHandModel hand, DefausseModel discardPile, PlayerModel joueur){
        for (int row = 0; row < hand.getRows(); row++) {
            if (isWildcardRow(hand, row)) {
                for (int col = 0; col < hand.getColumns(); col++) {
                    CardModel removedCard = hand.getCard(row, col);
                    discardPile.addCardAtTop(removedCard);
                    hand.replaceCard(row, col, null);
                }
            }
        }
    }

    /**
     * check si une colonne est composée des memes cartes
     * avec la règle joker pour les étoiles
     */
    private static boolean isWildcardColumn(PlayerHandModel hand, int col) {
        Integer targetValue = null;

        for (int row = 0; row < hand.getRows(); row++) {
            CardModel card = hand.getCard(row, col);
            if (card == null || card.isFaceDown()) {
                return false;
            }
            if (GameManager.isStarCard(card)) {
                continue;
            }

            if (targetValue == null) {
                targetValue = card.getCardValue();
            } else if (card.getCardValue() != targetValue) {
                return false;
            }
        }

        return true;
    }

    /**
     * check si la ligne est composée des memes cartes
     * avec la règle joker pour les étoiles
     */
    private static boolean isWildcardRow(PlayerHandModel hand, int row) {
        Integer targetValue = null;

        for (int col = 0; col < hand.getColumns(); col++) {
            CardModel card = hand.getCard(row, col);
            if (card == null || card.isFaceDown()) {
                return false;
            }
            if (GameManager.isStarCard(card)) {
                continue;
            }

            if (targetValue == null) {
                targetValue = card.getCardValue();
            } else if (card.getCardValue() != targetValue) {
                return false;
            }
        }

        return true;
    }

    /**
     * Vérifie si un joueur a toutes ses cartes révélées
     * @param players la liste des joueurs
     * @return true si au moins un joueur a toutes ses cartes révélées
     */
    public static boolean checkEndOfRound(ArrayList<PlayerModel> players) {
        for (PlayerModel player : players) {
            if (player.getHand().allCardsRevealed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Détermine le joueur avec le plus grand score (pour commencer la manche suivante)
     * @param players la liste des joueurs
     * @return l'index du joueur avec le plus grand score
     */
    public static int getBiggestScorePlayerIndex(ArrayList<PlayerModel> players) {
        int maxScore = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            int score = players.get(i).getTotalScore();
            if (score > maxScore) {
                maxScore = score;
                index = i;
            }
        }
        return index;
    }

    /**
     * Détermine le premier joueur à jouer en fonction des cartes visibles
     * @param players la liste des joueurs
     * @return l'index du joueur avec le plus grand score de cartes visibles
     */
    public static int getFirstPlayerIndex(ArrayList<PlayerModel> players) {
        int maxScore = Integer.MIN_VALUE;
        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            int visibleScore = players.get(i).getHand().getVisibleCardScore();
            if (visibleScore > maxScore) {
                maxScore = visibleScore;
                index = i;
            }
        }
        return index;
    }

    /**
     * Trouve le joueur qui a lancé la fin de la manche (toutes ses cartes révélées)
     * @param players la liste des joueurs
     * @return le joueur qui a toutes ses cartes révélées, ou null
     */
    public static PlayerModel findRoundEndPlayer(ArrayList<PlayerModel> players) {
        for (PlayerModel player : players) {
            if (player.getHand().allCardsRevealed()) {
                return player;
            }
        }
        return null;
    }

    /**
     * Trouve le joueur avec le score de manche le plus bas
     * @param players la liste des joueurs
     * @return le joueur avec le score le plus bas
     */
    public static PlayerModel findLowestScorePlayer(ArrayList<PlayerModel> players) {
        PlayerModel lowestPlayer = null;
        int lowestScore = Integer.MAX_VALUE;
        
        for (PlayerModel player : players) {
            int score = player.calculateRoundScore();
            if (score < lowestScore) {
                lowestScore = score;
                lowestPlayer = player;
            }
        }
        return lowestPlayer;
    }

    /**
     * Applique les scores de fin de manche selon les règles Skyjo
     * Si le joueur qui termine n'a pas le meilleur score, son score est doublé
     * @param players la liste des joueurs
     * @param roundEndPlayer le joueur qui a terminé la manche
     * @param lowestPlayer le joueur avec le score le plus bas
     */
    public static void applyRoundScores(ArrayList<PlayerModel> players, PlayerModel roundEndPlayer, PlayerModel lowestPlayer) {
        int roundEndPlayerScore = 0;
        // Score effectif (apres doublement eventuel) pour stocker dans l'historique
        java.util.HashMap<PlayerModel, Integer> effectiveRoundScore = new java.util.HashMap<>();

        //applique le score à tout le monde
        for (PlayerModel player : players) {
            int score = player.calculateRoundScore();
            player.addScore(score);
            effectiveRoundScore.put(player, score);

            //stocker le score du joueur qui a déclenché LAST_ROUND
            if (player == roundEndPlayer) {
                roundEndPlayerScore = score;
            }
        }

        //double le score du joueur qui a lancé la fin de la manche s'il n'a pas le meilleur score
        if (roundEndPlayer != null && lowestPlayer != null && roundEndPlayer != lowestPlayer) {
            roundEndPlayer.addScore(roundEndPlayerScore);
            effectiveRoundScore.put(roundEndPlayer, roundEndPlayerScore * 2);
            System.out.println("[SCORE] " + roundEndPlayer.getName() + " a ses points doubles : +" + roundEndPlayerScore + " x2");
        }

        // Historique par manche (utilise pour le panel Tab)
        for (PlayerModel player : players) {
            player.recordRoundScore(effectiveRoundScore.getOrDefault(player, 0));
        }
    }

}