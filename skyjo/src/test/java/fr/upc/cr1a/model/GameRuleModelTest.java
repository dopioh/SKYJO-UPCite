package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import fr.upc.cr1a.model.player.PlayerData;

/**
 * Tests JUnit pour GameRuleModel
 */
public class GameRuleModelTest {
    
    private PiocheModel deck;
    private DefausseModel discard;
    private ArrayList<PlayerModel> players;
    private PlayerModel player1;
    private PlayerModel player2;

    @BeforeEach
    public void setUp() {
        deck = new PiocheModel(false);
        discard = new DefausseModel();
        players = new ArrayList<>();
        PlayerData playerData1 = new PlayerData("Player1", "player1@test.com");
        PlayerData playerData2 = new PlayerData("Player2", "player2@test.com");
        player1 = new PlayerModel(playerData1);
        player2 = new PlayerModel(playerData2);
        players.add(player1);
        players.add(player2);
    }

    @Test
    public void testCanTakeFromDiscardWhenEmpty() {
        assertFalse(GameRuleModel.canTakeFromDiscard(discard), 
            "Should not be able to take from empty discard");
    }

    @Test
    public void testCanTakeFromDiscardWhenFull() {
        CardModel card = new CardModel(false);
        discard.addCardAtTop(card);
        assertTrue(GameRuleModel.canTakeFromDiscard(discard), 
            "Should be able to take from non-empty discard");
    }

    @Test
    public void testCanDrawFromDeck() {
        assertTrue(GameRuleModel.canDrawFromDeck(deck), 
            "Should be able to draw from a new deck");
    }

    @Test
    public void testCanDrawFromEmptyDeck() {
        // Vider le deck
        while (!deck.isEmpty()) {
            deck.removeCardAtTop();
        }
        assertFalse(GameRuleModel.canDrawFromDeck(deck), 
            "Should not be able to draw from empty deck");
    }

    @Test
    public void testMustExchangeCard() {
        assertTrue(GameRuleModel.mustExchangeCard(GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD));
        assertFalse(GameRuleModel.mustExchangeCard(GameManager.TurnPhase.CARD_DRAW_FROM_DECK));
    }

    @Test
    public void testCanDiscardCard() {
        assertTrue(GameRuleModel.canDiscardCard(GameManager.TurnPhase.CARD_DRAW_FROM_DECK));
        assertFalse(GameRuleModel.canDiscardCard(GameManager.TurnPhase.CARD_TAKE_FROM_DISCARD));
    }

    @Test
    public void testGameNotOver() {
        assertFalse(GameRuleModel.isGameOver(players), 
            "Game should not be over with low scores");
    }

    @Test
    public void testGameOverWhenOnePlayerExceedsWinningScore() {
        player1.addScore(GameRuleModel.WINNING_SCORE + 10);
        assertTrue(GameRuleModel.isGameOver(players), 
            "Game should be over when a player exceeds winning score");
    }

    @Test
    public void testDetermineWinner() {
        player1.addScore(50);
        player2.addScore(30);
        
        PlayerModel winner = GameRuleModel.determineWinner(players);
        assertEquals(player2, winner, "Player with lowest score should win");
    }

    @Test
    public void testDetermineWinnerNegativeScores() {
        player1.addScore(-10);
        player2.addScore(5);
        
        PlayerModel winner = GameRuleModel.determineWinner(players);
        assertEquals(player1, winner, "Player with lowest score (negative) should win");
    }

    @Test
    public void testGameRuleConstants() {
        assertEquals(2, GameRuleModel.INITIAL_REVEALED_CARD);
        assertEquals(100, GameRuleModel.WINNING_SCORE);
        assertEquals(4, GameRuleModel.COLUMN_SIZE);
        assertEquals(3, GameRuleModel.ROWNS_SIZE);
    }
}
