package fr.upc.cr1a.view.help_ingame;

import fr.upc.cr1a.view.DoodleButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HelpOverlayTest {

    private HelpOverlay helpOverlay;

    @Mock
    private Component mockComponent1;

    @Mock
    private Component mockComponent2;

    @Mock
    private DoodleButton mockDoodleButton1;

    @Mock
    private DoodleButton mockDoodleButton2;

    @BeforeEach
    public void setUp() {
        helpOverlay = new HelpOverlay();
    }

    @Test
    public void testInitialState() {

        assertFalse(helpOverlay.isActive(), "L'overlay ne devrait pas être actif par défaut");
        assertFalse(helpOverlay.isOpaque(), "L'overlay devrait être transparent (opaque = false)");
    }

    @Test
    public void testShowPanel() {

        helpOverlay.showPanel(true);
        assertTrue(helpOverlay.isActive(), "L'overlay devrait être actif");
        assertTrue(helpOverlay.isVisible(), "L'overlay devrait être visible");


        helpOverlay.showPanel(false);
        assertFalse(helpOverlay.isActive(), "L'overlay ne devrait plus être actif");
        assertFalse(helpOverlay.isVisible(), "L'overlay ne devrait plus être visible");
    }

    @Test
    public void testAddSingleTargetComponent() {

        when(mockComponent1.isShowing()).thenReturn(true);

        helpOverlay.addTargetComponent(mockComponent1);

    }

    @Test
    public void testAddTargetComponent_NotShowing() {

        when(mockComponent1.isShowing()).thenReturn(false);

        helpOverlay.addTargetComponent(mockComponent1);

    }

    @Test
    public void testAddArrayTargetComponent() {
        Component[] components = {mockComponent1, mockComponent2};

        helpOverlay.addTargetComponent(components);
        
    }

    @Test
    public void testAddNullArrayTargetComponent() {
        Component[] components = null;

        assertDoesNotThrow(() -> helpOverlay.addTargetComponent(components));
    }

    @Test
    public void testAddArrayListDoodleButton() {
        // Configuration des mocks
        when(mockDoodleButton1.getText()).thenReturn("Joueur 1");
        when(mockDoodleButton2.getText()).thenReturn("Joueur Actif"); // celui la doit être ignore

        ArrayList<DoodleButton> buttons = new ArrayList<>();
        buttons.add(mockDoodleButton1);
        buttons.add(mockDoodleButton2);

        helpOverlay.addTargetComponent(buttons, "Joueur Actif");


        verify(mockDoodleButton1, atLeastOnce()).getText();
        verify(mockDoodleButton2, atLeastOnce()).getText();
    }

    @Test
    public void testAddMatrixTargetComponent() {
        Component[][] matrix = {
                {mockComponent1, mockComponent2},
                {mockComponent1, mockComponent2}
        };

        helpOverlay.addTargetComponent(matrix);
        // Exécution réussie
    }

    @Test
    public void testClearHighlight() {
        when(mockComponent1.isShowing()).thenReturn(true);
        helpOverlay.addTargetComponent(mockComponent1);

        helpOverlay.clearHighlight();

    }
}