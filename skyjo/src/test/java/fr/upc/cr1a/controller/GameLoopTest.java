package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.view.GameBoardView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JUnit pour GameLoop (orchestration des tours).
 * Utilise Mockito pour les dependances lourdes (GameManager, GameBoardView).
 */
@ExtendWith(MockitoExtension.class)
public class GameLoopTest {

    @Mock private GameManager mockGameManager;
    @Mock private GameBoardView mockGameBoardView;

    private GameLoop gameLoop;

    @BeforeEach
    public void setUp() {
        gameLoop = new GameLoop(mockGameManager, mockGameBoardView);
    }

    @Test
    public void testInitialStateActive() {
        assertTrue(gameLoop.isGameActive(),
            "Le jeu doit etre actif a la creation du GameLoop");
    }

    @Test
    public void testStopGameDeactivates() {
        gameLoop.stopGame();
        assertFalse(gameLoop.isGameActive(),
            "Apres stopGame() le jeu ne doit plus etre actif");
    }

    @Test
    public void testStopGameUpdatesTimerLabel() {
        gameLoop.stopGame();
        // Verifie que stopGame appelle setTurnTimerText("--") sur la view
        org.mockito.Mockito.verify(mockGameBoardView).setTurnTimerText("--");
    }

    @Test
    public void testSetTimerEnabledFalseHidesTimer() {
        gameLoop.setTimerEnabled(false);
        org.mockito.Mockito.verify(mockGameBoardView).setTurnTimerText("--");
    }

    @Test
    public void testSetPausedDoesNotCrash() {
        // setPaused doit etre tolerant meme sans controllers (humain/bot) configures
        assertDoesNotThrow(() -> gameLoop.setPaused(true));
        assertDoesNotThrow(() -> gameLoop.setPaused(false));
    }

    @Test
    public void testRestartCountdownNoopWhenTimerDisabled() {
        // Par defaut timerEnabled = false : restart doit etre un no-op
        assertDoesNotThrow(() -> gameLoop.restartCountdownForNetworkTurn());
    }

    @Test
    public void testRestartCountdownNoopWhenGameStopped() {
        gameLoop.stopGame();
        // Apres stop, restartCountdown ne doit rien faire
        assertDoesNotThrow(() -> gameLoop.restartCountdownForNetworkTurn());
    }

    @Test
    public void testSettersAcceptNull() {
        // Les setters doivent tolerer null sans crasher
        assertDoesNotThrow(() -> gameLoop.setHumanController(null));
        assertDoesNotThrow(() -> gameLoop.setBotController(null));
        assertDoesNotThrow(() -> gameLoop.setTimeoutController(null));
    }
}
