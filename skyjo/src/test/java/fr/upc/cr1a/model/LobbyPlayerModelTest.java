package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour LobbyPlayerModel
 */
public class LobbyPlayerModelTest {
    
    private LobbyPlayerModel humanPlayer;
    private LobbyPlayerModel botPlayer;

    @BeforeEach
    public void setUp() {
        humanPlayer = new LobbyPlayerModel("player1", "Player1", true, false);
        botPlayer = new LobbyPlayerModel("bot1", "Bot1", false, true);
    }

    @Test
    public void testHumanPlayerInitialization() {
        assertEquals("player1", humanPlayer.getPlayerId());
        assertEquals("Player1", humanPlayer.getPlayerName());
        assertTrue(humanPlayer.isHost());
        assertFalse(humanPlayer.isBot());
        assertFalse(humanPlayer.isReady());
    }

    @Test
    public void testBotPlayerInitialization() {
        assertEquals("bot1", botPlayer.getPlayerId());
        assertEquals("Bot1", botPlayer.getPlayerName());
        assertFalse(botPlayer.isHost());
        assertTrue(botPlayer.isBot());
        assertTrue(botPlayer.isReady(), "Bot should always be ready");
    }

    @Test
    public void testSetHumanPlayerReady() {
        assertFalse(humanPlayer.isReady());
        humanPlayer.setReady(true);
        assertTrue(humanPlayer.isReady());
    }

    @Test
    public void testSetHumanPlayerNotReady() {
        humanPlayer.setReady(true);
        assertTrue(humanPlayer.isReady());
        humanPlayer.setReady(false);
        assertFalse(humanPlayer.isReady());
    }

    @Test
    public void testBotCannotChangeReadyStatus() {
        assertTrue(botPlayer.isReady());
        botPlayer.setReady(false);
        assertTrue(botPlayer.isReady(), "Bot ready status should not change");
    }

    @Test
    public void testMultiplePlayers() {
        LobbyPlayerModel player1 = new LobbyPlayerModel("id1", "Player1", true, false);
        LobbyPlayerModel player2 = new LobbyPlayerModel("id2", "Player2", false, false);
        LobbyPlayerModel bot = new LobbyPlayerModel("bot1", "Bot", false, true);
        
        assertNotNull(player1.getPlayerId());
        assertNotEquals(player1.getPlayerId(), player2.getPlayerId());
        assertNotEquals(player2.getPlayerId(), bot.getPlayerId());
    }

    @Test
    public void testHostStatus() {
        LobbyPlayerModel host = new LobbyPlayerModel("id1", "Host", true, false);
        LobbyPlayerModel guest = new LobbyPlayerModel("id2", "Guest", false, false);
        
        assertTrue(host.isHost());
        assertFalse(guest.isHost());
    }

    @Test
    public void testPlayerNames() {
        assertEquals("Player1", humanPlayer.getPlayerName());
        assertEquals("Bot1", botPlayer.getPlayerName());
        assertNotEquals(humanPlayer.getPlayerName(), botPlayer.getPlayerName());
    }
}
