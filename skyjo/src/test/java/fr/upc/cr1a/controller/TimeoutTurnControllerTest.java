package fr.upc.cr1a.controller;

import fr.upc.cr1a.model.GameManager;
import fr.upc.cr1a.view.GameBoardView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests JUnit pour TimeoutTurnController (gestion auto-play AFK).
 * La logique de jeu est difficile a tester sans GameManager reel, donc on couvre
 * les invariants : construction sans crash, et tolerance aux gameLoop inactif.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TimeoutTurnControllerTest {

    @Mock private GameBoardView mockGameBoardView;
    @Mock private GameManager mockGameManager;
    @Mock private GameBoardController mockMainController;
    @Mock private GameLoop mockGameLoop;

    @Test
    public void testConstructorDoesNotThrow() {
        assertDoesNotThrow(() ->
            new TimeoutTurnController(mockGameBoardView, mockGameManager, mockMainController, mockGameLoop)
        );
    }

    @Test
    public void testHandleTimeoutReturnsEarlyIfGameInactive() {
        // gameLoop.isGameActive() = false : handleTimeout doit return immediatement sans action
        when(mockGameLoop.isGameActive()).thenReturn(false);

        TimeoutTurnController ttc = new TimeoutTurnController(
            mockGameBoardView, mockGameManager, mockMainController, mockGameLoop
        );

        assertDoesNotThrow(() -> ttc.handleTimeoutForCurrentPlayer());
        // verify qu'on n'a pas appele de methode metier sur gameManager
        verify(mockGameManager, never()).drawDeck();
        verify(mockGameManager, never()).takeDiscard();
    }

    @Test
    public void testHandleTimeoutReturnsIfNoCurrentPlayer() {
        // Si getCurrentPlayer renvoie null, la methode doit return sans crasher
        when(mockGameLoop.isGameActive()).thenReturn(true);
        when(mockGameManager.getCurrentPlayer()).thenReturn(null);

        TimeoutTurnController ttc = new TimeoutTurnController(
            mockGameBoardView, mockGameManager, mockMainController, mockGameLoop
        );

        assertDoesNotThrow(() -> ttc.handleTimeoutForCurrentPlayer());
        verify(mockGameManager, never()).drawDeck();
    }
}
