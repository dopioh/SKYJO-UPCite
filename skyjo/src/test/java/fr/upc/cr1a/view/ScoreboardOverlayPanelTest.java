package fr.upc.cr1a.view;

import fr.upc.cr1a.model.PlayerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests JUnit pour ScoreboardOverlayPanel (overlay Tab affichant les scores par manche).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScoreboardOverlayPanelTest {

    @Mock private PlayerModel mockPlayer1;
    @Mock private PlayerModel mockPlayer2;
    @Mock private PlayerModel mockPlayer3;

    @BeforeEach
    public void setUp() {
        when(mockPlayer1.getName()).thenReturn("Alice");
        when(mockPlayer2.getName()).thenReturn("Bob");
        when(mockPlayer3.getName()).thenReturn("Charlie");

        when(mockPlayer1.getTotalScore()).thenReturn(25);
        when(mockPlayer2.getTotalScore()).thenReturn(40);
        when(mockPlayer3.getTotalScore()).thenReturn(30);

        when(mockPlayer1.getRoundScoreHistory()).thenReturn(new ArrayList<>(Arrays.asList(10, 15)));
        when(mockPlayer2.getRoundScoreHistory()).thenReturn(new ArrayList<>(Arrays.asList(20, 20)));
        when(mockPlayer3.getRoundScoreHistory()).thenReturn(new ArrayList<>(Arrays.asList(15, 15)));

        when(mockPlayer1.calculateRoundScore()).thenReturn(5);
        when(mockPlayer2.calculateRoundScore()).thenReturn(8);
        when(mockPlayer3.calculateRoundScore()).thenReturn(3);
    }

    @Test
    public void testConstructorWithoutCurrentRound() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(players);
        assertNotNull(panel, "Le panel doit etre instancie sans current round");
    }

    @Test
    public void testConstructorWithCurrentRound() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        // currentRoundNumber = 3, history.size() = 2, donc on doit afficher la manche 3 en live
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(players, 3);
        assertNotNull(panel);
    }

    @Test
    public void testConstructorWithEmptyPlayerList() {
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(new ArrayList<>());
        assertNotNull(panel, "Le panel doit gerer une liste vide sans crasher");
    }

    @Test
    public void testConstructorWithSinglePlayer() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(players, 2);
        assertNotNull(panel);
    }

    @Test
    public void testConstructorWithMultiplePlayers() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2, mockPlayer3);
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(players, 3);
        assertNotNull(panel);
    }

    @Test
    public void testCurrentRoundEqualHistorySize() {
        // currentRound = history.size() = 2 : la manche en cours est deja dans l'historique
        // -> pas de colonne "live" ajoutee
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(players, 2);
        assertNotNull(panel);
    }

    @Test
    public void testNoCurrentRoundFlag() {
        // currentRoundNumber = -1 : pas de manche en cours -> seul l'historique
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(players, -1);
        assertNotNull(panel);
    }

    @Test
    public void testPanelIsTransparent() {
        ScoreboardOverlayPanel panel = new ScoreboardOverlayPanel(new ArrayList<>());
        assertFalse(panel.isOpaque(), "Le panel doit etre semi-transparent pour overlay");
    }

    @Test
    public void testPlayersWithDifferentHistorySizes() {
        // Player 1 a 3 manches, Player 2 en a 1 : on prend le max
        when(mockPlayer1.getRoundScoreHistory()).thenReturn(new ArrayList<>(Arrays.asList(10, 15, 20)));
        when(mockPlayer2.getRoundScoreHistory()).thenReturn(new ArrayList<>(Arrays.asList(15)));

        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        assertDoesNotThrow(() -> new ScoreboardOverlayPanel(players, 4));
    }
}
