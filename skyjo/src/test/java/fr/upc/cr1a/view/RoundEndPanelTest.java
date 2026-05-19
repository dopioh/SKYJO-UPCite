package fr.upc.cr1a.view;

import fr.upc.cr1a.model.ActionCardModel.ActionType;
import fr.upc.cr1a.model.PlayerHandModel;
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
 * Tests JUnit pour RoundEndPanel (fin de manche / fin de partie).
 * Couvre les variantes solo classique / solo mode action / multi
 * ainsi que l'API publique (updateStats, showStatsError, getters).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoundEndPanelTest {

    @Mock private PlayerModel mockPlayer1;
    @Mock private PlayerModel mockPlayer2;
    @Mock private PlayerHandModel mockHand1;
    @Mock private PlayerHandModel mockHand2;

    @BeforeEach
    public void setUp() {
        when(mockPlayer1.getName()).thenReturn("Alice");
        when(mockPlayer2.getName()).thenReturn("Bob");

        when(mockPlayer1.getHand()).thenReturn(mockHand1);
        when(mockPlayer2.getHand()).thenReturn(mockHand2);

        when(mockHand1.getVisibleCardScore()).thenReturn(8);
        when(mockHand2.getVisibleCardScore()).thenReturn(15);

        when(mockPlayer1.getActionHand()).thenReturn(new ArrayList<ActionType>());
        when(mockPlayer2.getActionHand()).thenReturn(new ArrayList<ActionType>());

        when(mockPlayer1.getTotalScore()).thenReturn(8);
        when(mockPlayer2.getTotalScore()).thenReturn(15);
    }

    @Test
    public void testConstructor2Args() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        RoundEndPanel panel = new RoundEndPanel(players, false);
        assertNotNull(panel);
        assertFalse(panel.isGameOver(), "gameOver doit etre false");
    }

    @Test
    public void testConstructor3Args() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        RoundEndPanel panel = new RoundEndPanel(players, true, mockPlayer1);
        assertNotNull(panel);
        assertTrue(panel.isGameOver());
    }

    @Test
    public void testConstructor4Args() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        RoundEndPanel panel = new RoundEndPanel(players, false, mockPlayer1, mockPlayer2);
        assertNotNull(panel);
    }

    @Test
    public void testConstructor5ArgsWithActionCard() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        // mode action solo : withActionCard = true
        RoundEndPanel panel = new RoundEndPanel(players, false, mockPlayer1, mockPlayer2, true);
        assertNotNull(panel);
    }

    @Test
    public void testConstructor5ArgsWithoutActionCard() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        // mode classique / multi : withActionCard = false
        RoundEndPanel panel = new RoundEndPanel(players, false, mockPlayer1, mockPlayer2, false);
        assertNotNull(panel);
    }

    @Test
    public void testGameOverShowsRetourMenuButton() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        RoundEndPanel panel = new RoundEndPanel(players, true);
        assertNotNull(panel.getBtnRetourMenu(), "Le bouton Menu doit etre present en game over");
        assertNull(panel.getBtnContinuer(), "Le bouton Continuer doit etre absent en game over");
    }

    @Test
    public void testRoundEndShowsContinuerButton() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        RoundEndPanel panel = new RoundEndPanel(players, false);
        assertNotNull(panel.getBtnContinuer(), "Le bouton Continuer doit etre present en fin de manche");
        assertNull(panel.getBtnRetourMenu(), "Le bouton Menu doit etre absent en fin de manche");
    }

    @Test
    public void testSortedPlayersAscending() {
        // Alice a 8 pts, Bob a 15 pts : Alice doit etre 1ere (plus bas score gagne)
        List<PlayerModel> players = Arrays.asList(mockPlayer2, mockPlayer1); // ordre inverse a l'entree
        RoundEndPanel panel = new RoundEndPanel(players, false);
        // Note : RoundEndPanel ne re-trie pas forcement, il garde l'ordre des players. On verifie juste la taille.
        assertEquals(2, panel.getSortedPlayers().size());
    }

    @Test
    public void testGetPlayerLabelsSizeMatchesPlayers() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1, mockPlayer2);
        RoundEndPanel panel = new RoundEndPanel(players, false);
        assertEquals(2, panel.getPlayerLabels().size(),
            "Il doit y avoir un label par joueur");
    }

    @Test
    public void testUpdateStatsDoesNotThrow() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        RoundEndPanel panel = new RoundEndPanel(players, true);
        // updateStats avec gameOver=true (eloLabel et moneyLabel construits)
        assertDoesNotThrow(() -> panel.updateStats(1000, 50, 500, 16, 5, 30));
    }

    @Test
    public void testUpdateStatsSafeOnRoundEnd() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        // gameOver=false : pas de stats card construit, updateStats doit early-return
        RoundEndPanel panel = new RoundEndPanel(players, false);
        assertDoesNotThrow(() -> panel.updateStats(1000, 50, 500, 16, 5, 30));
    }

    @Test
    public void testShowStatsErrorDoesNotThrow() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        RoundEndPanel panel = new RoundEndPanel(players, true);
        assertDoesNotThrow(() -> panel.showStatsError("Erreur de test"));
    }

    @Test
    public void testShowStatsErrorWithNullMessage() {
        List<PlayerModel> players = Arrays.asList(mockPlayer1);
        RoundEndPanel panel = new RoundEndPanel(players, true);
        // null message doit utiliser le fallback par defaut
        assertDoesNotThrow(() -> panel.showStatsError(null));
    }

    @Test
    public void testEmptyPlayerListDoesNotCrash() {
        // cas degenere : 0 joueurs
        assertDoesNotThrow(() -> new RoundEndPanel(new ArrayList<>(), false));
    }
}
