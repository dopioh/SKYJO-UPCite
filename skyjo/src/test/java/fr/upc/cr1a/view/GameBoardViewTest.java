package fr.upc.cr1a.view;

//import fr.upc.cr1a.model.CardModel;
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

@ExtendWith(MockitoExtension.class)
public class GameBoardViewTest {

    @Mock private PlayerModel mockPlayer1;
    @Mock private PlayerModel mockPlayer2;
    @Mock private PlayerHandModel mockHand;

    private GameBoardView view;
    private ArrayList<PlayerModel> players;

    @BeforeEach
    public void setUp() {
        players = new ArrayList<>();
        players.add(mockPlayer1);
        players.add(mockPlayer2);

        when(mockPlayer1.getName()).thenReturn("MeiMei");
        when(mockPlayer2.getName()).thenReturn("NassNass");
        
        when(mockPlayer1.getHand()).thenReturn(mockHand); // pour eviter les null exceptions

        view = new GameBoardView(players);
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
}