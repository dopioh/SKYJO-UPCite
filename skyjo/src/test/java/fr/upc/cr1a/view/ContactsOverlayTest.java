package fr.upc.cr1a.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactsOverlayTest {

    private ContactsOverlay contactsOverlay;

    @BeforeEach
    void setUp() {
        contactsOverlay = new ContactsOverlay();
    }

    @Test
    void testInitialState() {
        assertFalse(contactsOverlay.isVisible(), "L'overlay devrait être invisible par défaut.");
        assertFalse(contactsOverlay.isOpaque(), "L'overlay devrait être transparent (opaque=false).");
        
        assertTrue(contactsOverlay.getLayout() instanceof GridBagLayout, "Le layout principal doit être un GridBagLayout.");
    }

    @Test
    void testCloseButtonInitialization() {
        JLabel closeButton = contactsOverlay.getCloseButton();
        
        assertNotNull(closeButton, "Le bouton de fermeture ne doit pas être null.");
        assertEquals("X", closeButton.getText(), "Le texte du bouton de fermeture doit être 'X'.");
        assertEquals(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), closeButton.getCursor(), "Le curseur doit être en forme de main.");
    }

    @Test
    void testComponentResized() {
        contactsOverlay.setSize(1000, 800);
        
        ComponentEvent resizeEvent = new ComponentEvent(contactsOverlay, ComponentEvent.COMPONENT_RESIZED);

        assertDoesNotThrow(() -> {
            for (java.awt.event.ComponentListener listener : contactsOverlay.getComponentListeners()) {
                listener.componentResized(resizeEvent);
            }
        });
        
        Component card = contactsOverlay.getComponent(0);
        Dimension preferredSize = card.getPreferredSize();
        assertNotNull(preferredSize, "La carte interne doit avoir une dimension préférée après redimensionnement.");
        assertTrue(preferredSize.width >= 500 && preferredSize.width <= 900, "La largeur de la carte doit respecter les bornes.");
    }

    @Test
    void testPaintComponentWithMockGraphics() {

        Graphics2D g2dMock = mock(Graphics2D.class);
        
        when(g2dMock.create()).thenReturn(g2dMock);

        contactsOverlay.setSize(1280, 720);
        contactsOverlay.paintComponent(g2dMock);

        verify(g2dMock).setColor(new Color(0, 0, 0, 120));
        verify(g2dMock).fillRect(0, 0, 1280, 720);
        verify(g2dMock).dispose();
    }
}
