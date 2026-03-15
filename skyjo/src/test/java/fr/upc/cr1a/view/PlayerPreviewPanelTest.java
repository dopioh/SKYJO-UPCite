package fr.upc.cr1a.view;

import fr.upc.cr1a.model.CardModel;
import fr.upc.cr1a.model.PlayerHandModel;
import fr.upc.cr1a.model.PlayerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerPreviewPanelTest {

    @Mock private PlayerModel mockPlayer;
    @Mock private PlayerHandModel mockHand;
    @Mock private CardModel mockCard;

    private PlayerPreviewPanel previewPanel;

    @BeforeEach
    public void setUp() {
        previewPanel = new PlayerPreviewPanel();
    }

    @Test
    public void testShowOtherPlayerHand_PopulatesGrid() {

        when(mockPlayer.getHand()).thenReturn(mockHand);
        // On simule une carte à la position 0,0 et null ailleurs
        when(mockHand.getCard(0, 0)).thenReturn(mockCard);
        when(mockCard.isFaceDown()).thenReturn(true);

        previewPanel.showOtherPlayerHand(mockPlayer);

        Component[] components = previewPanel.getComponents();
        assertTrue(components.length > 0, "Le panel devrait contenir la grille de cartes.");

        verify(mockPlayer, atLeastOnce()).getHand();
        verify(mockHand, times(12)).getCard(anyInt(), anyInt());
    }
    
    @Test
    public void testFadeInAndOut() {
        previewPanel.fade(true);
        assertTrue(previewPanel.isVisible(), "Le panel devrait etre rendu visible au debut du fondu.");
    }
}
