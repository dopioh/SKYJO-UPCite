package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import fr.upc.cr1a.model.player.PlayerData;

/**
 * Tests JUnit pour PlayerModel
 */
public class PlayerModelTest {
    
    private PlayerModel player;
    private PlayerData playerData;

    @BeforeEach
    public void setUp() {
        playerData = new PlayerData("TestPlayer", "test@test.com");
        player = new PlayerModel(playerData);
    }

    @Test
    public void testPlayerInitialization() {
        assertNotNull(player.getName(), "Player name should not be null");
        assertEquals(0, player.getTotalScore(), "Initial score should be 0");
    }

    @Test
    public void testAddScore() {
        player.addScore(50);
        assertEquals(50, player.getTotalScore());
    }

    @Test
    public void testAddMultipleScores() {
        player.addScore(30);
        player.addScore(20);
        player.addScore(15);
        assertEquals(65, player.getTotalScore());
    }

    @Test
    public void testAddNegativeScore() {
        player.addScore(100);
        player.addScore(-30);
        assertEquals(70, player.getTotalScore());
    }

    @Test
    public void testPlayerHasHand() {
        assertNotNull(player.getHand(), "Player should have a hand");
        assertInstanceOf(PlayerHandModel.class, player.getHand());
    }

    @Test
    public void testPlayerName() {
        assertNotNull(player.getName());
        assertFalse(player.getName().isEmpty());
    }

    @Test
    public void testDifferentPlayers() {
        PlayerData playerData2 = new PlayerData("Player2", "player2@test.com");
        PlayerModel player2 = new PlayerModel(playerData2);
        assertNotEquals(player.getName(), player2.getName());
    }

    @Test
    public void testScoreIncrementally() {
        int[] scores = {10, 20, 15, 5, 30};
        int total = 0;
        
        for (int score : scores) {
            player.addScore(score);
            total += score;
            assertEquals(total, player.getTotalScore());
        }
    }
}
