package fr.upc.cr1a.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour LobbyModel
 */
public class LobbyModelTest {
    
    private LobbyModel lobby;

    @BeforeEach
    public void setUp() {
        lobby = new LobbyModel("lobby1", "Test Lobby", "HostPlayer", 2, 4, false);
    }

    @Test
    public void testLobbyInitialization() {
        assertEquals("lobby1", lobby.getLobbyId());
        assertEquals("Test Lobby", lobby.getLobbyName());
        assertEquals("HostPlayer", lobby.getHostName());
        assertEquals(2, lobby.getCurrentPlayers());
        assertEquals(4, lobby.getMaxPlayers());
        assertFalse(lobby.isActionMode());
    }

    @Test
    public void testLobbyStatusWaiting() {
        assertEquals(LobbyModel.LobbyStatus.WAITING, lobby.getStatus());
    }

    @Test
    public void testLobbyStatusFull() {
        LobbyModel fullLobby = new LobbyModel("lobby2", "Full Lobby", "Host2", 4, 4, true);
        assertEquals(LobbyModel.LobbyStatus.FULL, fullLobby.getStatus());
    }

    @Test
    public void testActionMode() {
        LobbyModel actionLobby = new LobbyModel("lobby3", "Action", "Host3", 1, 4, true);
        assertTrue(actionLobby.isActionMode());
    }

    @Test
    public void testClassicMode() {
        LobbyModel classicLobby = new LobbyModel("lobby4", "Classic", "Host4", 1, 4, false);
        assertFalse(classicLobby.isActionMode());
    }

    @Test
    public void testMultipleLobby() {
        LobbyModel lobby1 = new LobbyModel("id1", "Lobby1", "Host1", 1, 4, false);
        LobbyModel lobby2 = new LobbyModel("id2", "Lobby2", "Host2", 2, 4, true);
        
        assertNotEquals(lobby1.getLobbyId(), lobby2.getLobbyId());
        assertNotEquals(lobby1.getLobbyName(), lobby2.getLobbyName());
    }

    @Test
    public void testLobbyPlayerCount() {
        assertEquals(2, lobby.getCurrentPlayers());
        assertEquals(4, lobby.getMaxPlayers());
    }

    @Test
    public void testLobbyStatusEnum() {
        assertNotNull(LobbyModel.LobbyStatus.WAITING);
        assertNotNull(LobbyModel.LobbyStatus.PLAYING);
        assertNotNull(LobbyModel.LobbyStatus.FULL);
    }

    @Test
    public void testDifferentMaxPlayers() {
        LobbyModel lobby2P = new LobbyModel("id", "Name", "Host", 0, 2, false);
        LobbyModel lobby6P = new LobbyModel("id", "Name", "Host", 0, 6, false);
        
        assertNotEquals(lobby2P.getMaxPlayers(), lobby6P.getMaxPlayers());
    }
}
