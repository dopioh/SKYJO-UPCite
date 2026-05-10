package fr.upc.cr1a.view;

import fr.upc.cr1a.model.ActionCardModel;
import fr.upc.cr1a.model.NumberCardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.upc.cr1a.view.help_ingame.HelpOverlay;

@ExtendWith(MockitoExtension.class)
public class GameBoardViewTest {
    HelpOverlay helpOverlay = new HelpOverlay();

    @Mock private PlayerModel mockPlayer1;
    @Mock private PlayerModel mockPlayer2;
    @Mock private PlayerHandModel mockHand;
    @Mock private Frame mockFrame;

    private GameBoardView view;
    private ArrayList<PlayerModel> players;

    @BeforeEach
    public void setUp() {
        players = new ArrayList<>();
        players.add(mockPlayer1);
        players.add(mockPlayer2);

        when(mockPlayer1.getName()).thenReturn("MeiMei");
        when(mockPlayer2.getName()).thenReturn("NassNass");
        when(mockPlayer1.getHand()).thenReturn(mockHand);
        when(mockFrame.getHelpOverlay()).thenReturn(helpOverlay);

        when(mockPlayer1.getHand()).thenReturn(mockHand); // pour eviter les null exceptions
        when(mockFrame.getHelpOverlay()).thenReturn(helpOverlay);

        view = new GameBoardView(players,mockFrame);
    }

    @Test
    public void testInitComponents_ButtonsCreated() {

        assertEquals(2, view.getListPlayerButton().size(), "Il devrait y avoir 2 boutons de joueurs.");
        assertEquals("MeiMei", view.getListPlayerButton().get(0).getText());
        assertEquals("NassNass", view.getListPlayerButton().get(1).getText());
    }

    @Test
    public void testSetActionDecksVisible() {
        view.setActionDecksVisible(true);

        assertTrue(view.getBtnPiocheAction().isVisible());
        assertTrue(view.getBtnDefausseAction().isVisible());

        view.setActionDecksVisible(false);

        assertFalse(view.getBtnPiocheAction().isVisible());
        assertFalse(view.getBtnDefausseAction().isVisible());
    }

    @Test
    public void testGetCardButtons_GrilleCreee() {
        CardButton[][] grille = view.getCardButtons();
        assertNotNull(grille, "La grille de cartes ne doit pas etre null");
        assertEquals(3, grille.length, "La grille doit avoir 3 lignes");
        assertEquals(4, grille[0].length, "La grille doit avoir 4 colonnes");
    }

    @Test
    public void testGetCardButtons_TousNonNull() {
        CardButton[][] grille = view.getCardButtons();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                assertNotNull(grille[row][col], "Chaque CardButton de la grille doit etre non null");
            }
        }
    }

    @Test
    public void testGetCardImagePath_CarteDos() {
        NumberCardModel carteDos = new NumberCardModel(true, 5, "rouge", false);
        String path = view.getCardImagePath(carteDos);
        assertEquals("/cartes/carteDos.png", path, "Une carte face cachee doit retourner le dos");
    }

    @Test
    public void testGetCardImagePath_CarteNombre() {
        NumberCardModel carte = new NumberCardModel(false, 7, "rouge", false);
        String path = view.getCardImagePath(carte);
        assertEquals("/cartes/carte7.png", path);
    }

    @Test
    public void testGetCardImagePath_CarteMoins2() {
        NumberCardModel carte = new NumberCardModel(false, -2, "rouge", false);
        String path = view.getCardImagePath(carte);
        assertEquals("/cartes/carteMoins2.png", path);
    }

    @Test
    public void testClickBoutonJoueur_SansException() {
        boolean executed = false;
        view.getListPlayerButton().get(0).doClick();
        executed = true;
        assertTrue(executed, "Cliquer sur un bouton joueur ne doit pas lancer d'exception");
    }
}