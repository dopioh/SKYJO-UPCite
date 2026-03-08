package fr.upc.cr1a.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Couverture de CursorCard par tests unitaires 
 * @author Nassim
 */
class CursorCardViewTest {

    private CursorCardView cursorCard;
    private JPanel fakeGameBoard;

    @BeforeEach
    void setUp() {
        fakeGameBoard = new JPanel();
        fakeGameBoard.setBounds(0, 0, 800, 600);
        cursorCard = new CursorCardView(fakeGameBoard);
    }


    @Test
    void testGetOverlay() {
        JComponent overlay = cursorCard.getOverlay();
        assertNotNull(overlay);
    }


    @Test
    void testHideCard() {
        assertDoesNotThrow(() -> cursorCard.hideCard());
    }
}
