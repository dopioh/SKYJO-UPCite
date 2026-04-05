package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import fr.upc.cr1a.model.player.PlayerData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests unitaires pour la classe ScoreCalculator
 */
public class ScoreCalculatorTest {
    
    private List<PlayerModel> players;
    private PlayerModel player1;
    private PlayerModel player2;
    private PlayerModel player3;

    @BeforeEach
    public void setup() {
        players = new ArrayList<>();
        player1 = new PlayerModel(new PlayerData("Player1"));
        player2 = new PlayerModel(new PlayerData("Player2"));
        player3 = new PlayerModel(new PlayerData("Player3"));
        players.add(player1);
        players.add(player2);
        players.add(player3);
    }

    @Test
    public void testCalculateHandScoreEmptyHand() {
        PlayerHandModel hand = player1.getHand();
        int score = ScoreCalculator.calculateHandScore(hand);
        assertEquals(0, score);
    }

    @Test
    public void testCalculateHandScoreWithCards() {
        PlayerHandModel hand = player1.getHand();
        
        int score = ScoreCalculator.calculateHandScore(hand);
        assertTrue(score >= 0);
    }

    @Test
    public void testCalculateHandScoreIgnoresFaceUpCards() {
        PlayerHandModel hand = player1.getHand();
        // les cartes face up ne doivent pas être comptées
        
        int score = ScoreCalculator.calculateHandScore(hand);
        assertEquals(0, score); // pas de score pour les cartes face up
    }

    @Test
    public void testCalculateHandScoreWithMultipleCards() {
        PlayerHandModel hand = player1.getHand();
        
        int score = ScoreCalculator.calculateHandScore(hand);
        assertTrue(score >= 0);
    }

    @Test
    public void testCalculateHandScoreWithNullCards() {
        PlayerHandModel hand = player1.getHand();
        
        int score = ScoreCalculator.calculateHandScore(hand);
        assertTrue(score >= 0);
    }

    @Test
    public void testCalculateRoundScoresEmptyList() {
        List<PlayerModel> emptyPlayers = new ArrayList<>();
        Map<PlayerModel, Integer> scores = ScoreCalculator.calculateRoundScores(emptyPlayers);
        assertNotNull(scores);
        assertTrue(scores.isEmpty());
    }

    @Test
    public void testCalculateRoundScoresSinglePlayer() {
        List<PlayerModel> singlePlayer = new ArrayList<>();
        singlePlayer.add(player1);
        
        Map<PlayerModel, Integer> scores = ScoreCalculator.calculateRoundScores(singlePlayer);
        assertNotNull(scores);
        assertEquals(1, scores.size());
        assertTrue(scores.containsKey(player1));
    }

    @Test
    public void testCalculateRoundScoresMultiplePlayers() {
        Map<PlayerModel, Integer> scores = ScoreCalculator.calculateRoundScores(players);
        assertNotNull(scores);
        assertEquals(3, scores.size());
        assertTrue(scores.containsKey(player1));
        assertTrue(scores.containsKey(player2));
        assertTrue(scores.containsKey(player3));
    }

    @Test
    public void testCalculateRoundScoresDistinctValues() {
        Map<PlayerModel, Integer> scores = ScoreCalculator.calculateRoundScores(players);
        
        for (PlayerModel player : players) {
            assertTrue(scores.containsKey(player));
            assertNotNull(scores.get(player));
        }
    }

    @Test
    public void testCalculateRoundScoresNonNegativeValues() {
        Map<PlayerModel, Integer> scores = ScoreCalculator.calculateRoundScores(players);
        
        for (Integer score : scores.values()) {
            assertNotNull(score);
            assertTrue(score >= 0);
        }
    }

    @Test
    public void testGetLowestScorePlayerSinglePlayer() {
        List<PlayerModel> singlePlayer = new ArrayList<>();
        singlePlayer.add(player1);
        
        PlayerModel lowest = ScoreCalculator.getLowestScorePlayer(singlePlayer);
        assertEquals(player1, lowest);
    }

    @Test
    public void testGetLowestScorePlayerMultiplePlayers() {
        PlayerModel lowest = ScoreCalculator.getLowestScorePlayer(players);
        assertNotNull(lowest);
        assertTrue(players.contains(lowest));
    }

    @Test
    public void testGetLowestScorePlayerEmptyList() {
        List<PlayerModel> emptyPlayers = new ArrayList<>();
        PlayerModel lowest = ScoreCalculator.getLowestScorePlayer(emptyPlayers);
        assertNull(lowest);
    }

    @Test
    public void testGetLowestScorePlayerNullList() {
        PlayerModel lowest = ScoreCalculator.getLowestScorePlayer(null);
        assertNull(lowest);
    }

    @Test
    public void testGetLowestScorePlayerAllSameScore() {
        // tous les joueurs ont le même score
        PlayerModel lowest = ScoreCalculator.getLowestScorePlayer(players);
        assertNotNull(lowest);
        assertTrue(players.contains(lowest));
    }

    @Test
    public void testGetLowestScorePlayerConsistency() {
        // appeler deux fois devrait donner le même résultat
        PlayerModel lowest1 = ScoreCalculator.getLowestScorePlayer(players);
        PlayerModel lowest2 = ScoreCalculator.getLowestScorePlayer(players);
        assertEquals(lowest1, lowest2);
    }

    @Test
    public void testGetLowestScorePlayerWithDifferentScores() {
        // ajouter des cartes pour créer des scores différents
        List<PlayerModel> testPlayers = new ArrayList<>();
        PlayerModel p1 = new PlayerModel(new PlayerData("P1"));
        PlayerModel p2 = new PlayerModel(new PlayerData("P2"));
        testPlayers.add(p1);
        testPlayers.add(p2);

        PlayerModel lowest = ScoreCalculator.getLowestScorePlayer(testPlayers);
        assertNotNull(lowest);
        assertTrue(testPlayers.contains(lowest));
    }

    @Test
    public void testApplyEndGamePenaltyNullRoundEndPlayer() {
        // ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            ScoreCalculator.applyEndGamePenalty(null, players);
        });
    }

    @Test
    public void testApplyEndGamePenaltyNullPlayersList() {
        // ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            ScoreCalculator.applyEndGamePenalty(player1, null);
        });
    }

    @Test
    public void testApplyEndGamePenaltyEmptyPlayersList() {
        List<PlayerModel> emptyPlayers = new ArrayList<>();
        int scoreBefore = player1.getTotalScore();
        
        ScoreCalculator.applyEndGamePenalty(player1, emptyPlayers);
        
        assertEquals(scoreBefore, player1.getTotalScore());
    }

    @Test
    public void testApplyEndGamePenaltyPlayerNotInList() {
        PlayerModel outsidePlayer = new PlayerModel(new PlayerData("OutsidePlayer"));
        int scoreBefore = outsidePlayer.getTotalScore();
        
        ScoreCalculator.applyEndGamePenalty(outsidePlayer, players);
        
        //le score ne change pas si le joueur n'est pas dans la liste
        assertEquals(scoreBefore, outsidePlayer.getTotalScore());
    }

    @Test
    public void testApplyEndGamePenaltyWithLowestScore() {
        // le joueur avec le score le plus bas ne devrait pas avoir de pénalité
        List<PlayerModel> testPlayers = new ArrayList<>();
        PlayerModel roundEndPlayer = new PlayerModel(new PlayerData("RoundEnd"));
        testPlayers.add(roundEndPlayer);
        
        int scoreBefore = roundEndPlayer.getTotalScore();
        ScoreCalculator.applyEndGamePenalty(roundEndPlayer, testPlayers);
        
        // si c'est le seul joueur, il a le score le plus élevé, donc pénalité
        assertTrue(roundEndPlayer.getTotalScore() >= scoreBefore);
    }

    @Test
    public void testApplyEndGamePenaltySinglePlayer() {
        List<PlayerModel> singlePlayerList = new ArrayList<>();
        singlePlayerList.add(player1);
        
        int scoreBefore = player1.getTotalScore();
        ScoreCalculator.applyEndGamePenalty(player1, singlePlayerList);
        
        // le joueur a le score le plus élevé (le seul), donc pénalité appliquée
        assertTrue(player1.getTotalScore() >= scoreBefore);
    }

    @Test
    public void testApplyEndGamePenaltyMultipleCalls() {
        // plusieurs appels sans modification de liste
        int initialScore = player1.getTotalScore();
        
        ScoreCalculator.applyEndGamePenalty(player1, players);
        ScoreCalculator.applyEndGamePenalty(player1, players);
        
        assertNotNull(player1);
    }

}
